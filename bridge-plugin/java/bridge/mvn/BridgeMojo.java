package bridge.mvn;

import bridge.asm.HierarchicalWriter;
import bridge.asm.HierarchyScanner;
import bridge.asm.TypeMap;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static bridge.mvn.ForkVisitor.*;

/**
 * Compiles bridge features in application classes
 */
@Mojo(
        name = "bridge",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true
)
public final class BridgeMojo extends AbstractMojo {

    private static final String CANONICAL_GROUP = "io.github.supracraft.bridge";
    private static final String LEGACY_GROUP = "net.ME1312.ASM";

    /** The maven repository system */
    @Component
    private RepositorySystem system;

    /** The maven artifact manager */
    @Component
    private ArtifactHandlerManager manager;

    /** The maven session */
    @Component
    private MavenSession session;

    /** The maven project */
    @Component
    private MavenProject project;

    /** The maven plugin execution */
    @Component
    private MojoExecution execution;

    /** Additional dependencies to add to the class hierarchy */
    @Parameter(property = "bridge.dependencies")
    private Dependency[] dependencies;

    /** The top level input/output directory for classes */
    @Parameter(property = "bridge.classpath", defaultValue = "${project.build.outputDirectory}")
    private File classpath;

    /** Include filters to apply when searching for classes to recompile */
    @Parameter(property = "bridge.includes", defaultValue = "**/*.class")
    private String[] includes;

    /** Exclude filters to apply when searching for classes to recompile */
    @Parameter(property = "bridge.excludes")
    private String[] excludes;

    /** Flags to apply when recompiling classes */
    @Parameter(property = "bridge.flags")
    private String[] flags;

    /** Whether to emit the machine-readable Bridge run report. */
    @Parameter(property = "bridge.report", defaultValue = "true")
    private boolean reportEnabled;

    /** Destination for the machine-readable Bridge run report. */
    @Parameter(property = "bridge.reportFile", defaultValue = "${project.build.directory}/bridge/bridge-report.json")
    private File reportFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();
        final BridgeRunReport report = new BridgeRunReport(execution.getVersion());
        final long runStarted = System.nanoTime();

        check: {
            String v1, v2 = project.getProperties().getProperty("bridge.version");
            for (Artifact artifact : project.getArtifacts()) {
                if (!isBridgeApiArtifact(artifact)) continue;
                if (v2 != null) {
                    if (!v2.equalsIgnoreCase(v1 = artifact.getVersion())) {
                        warn(log, report, "BRIDGE-W001", "The api version differs from ${bridge.version}: " + v1 + " != " + v2);
                    }
                    break;
                }
                if (((v1 = execution.getVersion()) != (v2 = artifact.getVersion())) && (v1 == null || !v1.equalsIgnoreCase(v2))) {
                    warn(log, report, "BRIDGE-W002", "The plugin version differs from the api version: " + v1 + " != " + v2);
                }
                break check;
            }
            if (v2 != null && !v2.equalsIgnoreCase(v1 = execution.getVersion())) {
                warn(log, report, "BRIDGE-W003", "The plugin version differs from ${bridge.version}: " + v1 + " != " + v2);
            }
        }

        int flags = 0;
        boolean lazy = true;
        if (this.flags != null) for (String flag : this.flags) {
            switch (flag.replaceAll("[\\s\\-]", "_").toUpperCase(Locale.ROOT)) {
                case "NO_DEBUG":
                    flags |= NO_NAMED_PARAMS | NO_NAMED_LOCALS | NO_SOURCE_EXT | NO_SOURCE_NAMES | NO_MODULE_VERSIONS | NO_LINE_NUMBERS;
                    break;
                case "NO_NAMED_PARAMETERS":
                case "NO_NAMED_PARAMS":
                    flags |= NO_NAMED_PARAMS;
                    break;
                case "NO_NAMED_LOCALS":
                    flags |= NO_NAMED_LOCALS;
                    break;
                case "NO_SOURCE":
                    flags |= NO_SOURCE_EXT | NO_SOURCE_NAMES;
                    break;
                case "NO_SOURCE_NAMES":
                case "NO_SOURCE_NAME":
                    flags |= NO_SOURCE_NAMES;
                    break;
                case "NO_SOURCE_EXT":
                case "NO_SOURCE_EXTENSION":
                case "NO_SOURCE_EXTENSIONS":
                    flags |= NO_SOURCE_EXT;
                    break;
                case "NO_MODULE_VERSION":
                case "NO_MODULE_VERSIONS":
                    flags |= NO_MODULE_VERSIONS;
                    break;
                case "NO_LINE_NUMBERS":
                    flags |= NO_LINE_NUMBERS;
                    break;
                case "FORCE_COMPILE":
                case "FORCE_RECOMPILE":
                    lazy = false;
                    break;
                case "NO_COMPILE":
                case "NO_RECOMPILE":
                case "SKIP_COMPILE":
                case "SKIP_RECOMPILE":
                    warn(log, report, "BRIDGE-W004", "Skipped previously defined recompilation goal");
                    report.status = "skipped";
                    report.totalNanos = System.nanoTime() - runStarted;
                    logSummary(log, report);
                    writeRunReport(report);
                    return;
                default:
                    warn(log, report, "BRIDGE-W005", "Unknown recompilation flag: " + flag);
            }
        }

