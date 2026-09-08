package bridge.objects;

import bridge.Invocation;

class Super extends SuperAbstract {
    Super(CharSequence cs) {

    }

    @Override
    protected float superClass(double value) {
        return new Invocation(SuperAbstract.class, this).ofMethod("superClass").with(value).invoke();
    }

    protected int privateClass(long value) {
        return new Invocation(this).ofMethod("secret").with(value).invoke();
    }

    private int secret(long value) {
        return (int) value;
    }
}
