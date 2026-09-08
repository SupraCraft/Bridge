package bridge.objects;

import bridge.Invocation;
import bridge.Jump;
import bridge.Label;
import bridge.Unchecked;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
class Invocations {

    private static final Object alternate = new Dummy(null);
    private static final Dummy instance = new Dummy(null);
    private static final Super generic = Dummy.s_obj = instance.v_obj = new Super(null);
    private static final Integer zero = 0;
    private static final short label = 420;

    // jump & label test
    @SuppressWarnings({"ConstantConditions", "ThrowableNotThrown"})
    static void JUMP() {
        int i = -1;
        new Label(label);
        if (++i == 5) throw new Jump("exit");
        new Jump(label);
        new Label("exit");
        assert i == 5;
    }

    // unchecked casting test
    static void CAST() {
        HashMap<?, ?> a = Unchecked.cast(new HashMap<>());
        Map<Object, Object> b = Unchecked.cast(a);
        HashMap<?, ?> c = Unchecked.cast(b);
        assert c != null;
    }

    // unchecked exception handling test
    static void THRW() {
        try {
            Unchecked.<NonException>check();
            Jester.sneak();
            assert false;
        } catch (NonException e) {
          //return;
        }
    }



    // static class literals, test 1
    static void SL0A() {
        assert new Invocation(Dummy.class).ofClassLiteral() == Dummy.class;
    }

    // static class literals, test 2
    static void SL0B() {
        assert new Invocation("bridge.objects.Dummy").ofClassLiteral() == Dummy.class;
    }



    // static type checking, test 1
    static void SI0A() {
        assert new Invocation(Dummy.class).ofInstanceOf(Dummy.class);
    }

    // static type checking, test 2
    static void SI0B() {
        assert new Invocation(Dummy.class).ofInstanceOf(Super.class);
    }

    // static type checking, test 3
    static void SI0C() {
        assert new Invocation(Dummy.class).ofInstanceOf(Adoptable.class);
    }

    // static type checking, test 4
    static void SI0D() {
        assert !new Invocation(Dummy.class).ofInstanceOf(Jester.class);
    }

    // static type checking, test 5
    static void SI0E() {
        assert !new Invocation(Void.class).ofInstanceOf(Invocations.class);
    }

    // static type checking, test 6
    static void SI0F() {
        assert new Invocation(Dummy[][].class).ofInstanceOf(Object[].class);
    }

    // static type checking, test 7
    static void SI0G() {
        assert !new Invocation(Dummy[][].class).ofInstanceOf(Super[].class);
    }



    // static fields, object get
    static void SF0A() {
        assert new Invocation(Dummy.class).ofField(Super.class, "s_obj").get() == generic;
    }

    // static fields, object get-and-set, use
    static void SF0B() {
        assert new Invocation(Dummy.class).ofField(Super.class, "s_obj").getAndSet(alternate) == generic;
    }

    // static fields, object get-and-set, store
    static void SF0C() {
        Super value = new Invocation(Dummy.class).ofField("s_obj").getAndSet(generic);
        assert value == alternate;
    }

    // static fields, object get-and-set, store-and-use
    @SuppressWarnings("AssertWithSideEffects")
    static void SF0D() {
        Super value;
        assert (value = new Invocation(Dummy.class).ofField("s_obj").getAndSet(alternate)) == generic;
    }

    // static fields, object get-and-set, void
    static void SF0E() {
        new Invocation(Dummy.class).ofField("s_obj").getAndSet(generic);
    }

    // static fields, object set
    static void SF0F() {
        assert new Invocation(Dummy.class).ofField("bridge.objects.Super", "s_obj").set(alternate) == alternate;
    }

    // static fields, object set, void
    static void SF0G() {
        new Invocation(Dummy.class).ofField("s_obj").set(generic);
    }

    // static fields, object set-and-get
    static void SF0H() {
        assert new Invocation(Dummy.class).ofField(Super.class, "s_obj").setAndGet(alternate) == alternate;
    }

    // static fields, object set-and-get, void
    static void SF0I() {
        new Invocation(Dummy.class).ofField("s_obj").setAndGet(generic);
    }


