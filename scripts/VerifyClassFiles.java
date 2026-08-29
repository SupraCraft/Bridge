import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Verifies Bridge-produced class files with the JDK Class-File API (JDK 24+).
 *
 * Usage:
 *   java scripts/VerifyClassFiles.java <verify-root> <classpath-root>...
 */
public final class VerifyClassFiles {
    private VerifyClassFiles() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Usage: VerifyClassFiles <verify-root> <classpath-root>...");
        }

        Path verifyRoot = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isDirectory(verifyRoot)) {
            throw new IllegalArgumentException("Verification root is not a directory: " + verifyRoot);
        }

        List<URL> classpath = new ArrayList<>();
        for (int index = 1; index < args.length; ++index) {
            Path root = Path.of(args[index]).toAbsolutePath().normalize();
            if (Files.isDirectory(root)) {
                classpath.add(root.toUri().toURL());
            }
        }

        int verified = 0;
        List<String> failures = new ArrayList<>();
        try (URLClassLoader loader = new URLClassLoader(
                classpath.toArray(URL[]::new),
                ClassLoader.getSystemClassLoader()
        )) {
            ClassFile verifier = ClassFile.of(
                    ClassFile.ClassHierarchyResolverOption.of(
                            ClassHierarchyResolver.ofResourceParsing(loader).cached()
                    )
            );

            try (Stream<Path> paths = Files.walk(verifyRoot)) {
                for (Path path : paths.filter(Files::isRegularFile)
                        .filter(value -> value.getFileName().toString().endsWith(".class"))
                        .sorted()
                        .toList()) {
                    ++verified;
                    var errors = verifier.verify(path);
                    if (!errors.isEmpty()) {
                        failures.add(verifyRoot.relativize(path) + ": " + errors);
                    }
                }
            }
        }

        if (verified == 0) {
            throw new AssertionError("No class files found under " + verifyRoot);
        }
        if (!failures.isEmpty()) {
            throw new AssertionError("JDK Class-File API verification failed:\n" + String.join("\n", failures));
        }

        System.out.println("JDK Class-File API verification passed for " + verified + " class files.");
    }
}
