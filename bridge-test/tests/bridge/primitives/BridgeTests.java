package bridge.primitives;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BridgeTests {

    public void testVoid() throws Throwable {
        access(new VoidBridge());
    }

    public void testBoolean() throws Throwable {
        access(new BooleanBridge());
    }

    public void testChar() throws Throwable {
        access(new CharBridge());
    }

    public void testByte() throws Throwable {
        access(new ByteBridge());
    }

    public void testShort() throws Throwable {
        access(new ShortBridge());
    }

    public void testInt() throws Throwable {
        access(new IntBridge());
    }

    public void testFloat() throws Throwable {
        access(new FloatBridge());
    }

    public void testLong() throws Throwable {
        access(new LongBridge());
    }

    public void testDouble() throws Throwable {
        access(new DoubleBridge());
    }

    public void testBox() throws Throwable {
        access(new BoxBridge());
    }

    public void testUnknown() throws Throwable {
        access(new UnknownBridge());
    }

    private static void access(Object test) throws Throwable {
        final Class<?> clazz;
        for (Field field : (clazz = test.getClass()).getDeclaredFields()) {
            if ((field.getModifiers() & Modifier.PRIVATE) == 0) {
                try {
                    if (field.get(null) == null) throw new AssertionError(field.toString());
                } catch (IllegalAccessException e) {
                    throw new AssertionError(field.toString(), e);
                }
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.PRIVATE) == 0) {
                try {
                    final Class<?> returns;
                    if (clazz == VoidBridge.class || (returns = method.getReturnType()) == Void.class || returns == void.class) {
                        method.invoke(test);
                    } else {
                        if (method.invoke(test) == null) throw new AssertionError(method.toString());
                    }
                } catch (IllegalAccessException e) {
                    throw new AssertionError(method.toString(), e);
                }
            }
        }
    }
}
