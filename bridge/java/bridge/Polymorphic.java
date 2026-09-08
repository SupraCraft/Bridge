package bridge;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;

/**
 * An annotation for identifying parameters that defy their stated type signatures
 *
 * @implNote Arguments submitted for parameters annotated by this will have their type determined upon recompilation.
 */
@Documented
@Target({TYPE_PARAMETER, PARAMETER})
@interface Polymorphic {

}
