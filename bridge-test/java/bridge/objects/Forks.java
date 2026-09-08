package bridge.objects;

import bridge.Invocation;

/**
 * Runtime probe for Bridge's Invocation.LANGUAGE_LEVEL multi-release forking.
 */
public final class Forks {
    private Forks() {}

    public static int selectedLanguageLevel() {
        if (Invocation.LANGUAGE_LEVEL >= 25) {
            return 25;
        }
        return 21;
    }

    public static void main(String[] args) {
        int runtime = Runtime.version().feature();
        int expected = runtime >= 25 ? 25 : 21;
        int actual = selectedLanguageLevel();
        if (actual != expected) {
            throw new AssertionError(
                    "Bridge multi-release fork mismatch: runtime=" + runtime +
                    ", expected=" + expected + ", actual=" + actual
            );
        }
        System.out.println("Bridge multi-release fork selected Java " + actual + " on runtime " + runtime);
    }
}
