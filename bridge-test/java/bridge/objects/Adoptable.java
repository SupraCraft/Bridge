package bridge.objects;

import bridge.Invocation;

interface Adoptable extends SuperInterface {
    Void v_method();

    @Override
    default float superInterface(double value) {
        return new Invocation(SuperInterface.class, this).ofMethod("superInterface").with(value).invoke();
    }

    default int privateInterface(long value) {
        return new Invocation(this).ofMethod("secret").with(value).invoke();
    }

    private int secret(long value) {
        return (int) value;
    }
}
