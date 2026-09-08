package bridge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * An annotation for editing class properties
 *
 * @implNote Usages of this annotation are counted as {@code adjustments}.
 */
@Target(ElementType.TYPE)
public @interface Adopt {

    /**
     * Removes existing class metadata when {@code true}
     *
     * @return Clean status
     */
    boolean clean() default false;

    /**
     * Overwrites the {@code extends} clause
     *
     * @implNote Compatible super constructor(s) must exist within the specified class for them to be usable
     * @return Parent class
     */
    Class<?> parent() default Polymorphic.class;

    /**
     * Appends to the {@code implements} clause
     *
     * @return Parent interfaces
     */
    Class<?>[] interfaces() default {};

    /**
     * Adds generic type data to the class definition
     *
     * @return Signature string
     */
    String signature() default "";
}
