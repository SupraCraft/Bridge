package bridge.objects;

import java.lang.reflect.Member;

public class InvocationTests {

    public void test() throws Throwable {
        Invocations.JUMP();
        Invocations.CAST();
        Invocations.THRW();

        Invocations.SL0A();
        Invocations.SL0B();

        Invocations.SI0A();
        Invocations.SI0B();
        Invocations.SI0C();
        Invocations.SI0D();
        Invocations.SI0E();
        Invocations.SI0F();
        Invocations.SI0G();

        Invocations.SF0A();
        Invocations.SF0B();
        Invocations.SF0C();
        Invocations.SF0D();
        Invocations.SF0E();
        Invocations.SF0F();
        Invocations.SF0G();
        Invocations.SF0H();
        Invocations.SF0I();
        Invocations.SF1A();
        Invocations.SF1B();
        Invocations.SF1C();
        Invocations.SF1D();
        Invocations.SF1E();
        Invocations.SF1F();
        Invocations.SF1G();
        Invocations.SF1H();
        Invocations.SF1I();
        Invocations.SF2A();
        Invocations.SF2B();
        Invocations.SF2C();
        Invocations.SF2D();
        Invocations.SF2E();
        Invocations.SF2F();
        Invocations.SF2G();
        Invocations.SF2H();
        Invocations.SF2I();

        Invocations.SC0A();
        Invocations.SC0B();
        Invocations.SC0C();
        Invocations.SC0D();
        Invocations.SC0E();
        Invocations.SC0F();
        Invocations.SC1A();
        Invocations.SC1B();
        Invocations.SC1C();
        Invocations.SC1D();
        Invocations.SC1E();
        Invocations.SC1F();
        Invocations.SC1G();
        Invocations.SC1H();
        Invocations.SC1I();
        Invocations.SC2A();
        Invocations.SC2B();
        Invocations.SC2C();
        Invocations.SC2D();
        Invocations.SC2E();
        Invocations.SC2F();
        Invocations.SC2G();
        Invocations.SC2H();
        Invocations.SC2I();
        Invocations.SC2J();

        Invocations.VL0A();
        Invocations.VL0B();

        Invocations.VI0A();
        Invocations.VI0B();
        Invocations.VI0C();
        Invocations.VI0D();
        Invocations.VI0E();
        Invocations.VI0F();
        Invocations.VI0G();

        Invocations.VF0A();
        Invocations.VF0B();
        Invocations.VF0C();
        Invocations.VF0D();
        Invocations.VF0E();
        Invocations.VF0F();
        Invocations.VF0G();
        Invocations.VF0H();
        Invocations.VF0I();
        Invocations.VF1A();
        Invocations.VF1B();
        Invocations.VF1C();
        Invocations.VF1D();
        Invocations.VF1E();
        Invocations.VF1F();
        Invocations.VF1G();
        Invocations.VF1H();
        Invocations.VF1I();
        Invocations.VF2A();
        Invocations.VF2B();
        Invocations.VF2C();
        Invocations.VF2D();
        Invocations.VF2E();
        Invocations.VF2F();
        Invocations.VF2G();
        Invocations.VF2H();
        Invocations.VF2I();
    }

    public void testMethods() {
        Dummy dummy = new Dummy(null);
        expect(1F, dummy.superInterface(1D), "interface-super method invocation");
        expect(2F, dummy.superClass(2D), "class-super method invocation");
        expect(3, dummy.privateInterface(3L), "private interface method invocation");
        expect(4, dummy.privateClass(4L), "private class method invocation");
        expect(5, Dummy.privateNestInterface(5L), "private nest-interface method invocation");
        expect(6, Dummy.privateNestClass(6L), "private nest-class method invocation");
        expect(7, Dummy.privateStaticNestInterface(7L), "private static nest-interface method invocation");
        expect(8, Dummy.privateStaticNestClass(8L), "private static nest-class method invocation");
    }

    public void testSynthetic() throws Throwable {
        assert Jester.class.isSynthetic();
        synthetic(Jester.class.getDeclaredFields());
        synthetic(Jester.class.getDeclaredConstructors());
        synthetic(Jester.class.getDeclaredMethods());
    }

    private static void expect(float expected, float actual, String label) {
        if (Float.compare(expected, actual) != 0) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void expect(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void synthetic(Member[] members) throws Throwable {
        for (int i = 0, length = members.length; i != length; ++i) {
            if (!members[i].isSynthetic()) throw new AssertionError(members[i].toString());
        }
    }
}
