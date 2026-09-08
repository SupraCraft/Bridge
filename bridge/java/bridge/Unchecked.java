package bridge;

import static bridge.AccessError.$;

/**
 * A class for interacting with unchecked exceptions
 *
 * @implNote Unchecked must be constructed and thrown in a single statement. They may not be stored.
 * Usages of these constructors and methods are counted as {@code removals}.
 */
public final class Unchecked extends RuntimeException {

    /**
     * Allows any type of exception to become unchecked
     *
     * @param exception Exception
     */
    public Unchecked(Throwable exception) { $(exception); }

    /**
     * Brings an (unchecked) exception into scope
     *
     * @param <X> Exception type
     */
    public static <X extends Throwable> void check() throws X { $(); }

    /**
     * Performs an unchecked cast
     *
     * @param <T> Requested type
     * @param instance Object instance
     * @return The instance converted to the requested type.
     * @throws ClassCastException When the instance is not convertible to the requested type.
     */
    public static <T> T cast(Object instance) { return $(instance); }
}