    // static fields, category 1 primitive get
    static void SF1A() {
        assert (short) new Invocation(Dummy.class).ofField("sp_x1").get() == 0;
    }

    // static fields, category 1 primitive get-and-set, 1-to-1
    static void SF1B() {
        int constant = 1;
        assert (short) new Invocation(Dummy.class).ofField("sp_x1").getAndSet(constant) == 0;
    }

    // static fields, category 1 primitive get-and-set, 1-to-2
    static void SF1C() {
        int constant = 0;
        assert (long) new Invocation(Dummy.class).ofField(short.class, "sp_x1").getAndSet(constant) == 1;
    }

    // static fields, category 1 primitive get-and-set, 1-to-reference
    static void SF1D() {
        int constant = 1;
        assert ((Boolean) new Invocation(Dummy.class).ofField(Short.TYPE, "sp_x1").getAndSet(constant)).compareTo(Boolean.FALSE) == 0;
    }

    // static fields, category 1 primitive get-and-set, 1-to-void
    static void SF1E() {
        int constant = 0;
        new Invocation(Dummy.class).ofField(short.class, "sp_x1").getAndSet(constant);
    }

    // static fields, category 1 primitive set
    static void SF1F() {
        int constant = 1;
        assert new Invocation(Dummy.class).ofField(Short.TYPE, "sp_x1").set(constant) == constant;
    }

    // static fields, category 1 primitive set, void
    static void SF1G() {
        int constant = 0;
        new Invocation(Dummy.class).ofField(short.class, "sp_x1").set(constant);
    }

    // static fields, category 1 primitive set-and-get
    static void SF1H() {
        int constant = 1;
        assert (short) new Invocation(Dummy.class).ofField("sp_x1").setAndGet(constant) == constant;
    }

    // static fields, category 1 primitive set-and-get, void
    static void SF1I() {
        int constant = 0;
        new Invocation(Dummy.class).ofField(Short.TYPE, "sp_x1").setAndGet(constant);
    }


    // static fields, category 2 primitive get
    static void SF2A() {
        assert (double) new Invocation(Dummy.class).ofField("sp_x2").get() == 0;
    }

    // static fields, category 2 primitive get-and-set, 2-to-2
    static void SF2B() {
        float constant = 1;
        assert (double) new Invocation(Dummy.class).ofField("sp_x2").getAndSet(constant) == 0;
    }

    // static fields, category 2 primitive get-and-set, 2-to-1
    static void SF2C() {
        float constant = 0;
        assert (int) new Invocation(Dummy.class).ofField(double.class, "sp_x2").getAndSet(constant) == 1;
    }

    // static fields, category 2 primitive get-and-set, 2-to-reference
    static void SF2D() {
        float constant = 1;
        assert ((Boolean) new Invocation(Dummy.class).ofField(Double.TYPE, "sp_x2").getAndSet(constant)).compareTo(Boolean.FALSE) == 0;
    }

    // static fields, category 2 primitive get-and-set, 2-to-void
    static void SF2E() {
        float constant = 0;
        new Invocation(Dummy.class).ofField(double.class, "sp_x2").getAndSet(constant);
    }

    // static fields, category 2 primitive set
    static void SF2F() {
        float constant = 1;
        assert new Invocation(Dummy.class).ofField(Double.TYPE, "sp_x2").set(constant) == constant;
    }

    // static fields, category 2 primitive set, void
    static void SF2G() {
        float constant = 0;
        new Invocation(Dummy.class).ofField(double.class, "sp_x2").set(constant);
    }

    // static fields, category 2 primitive set-and-get
    static void SF2H() {
        float constant = 1;
        assert (double) new Invocation(Dummy.class).ofField("sp_x2").setAndGet(constant) == constant;
    }

    // static fields, category 2 primitive set-and-get, void
    static void SF2I() {
        float constant = 0;
        new Invocation(Dummy.class).ofField(Double.TYPE, "sp_x2").setAndGet(constant);
    }



    // object instantiation, test 1
    static void SC0A() {
        assert new Invocation(Dummy.class).ofConstructor().invoke() instanceof Dummy;
    }

