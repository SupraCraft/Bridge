package fixture;

public final class CompatibilityFixture {
    private CompatibilityFixture() {}

    public static int value() {
        return 42;
    }

    public static void main(String[] args) {
        if (value() != 42) throw new AssertionError("unexpected transformed behavior");
        System.out.println("compatibility-ok");
    }
}
