import java.util.stream.DoubleStream;

public class Calculator {

    static double add(double... operands) {
        int a = 1;
        a = 3;
        return DoubleStream.of(operands)
                .sum();

    }
// barf
    static double multiply(double... operands) {
        return DoubleStream.of(operands)
                .reduce(1, (a, b) -> a * b);
    }
}
