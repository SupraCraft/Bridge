package bridge.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;

public final class Types {
    private Types() {}

    // Sort Constants
    public static final int VOID_SORT = Type.VOID;
    public static final int BOOLEAN_SORT = Type.BOOLEAN;
    public static final int CHAR_SORT = Type.CHAR;
    public static final int BYTE_SORT = Type.BYTE;
    public static final int SHORT_SORT = Type.SHORT;
    public static final int INT_SORT = Type.INT;
    public static final int FLOAT_SORT = Type.FLOAT;
    public static final int LONG_SORT = Type.LONG;
    public static final int DOUBLE_SORT = Type.DOUBLE;
    public static final int ARRAY_SORT = Type.ARRAY;
    public static final int OBJECT_SORT = Type.OBJECT;
    private static final Map<Type, Integer> PRIMITIVE_SORT;

    // Primitive Types
    public static final Type VOID_TYPE = Type.VOID_TYPE;
    public static final Type BOOLEAN_TYPE = Type.BOOLEAN_TYPE;
    public static final Type CHAR_TYPE = Type.CHAR_TYPE;
    public static final Type BYTE_TYPE = Type.BYTE_TYPE;
    public static final Type SHORT_TYPE = Type.SHORT_TYPE;
    public static final Type INT_TYPE = Type.INT_TYPE;
    public static final Type FLOAT_TYPE = Type.FLOAT_TYPE;
    public static final Type LONG_TYPE = Type.LONG_TYPE;
    public static final Type DOUBLE_TYPE = Type.DOUBLE_TYPE;
    public static final Type OBJECT_TYPE = Type.getType(Object.class);
    public static final Type CLASS_TYPE = Type.getType(Class.class);

    // Primitive Type Boxes
    public static final Type VOID_BOX = Type.getType(Void.class);
    public static final Type BOOLEAN_BOX = Type.getType(Boolean.class);
    public static final Type CHAR_BOX = Type.getType(Character.class);
    public static final Type BYTE_BOX = Type.getType(Byte.class);
    public static final Type SHORT_BOX = Type.getType(Short.class);
    public static final Type INT_BOX = Type.getType(Integer.class);
    public static final Type FLOAT_BOX = Type.getType(Float.class);
    public static final Type LONG_BOX = Type.getType(Long.class);
    public static final Type DOUBLE_BOX = Type.getType(Double.class);
    public static final Type NUMBER_BOX = Type.getType(Number.class);

    // Primitive Type Mappings
    static {
        HashMap<Type, Integer> sort = new HashMap<>();
        sort.put(VOID_TYPE, VOID_SORT);
        sort.put(VOID_BOX, VOID_SORT);
        sort.put(BOOLEAN_TYPE, BOOLEAN_SORT);
        sort.put(BOOLEAN_BOX, BOOLEAN_SORT);
        sort.put(CHAR_TYPE, CHAR_SORT);
        sort.put(CHAR_BOX, CHAR_SORT);
        sort.put(BYTE_TYPE, BYTE_SORT);
        sort.put(BYTE_BOX, BYTE_SORT);
        sort.put(SHORT_TYPE, SHORT_SORT);
        sort.put(SHORT_BOX, SHORT_SORT);
        sort.put(INT_TYPE, INT_SORT);
        sort.put(INT_BOX, INT_SORT);
        sort.put(FLOAT_TYPE, FLOAT_SORT);
        sort.put(FLOAT_BOX, FLOAT_SORT);
        sort.put(LONG_TYPE, LONG_SORT);
        sort.put(LONG_BOX, LONG_SORT);
        sort.put(DOUBLE_TYPE, DOUBLE_SORT);
        sort.put(DOUBLE_BOX, DOUBLE_SORT);
        PRIMITIVE_SORT = Collections.unmodifiableMap(sort);
    }

    public static Type get(int sort) {
        return get(sort, (Type) null);
    }

    public static Type get(int sort, Type def) {
        return get(sort, () -> def);
    }

