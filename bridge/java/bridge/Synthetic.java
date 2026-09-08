package bridge;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * An annotation for accessing the hidden {@code synthetic} access modifier
 *
 * @implNote Usages of this annotation are counted as {@code adjustments}.
 */
@Documented
@Target({TYPE, CONSTRUCTOR, METHOD, FIELD})
public @interface Synthetic {

}
