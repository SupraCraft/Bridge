package bridge.primitives;

import bridge.Bridge;

class VoidBridge {

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
    private void value() {
      //return;
    }

    @Bridge(access = ~Bridge.PRIVATE, name = "toBox", returns = Void.class)
    @Bridge(access = ~Bridge.PRIVATE, name = "toInvalid", returns = Boolean.class)
    private Void box() {
        return null;
    }
}
