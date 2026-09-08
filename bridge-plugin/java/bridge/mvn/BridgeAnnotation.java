package bridge.mvn;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM9;

final class BridgeAnnotation extends AnnotationVisitor {
    private static final int MAX_ARITY = 255;
    private final int ACCESS;
    private final String NAME, DESC;
    private final Type RETURNS;
    private final String[] EX;
    private final Consumer<Data> ACTION;

    private Map<AnnotationNode, Boolean> annotations;
    private int access, fromIndex, toIndex, length = MAX_ARITY;
    private String name, desc, sign;
    private Type returns;
    private String[] ex;

    BridgeAnnotation(int access, String name, String descriptor, String[] exceptions, Type returns, Consumer<Data> action) {
        super(ASM9);
        this.ACCESS = this.access = access;
        this.NAME = this.name = name;
        this.DESC = this.desc = descriptor;
        this.EX = this.ex = exceptions;
        this.RETURNS = this.returns = returns;
        this.ACTION = action;
    }

    @Override
    public void visit(String name, Object value) {
        if ("access".equals(name)) {
            int access = (int) value;
            this.access = (access < 0)? this.access & access : access;
        } else if ("name".equals(name)) {
            this.name = value.toString();
        } else if ("fromIndex".equals(name)) {
            if ((fromIndex = Math.min((int) value, MAX_ARITY)) < 0) fromIndex = 0;
        } else if ("toIndex".equals(name)) {
            if ((toIndex = Math.min((int) value, MAX_ARITY)) < 0) toIndex = 0;
        } else if ("length".equals(name)) {
            if ((length = Math.min((int) value, MAX_ARITY)) < 0) length = 0;
        } else if ("signature".equals(name)) {
            sign = value.toString();
        } else if ("returns".equals(name)) {
            String desc = this.desc, returns = value.toString();
            this.returns = Type.getType(returns);
            this.desc = new StringBuilder().append(desc).replace(desc.indexOf(')') + 1, desc.length(), returns).toString();
        }
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        if ("params".equals(name)) {
            return new AnnotationVisitor(ASM9) {
                private final StringBuilder desc = new StringBuilder().append('(');

                @Override
                public void visit(String name, Object descriptor) {
                    desc.append(descriptor);
                }

                @Override
                public void visitEnd() {
                    BridgeAnnotation.this.desc = desc.append(')').append(returns.getDescriptor()).toString();
                }
            };
        } else if ("exceptions".equals(name)) {
            return new AnnotationVisitor(ASM9) {
                private final ArrayList<String> list = new ArrayList<>();

                @Override
                public void visit(String name, Object descriptor) {
                    list.add(((Type) descriptor).getInternalName());
                }

                @Override
                public void visitEnd() {
                    ex = list.toArray(new String[0]);
                }
            };
        } else {
            return new AnnotationVisitor(ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                    return new BridgeAnnotation(ACCESS, NAME, DESC, EX, RETURNS, ACTION);
                }
            };
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        final AnnotationNode annotation;
        if (annotations == null) annotations = new LinkedHashMap<>();
        annotations.put(annotation = new AnnotationNode(desc), Boolean.TRUE);
        return annotation;
    }

    @Override
    public void visitEnd() {
        if (!NAME.equals(name) || !DESC.equals(desc)) {
            ACTION.accept(new Data(access, name, desc, fromIndex, toIndex, length, ex, sign, returns, annotations));
        }
    }

    static final class Data {
        int access;
        final String name, desc, sign;
        final int fromIndex, toIndex, length;
        final String[] ex;
        final Type returns;
        private final Map<AnnotationNode, Boolean> annotations;

        private Data(int access, String name, String descriptor, int fromIndex, int toIndex, int length, String[] exceptions, String signature, Type returns, Map<AnnotationNode, Boolean> annotations) {
            this.access = access;
            this.name = name;
            this.desc = descriptor;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.length = length;
            this.ex = exceptions;
            this.sign = signature;
            this.returns = returns;
            this.annotations = (annotations == null)? Collections.emptyMap() : Collections.unmodifiableMap(annotations);
        }

        void annotate(BiFunction<String, Boolean, AnnotationVisitor> code) {
            for (Map.Entry<AnnotationNode, Boolean> entry : annotations.entrySet()) {
                final AnnotationNode annotation;
                (annotation = entry.getKey()).accept(code.apply(annotation.desc, entry.getValue()));
            }
        }
    }
}
