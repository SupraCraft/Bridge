package bridge;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Independent structural/data-flow verification of the class files produced by Bridge.
 */
public final class VerificationTests {

    public void testTransformedClassesPassAsmVerification() throws Exception {
        String configured = System.getProperty("bridge.test.outputDirectory");
        if (configured == null || configured.isBlank()) {
            throw new AssertionError("bridge.test.outputDirectory was not configured by Maven");
        }

        Path root = Path.of(configured);
        if (!Files.isDirectory(root)) {
            throw new AssertionError("Bridge test output directory does not exist: " + root);
        }

        List<String> failures = new ArrayList<>();
        int verified = 0;
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .filter(value -> value.getFileName().toString().endsWith(".class"))
                    .sorted()
                    .toList()) {
                ++verified;
                StringWriter details = new StringWriter();
                try (PrintWriter writer = new PrintWriter(details)) {
                    CheckClassAdapter.verify(
                            new ClassReader(Files.readAllBytes(path)),
                            Thread.currentThread().getContextClassLoader(),
                            false,
                            writer
                    );
                } catch (Throwable failure) {
                    failures.add(root.relativize(path) + ": verifier threw " + failure);
                    continue;
                }
                if (!details.toString().isBlank()) {
                    failures.add(root.relativize(path) + ":\n" + details);
                }
            }
        }

        if (verified == 0) {
            throw new AssertionError("No transformed class files were found under " + root);
        }
        if (!failures.isEmpty()) {
            throw new AssertionError("ASM verification failed for transformed classes:\n" + String.join("\n", failures));
        }
    }
}
