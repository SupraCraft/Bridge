package bridge.objects;

interface SuperInterface {

    default float superInterface(double value) {
        return (float) value;
    }
}
