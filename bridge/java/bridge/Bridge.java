package bridge;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * An annotation for creating field &amp; method bridges
 *
 * @implNote Usages of this annotation are counted as {@code bridges}.
 */
@Repeatable(Bridges.class)
@Target({CONSTRUCTOR, METHOD, FIELD})
public @interface Bridge {

    /**
     * {@code public} access modifier for field &amp; method bridging
     */
    int PUBLIC = ACC_PUBLIC;

    /**
     * {@code protected} access modifier for field &amp; method bridging
     */
    int PROTECTED = ACC_PROTECTED;

    /**
     * {@code private} access modifier for field &amp; method bridging
     */
    int PRIVATE = ACC_PRIVATE;

    /**
     * {@code static} access modifier for field &amp; method bridging
     * @implNote This flag cannot be added to fields or methods where it does not already exist
     */
    int STATIC = ACC_STATIC;

    /**
     * {@code final} access modifier for field &amp; method bridging
     */
    int FINAL = ACC_FINAL;

    /**
     * {@code synthetic} access modifier for field &amp; method bridging
     * @implNote This flag is applied by default to all bridges.
     */
    int SYNTHETIC = ACC_SYNTHETIC;

    /**
     * {@code transient} access modifier for field bridging
     * @implNote This flag is applied by default to field bridges.
     */
    int TRANSIENT = ACC_TRANSIENT;

    /**
     * {@code varargs} access modifier for method bridging
     */
    int VARARGS = ACC_VARARGS;

    /**
     * Specifies different access modifiers
     *
     * @implNote Specify an inverted (negative) value to remove existing modifiers using bit-masking
     * @return Access modifiers
     */
    int access() default 0xFFFFFFFF;

    /**
     * Specifies a different field/method name
     *
     * @return Name
     */
    String name() default "";

    /**
     * Specifies different method parameters
     *
     * @implNote The position of parameters in the bridge must be the same as those in the @annotated method
     * @return Parameters
     */
    Class<?>[] params() default Polymorphic.class;

    /**
     * Specifies when to start loading arguments from the bridge method
     *
     * @return From index
     */
    int fromIndex() default 0;

    /**
     * Specifies when to start inserting arguments into the @annotated method
     *
     * @return To index
     */
    int toIndex() default 0;

    /**
     * Specifies how many parameters from the bridge method to send to the @annotated method
     *
     * @return Parameter amount
     */
    int length() default Integer.MAX_VALUE;

    /**
     * Specifies different checked exceptions
     *
     * @return Checked exception list
     */
    Class<? extends Throwable>[] exceptions() default AccessError.class;

    /**
     * Specifies generic type data
     *
     * @return Signature string
     */
    String signature() default "";

    /**
     * Specifies a different field/return type
     *
     * @return Return type
     */
    Class<?> returns() default Polymorphic.class;

    /**
     * Specifies that this bridge is {@code @Deprecated}
     *
     * @return Deprecation status
     */
    Deprecated status() default @Deprecated;
}