    // object instantiation, test 2
    static void SC0B() {
        boolean const1 = false;
        assert new Invocation(Dummy.class).ofConstructor()
                .with((boolean) const1)
                .invoke() instanceof Dummy;
    }

    // object instantiation, test 3
    static void SC0C() {
        boolean const1 = false;
        long const2 = 64;
        assert new Invocation(Dummy.class).ofConstructor()
                .with((boolean) const1)
                .with((long) const2)
                .invoke() instanceof Dummy;
    }

    // object instantiation, test 4
    static void SC0D() {
        boolean const1 = false;
        int const2 = 64;
        String const3 = "";
        assert new Invocation(Dummy.class).ofConstructor()
                .with((boolean) const1)
                .with((long) const2)
                .with(CharSequence.class, const3)
                .invoke() instanceof Dummy;
    }

    // object instantiation, test 5
    static void SC0E() {
        boolean const1 = false;
        int const2 = 64;
        String const3 = "";
        try {
            new Invocation(Dummy.class).ofConstructor()
                    .with((boolean) const1)
                    .with((long) const2)
                    .with(CharSequence.class, const3)
                    .check(NonException.class)
                    .invoke();
        } catch (NonException e) {
            assert false;
        }
    }

    // object instantiation, test 6
    static void SC0F() {
        boolean const1 = true;
        int const2 = 64;
        String const3 = "";
        try {
            new Invocation(Dummy.class).ofConstructor()
                    .with((boolean) const1)
                    .with((long) const2)
                    .with(CharSequence.class, const3)
                    .<NonException>check()
                    .invoke();
        } catch (NonException e) {
            return;
        }
        assert false;
    }


    // boolean array instantiation
    static void SC1A() {
        assert new Invocation(boolean[].class).ofConstructor().with(false).invoke() instanceof boolean[];
    }

    // char array instantiation
    static void SC1B() {
        assert new Invocation(char[].class).ofConstructor().with('\u0000').invoke() instanceof char[];
    }

    // byte array instantiation
    static void SC1C() {
        assert new Invocation(byte[].class).ofConstructor().with((byte) 0).invoke() instanceof byte[];
    }

    // short array instantiation
    static void SC1D() {
        assert new Invocation(short[].class).ofConstructor().with((short) 0).invoke() instanceof short[];
    }

    // int array instantiation
    static void SC1E() {
        assert new Invocation(int[].class).ofConstructor().with(0).invoke() instanceof int[];
    }

    // float array instantiation
    static void SC1F() {
        assert new Invocation(float[].class).ofConstructor().with(0F).invoke() instanceof float[];
    }

    // long array instantiation
    static void SC1G() {
        assert new Invocation(long[].class).ofConstructor().with(0L).invoke() instanceof long[];
    }

    // double array instantiation
    static void SC1H() {
        assert new Invocation(double[].class).ofConstructor().with(0D).invoke() instanceof double[];
    }

    // object array instantiation
    static void SC1I() {
        assert new Invocation(Dummy[].class).ofConstructor().with(zero).invoke() instanceof Dummy[];
    }


    // multi-dimensional array instantiation, test 1
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2A() {
        long[][] array;
        assert (array = new Invocation(long[][].class).ofConstructor().with(1D).invoke()) instanceof long[][] && array[0] == null;
    }

    // multi-dimensional array instantiation, test 2
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2B() {
        Dummy[][] array;
        assert (array = new Invocation(Dummy[][].class).ofConstructor().with(1D).invoke()) instanceof Dummy[][] && array[0] == null;
    }

    // multi-dimensional array instantiation, test 3
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2C() {
        double[][][] array;
        assert (array = new Invocation(double[][][].class).ofConstructor().with(1D).invoke()) instanceof double[][][] && array[0] == null;
    }

    // multi-dimensional array instantiation, test 4
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2D() {
        Dummy[][][] array;
        assert (array = new Invocation(Dummy[][][].class).ofConstructor().with(1D).invoke()) instanceof Dummy[][][] && array[0] == null;
    }

