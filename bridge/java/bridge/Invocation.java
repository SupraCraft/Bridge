package bridge;

import static bridge.AccessError.$;

/**
 * A class that can be used to access field &amp; method bridges (and other things too)
 *
 * @implNote Invocations must be constructed and completed in a single statement. Partial invocations may not be stored.
 * Usages of these constructors are counted as {@code invocations}. Usages of {@link #LANGUAGE_LEVEL} may generate {@code forks}.
 */
@SuppressWarnings("InnerClassMayBeStatic")
public final class Invocation {

    /**
     * Creates fork(s) of your class with code tailored to the specified Java language level(s)
     *
     * @implNote This constant may only appear in integer comparisons as the first operand. Using it anywhere else will not compile correctly.
     */
    public static final int LANGUAGE_LEVEL = $();

    /**
     * Starts a direct reference to something static
     *
     * @param type Type constant &ndash; no dynamic values are permitted here.
     */
    public Invocation(Class<?> type) { $(type); }

    /**
     * Starts a direct reference to something static
     *
     * @param type Type name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @throws NoClassDefFoundError When no type with the provided name can be resolved.
     */
    public Invocation(String type) { $(type); }

    /**
     * Starts a direct reference to something that belongs to an instance of an object
     *
     * @implNote Calling this overload will cause the object type to be determined automatically.
     * @param instance Object instance
     */
    public Invocation(@Polymorphic Object instance) { $(instance); }

    /**
     * Starts a direct reference to something that belongs to an instance of an object
     *
     * @param type Type constant &ndash; no dynamic values are permitted here.
     * @param instance Object instance
     * @throws ClassCastException When the instance is not convertible to the provided type.
     */
    public Invocation(Class<?> type, Object instance) { $(type, instance); }

    /**
     * Starts a direct reference to something that belongs to an instance of an object
     *
     * @param type Type name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @param instance Object instance
     * @throws NoClassDefFoundError When no type with the provided name can be resolved.
     * @throws ClassCastException When the instance is not convertible to the provided type.
     */
    public Invocation(String type, Object instance) { $(type, instance); }

    /**
     * Creates a direct reference to a class
     *
     * @return The real class reference.
     * @throws NullPointerException When resolving the class of an instance and the instance is {@code null}.
     */
    public Class<?> ofClassLiteral() { return $(); }

    /**
     * Creates a direct type-compatibility check
     *
     * @param type Type constant &ndash; no dynamic values are permitted here.
     * @return The real boolean response.
     */
    public boolean ofInstanceOf(Class<?> type) { return $(type); }

    /**
     * Creates a direct type-compatibility check
     *
     * @param type Type name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @return The real boolean response.
     * @throws NoClassDefFoundError When no type with the provided name can be resolved.
     */
    public boolean ofInstanceOf(String type) { return $(type); }

    /**
     * Creates a direct reference to a constructor
     *
     * @implNote This operation requires static context.
     * @return An imaginary reference for you to execute your request with.
     */
    public Executor ofConstructor() { return $(); }

    /**
     * Creates a direct reference to a method
     *
     * @implNote Calling this overload will cause the method return type to be determined automatically.
     * @param name Method name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @return An imaginary reference for you to execute your request with.
     */
    public Executor ofMethod(String name) { return $(name); }

    /**
     * Creates a direct reference to a method
     *
     * @param type Method return type constant &ndash; no dynamic values are permitted here.
     * @param name Method name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @return An imaginary reference for you to execute your request with.
     */
    public Executor ofMethod(Class<?> type, String name) { return $(type, name); }

    /**
     * Creates a direct reference to a method
     *
     * @param type Method return type name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @param name Method name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @return An imaginary reference for you to execute your request with.
     * @throws NoClassDefFoundError When no type with the provided name can be resolved.
     */
    public Executor ofMethod(String type, String name) { return $(type, name); }

    /**
     * A class that is used to build method invocations
     *
     * @implNote Invocations must be constructed and completed in a single statement. Partial invocations may not be stored.
     */
    public final class Executor {
        private Executor() {}

