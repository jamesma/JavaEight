package com.james.practice.streams;

import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Fibonacci {
    private void runIterate() {
        Stream.iterate(new int[]{0, 1}, n -> new int[]{n[1], n[0] + n[1]})
              .limit(20)
              .forEach(t -> System.out.println("(" + t[0] + ", " + t[1] + ")"));
    }

    private void runGenerate() {
        IntSupplier intSupplier = new IntSupplier() {
            private int prev = 0;
            private int curr = 1;

            @Override
            public int getAsInt() {
                int oldPrev = prev;
                int nextVal = prev + curr;
                prev = curr;
                curr = nextVal;
                return oldPrev;
            }
        };

        IntStream.generate(intSupplier)
                 .limit(10)
                 .forEach(System.out::println);
    }

    public static void main(String[] args) {
        Fibonacci fibonacci = new Fibonacci();

        System.out.println("runIterate");
        fibonacci.runIterate();

        System.out.println("runGenerate");
        fibonacci.runGenerate();
    }
}
