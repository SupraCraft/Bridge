package bridge.primitives;

import bridge.Bridge;

class BoxBridge {

    @Bridge(access = ~Bridge.PRIVATE, name = "BOOLEAN", returns = boolean.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "BOOLEAN_BOX", returns = Boolean.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "CHAR", returns = char.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "CHAR_BOX", returns = Character.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "BYTE", returns = byte.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "BYTE_BOX", returns = Byte.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "SHORT", returns = short.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "SHORT_BOX", returns = Short.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "INT", returns = int.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "INT_BOX", returns = Integer.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "FLOAT", returns = float.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "FLOAT_BOX", returns = Float.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "LONG", returns = long.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "LONG_BOX", returns = Long.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "DOUBLE", returns = double.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "DOUBLE_BOX", returns = Double.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "OBJECT", returns = Object.class)
    private static final Long VALUE_BOX = -1L;



    @Bridge(access = ~Bridge.PRIVATE, name = "toVoid", returns = void.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toVoidBox", returns = Void.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toBoolean", returns = boolean.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toBooleanBox", returns = Boolean.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toChar", returns = char.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toCharBox", returns = Character.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toByte", returns = byte.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toByteBox", returns = Byte.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toShort", returns = short.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toShortBox", returns = Short.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toInt", returns = int.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toIntBox", returns = Integer.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toFloat", returns = float.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toFloatBox", returns = Float.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toLong", returns = long.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toLongBox", returns = Long.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toDouble", returns = double.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toDoubleBox", returns = Double.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toObject", returns = Object.class)
    private Long value() {
        return VALUE_BOX;
    }
}
