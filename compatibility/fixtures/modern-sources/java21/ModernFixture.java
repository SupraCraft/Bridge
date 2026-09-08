package modern;

import java.util.function.IntUnaryOperator;

public final class ModernFixture {

    sealed interface Shape permits Circle, Rectangle {}
    record Circle(int radius) implements Shape {}
    record Rectangle(int width, int height) implements Shape {}
    record Point(int x, int y) {}

    private ModernFixture() {}

    static int area(Shape shape) {
        return switch (shape) {
            case Circle(int radius) -> radius * radius;
            case Rectangle(int width, int height) -> width * height;
        };
    }

    static String point(Point point) {
        return switch (point) {
            case Point(int x, int y) -> x + ":" + y;
        };
    }

    public static void main(String[] args) {
        IntUnaryOperator twice = value -> value * 2;
        if (area(new Rectangle(3, 4)) != 12) throw new AssertionError("sealed/record pattern failed");
        if (area(new Circle(5)) != 25) throw new AssertionError("record pattern failed");
        if (!"2:3".equals(point(new Point(2, 3)))) throw new AssertionError("record pattern/string concat failed");
        if (twice.applyAsInt(21) != 42) throw new AssertionError("invokedynamic lambda failed");
        System.out.println("modern-ok");
    }
}
