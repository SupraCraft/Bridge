package bridge.mcit;

import bridge.Invocation;

/**
 * Minimal Invocation-based probe into Minecraft's SharedConstants class.
 * This is compiled against the Mojang-mapped server jar supplied via -Dminecraft.serverJar.
 */
public final class SharedConstantsProbe {

    public String versionName() {
        Object version = new Invocation("net.minecraft.SharedConstants")
                .ofMethod("getGameVersion")
                .invoke();
        return new Invocation(version)
                .ofMethod("getName")
                .invoke();
    }
}
