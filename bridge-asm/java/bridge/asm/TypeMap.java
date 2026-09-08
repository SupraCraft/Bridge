package bridge.asm;

import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public final class TypeMap implements Cloneable {
    public static final ClassLoader PLATFORM;
    public static final byte STORE_ARRAYS = 0b1;
    final ClassLoader loader;
    final HashMap<Type, KnownType> map = new HashMap<>();
    private final Collection<KnownType> values = Collections.unmodifiableCollection(map.values());
    private final boolean store_arrays;

    static {
        ClassLoader loader;
        try {
            loader = (ClassLoader) MethodHandles.lookup().findStatic(ClassLoader.class, "getPlatformClassLoader", MethodType.methodType(ClassLoader.class)).invokeExact();
        } catch (Throwable e) {
            loader = ClassLoader.getSystemClassLoader();
        }
        PLATFORM = loader;
    }

    public TypeMap() {
        this(PLATFORM, 0);
    }

    public TypeMap(int flags) {
        this(PLATFORM, flags);
    }

    public TypeMap(ClassLoader loader) {
        this(loader, 0);
    }

    public TypeMap(ClassLoader loader, int flags) {
        this.loader = loader;
        store_arrays = (flags & STORE_ARRAYS) != 0;
    }

    public KnownType add(KnownType type) {
        if (type.isArray() && !store_arrays) return type;
        KnownType value = map.putIfAbsent(type.type, type);
        return (value != null)? value : type;
    }

    public KnownType[] add(KnownType[] types) {
        final int length;
        if (types == null || (length = types.length) == 0) {
            return KnownType.EMPTY;
        }
        final KnownType[] value = new KnownType[length];
        for (int i = 0; i != length;) {
            value[i] = add(types[i++]);
        }
        return value;
    }

    public KnownType get(Class<?> loaded) {
        Type type = Type.getType(loaded);
        if (loaded.isArray()) {
            if (store_arrays) {
                KnownType value = map.get(type);
                if (value != null) return value;
                return new ArrayType(map, this, type, loaded);
            } else {
                return new ArrayType(new HashMap<>(), this, type, loaded);
            }
        }
        KnownType value = map.get(type);
        if (value != null) return value;
        return new KnownType(this, type, loaded);
    }

    public KnownType[] get(Class<?>[] loaded) {
        final int length;
        if (loaded == null || (length = loaded.length) == 0) {
            return KnownType.EMPTY;
        }
        final KnownType[] value = new KnownType[length];
        for (int i = 0; i != length;) {
            value[i] = get(loaded[i++]);
        }
        return value;
    }

    public KnownType load(Type type) {
        if (type.getSort() == Type.ARRAY) {
            if (store_arrays) {
                KnownType value = map.get(type);
                if (value != null) return value;
                return new ArrayType(map, this, type, null);
            } else {
                return new ArrayType(new HashMap<>(), this, type, null);
            }
        }
        KnownType value = map.get(type);
        if (value != null) return value;
        try {
            return new KnownType(this, type, Types.load(loader, type));
        } catch (ClassNotFoundException e) {
            map.put(type, value = new KnownType(type));
            if (type.getSort() == Type.OBJECT) {
                value.extended = get(Object.class);
            }
            value.implemented = KnownType.EMPTY;
            return value;
        }
    }

    public KnownType[] load(Type[] types) {
        final int length;
        if (types == null || (length = types.length) == 0) {
            return KnownType.EMPTY;
        }
        final KnownType[] value = new KnownType[length];
        for (int i = 0; i != length;) {
            value[i] = load(types[i++]);
        }
        return value;
    }

    public KnownType load(String type) {
        return load(Type.getType(type));
    }

    public KnownType[] load(String[] types) {
        final int length;
        if (types == null || (length = types.length) == 0) {
            return KnownType.EMPTY;
        }
        final KnownType[] value = new KnownType[length];
        for (int i = 0; i != length;) {
            value[i] = load(Type.getType(types[i++]));
        }
        return value;
    }

    public KnownType loadClass(String name) {
        return load(Type.getObjectType(name));
    }

    public KnownType[] loadClass(String[] names) {
        final int length;
        if (names == null || (length = names.length) == 0) {
            return KnownType.EMPTY;
        }
        final KnownType[] value = new KnownType[length];
        for (int i = 0; i != length;) {
            value[i] = load(Type.getObjectType(names[i++]));
        }
        return value;
    }

    public boolean contains(KnownType type) {
        if (type != null) return map.containsKey(type.type);
        return map.containsKey(null);
    }

    public boolean contains(Class<?> loaded) {
        if (loaded != null) return map.containsKey(Type.getType(loaded));
        return map.containsKey(null);
    }

    public boolean contains(Type type) {
        return map.containsKey(type);
    }

    public boolean contains(String type) {
        if (type != null) return map.containsKey(Type.getType(type));
        return map.containsKey(null);
    }

    public boolean containsClass(String name) {
        if (name != null) return map.containsKey(Type.getObjectType(name));
        return map.containsKey(null);
    }

    public Collection<KnownType> values() {
        return values;
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public TypeMap clone() {
     // int flags = 0;
     // if (store_arrays) flags += STORE_ARRAYS;

        TypeMap clone = new TypeMap(loader, (store_arrays)? STORE_ARRAYS : 0);
        clone.map.putAll(map);
        return clone;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.keySet().toString();
    }
}
