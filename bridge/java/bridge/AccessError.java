package bridge;

/**
 * @hidden error thrown when bridge api is accessed at runtime
 */
@Synthetic
final class AccessError extends IllegalAccessError {
    private static final boolean $;

    private AccessError() {
        super("Accessed bridge api at runtime");
    }

    static {
        $ = true;
    }

    static <T> T $(Object... arguments) {
        if ($) throw new AccessError();
        return error();
    }

    private static native <T> T error();
}
