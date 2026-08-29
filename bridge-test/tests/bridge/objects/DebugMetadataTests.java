package bridge.objects;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.nio.file.Files;
import java.nio.file.Path;

public class DebugMetadataTests {

    public void testNoDebugFlagRemovesMetadata() throws Exception {
        String configured = System.getProperty("bridge.test.outputDirectory");
        if (configured == null || configured.isBlank()) {
            throw new AssertionError("bridge.test.outputDirectory was not configured by Maven");
        }

        Path classFile = Path.of(configured, "bridge", "objects", "Dummy.class");
        if (!Files.isRegularFile(classFile)) {
            throw new AssertionError("Missing transformed fixture: " + classFile);
        }

        final boolean[] source = {false};
        final boolean[] parameter = {false};
        final boolean[] line = {false};
        final boolean[] local = {false};

        new ClassReader(Files.readAllBytes(classFile)).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visitSource(String sourceName, String debug) {
                source[0] = true;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitParameter(String name, int access) {
                        parameter[0] = true;
                    }

                    @Override
                    public void visitLineNumber(int lineNumber, Label start) {
                        line[0] = true;
                    }

                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        local[0] = true;
                    }
                };
            }
        }, 0);

        if (source[0] || parameter[0] || line[0] || local[0]) {
            throw new AssertionError(
                    "NO_DEBUG left metadata behind: source=" + source[0] +
                    ", parameter=" + parameter[0] +
                    ", line=" + line[0] +
                    ", local=" + local[0]
            );
        }
    }
}
