package bridge.asm;

import org.objectweb.asm.MethodVisitor;

public interface LinkedVisitor {
    MethodVisitor getDelegate();
    <T extends MethodVisitor> T setDelegate(T value);


    MethodVisitor getParent();
    <T extends MethodVisitor> T setParent(T value);
}