        try {
            int length;
            TypeMap types = new TypeMap();
            Set<String> unique = new HashSet<>();
            DirectoryScanner scan = new DirectoryScanner();
            String[] includes = new String[(length = this.includes.length) + 1];
            System.arraycopy(this.includes, 0, includes, 1, length);
            includes[0] = "**/*.class";
            scan.setBasedir(classpath);
            scan.setIncludes(includes);
            scan.scan();
            length = (includes = scan.getIncludedFiles()).length;
            final long timestamp = classpath.lastModified();
            if (lazy || length == 0) for (int i = 0;;) {
                if (i == length) {
                    log.info("Nothing to recompile");
                    report.classesExamined = length;
                    report.status = "no-op";
                    report.totalNanos = System.nanoTime() - runStarted;
                    logSummary(log, report);
                    writeRunReport(report);
                    return;
                }
                final File file;
                if ((file = new File(classpath, includes[i++])).exists() && file.lastModified() >= timestamp) {
                    break;
                }
            }

            final Set<Artifact> artifacts;
            final Dependency[] dependencies;
            if ((dependencies = this.dependencies) == null || (length = dependencies.length) == 0) {
                artifacts = project.getArtifacts();
            } else {
                artifacts = new LinkedHashSet<>(project.getArtifacts());
                for (int i = 0; i != length;) {
                    for (final Iterator<ArtifactResult> it = system.resolveDependencies(
                            session.getRepositorySession(),
                            new DependencyRequest(
                                    new CollectRequest(
                                            RepositoryUtils.toDependency(dependencies[i++], RepositoryUtils.newArtifactTypeRegistry(manager)),
                                            project.getRemoteProjectRepositories()
                                    ),
                                    null
                            )
                    ).getArtifactResults().iterator(); it.hasNext();) {
                        final Artifact artifact;
                        if (artifacts.add(artifact = RepositoryUtils.toArtifact(it.next().getArtifact()))) {
                            artifact.setScope("provided");
                        }
                    }
                }
            }

            log.info("Resolving class hierarchy...");
            long scantime = System.nanoTime();
            for (String path : includes) {
                File file = new File(classpath, path);
                if (file.exists()) {
                    if (log.isDebugEnabled()) log.debug(" + " + file);
                    unique.add(path.replace(File.separatorChar, '/'));
                    try (InputStream fis = Files.newInputStream(file.toPath())) {
                        new ClassReader(fis).accept(new BridgeScanner(types), ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                    }
                    continue;
                }
                if (log.isDebugEnabled()) log.debug(" - " + file);
            }

            for (Artifact artifact : artifacts) {
                File file = artifact.getFile();
                if (file.getName().endsWith(".jar") && file.exists()) {
                    log.info("  + " + file + " (" + artifact.getScope() + ')');
                    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file.toPath()))) {
                        for (ZipEntry entry; (entry = zis.getNextEntry()) != null;) {
                            if (!entry.getName().endsWith(".class") || !unique.add(entry.getName())) continue;
                            new ClassReader(zis).accept(new HierarchyScanner(types), ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                        }
                    }
                    continue;
                }
                if (log.isDebugEnabled()) log.debug(" - " + file + " (" + artifact.getScope() + ')');
            }

