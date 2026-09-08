package bridge.asm;

import org.objectweb.asm.Type;

import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getType;

public final class ArrayType extends KnownType {
    public final KnownType root, element;
    public final int depth;

    ArrayType(Map<Type, KnownType> arrays, TypeMap types, Type type, Class<?> loaded) {
        super(type);
        int depth = 0;
        final String desc = type.getDescriptor();
        final KnownType root;
        if (loaded != null) {
            while (loaded.isArray()) {
                loaded = loaded.getComponentType();
                ++depth;
            }
            this.root = root = types.get(loaded);
        } else {
            while (desc.charAt(depth) == '[') {
                ++depth;
            }
            this.root = root = types.load(getType(desc.substring(depth)));
        }
        if ((this.depth = depth) == 0) {
            throw new IllegalArgumentException(desc);
        } else if (depth == 1) {
            access = ((element = root).access & (ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE)) | (ACC_ABSTRACT | ACC_FINAL);
        } else {
            access = (element = get(arrays, types, getType(desc.substring(1)))).access;
        }
        arrays.put(type, this);

        final String depthd_m1 = desc.substring(1, depth);
        final String depthd = desc.substring(0, depth);
        final KnownType extended;
        if ((extended = root.extended) != null) {
            this.extended = get(arrays, types, getType(depthd + extended.type.getDescriptor()));
        } else {
            this.extended = get(arrays, types, getType(depthd_m1 + "Ljava/lang/Object;"));
        }

        int i = 0, length;
        final KnownType[] interfaces;
        final KnownType[] implemented = this.implemented = new KnownType[(length = (interfaces = root.implemented).length) + 2];
        while (i != length) {
            implemented[i] = get(arrays, types, getType(depthd + interfaces[i++].type.getDescriptor()));
        }
        implemented[i++] = get(arrays, types, getType(depthd_m1 + "Ljava/lang/Cloneable;"));
        implemented[i]   = get(arrays, types, getType(depthd_m1 + "Ljava/io/Serializable;"));
    }

    private static KnownType get(Map<Type, KnownType> arrays, TypeMap types, Type type) {
        if (type.getSort() != Type.ARRAY) return types.load(type);
        KnownType value = arrays.get(type);
        if (value != null) return value;
        return new ArrayType(arrays, types, type, null);
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }
}
