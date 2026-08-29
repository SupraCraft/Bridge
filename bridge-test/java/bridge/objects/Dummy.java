package bridge.objects;

import bridge.Adopt;
import bridge.Bridge;
import bridge.Invocation;
import bridge.Synthetic;

@Adopt(parent = Super.class, interfaces = Adoptable.class)
class Dummy extends Jester {
    static Super  s_obj;
    static short  sp_x1;
    static double sp_x2;

    Super  v_obj;
    short  vp_x1;
    double vp_x2;

    Dummy(String value) {
        super(value);
    }

    @Synthetic
    @Bridge(params = {})
    @Bridge(params = {boolean.class})
    @Bridge(params = {boolean.class, long.class})
    Dummy(final boolean THROW, final long VALUE, final CharSequence SEQ) throws NonException {
        super(SEQ);
        if (THROW) throw new NonException();
    }

    @Synthetic
    @Bridge(params = {})
    @Bridge(params = {}, returns = Void.class)
    @Bridge(params = {boolean.class})
    @Bridge(params = {boolean.class}, returns = Void.class)
    @Bridge(params = {boolean.class, long.class}, returns = Void.class)
    @Bridge(params = {boolean.class, long.class, CharSequence.class})
    @Bridge(params = {boolean.class, long.class, CharSequence.class}, returns = Void.class)
    @Bridge(params = {long.class}, toIndex = 1, returns = Void.class)
    static long s_method(final boolean THROW, final long VALUE) throws NonException {
        if (THROW) throw new NonException();
        return VALUE;
    }

    @Synthetic
    @Bridge(params = {})
    @Bridge(params = {}, returns = Void.class)
    @Bridge(params = {boolean.class})
    @Bridge(params = {boolean.class}, returns = Void.class)
    @Bridge(params = {boolean.class, long.class}, returns = Void.class)
    @Bridge(params = {boolean.class, long.class, CharSequence.class})
    @Bridge(params = {boolean.class, long.class, CharSequence.class}, returns = Void.class)
    @Bridge(params = {long.class}, toIndex = 1, returns = Void.class)
    long v_method(final boolean THROW, final long VALUE) throws NonException {
        if (THROW) throw new NonException();
        return VALUE;
    }

    @Override
    public float superInterface(double value) {
        return new Invocation(Adoptable.class, this).ofMethod("superInterface").with(value).invoke();
    }

    @Override
    public float superClass(double value) {
        return new Invocation(Super.class, this).ofMethod("superClass").with(value).invoke();
    }

    public static int privateNestInterface(long value) {
        return new Invocation(Interface.class, new Interface() {}).ofMethod("secret").with(value).invoke();
    }

    public static int privateNestClass(long value) {
        return new Invocation(Class.class, new Class()).ofMethod("secret").with(value).invoke();
    }

    public static int privateStaticNestInterface(long value) {
        return new Invocation(Interface.class).ofMethod("secrets").with(value).invoke();
    }

    public static int privateStaticNestClass(long value) {
        return new Invocation(Class.class).ofMethod("secrets").with(value).invoke();
    }

    private interface Interface {
        private int secret(long value) {
            return new Invocation(Interface.class).ofMethod("secrets").with(value).invoke();
        }

        private static int secrets(long value) {
            return (int) value;
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private static class Class {
        private int secret(long value) {
            return new Invocation(Class.class).ofMethod("secrets").with(value).invoke();
        }

        private static int secrets(long value) {
            return (int) value;
        }
    }
}
