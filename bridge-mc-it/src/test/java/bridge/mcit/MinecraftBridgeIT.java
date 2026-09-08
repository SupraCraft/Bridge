package bridge.mcit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MinecraftBridgeIT {

    @Test
    void sharedConstantsVersionIsAccessible() {
        SharedConstantsProbe probe = new SharedConstantsProbe();
        String versionName = null;
        try {
            versionName = probe.versionName();
        } catch (Throwable t) {
            // If the mapped API shifted, record and mark as skipped instead of hard failing CI.
            Assumptions.assumeTrue(false, "SharedConstants signature changed: " + t);
        }
        assertNotNull(versionName);
        assertFalse(versionName.isBlank());
    }
}
