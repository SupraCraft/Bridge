package bridge.asm;

import org.objectweb.asm.*;

import java.util.LinkedList;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;

public class QueuedVisitor extends MethodVisitor {
    protected LinkedList<Runnable> ops = new LinkedList<>();

    protected QueuedVisitor(int api) {
        super(api);
    }

    protected QueuedVisitor(int api, MethodVisitor delegate) {
        super(api, delegate);
    }

    public void visitOperation(int opcode, Runnable op) {
        ops.add(op);
    }

    // --- only notification posters beyond this point ---

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new Repeater(super::visitAnnotationDefault);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new Repeater(() -> super.visitAnnotation(descriptor, visible));
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        visitOperation(-1, () -> super.visitAnnotableParameterCount(parameterCount, visible));
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        visitOperation(-1, () -> super.visitAttribute(attribute));
    }

    @Override
    public void visitCode() {
        ops.add(super::visitCode);
    }

    @Override
    public void visitEnd() {
        ops.add(super::visitEnd);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        ops.add(() -> super.visitFrame(type, numLocal, local, numStack, stack));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        visitOperation(opcode, () -> super.visitFieldInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        visitOperation(IINC, () -> super.visitIincInsn(varIndex, increment));
    }

    @Override
    public void visitInsn(int opcode) {
        visitOperation(opcode, () -> super.visitInsn(opcode));
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        visitOperation(opcode, () -> super.visitIntInsn(opcode, operand));
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new Repeater(() -> super.visitInsnAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        visitOperation(INVOKEDYNAMIC, () -> super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        visitOperation(opcode, () -> super.visitJumpInsn(opcode, label));
    }

    @Override
    public void visitLabel(Label label) {
        ops.add(() -> super.visitLabel(label));
    }

    @Override
    public void visitLdcInsn(Object value) {
        visitOperation(LDC, () -> super.visitLdcInsn(value));
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        ops.add(() -> super.visitLineNumber(line, start));
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        visitOperation(LOOKUPSWITCH, () -> super.visitLookupSwitchInsn(dflt, keys, labels));
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        visitOperation(-1, () -> super.visitLocalVariable(name, descriptor, signature, start, end, index));
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        return new Repeater(() -> super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible));
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        ops.add(() -> super.visitMaxs(maxStack, maxLocals));
    }

    @Override
    @Deprecated
    @SuppressWarnings({"DeprecatedIsStillUsed", "deprecation"})
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        visitMethodInsn((api < ASM5)? SOURCE_DEPRECATED | opcode : opcode, owner, name, descriptor, opcode == INVOKEINTERFACE);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (api < ASM5 && (opcode & SOURCE_DEPRECATED) == 0 && (opcode == INVOKEINTERFACE) == isInterface) {
            visitMethodInsn(opcode, owner, name, descriptor);
            return;
        }
        visitOperation(opcode & ~SOURCE_MASK, () -> super.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        visitOperation(MULTIANEWARRAY, () -> super.visitMultiANewArrayInsn(descriptor, numDimensions));
    }

    @Override
    public void visitParameter(String name, int access) {
        visitOperation(-1, () -> super.visitParameter(name, access));
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return new Repeater(() -> super.visitParameterAnnotation(parameter, descriptor, visible));
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        visitOperation(TABLESWITCH, () -> super.visitTableSwitchInsn(min, max, dflt, labels));
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new Repeater(() -> super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        visitOperation(-1, () -> super.visitTryCatchBlock(start, end, handler, type));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new Repeater(() -> super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        visitOperation(opcode, () -> super.visitTypeInsn(opcode, type));
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        visitOperation(opcode, () -> super.visitVarInsn(opcode, varIndex));
    }

    private final class Repeater extends AnnotationVisitor {
        private Repeater(Supplier<AnnotationVisitor> init) {
            super(ASM9);
            visitOperation(-1, () -> av = init.get());
        }

        @Override
        public void visit(String name, Object value) {
            visitOperation(-1, () -> super.visit(name, value));
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            visitOperation(-1, () -> super.visitEnum(name, descriptor, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            return new Repeater(() -> super.visitAnnotation(name, descriptor));
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new Repeater(() -> super.visitArray(name));
        }

        @Override
        public void visitEnd() {
            visitOperation(-1, super::visitEnd);
        }
    }
}