    public static Type get(int sort, Supplier<Type> def) {
        switch (sort) {
            case VOID_SORT: return VOID_TYPE;
            case BOOLEAN_SORT: return BOOLEAN_TYPE;
            case CHAR_SORT: return CHAR_TYPE;
            case BYTE_SORT: return BYTE_TYPE;
            case SHORT_SORT: return SHORT_TYPE;
            case INT_SORT: return INT_TYPE;
            case FLOAT_SORT: return FLOAT_TYPE;
            case LONG_SORT: return LONG_TYPE;
            case DOUBLE_SORT: return DOUBLE_TYPE;
            default: return def.get();
        }
    }

    public static Type box(Type type) {
        switch (type.getSort()) {
            case VOID_SORT: return VOID_BOX;
            case BOOLEAN_SORT: return BOOLEAN_BOX;
            case CHAR_SORT: return CHAR_BOX;
            case BYTE_SORT: return BYTE_BOX;
            case SHORT_SORT: return SHORT_BOX;
            case INT_SORT: return INT_BOX;
            case FLOAT_SORT: return FLOAT_BOX;
            case LONG_SORT: return LONG_BOX;
            case DOUBLE_SORT: return DOUBLE_BOX;
            default: return type;
        }
    }

    public static Type array(Type type) {
        return Type.getType('[' + type.getDescriptor());
    }

    public static Type array(Type type, int depth) {
        if (depth <= 0) throw new IllegalArgumentException("Invalid array depth: " + depth);
        StringBuilder desc;
        (desc = new StringBuilder()).append('[');
        for (int i = 1; i < depth; ++i) desc.append('[');
        return Type.getType(desc.append(type.getDescriptor()).toString());
    }

    public static int sort(Type type) {
        return PRIMITIVE_SORT.getOrDefault(type, type.getSort());
    }

    private static int pair(int a, int b) {
        return (a += b) * (a + 1) / 2 + b;
    }

    public static Class<?> load(Type type) throws ClassNotFoundException {
        return load(null, type);
    }

    public static Class<?> load(ClassLoader loader, Type type) throws ClassNotFoundException {
        switch (type.getSort()) {
            case VOID_SORT: return void.class;
            case BOOLEAN_SORT: return boolean.class;
            case CHAR_SORT: return char.class;
            case BYTE_SORT: return byte.class;
            case SHORT_SORT: return short.class;
            case INT_SORT: return int.class;
            case FLOAT_SORT: return float.class;
            case LONG_SORT: return long.class;
            case DOUBLE_SORT: return double.class;
            case ARRAY_SORT:
            case OBJECT_SORT: return load(loader, type.getInternalName().replace('/', '.'));
            default: throw new AssertionError();
        }
    }

    private static Class<?> load(ClassLoader loader, String name) throws ClassNotFoundException {
        return (loader == null)? Class.forName(name) : Class.forName(name, true, loader);
    }

    public static int size(Type type) {
        return type.getSize();
    }

    public static int size(KnownType type) {
        return type.type.getSize();
    }

    public static int size(Type... types) {
        return size(types, 0, types.length);
    }

    public static int size(KnownType... types) {
        return size(types, 0, types.length);
    }

    public static int size(Type[] types, int index, int length) {
        int value = 0;
        while (index < length) {
            value += types[index++].getSize();
        }
        return value;
    }

    public static int size(KnownType[] types, int index, int length) {
        int value = 0;
        while (index < length) {
            value += types[index++].type.getSize();
        }
        return value;
    }

    public static void push(MethodVisitor mv, boolean value) {
        mv.visitInsn((value)? ICONST_1 : ICONST_0);
    }