        /**
         * Adds an argument to the stack
         *
         * @implNote Calling this overload will cause the parameter type to be determined automatically.
         * @param argument Argument value
         * @return An imaginary reference for you to continue executing your request with.
         */
        public Executor with(@Polymorphic Object argument) { return $(argument); }
        /**@hidden */public Executor with(double argument) { return $(); }
        /**@hidden */public Executor with(long argument) { return $(); }
        /**@hidden */public Executor with(float argument) { return $(); }
        /**@hidden */public Executor with(int argument) { return $(); }
        /**@hidden */public Executor with(short argument) { return $(); }
        /**@hidden */public Executor with(byte argument) { return $(); }
        /**@hidden */public Executor with(char argument) { return $(); }
        /**@hidden */public Executor with(boolean argument) { return $(); }

        /**
         * Adds an argument to the stack
         *
         * @param parameter Parameter type constant &ndash; no dynamic values are permitted here.
         * @param argument Argument value
         * @return An imaginary reference for you to continue executing your request with.
         * @throws ClassCastException When the argument is not convertible to the provided parameter type.
         */
        public Executor with(Class<?> parameter, Object argument) { return $(parameter, argument); }

        /**
         * Adds an argument to the stack
         *
         * @param parameter Parameter type name constant &ndash; no dynamic values or string manipulation is permitted here.
         * @param argument Argument value
         * @return An imaginary reference for you to continue executing your request with.
         * @throws NoClassDefFoundError When no type with the provided name can be resolved.
         * @throws ClassCastException When the argument is not convertible to the provided parameter type.
         */
        public Executor with(String parameter, Object argument) { return $(parameter, argument); }

        /**
         * Brings an exception into scope
         *
         * @param <X> Exception type
         * @return An imaginary reference for you to continue executing your request with.
         * @throws X When the invoked method throws {@code X}.
         */
        public <X extends Throwable> Executor check() throws X { return $(); }

        /**
         * Brings an exception into scope
         *
         * @param <X> Exception type
         * @param exception Exception type constant &ndash; no dynamic values are permitted here.
         * @return An imaginary reference for you to continue executing your request with.
         * @throws X When the invoked method throws {@code X}.
         */
        public <X extends Throwable> Executor check(Class<X> exception) throws X { return $(exception); };

        /**
         * Executes the method
         *
         * @param <T> Requested type
         * @return The real return value of the method.
         * @throws NoSuchMethodError When no method with the provided description can be resolved.
         * @throws IllegalAccessError When the resolved method's access modifiers prevent it from being invoked.
         * @throws IncompatibleClassChangeError When the resolved method is accessed from the wrong context.
         * @throws AbstractMethodError When the resolved method is {@code abstract} and no implementation exists.
         * @throws UnsatisfiedLinkError When the resolved method is {@code native} and no implementation exists.
         * @throws NullPointerException When the resolved method is an instance method and the instance is {@code null}.
         * @throws ClassCastException When the returned value is not convertible to the requested type.
         */
        public <@Polymorphic T> T invoke() { return $(); }
    }

    /**
     * Creates a direct reference to a field
     *
     * @implNote Calling this overload will cause the field type to be determined automatically.
     * @param name Field name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @return An imaginary reference for you to execute your request with.
     */
    public Accessor ofField(String name) { return $(name); }

    /**
     * Creates a direct reference to a field
     *
     * @param type Field type constant &ndash; no dynamic values are permitted here.
     * @param name Field name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @return An imaginary reference for you to execute your request with.
     */
    public Accessor ofField(Class<?> type, String name) { return $(type, name); }

    /**
     * Creates a direct reference to a field
     *
     * @param type Field type name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @param name Field name constant &ndash; no dynamic values or string manipulation is permitted here.
     * @return An imaginary reference for you to execute your request with.
     * @throws NoClassDefFoundError When no type with the provided name can be resolved.
     */
    public Accessor ofField(String type, String name) { return $(type, name); }

    /**
     * A class that is used to build field invocations
     *
     * @implNote Invocations must be constructed and completed in a single statement. Partial invocations may not be stored.
     */
    public final class Accessor {
        private Accessor() {}

