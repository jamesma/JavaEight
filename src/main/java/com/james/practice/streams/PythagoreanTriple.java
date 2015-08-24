package com.james.practice.streams;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PythagoreanTriple {
    private void run() {
        Stream<double[]> pythagoreanTriples =
            IntStream.rangeClosed(1, 100)
                     .boxed()
                     .flatMap(a ->
                                  IntStream.rangeClosed(a, 100)
                                           .mapToObj(b -> new double[]{a, b, Math.sqrt(a * a + b * b)})
                                           .filter(t -> t[2] % 1 == 0));

        pythagoreanTriples.forEach(d -> {
            for (double single : d) {
                System.out.print(((int) single) + "  ");
            }
            System.out.println();
        });
    }

    public static void main(String[] args) {
        PythagoreanTriple triple = new PythagoreanTriple();
        triple.run();
    }
}
