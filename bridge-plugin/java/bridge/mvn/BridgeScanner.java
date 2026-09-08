package bridge.mvn;

import bridge.asm.HierarchyScanner;
import bridge.asm.TypeMap;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import static org.objectweb.asm.Opcodes.*;

final class BridgeScanner extends HierarchyScanner {
    private static final int ACC_VALID = ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE | ACC_FINAL | ACC_SYNTHETIC | ACC_TRANSIENT | ACC_VARARGS;
    private static final String[] EMPTY = new String[0];
    private final BridgeData data;

    BridgeScanner(TypeMap types) {
        super(types);
        super.data = data = new BridgeData();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (!visible) {
            if (desc.equals("Lbridge/Adopt;")) {
                return new AnnotationVisitor(ASM9) {
                    LinkedList<String> implemented;
                    String extended;
                    String signature;
                    boolean clean;

                    @Override
                    public void visit(String name, Object value) {
                        if (name.equals("clean")) {
                            clean = (boolean) value;
                        } else if (name.equals("parent")) {
                            extended = ((Type) value).getInternalName();
                        } else {
                            signature = value.toString();
                        }
                    }

                    @Override
                    public AnnotationVisitor visitArray(String name) {
                        if (implemented == null) implemented = new LinkedList<>();
                        return new AnnotationVisitor(ASM9) {
                            public void visit(String name, Object value) {
                                implemented.add(((Type) value).getInternalName());
                            }
                        };
                    }

                    @Override
                    public void visitEnd() {
                        int i;
                        if (implemented != null && implemented.size() != 0) {
                            if (!clean && BridgeScanner.super.implemented != null && (i = BridgeScanner.super.implemented.length) != 0) {
                                String[] array = BridgeScanner.super.implemented = Arrays.copyOf(BridgeScanner.super.implemented, i + implemented.size(), String[].class);
                                for (Iterator<String> it = implemented.iterator(); it.hasNext();) array[i++] = it.next();
                            } else {
                                BridgeScanner.super.implemented = implemented.toArray(new String[0]);
                            }
                        } else if (clean) {
                            BridgeScanner.super.implemented = EMPTY;
                        }
                        if (extended != null) {
                            BridgeScanner.super.extended = extended;
                        } else if (clean) {
                            BridgeScanner.super.extended = "java/lang/Object";
                        }
                        data.signature = signature;
                        data.adopted = true;
                    }
                };
            } else if (desc.equals("Lbridge/Synthetic;")) {
                access |= ACC_SYNTHETIC;
                return null;
            }
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        String identifier;
        data.members.put(identifier = descriptor + name, access);
        return new FieldVisitor(ASM9, super.visitField(access, name, descriptor, signature, value)) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (!visible) {
                    if (desc.equals("Lbridge/Bridges;") || desc.equals("Lbridge/Bridge;")) {
                        final int ACC_VALID = ((access & ACC_STATIC) == 0)? BridgeScanner.ACC_VALID : BridgeScanner.ACC_VALID | ACC_STATIC;
                        return new BridgeAnnotation(ACC_TRANSIENT | ACC_SYNTHETIC | access, name, descriptor, null, Type.getType(descriptor), data -> {
                            BridgeScanner.this.data.bridges.computeIfAbsent(identifier, k -> new LinkedList<>()).add(data);
                            BridgeScanner.this.data.members.put(data.desc + data.name, data.access &= ACC_VALID);
                        });
                    } else if (desc.equals("Lbridge/Synthetic;")) {
                        data.members.put(identifier, access | ACC_SYNTHETIC);
                        return null;
                    }
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        String identifier;
        data.members.put(identifier = name + descriptor, access);
        return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (!visible) {
                    if (desc.equals("Lbridge/Bridges;") || desc.equals("Lbridge/Bridge;")) {
                        final int ACC_VALID = ((access & ACC_STATIC) == 0)? BridgeScanner.ACC_VALID : BridgeScanner.ACC_VALID | ACC_STATIC;
                        final int ACC_COMPATIBLE = (name.equals("<init>"))? (access & ACC_SYNCHRONIZED) : (access & ACC_SYNCHRONIZED) | ACC_BRIDGE;
                        return new BridgeAnnotation(ACC_SYNTHETIC | access, name, descriptor, exceptions, Type.getReturnType(descriptor), data -> {
                            BridgeScanner.this.data.bridges.computeIfAbsent(identifier, k -> new LinkedList<>()).add(data);
                            BridgeScanner.this.data.members.put(data.name + data.desc, data.access = (data.access & ACC_VALID) | ACC_COMPATIBLE);
                        });
                    } else if (desc.equals("Lbridge/Synthetic;")) {
                        data.members.put(identifier, access | ACC_SYNTHETIC);
                        return null;
                    }
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }
}
