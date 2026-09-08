package bridge.asm;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public abstract class OperationVisitor extends MethodVisitor {

    protected OperationVisitor(int api) {
        super(api);
    }

    protected OperationVisitor(int api, MethodVisitor delegate) {
        super(api, delegate);
    }

    public abstract void visitOperation(int opcode);

    // --- only notification posters beyond this point ---

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        visitOperation(opcode);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        visitOperation(IINC);
        super.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitInsn(int opcode) {
        visitOperation(opcode);
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        visitOperation(opcode);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        visitOperation(-1);
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        visitOperation(INVOKEDYNAMIC);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        visitOperation(opcode);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        visitOperation(LDC);
        super.visitLdcInsn(value);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        visitOperation(LOOKUPSWITCH);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        visitOperation(-1);
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        visitOperation(-1);
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
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
        visitOperation(opcode & ~SOURCE_MASK);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        visitOperation(MULTIANEWARRAY);
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        visitOperation(TABLESWITCH);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        visitOperation(-1);
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        visitOperation(-1);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        visitOperation(-1);
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        visitOperation(opcode);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        visitOperation(opcode);
        super.visitVarInsn(opcode, varIndex);
    }
}
