package modern25;

class FlexibleBase {
    final int base;

    FlexibleBase(int base) {
        this.base = base;
    }
}

public final class FlexibleConstructorFixture extends FlexibleBase {
    private final int doubled;

    public FlexibleConstructorFixture(int value) {
        int normalized = Math.abs(value);
        super(normalized);
        doubled = normalized * 2;
    }

    public static void main(String[] args) {
        FlexibleConstructorFixture fixture = new FlexibleConstructorFixture(-21);
        if (fixture.base != 21 || fixture.doubled != 42) {
            throw new AssertionError("flexible constructor semantics changed");
        }
        System.out.println("flexible-constructor-ok");
    }
}
