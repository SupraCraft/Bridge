package bridge;

import static bridge.AccessError.$;

/**
 * A class for creating labels with persistent identifiers
 *
 * @implNote Labels must be constructed and disposed of in a single statement. They may not be stored.
 * Usages of these constructors are counted as {@code removals}.
 *
 * @see Jump Teleport to a persistent label in the same method with {@code Jump}
 */
public final class Label {

    /**
     * Creates a persistent label at this location
     *
     * @param id Label ID constant &ndash; no dynamic values or mathematics is permitted here.
     */
    public Label(int id) { $(); }

    /**
     * Creates a persistent label at this location
     *
     * @param name Label name constant &ndash; no dynamic values or string manipulation is permitted here.
     */
    public Label(String name) { $(name); }
}