            scantime = System.nanoTime() - scantime;
            report.hierarchyScanNanos = scantime;
            log.info("");
            log.info("Building bridges...");
            scan = new DirectoryScanner();
            scan.setBasedir(classpath);
            scan.setIncludes(this.includes);
            scan.setExcludes(this.excludes);
            scan.scan();
            String[] targets = scan.getIncludedFiles();
            report.classesExamined = targets.length;
            long comptime = System.nanoTime();
            for (String path : targets) {
                File in = new File(classpath, path);
                if (in.exists()) {
                    ClassNode code;
                    BridgeVisitor visitor;
                    path = path.replace(File.separatorChar, '/');
                    if (log.isDebugEnabled()) log.debug("<- " + path);
                    try (InputStream is = Files.newInputStream(in.toPath())) {
                        new ClassReader(is).accept(visitor = new BridgeVisitor(code = new ClassNode(), types), ClassReader.EXPAND_FRAMES);
                    }
                    int i;
                    boolean changed = visitor.bridges != 0 || visitor.invocations != 0 || visitor.adjustments != 0 ||
                            visitor.removals != 0 || visitor.forks.size() != 1;
                    if (changed) {
                        ++report.classesTransformed;
                        report.bridges += visitor.bridges;
                        report.invocations += visitor.invocations;
                        report.adjustments += visitor.adjustments;
                        report.removals += visitor.removals;
                        report.forks += Math.max(0, visitor.forks.size() - 1);

                        StringBuilder msg = new StringBuilder().append(" -> ").append(
                                ((i = path.lastIndexOf(visitor.name)) >= 0 && path.indexOf('/', i + visitor.name.length()) < 0)?
                                        path.substring(0, i) + visitor.name.replace('/', '.') : path
                        );
                        if ((i = visitor.forks.size()) != 1) msg.append("  +").append(--i).append(" fork").append((i == 1)?"":"s");
                        if ((i = visitor.bridges) != 0) msg.append("  +").append(i).append(" bridge").append((i == 1)?"":"s");
                        if ((i = visitor.invocations) != 0) msg.append("  +").append(i).append(" invocation").append((i == 1)?"":"s");
                        if ((i = visitor.adjustments) != 0) msg.append("  +").append(i).append(" adjustment").append((i == 1)?"":"s");
                        if ((i = visitor.removals) != 0) msg.append("  +").append(i).append(" removal").append((i == 1)?"":"s");
                        log.info(msg.toString());
                    }
                    File out;
                    HierarchicalWriter writer;
                    for (Map.Entry<Integer, Boolean> e : visitor.forks.entrySet()) {
                        code.accept(new ForkVisitor(
                                writer = new HierarchicalWriter(types, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS),
                                visitor,
                                i = e.getKey(),
                                flags
                        ));
                        (out = ((e.getValue())? new File(classpath, "META-INF/versions/" + i + '/' + visitor.name + ".class") : in)).getParentFile().mkdirs();
                        try (OutputStream os = Files.newOutputStream(out.toPath())) {
                            os.write(writer.toByteArray());
                        }
                    }
                }
            }

            comptime = System.nanoTime() - comptime;
            report.transformNanos = comptime;
            report.status = "success";
            report.totalNanos = System.nanoTime() - runStarted;
            log.info("");
            log.info("Hierarchy resolved in " + humanize(scantime));
            log.info("Recompiled in " + humanize(comptime));
            logSummary(log, report);
            writeRunReport(report);
            classpath.setLastModified((Instant.now().getEpochSecond() * 1000) + 1000);
        } catch (Throwable e) {
            if (e instanceof MojoExecutionException mojo && mojo.getMessage() != null && mojo.getMessage().startsWith("[BRIDGE-E002]")) {
                throw mojo;
            }
            report.status = "failed";
            report.totalNanos = System.nanoTime() - runStarted;
            report.error("BRIDGE-E001", "Bridge transformation failed", e);
            try {
                writeRunReport(report);
            } catch (MojoExecutionException reportFailure) {
                e.addSuppressed(reportFailure);
                log.error(reportFailure.getMessage(), reportFailure.getCause());
            }
            throw new MojoExecutionException("[BRIDGE-E001] Bridge transformation failed", e);
        }
    }

    private void writeRunReport(BridgeRunReport report) throws MojoExecutionException {
        if (!reportEnabled) return;
        try {
            report.write(reportFile.toPath());
        } catch (Throwable e) {
            throw new MojoExecutionException("[BRIDGE-E002] Failed to write Bridge run report", e);
        }
    }

    private static void warn(Log log, BridgeRunReport report, String id, String message) {
        log.warn('[' + id + "] " + message);
        report.warning(id, message);
    }

    private static void logSummary(Log log, BridgeRunReport report) {
        log.info("Bridge " + report.status + ": " + report.classesExamined + " classes examined; " +
                report.classesTransformed + " transformed; " + report.bridges + " bridges; " +
                report.invocations + " invocations; " + report.adjustments + " adjustments; " +
                report.removals + " removals; " + report.forks + " forks; " + report.warningCount() + " warnings");
    }

    private static boolean isBridgeApiArtifact(Artifact artifact) {
        String group = artifact.getGroupId();
        return (CANONICAL_GROUP.equalsIgnoreCase(group) || LEGACY_GROUP.equalsIgnoreCase(group)) &&
                "bridge".equalsIgnoreCase(artifact.getArtifactId()) &&
                "jar".equalsIgnoreCase(artifact.getType()) &&
                artifact.getClassifier() == null;
    }

    private static String humanize(long timing) {
        if (timing < 1000000L) {
            return timing + "ns";
        } else if (timing < 1000000000L) {
            return String.format("%.2fms", timing / 1000000D);
        } else if (timing < 60000000000L) {
            return String.format("%.4fs", timing / 1000000000D);
        } else if (timing < 3600000000000L) {
            return String.format("%.4fm", timing / 60000000000D);
        } else if (timing < 86400000000000L) {
            return String.format("%.4fh", timing / 3600000000000D);
        } else if (timing < 604800000000000L) {
            return String.format("%.5fd", timing / 86400000000000D);
        } else {
            return String.format("%.5fw", timing / 604800000000000D);
        }
    }
}
