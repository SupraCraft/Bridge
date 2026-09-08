package bridge.mvn;

import bridge.asm.KnownType;
import bridge.asm.TypeMap;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import static bridge.asm.Types.*;
import static org.objectweb.asm.Opcodes.*;

final class BridgeVisitor extends ClassVisitor {
    private Map<BridgeAnnotation.Data, Field> clfields, fields;
    private boolean clinit, init;
    final TypeMap types;

    KnownType type;
    BridgeData data;
    HashMap<Integer, Boolean> forks = new HashMap<>();
    String adopt, name, src = "Unknown Source";
    int version, bridges, invocations, adjustments, removals;

    BridgeVisitor(ClassVisitor delegate, TypeMap types) {
        super(ASM9, delegate);
        this.types = types;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String extended, String[] implemented) {
        KnownType type = this.type = types.loadClass(this.name = name);
        BridgeData data = this.data = (BridgeData) type.data();
        if (data != null) {
            if (data.adopted) {
                final KnownType[] interfaces;
                int i = 0, length = (interfaces = type.interfaces()).length;
                if (implemented == null || implemented.length != length) implemented = new String[length];
                while (i != length) implemented[i] = interfaces[i++].type.getInternalName();
                extended = adopt = type.supertype().type.getInternalName();
                signature = data.signature;
            }
            access = type.modifiers();
        } else this.data = new BridgeData();
        this.forks.put(this.version = version - 44, Boolean.FALSE);
        super.visit(version, access, name, signature, extended, implemented);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (!visible && (descriptor.equals("Lbridge/Adopt;") || descriptor.equals("Lbridge/Synthetic;"))) {
            ++adjustments;
            return null;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(this.src = source, debug);
    }

    private final class Field extends FieldVisitor {
        private final int access;
        private final String name, desc;
        private final KnownType returns;
        private final Object value;

        private Field(int access, String name, String descriptor, String signature, Object value) {
            super(ASM9, BridgeVisitor.super.visitField(access, name, descriptor, signature, value));
            this.access = access;
            this.name = name;
            this.desc = descriptor;
            this.returns = types.load(descriptor);
            this.value = value;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (!visible) switch (desc) {
                case "Lbridge/Synthetic;":
                    ++adjustments;
                case "Lbridge/Bridges;":
                case "Lbridge/Bridge;":
                    return null;
            }
            return super.visitAnnotation(desc, visible);
        }

        private Map<BridgeAnnotation.Data, Field> initStatic() {
            if (clinit) throw new IllegalStateException("Attempted @Bridge to static field after <clinit>: " + this.name.replace('/', '.') + '.' + name + '(' + src + ')');
            if (clfields == null) clfields = new HashMap<>();
            return clfields;
        }

        private Map<BridgeAnnotation.Data, Field> initVirtual() {
            if (init) throw new IllegalStateException("Attempted @Bridge to instance field after <init>: " + this.name.replace('/', '.') + '.' + name + '(' + src + ')');
            if (fields == null) fields = new HashMap<>();
            return fields;
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            final LinkedList<BridgeAnnotation.Data> bridges;
            if ((bridges = data.bridges.get(desc + name)) != null) {
                boolean safe, constant = (access & ACC_FINAL) != 0;
                for (BridgeAnnotation.Data data : bridges) {
                    safe = constant && returns.type.equals(data.returns);
                    FieldVisitor fv = cv.visitField(data.access, data.name, data.desc, data.sign, (safe)? value : null);
                    data.annotate(fv::visitAnnotation);
                    fv.visitEnd();
                    if (!safe || value == null) {
                        (((data.access & ACC_STATIC) != 0)? initStatic() : initVirtual()).put(data, this);
                    }
                }
                BridgeVisitor.this.bridges += bridges.size();
            }
        }
    }

    @Override
    public FieldVisitor visitField(int $, String name, String descriptor, String signature, Object value) {
        return new Field(data.members.getOrDefault(descriptor + name, $), name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int $, String name, String descriptor, String signature, String[] exceptions) {
        final int access = data.members.getOrDefault(name + descriptor, $);
        return new InvocationVisitor(this, access, name, descriptor, new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            private final KnownType returns = types.load(Type.getReturnType(descriptor));
            private final boolean amend = name.equals("<init>");
            private boolean safe = true;
            private int occ = 0;

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (!visible) switch (desc) {
                    case "Lbridge/Synthetic;":
                        ++adjustments;
                    case "Lbridge/Bridges;":
                    case "Lbridge/Bridge;":
                        return null;
                }
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
                if (opcode == NEW) ++occ;
                super.visitTypeInsn(opcode, type);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
                if (opcode == INVOKESPECIAL && name.equals("<init>")) {
                    if (occ > 0) {
                        --occ;
                    } else if (amend) {
                        if (BridgeVisitor.this.name.equals(owner)) {
                            safe = false;
                        } else if (adopt != null) {
                            owner = adopt;
                        }
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            }

            @Override
            public void visitInsn(int opcode) {
                if (opcode == RETURN) {
                    Map<BridgeAnnotation.Data, Field> map;
                    if (amend) {
                        init = true;
                        map = fields;
                    } else if (name.equals("<clinit>")) {
                        clinit = true;
                        map = clfields;
                    } else {
                        map = null;
                    }

                    if (map != null) {
                        BridgeAnnotation.Data data; Field field;
                        for (Entry<BridgeAnnotation.Data, Field> entry : map.entrySet()) {
                            data = entry.getKey();
                            if (safe || (data.access & ACC_FINAL) == 0) {
                                field = entry.getValue();
                                super.visitLabel(new Label());
                                int get = GETSTATIC, set = PUTSTATIC;
                                if ((data.access & ACC_STATIC) == 0) {
                                    super.visitVarInsn(ALOAD, 0);
                                    set = PUTFIELD;
                                    if ((field.access & ACC_STATIC) == 0) {
                                        super.visitInsn(DUP);
                                        get = GETFIELD;
                                    }
                                }
                                super.visitFieldInsn(get, BridgeVisitor.this.name, field.name, field.desc);
                                cast(mv, field.returns, types.load(data.returns));
                                super.visitFieldInsn(set, BridgeVisitor.this.name, data.name, data.desc);
                            }
                        }
                    }
                }
                super.visitInsn(opcode);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                final LinkedList<BridgeAnnotation.Data> bridges;
                if ((bridges = data.bridges.get(name + descriptor)) != null) {
                    final KnownType[] METHOD = types.load(Type.getArgumentTypes(descriptor));
                    for (BridgeAnnotation.Data data : bridges) {
                        MethodVisitor mv = cv.visitMethod(data.access, data.name, data.desc, data.sign, data.ex);
                        data.annotate(mv::visitAnnotation);
                        mv.visitCode();
                        mv.visitLabel(new Label());
                        KnownType[] params = types.load(Type.getArgumentTypes(data.desc));

                        int size = 0;
                        int invoke = INVOKESTATIC;
                        if ((data.access & ACC_STATIC) == 0) {
                            if ((access & ACC_STATIC) == 0) {
                                mv.visitVarInsn(ALOAD, 0);
                                invoke = INVOKESPECIAL;
                            }
                            size = 1;
                        }

                        // bridge -> method
                        int fromIndex = data.fromIndex;
                        final int length = Math.min(Math.min(METHOD.length, params.length), fromIndex + data.length);
                        size += size(params, 0, Math.min(fromIndex, params.length));

                        int toIndex = 0;
                        while (toIndex < data.toIndex) {
                            cast(mv, VOID_TYPE, METHOD[toIndex++].type);
                        }
                        for (KnownType from; fromIndex < length; ++fromIndex, ++toIndex) {
                            mv.visitVarInsn((from = params[fromIndex]).type.getOpcode(ILOAD), size);
                            size += size(from);
                            cast(mv, from, METHOD[toIndex]);
                        }
                        while (toIndex < METHOD.length) {
                            cast(mv, VOID_TYPE, METHOD[toIndex++].type);
                        }

                        // method -> bridge
                        mv.visitMethodInsn(invoke, BridgeVisitor.this.name, name, descriptor, type.isInterface());
                        cast(mv, this.returns, types.load(data.returns));
                        mv.visitInsn(data.returns.getOpcode(IRETURN));
                        mv.visitMaxs(0, 0);
                        mv.visitEnd();
                    }
                    BridgeVisitor.this.bridges += bridges.size();
                }
            }
        });
    }

    private void visitSpecial(int access, String name) {
        MethodVisitor mv = this.visitMethod(access, name, "()V", null, null);
        mv.visitCode();
        mv.visitLabel(new Label());
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @Override
    public void visitEnd() {
        if (!init && fields != null) this.visitSpecial(ACC_PUBLIC, "<init>");
        if (!clinit && clfields != null) this.visitSpecial(ACC_STATIC, "<clinit>");
        super.visitEnd();
    }
}
