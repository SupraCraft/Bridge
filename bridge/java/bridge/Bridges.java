package bridge;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @hidden annotation for using multiple @Bridge annotations
 */
@Synthetic
@Target({CONSTRUCTOR, METHOD, FIELD})
public @interface Bridges {
    Bridge[] value();
}
