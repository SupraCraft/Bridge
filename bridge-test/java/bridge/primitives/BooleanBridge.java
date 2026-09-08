package bridge.primitives;

import bridge.Bridge;

class BooleanBridge {

    @Bridge(access = ~Bridge.PRIVATE, name = "BOOLEAN", returns = boolean.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "CHAR", returns = char.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "BYTE", returns = byte.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "SHORT", returns = short.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "INT", returns = int.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "FLOAT", returns = float.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "LONG", returns = long.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "DOUBLE", returns = double.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "OBJECT", returns = Object.class)
    private static final boolean VALUE = true;



    @Bridge(access = ~Bridge.PRIVATE, name = "toVoid", returns = void.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toBoolean", returns = boolean.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toChar", returns = char.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toByte", returns = byte.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toShort", returns = short.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toInt", returns = int.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toFloat", returns = float.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toLong", returns = long.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toDouble", returns = double.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toObject", returns = Object.class)
    private boolean value() {
        return VALUE;
    }
}