    // multi-dimensional array instantiation, test 5
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2E() {
        short[][] array;
        assert (array = new Invocation(short[][].class).ofConstructor().with(1D).with(2F).invoke()) instanceof short[][] && array[0][1] == 0;
    }

    // multi-dimensional array instantiation, test 6
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2F() {
        Dummy[][] array;
        assert (array = new Invocation(Dummy[][].class).ofConstructor().with(1D).with(2F).invoke()) instanceof Dummy[][] && array[0][1] == null;
    }

    // multi-dimensional array instantiation, test 7
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2G() {
        float[][][] array;
        assert (array = new Invocation(float[][][].class).ofConstructor().with(1D).with(2F).invoke()) instanceof float[][][] && array[0][1] == null;
    }

    // multi-dimensional array instantiation, test 8
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2H() {
        Dummy[][][] array;
        assert (array = new Invocation(Dummy[][][].class).ofConstructor().with(1D).with(2F).invoke()) instanceof Dummy[][][] && array[0][1] == null;
    }

    // multi-dimensional array instantiation, test 9
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2I() {
        byte[][][] array;
        assert (array = new Invocation(byte[][][].class).ofConstructor().with(1D).with(2F).with(3L).invoke()) instanceof byte[][][] && array[0][1][2] == 0;
    }

    // multi-dimensional array instantiation, test X
    @SuppressWarnings({"AssertWithSideEffects", "DataFlowIssue"})
    static void SC2J() {
        Dummy[][][] array;
        assert (array = new Invocation(Dummy[][][].class).ofConstructor().with(1D).with(2F).with(3L).invoke()) instanceof Dummy[][][] && array[0][1][2] == null;
    }



    // instance class literals, test 1
    static void VL0A() {
        assert new Invocation(instance).ofClassLiteral() == Dummy.class;
    }

    // instance class literals, test 2
    static void VL0B() {
        assert new Invocation(alternate).ofClassLiteral() == Dummy.class;
    }



    // instance type checking, test 1
    static void VI0A() {
        assert new Invocation(instance).ofInstanceOf(Dummy.class);
    }

    // instance type checking, test 2
    static void VI0B() {
        assert new Invocation(instance).ofInstanceOf(Super.class);
    }

    // instance type checking, test 3
    static void VI0C() {
        assert new Invocation(instance).ofInstanceOf(Adoptable.class);
    }

    // instance type checking, test 4
    static void VI0D() {
        assert !new Invocation(instance).ofInstanceOf(Jester.class);
    }

    // instance type checking, test 5
    static void VI0E() {
        assert !new Invocation((Object) null).ofInstanceOf(Invocations.class);
    }

    // instance type checking, test 6
    static void VI0F() {
        assert new Invocation(new Dummy[0][]).ofInstanceOf(Object[].class);
    }

    // instance type checking, test 7
    static void VI0G() {
        assert !new Invocation(new Dummy[0][]).ofInstanceOf(Super[].class);
    }



    // instance fields, object get
    static void VF0A() {
        assert new Invocation(instance).ofField(Super.class, "v_obj").get() == generic;
    }

    // instance fields, object get-and-set, use
    static void VF0B() {
        assert new Invocation(instance).ofField(Super.class, "v_obj").getAndSet(alternate) == generic;
    }

    // instance fields, object get-and-set, store
    static void VF0C() {
        Super value = new Invocation(instance).ofField("v_obj").getAndSet(generic);
        assert value == alternate;
    }

    // instance fields, object get-and-set, store-and-use
    @SuppressWarnings("AssertWithSideEffects")
    static void VF0D() {
        Super value;
        assert (value = new Invocation(instance).ofField("v_obj").getAndSet(alternate)) == generic;
    }

    // instance fields, object get-and-set, void
    static void VF0E() {
        new Invocation(instance).ofField("v_obj").getAndSet(generic);
    }

    // instance fields, object set
    static void VF0F() {
        assert new Invocation(instance).ofField("bridge.objects.Super", "v_obj").set(alternate) == alternate;
    }

    // instance fields, object set, void
    static void VF0G() {
        new Invocation(instance).ofField("v_obj").set(generic);
    }

