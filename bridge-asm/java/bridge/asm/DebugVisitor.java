package bridge.asm;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Locale;

import static org.objectweb.asm.Opcodes.*;

public final class DebugVisitor extends MethodVisitor {
    final LinkedList<String> handlers = new LinkedList<>();
    final LinkedList<String> locals = new LinkedList<>();
    final PrintStream out;
    boolean merging;
    int id;

    public DebugVisitor(PrintStream output, String owner, String name, String descriptor) {
        this(output, owner, name, descriptor, null);
    }

    public DebugVisitor(PrintStream output, String owner, String name, String descriptor, MethodVisitor delegate) {
        super(ASM9, delegate);
        this.out = output;
        out.print(owner);
        out.print('.');
        out.print(name);
        out.print(descriptor);
    }

    private static String name(int label) {
        final StringBuilder result = new StringBuilder();
        while (--label >= 0) {
            result.insert(0, (char) ('A' + (label % 26)));
            label /= 26;
        }
        return result.toString();
    }

    private static String hex(Label label) {
        return Integer.toHexString(label.hashCode()).toUpperCase(Locale.ROOT);
    }
    public void visitOperation(int opcode) {
        merging = false;
        out.println();
        final StringBuilder result = (opcode >= 0)?
                new StringBuilder().append("    0x").append(Integer.toHexString(opcode)) :
                new StringBuilder().append("     ").append(opcode);
        while (result.length() < 8) result.append(' ');
        out.print(result);
    }

    @Override
    public void visitEnd() {
        out.println();
        super.visitEnd();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        visitOperation(opcode);
        out.print("  ");
        out.print(owner);
        out.print('.');
        out.print(name);
        out.print(' ');
        out.print(descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        visitOperation(IINC);
        out.print("  ");
        out.print(increment);
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
        out.print("  ");
        out.print(operand);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        visitOperation(INVOKEDYNAMIC);
        out.print("  ");
        out.print(name);
        out.print(descriptor);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        visitOperation(opcode);
        out.print(" -> #");
        out.print(hex(label));
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        if (!merging) {
            merging = true;
            out.println();
            out.print('[');
            out.print(name(++id));
            out.print("]:");
        } else {
            out.print(',');
        }
        out.print(" #");
        out.print(hex(label));
        super.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        visitOperation(LDC);
        out.print("  ");
        out.print(value);
        super.visitLdcInsn(value);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        visitOperation(LOOKUPSWITCH);
        out.print("  {");
        for (int i = 0, length = Math.min(keys.length, labels.length); i != length;) {
            out.print(keys[i]);
            out.print(": #");
            out.print(hex(labels[i++]));
            out.print(", ");
        }
        out.print("default: #");
        out.print(hex(dflt));
        out.print('}');
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        locals.add("    [#" + hex(start) + ", #" + hex(end) + "] " + index + " = " + name + ' ' + descriptor);
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        out.println();
        out.println();
        if (handlers.size() != 0) {
            out.println("Exception handlers:");
            for (String handler : handlers) {
                out.println(handler);
            }
            out.println();
        }
        if (locals.size() != 0) {
            out.println("Local variables:");
            for (String local : locals) {
                out.println(local);
            }
            out.println();
        }
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        visitOperation(opcode);
        out.print("  ");
        out.print(owner);
        out.print('.');
        out.print(name);
        out.print(descriptor);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        visitOperation(MULTIANEWARRAY);
        out.print("  ");
        for (int i = numDimensions; i > 0; --i) {
            out.print('[');
        }
        out.print(descriptor);
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        visitOperation(TABLESWITCH);
        out.print("  [");
        out.print(min);
        out.print(", ");
        out.print(max);
        out.print("] {");
        for (int i = 0, length = labels.length; i != length;) {
            out.print(min + i);
            out.print(": #");
            out.print(hex(labels[i++]));
            out.print(", ");
        }
        out.print("default: #");
        out.print(hex(dflt));
        out.print('}');
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        handlers.add("    [#" + hex(start) + ", #" + hex(end) + "] -> #" + hex(handler));
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        visitOperation(opcode);
        out.print("  ");
        out.print(type);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        visitOperation(opcode);
        out.print("  ");
        out.print(varIndex);
        super.visitVarInsn(opcode, varIndex);
    }
}
