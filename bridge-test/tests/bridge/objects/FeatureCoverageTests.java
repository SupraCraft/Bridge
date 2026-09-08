package bridge.objects;

public class FeatureCoverageTests {

    public void testAdoptedHierarchyIsMaterialized() {
        if (Dummy.class.getSuperclass() != Super.class) {
            throw new AssertionError("@Adopt did not replace Dummy's superclass with Super");
        }
        if (!Adoptable.class.isAssignableFrom(Dummy.class)) {
            throw new AssertionError("@Adopt did not add Adoptable to Dummy's hierarchy");
        }
        if (!SuperInterface.class.isAssignableFrom(Dummy.class)) {
            throw new AssertionError("Transitive adopted interface hierarchy was not detected");
        }
    }

    public void testBaseLanguageLevelFork() {
        int actual = Forks.selectedLanguageLevel();
        if (actual != 21) {
            throw new AssertionError("Classes-directory base fork should target Java 21, got " + actual);
        }
    }
}
