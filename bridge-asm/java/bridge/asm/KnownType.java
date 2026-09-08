package bridge.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Locale;
import java.util.Objects;

public class KnownType {
    static final KnownType[] EMPTY = new KnownType[0];
    public final Type type;
    private Object search;
    int access;
    Object data;
    KnownType extended;
    KnownType[] implemented;

    KnownType(Type anonymous) {
        this.type = Objects.requireNonNull(anonymous);
    }

    KnownType(TypeMap types, Type type, Class<?> loaded) {
        types.map.put(this.type = Objects.requireNonNull(type), this);
        access = loaded.getModifiers();

        final Class<?> extended;
        if ((extended = loaded.getSuperclass()) != null) {
            this.extended = types.get(extended);
        } else if (loaded != Object.class && !loaded.isPrimitive()) {
            this.extended = types.get(Object.class);
        }
        this.implemented = types.get(loaded.getInterfaces());
    }

    public int hashCode() {
        return type.hashCode();
    }

    public boolean equals(Object type) {
        return type == this || (type instanceof KnownType && ((KnownType) type).type.equals(this.type));
    }

    public boolean represents(KnownType type) {
        return type == this || (type != null && type.type.equals(this.type));
    }

    public boolean represents(Class<?> loaded) {
        return loaded != null && Type.getType(loaded).equals(this.type);
    }

    public boolean represents(Type type) {
        return this.type.equals(type);
    }

    public boolean represents(String type) {
        return type != null && Type.getType(type).equals(this.type);
    }

    public boolean representsClass(String name) {
        return name != null && Type.getObjectType(name).equals(this.type);
    }

    public Object data() {
        return data;
    }

    public int modifiers() {
        return access;
    }

    public KnownType supertype() {
        return extended;
    }

    public boolean extended(KnownType type) {
        if (type != null) {
            KnownType extended = this;
            do {
                if (type == extended || type.type.equals(extended.type)) return true;
            } while ((extended = extended.extended) != null);
        }
        return false;
    }

    public KnownType[] interfaces() {
        return implemented.clone();
    }

    public boolean implemented(KnownType type) {
        return type != null && implemented(type, new Object());
    }

    private boolean implemented(KnownType type, Object search) {
        final KnownType extended;
        if (type == this || type.type.equals(this.type)) return true;
        if ((extended = this.extended) != null && extended.search != search) {
            if (extended.implemented(type, search)) return true;
            extended.search = search;
        }
        for (KnownType implemented : this.implemented) {
            if (implemented.search != search) {
                if (implemented.implemented(type, search)) return true;
                implemented.search = search;
            }
        }
        return false;
    }

    public boolean isPrimitive() {
        return type.getSort() < Type.ARRAY;
    }

    public boolean isInterface() {
        return (access & Opcodes.ACC_INTERFACE) != 0;
    }

    public boolean isArray() {
        return false;
    }

    private static final int INDENT = 4;
    private static StringBuilder newline(StringBuilder builder, int indent) {
        builder.append('\n');
        while (indent != 0) {
            builder.append(' ');
            --indent;
        }
        return builder;
    }

    private void toString(StringBuilder builder, int indent) {
        builder.append(type.getClassName()).append(" 0x").append(Integer.toHexString(super.hashCode()).toUpperCase(Locale.ROOT)).append(" {");
        indent += INDENT;

        KnownType extended = this.extended;
        if (extended != null) {
            extended.toString(newline(builder, indent).append("extends "), indent);
        }

        KnownType[] implemented = this.implemented;
        if (implemented.length != 0) {
            newline(builder, indent).append("implements {");
            implemented[0].toString(newline(builder, indent += INDENT), indent);
            for (int i = 1, length = implemented.length; i != length;) {
                implemented[i++].toString(newline(builder.append(','), indent), indent);
            }
            newline(builder, indent -= INDENT).append('}');
        }

        if (isArray()) {
            ((ArrayType) this).element.toString(newline(builder, indent).append("return "), indent);
        }

        Object data = this.data;
        if (data != null) {
            String string = data.toString();
            Class<?> type = data.getClass();
            String hash = Integer.toHexString(data.hashCode());
            newline(builder, indent).append("continue ").append(type.getTypeName()).append(" 0x").append(hash.toUpperCase(Locale.ROOT)).append(" {");
            if (string != null && !string.equals(type.getName() + '@' + hash)) {
                StringBuilder indentation = new StringBuilder();
                newline(indentation, indent + INDENT);
                newline(builder.append(indentation).append(string.replace("\n", indentation.toString())), indent);
            }
            builder.append('}');
        }

        if (builder.codePointBefore(builder.length()) != '{')
            newline(builder, indent - INDENT);
        builder.append('}');
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder, 0);
        return builder.toString();
    }
}
