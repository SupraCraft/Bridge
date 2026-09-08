package bridge.primitives;

import bridge.Bridge;

class UnknownBridge {

    @Bridge(access = ~Bridge.PRIVATE, name = "BOOLEAN", returns = boolean.class)
    private static final Object BOOLEAN_BOX = true;

    @Bridge(access = ~Bridge.PRIVATE, name = "CHAR", returns = char.class)
    private static final Object CHAR_BOX = '\uFFFF';

    @Bridge(access = ~Bridge.PRIVATE, name = "BYTE", returns = byte.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "SHORT", returns = short.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "INT", returns = int.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "FLOAT", returns = float.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "LONG", returns = long.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "DOUBLE", returns = double.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "OBJECT", returns = Object.class)
    private static final Object NUMBER_BOX = -1L;

    @Bridge(access = ~Bridge.PRIVATE, name = "STRING", returns = Object.class)
    @Bridge(access = ~Bridge.STATIC,  name = "STRING_VIRTUAL", returns = Object.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "STRING_COPY")
    private static final String STRING_TEST = "Hello world!";

    @Bridge(access = ~Bridge.PRIVATE, name = "toVoid", returns = void.class)
    private Void voidValue() {
        return null;
    }

    @Bridge(access = ~Bridge.PRIVATE, name = "toBoolean", returns = boolean.class)
    private Object booleanValue() {
        return BOOLEAN_BOX;
    }

    @Bridge(access = ~Bridge.PRIVATE, name = "toChar", returns = char.class)
    private Object charValue() {
        return CHAR_BOX;
    }

    @Bridge(access = ~Bridge.PRIVATE, name = "toByte", returns = byte.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toShort", returns = short.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toInt", returns = int.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toFloat", returns = float.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toLong", returns = long.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toDouble", returns = double.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toObject", returns = Object.class)
    private Object numberValue() {
        return NUMBER_BOX;
    }

    @Bridge(access =  Bridge.PUBLIC,  name = "toString", returns = String.class)
    private static Object stringValue() {
        return STRING_TEST;
    }
}