    // instance fields, object set-and-get
    static void VF0H() {
        assert new Invocation(instance).ofField(Super.class, "v_obj").setAndGet(alternate) == alternate;
    }

    // instance fields, object set-and-get, void
    static void VF0I() {
        new Invocation(instance).ofField("v_obj").setAndGet(generic);
    }


    // instance fields, category 1 primitive get
    static void VF1A() {
        assert (short) new Invocation(instance).ofField("vp_x1").get() == 0;
    }

    // instance fields, category 1 primitive get-and-set, 1-to-1
    static void VF1B() {
        int constant = 1;
        assert (short) new Invocation(instance).ofField("vp_x1").getAndSet(constant) == 0;
    }

    // instance fields, category 1 primitive get-and-set, 1-to-2
    static void VF1C() {
        int constant = 0;
        assert (long) new Invocation(instance).ofField(short.class, "vp_x1").getAndSet(constant) == 1;
    }

    // instance fields, category 1 primitive get-and-set, 1-to-reference
    static void VF1D() {
        int constant = 1;
        assert ((Boolean) new Invocation(instance).ofField(Short.TYPE, "vp_x1").getAndSet(constant)).compareTo(Boolean.FALSE) == 0;
    }

    // instance fields, category 1 primitive get-and-set, 1-to-void
    static void VF1E() {
        int constant = 0;
        new Invocation(instance).ofField(short.class, "vp_x1").getAndSet(constant);
    }

    // instance fields, category 1 primitive set
    static void VF1F() {
        int constant = 1;
        assert new Invocation(instance).ofField(Short.TYPE, "vp_x1").set(constant) == constant;
    }

    // instance fields, category 1 primitive set, void
    static void VF1G() {
        int constant = 0;
        new Invocation(instance).ofField(short.class, "vp_x1").set(constant);
    }

    // instance fields, category 1 primitive set-and-get
    static void VF1H() {
        int constant = 1;
        assert (short) new Invocation(instance).ofField("vp_x1").setAndGet(constant) == constant;
    }

    // instance fields, category 1 primitive set-and-get, void
    static void VF1I() {
        int constant = 0;
        new Invocation(instance).ofField(Short.TYPE, "vp_x1").setAndGet(constant);
    }


    // instance fields, category 2 primitive get
    static void VF2A() {
        assert (double) new Invocation(instance).ofField("vp_x2").get() == 0;
    }

    // instance fields, category 2 primitive get-and-set, 2-to-2
    static void VF2B() {
        float constant = 1;
        assert (double) new Invocation(instance).ofField("vp_x2").getAndSet(constant) == 0;
    }

    // instance fields, category 2 primitive get-and-set, 2-to-1
    static void VF2C() {
        float constant = 0;
        assert (int) new Invocation(instance).ofField(double.class, "vp_x2").getAndSet(constant) == 1;
    }

    // instance fields, category 2 primitive get-and-set, 2-to-reference
    static void VF2D() {
        float constant = 1;
        assert ((Boolean) new Invocation(instance).ofField(Double.TYPE, "vp_x2").getAndSet(constant)).compareTo(Boolean.FALSE) == 0;
    }

    // instance fields, category 2 primitive get-and-set, 2-to-void
    static void VF2E() {
        float constant = 0;
        new Invocation(instance).ofField(double.class, "vp_x2").getAndSet(constant);
    }

    // instance fields, category 2 primitive set
    static void VF2F() {
        float constant = 1;
        assert new Invocation(instance).ofField(Double.TYPE, "vp_x2").set(constant) == constant;
    }

    // instance fields, category 2 primitive set, void
    static void VF2G() {
        float constant = 0;
        new Invocation(instance).ofField(double.class, "vp_x2").set(constant);
    }

    // instance fields, category 2 primitive set-and-get
    static void VF2H() {
        float constant = 1;
        assert (double) new Invocation(instance).ofField("vp_x2").setAndGet(constant) == constant;
    }

    // instance fields, category 2 primitive set-and-get, void
    static void VF2I() {
        float constant = 0;
        new Invocation(instance).ofField(Double.TYPE, "vp_x2").setAndGet(constant);
    }
}