    public static void push(MethodVisitor mv, int value) {
        if (5 >= value && value >= -1) {
            mv.visitInsn(ICONST_0 + value);
        } else if (Byte.MAX_VALUE >= value && value >= Byte.MIN_VALUE) {
            mv.visitIntInsn(BIPUSH, value);
        } else if (Short.MAX_VALUE >= value && value >= Short.MIN_VALUE) {
            mv.visitIntInsn(SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public static void push(MethodVisitor mv, float value) {
        if (value == 0) {
            mv.visitInsn(FCONST_0);
        } else if (value == 1) {
            mv.visitInsn(FCONST_1);
        } else if (value == 2) {
            mv.visitInsn(FCONST_2);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public static void push(MethodVisitor mv, long value) {
        if (value == 0) {
            mv.visitInsn(LCONST_0);
        } else if (value == 1) {
            mv.visitInsn(LCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public static void push(MethodVisitor mv, double value) {
        if (value == 0) {
            mv.visitInsn(DCONST_0);
        } else if (value == 1) {
            mv.visitInsn(DCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public static void push(MethodVisitor mv, Object value) {
        if (value == null) {
            mv.visitInsn(ACONST_NULL);
            return;
        } else if (value instanceof Double) {
            push(mv, ((Double) value).doubleValue());
            return;
        } else if (value instanceof Long) {
            push(mv, ((Long) value).longValue());
            return;
        } else if (value instanceof Float) {
            push(mv, ((Float) value).floatValue());
            return;
        } else if (value instanceof Number) {
            push(mv, ((Number) value).intValue());
            return;
        } else if (value instanceof Character) {
            push(mv, ((Character) value).charValue());
            return;
        } else if (value instanceof Boolean) {
            push(mv, ((Boolean) value).booleanValue());
            return;
        } else if (value instanceof KnownType) {
            value = ((KnownType) value).type;
          //break;
        } else if (value instanceof Class) {
            value = Type.getType((Class<?>) value);
          //break;
        }
        if (value instanceof Type) {
            switch (((Type) value).getSort()) {
                case VOID_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
                    return;
                case BOOLEAN_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                    return;
                case CHAR_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
                    return;
                case BYTE_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                    return;
                case SHORT_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                    return;
                case INT_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                    return;
                case FLOAT_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                    return;
                case LONG_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                    return;
                case DOUBLE_SORT:
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                    return;
            }
        }
        mv.visitLdcInsn(value);
    }

    public static void cast(MethodVisitor mv, KnownType from, KnownType to) {
        if (!from.implemented(to)) {
            cast(mv, from.type, to.type);
        }
    }

    public static void cast(MethodVisitor mv, Type from, Type to) {
        if (from.equals(to)) {
            return;
        }

        int a = from.getSort();
        int b = to.getSort();
        if (a < ARRAY_SORT) {
            if (b < ARRAY_SORT) {
                // primitive -> void
                // primitive -> primitive
                convert(mv, a, b);
                return;
            }
        } else if (b == VOID_SORT) {
            // box -> void
            // object -> void
            mv.visitInsn(POP);
            return;
        }

        Integer fromPrimitive = PRIMITIVE_SORT.get(from);
        Integer toPrimitive = PRIMITIVE_SORT.get(to);
        if (a == OBJECT_SORT && fromPrimitive != null && b == OBJECT_SORT && toPrimitive != null) {
            // box -> box
            unbox(mv, from, a = (toPrimitive == VOID_SORT)? VOID_SORT : fromPrimitive);
        }

        if (a < ARRAY_SORT) {
            // primitive -> box
            // primitive -> object
            convert(mv, a, (toPrimitive != null)? a = toPrimitive : a);
            switch (a) {
                case VOID_SORT:
                    mv.visitInsn(ACONST_NULL);
                    return;
                case BOOLEAN_SORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                    return;
                case CHAR_SORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                    return;
                case BYTE_SORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                    return;
                case SHORT_SORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                    return;
                case INT_SORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                    return;
                case FLOAT_SORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                    return;
                case LONG_SORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                    return;
                case DOUBLE_SORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                    return;
                default:
                    throw new AssertionError();
            }
        }

        if (b < ARRAY_SORT) {
            // box -> primitive
            if (fromPrimitive != null) {
                unbox(mv, from, a = fromPrimitive);
                convert(mv, a, b);
                return;
            }

            // object -> primitive
            if (b == BOOLEAN_SORT) {
                to = BOOLEAN_BOX;
            } else if (b == CHAR_SORT) {
                to = CHAR_BOX;
            } else {
                to = NUMBER_BOX;
            }
            if (!from.equals(to)) {
                mv.visitTypeInsn(CHECKCAST, to.getInternalName());
            }
            unbox(mv, to, b);
            return;
        }

        // box -> object
        // object -> box
        // object -> object
        mv.visitTypeInsn(CHECKCAST, to.getInternalName());
    }

    private static void unbox(MethodVisitor mv, Type from, int b) {
        switch (b) {
            case VOID_SORT:
                mv.visitInsn(POP);
                return;
            case BOOLEAN_SORT:
                mv.visitMethodInsn(INVOKEVIRTUAL, from.getInternalName(), "booleanValue", "()Z", false);
                return;
            case CHAR_SORT:
                mv.visitMethodInsn(INVOKEVIRTUAL, from.getInternalName(), "charValue", "()C", false);
                return;
            case BYTE_SORT:
                mv.visitMethodInsn(INVOKEVIRTUAL, from.getInternalName(), "byteValue", "()B", false);
                return;
            case SHORT_SORT:
                mv.visitMethodInsn(INVOKEVIRTUAL, from.getInternalName(), "shortValue", "()S", false);
                return;
            case INT_SORT:
                mv.visitMethodInsn(INVOKEVIRTUAL, from.getInternalName(), "intValue", "()I", false);
                return;
            case FLOAT_SORT:
                mv.visitMethodInsn(INVOKEVIRTUAL, from.getInternalName(), "floatValue", "()F", false);
                return;
            case LONG_SORT:
                mv.visitMethodInsn(INVOKEVIRTUAL, from.getInternalName(), "longValue", "()J", false);
                return;
            case DOUBLE_SORT:
                mv.visitMethodInsn(INVOKEVIRTUAL, from.getInternalName(), "doubleValue", "()D", false);
                return;
            default:
                throw new AssertionError();
        }
    }

    private static void convert(MethodVisitor mv, int a, int b) {
        if (b == a) return;
        if (a == VOID_SORT) {
            switch (b) {
                case FLOAT_SORT:
                    mv.visitInsn(FCONST_0);
                    return;
                case LONG_SORT:
                    mv.visitInsn(LCONST_0);
                    return;
                case DOUBLE_SORT:
                    mv.visitInsn(DCONST_0);
                    return;
                default:
                    mv.visitInsn(ICONST_0);
                    return;
            }
        }
        if (b <= INT_SORT && a < b) {
            if (a == CHAR_SORT && b == BYTE_SORT) {
                mv.visitInsn(I2B); // char is not smaller than byte
            }
            return;
        }
        if (a <= INT_SORT) {
            switch (b) {
                case VOID_SORT:
                    mv.visitInsn(POP);
                    return;
                case BOOLEAN_SORT: // no conversion required

                    return;
                case CHAR_SORT: // byte is smaller than char
                    if (a != BYTE_SORT) mv.visitInsn(I2C);
                    return;
                case BYTE_SORT:
                    mv.visitInsn(I2B);
                    return;
                case SHORT_SORT:
                    mv.visitInsn(I2S);
                    return;
                case FLOAT_SORT:
                    mv.visitInsn(I2F);
                    return;
                case LONG_SORT:
                    mv.visitInsn(I2L);
                    return;
                case DOUBLE_SORT:
                    mv.visitInsn(I2D);
                    return;
            }
        } else if (a == FLOAT_SORT) {
            switch (b) {
                case VOID_SORT:
                    mv.visitInsn(POP);
                    return;
                case LONG_SORT:
                    mv.visitInsn(F2L);
                    return;
                case DOUBLE_SORT:
                    mv.visitInsn(F2D);
                    return;
                default:
                    mv.visitInsn(F2I);
                    convert(mv, INT_SORT, b);
                    return;
            }
        } else if (a == LONG_SORT) {
            switch (b) {
                case VOID_SORT:
                    mv.visitInsn(POP2);
                    return;
                case FLOAT_SORT:
                    mv.visitInsn(L2F);
                    return;
                case DOUBLE_SORT:
                    mv.visitInsn(L2D);
                    return;
                default:
                    mv.visitInsn(L2I);
                    convert(mv, INT_SORT, b);
                    return;
            }
        } else if (a == DOUBLE_SORT) {
            switch (b) {
                case VOID_SORT:
                    mv.visitInsn(POP2);
                    return;
                case FLOAT_SORT:
                    mv.visitInsn(D2F);
                    return;
                case LONG_SORT:
                    mv.visitInsn(D2L);
                    return;
                default:
                    mv.visitInsn(D2I);
                    convert(mv, INT_SORT, b);
                    return;
            }
        }
        throw new AssertionError();
    }
}
