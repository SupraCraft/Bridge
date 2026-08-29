package bridge.mvn;

import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Structured, machine-readable summary of one Bridge Maven-plugin invocation.
 *
 * <p>This report is observational evidence only. It is never part of the
 * transformed artifact identity.</p>
 */
final class BridgeRunReport {

    static final String SCHEMA = "bridge-run/1";

    private final String bridgeVersion;
    private final String javaVersion = System.getProperty("java.version", "unknown");
    private final String asmVersion;
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    String status = "running";
    int classesExamined;
    int classesTransformed;
    int bridges;
    int invocations;
    int adjustments;
    int removals;
    int forks;
    long hierarchyScanNanos;
    long transformNanos;
    long totalNanos;

    BridgeRunReport(String bridgeVersion) {
        this.bridgeVersion = (bridgeVersion == null)? "unknown" : bridgeVersion;
        Package asmPackage = ClassReader.class.getPackage();
        String implementationVersion = (asmPackage == null)? null : asmPackage.getImplementationVersion();
        this.asmVersion = (implementationVersion == null)? "unknown" : implementationVersion;
    }

    void warning(String id, String message) {
        diagnostics.add(new Diagnostic(id, "warning", message, null));
    }

    void error(String id, String message, Throwable cause) {
        diagnostics.add(new Diagnostic(
                id,
                "error",
                message,
                (cause == null)? null : cause.getClass().getName()
        ));
    }

    int warningCount() {
        int count = 0;
        for (Diagnostic diagnostic : diagnostics) {
            if ("warning".equals(diagnostic.severity)) ++count;
        }
        return count;
    }

    void write(Path requestedPath) throws IOException {
        Path path = requestedPath.toAbsolutePath().normalize();
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, path.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, toJson(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private String toJson() {
        StringBuilder json = new StringBuilder(1024);
        json.append("{\n");
        field(json, "schema", SCHEMA, 1, true);
        field(json, "status", status, 1, true);
        field(json, "bridgeVersion", bridgeVersion, 1, true);
        field(json, "asmVersion", asmVersion, 1, true);
        field(json, "hostJavaVersion", javaVersion, 1, true);

        json.append("  \"classes\": {\n");
        numberField(json, "examined", classesExamined, 2, true);
        numberField(json, "transformed", classesTransformed, 2, false);
        json.append("  },\n");

        json.append("  \"transformations\": {\n");
        numberField(json, "bridges", bridges, 2, true);
        numberField(json, "invocations", invocations, 2, true);
        numberField(json, "adjustments", adjustments, 2, true);
        numberField(json, "removals", removals, 2, true);
        numberField(json, "forks", forks, 2, false);
        json.append("  },\n");

        json.append("  \"timingNanos\": {\n");
        numberField(json, "hierarchyScan", hierarchyScanNanos, 2, true);
        numberField(json, "transform", transformNanos, 2, true);
        numberField(json, "total", totalNanos, 2, false);
        json.append("  },\n");

        json.append("  \"diagnostics\": [");
        if (!diagnostics.isEmpty()) json.append('\n');
        for (int i = 0; i < diagnostics.size(); ++i) {
            Diagnostic diagnostic = diagnostics.get(i);
            json.append("    {\n");
            field(json, "id", diagnostic.id, 3, true);
            field(json, "severity", diagnostic.severity, 3, true);
            field(json, "message", diagnostic.message, 3, diagnostic.causeType != null);
            if (diagnostic.causeType != null) {
                field(json, "causeType", diagnostic.causeType, 3, false);
            }
            json.append("    }");
            if (i + 1 < diagnostics.size()) json.append(',');
            json.append('\n');
        }
        if (!diagnostics.isEmpty()) json.append("  ");
        json.append("]\n");
        json.append("}\n");
        return json.toString();
    }

    private static void field(StringBuilder json, String name, String value, int indent, boolean comma) {
        indent(json, indent);
        json.append('"').append(escape(name)).append("\": \"").append(escape(value)).append('"');
        if (comma) json.append(',');
        json.append('\n');
    }

    private static void numberField(StringBuilder json, String name, long value, int indent, boolean comma) {
        indent(json, indent);
        json.append('"').append(escape(name)).append("\": ").append(value);
        if (comma) json.append(',');
        json.append('\n');
    }

    private static void indent(StringBuilder json, int depth) {
        for (int i = 0; i < depth; ++i) json.append("  ");
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    private record Diagnostic(String id, String severity, String message, String causeType) {}
}
