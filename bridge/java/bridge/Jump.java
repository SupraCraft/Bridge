package bridge;

import static bridge.AccessError.$;

/**
 * A class for teleporting to persistent labels
 *
 * @implNote Jumps must be constructed and thrown in a single statement. They may not be stored.
 * Usages of these constructors are counted as {@code invocations}.
 *
 * @see Label Create persistent labels with {@code Label}
 */
public final class Jump extends RuntimeException {

    /**
     * Teleports to a persistent label in this method
     *
     * @param id Label ID constant &ndash; no dynamic values or mathematics is permitted here.
     */
    public Jump(int id) { $(); }

    /**
     * Teleports to a persistent label in this method
     *
     * @param name Label name constant &ndash; no dynamic values or string manipulation is permitted here.
     */
    public Jump(String name) { $(name); }
}