        /**
         * Gets the field value
         *
         * @param <T> Requested type
         * @return The current, real value the field holds.
         * @throws NoSuchFieldError When no field with the provided description can be resolved.
         * @throws IllegalAccessError When the resolved field's access modifiers prevent it from being read.
         * @throws IncompatibleClassChangeError When the resolved field is accessed from the wrong context.
         * @throws NullPointerException When the resolved field is an instance field and the instance is {@code null}.
         * @throws ClassCastException When the field value is not convertible to the requested type.
         */
        public <@Polymorphic T> T get() { return $(); }

        /**
         * Gets, then sets, the field value
         *
         * @param <T> Requested type
         * @param value The new value the field will hold.
         * @return The previous, real value the field held.
         * @throws NoSuchFieldError When no field with the provided description can be resolved.
         * @throws IllegalAccessError When the resolved field's access modifiers prevent it from being read or written to.
         * @throws IncompatibleClassChangeError When the resolved field is accessed from the wrong context.
         * @throws NullPointerException When the resolved field is an instance field and the instance is {@code null}.
         * @throws ClassCastException When the provided value is not convertible to the field type or the field value is not convertible to the requested type.
         */
        public <@Polymorphic T> T getAndSet(Object value) { return $(value); }
        /**@hidden */public <@Polymorphic T> T getAndSet(double value) { return $(); }
        /**@hidden */public <@Polymorphic T> T getAndSet(long value) { return $(); }
        /**@hidden */public <@Polymorphic T> T getAndSet(float value) { return $(); }
        /**@hidden */public <@Polymorphic T> T getAndSet(int value) { return $(); }
        /**@hidden */public <@Polymorphic T> T getAndSet(short value) { return $(); }
        /**@hidden */public <@Polymorphic T> T getAndSet(byte value) { return $(); }
        /**@hidden */public <@Polymorphic T> T getAndSet(char value) { return $(); }
        /**@hidden */public <@Polymorphic T> T getAndSet(boolean value) { return $(); }

        /**
         * Sets the field value
         *
         * @param <T> Requested type
         * @param value The new value the field will hold.
         * @return That same, real value, without any conversions.
         * @throws NoSuchFieldError When no field with the provided description can be resolved.
         * @throws IllegalAccessError When the resolved field's access modifiers prevent it from being written to.
         * @throws IncompatibleClassChangeError When the resolved field is accessed from the wrong context.
         * @throws NullPointerException When the resolved field is an instance field and the instance is {@code null}.
         * @throws ClassCastException When the provided value is not convertible to the field type.
         */
        public <T> T set(@Polymorphic T value) { return $(value); }
        /**@hidden */public double set(double value) { return $(); }
        /**@hidden */public long set(long value) { return $(); }
        /**@hidden */public float set(float value) { return $(); }
        /**@hidden */public int set(int value) { return $(); }
        /**@hidden */public short set(short value) { return $(); }
        /**@hidden */public byte set(byte value) { return $(); }
        /**@hidden */public char set(char value) { return $(); }
        /**@hidden */public boolean set(boolean value) { return $(); }

        /**
         * Sets, then gets, the field value
         *
         * @param <T> Requested type
         * @param value The new value the field will hold.
         * @return That same, real value, with all required conversions applied.
         * @throws NoSuchFieldError When no field with the provided description can be resolved.
         * @throws IllegalAccessError When the resolved field's access modifiers prevent it from being written to.
         * @throws IncompatibleClassChangeError When the resolved field is accessed from the wrong context.
         * @throws NullPointerException When the resolved field is an instance field and the instance is {@code null}.
         * @throws ClassCastException When the provided value is not convertible to the field type or the field value is not convertible to the requested type.
         */
        public <@Polymorphic T> T setAndGet(Object value) { return $(value); }
        /**@hidden */public <@Polymorphic T> T setAndGet(double value) { return $(); }
        /**@hidden */public <@Polymorphic T> T setAndGet(long value) { return $(); }
        /**@hidden */public <@Polymorphic T> T setAndGet(float value) { return $(); }
        /**@hidden */public <@Polymorphic T> T setAndGet(int value) { return $(); }
        /**@hidden */public <@Polymorphic T> T setAndGet(short value) { return $(); }
        /**@hidden */public <@Polymorphic T> T setAndGet(byte value) { return $(); }
        /**@hidden */public <@Polymorphic T> T setAndGet(char value) { return $(); }
        /**@hidden */public <@Polymorphic T> T setAndGet(boolean value) { return $(); }
    }
}
