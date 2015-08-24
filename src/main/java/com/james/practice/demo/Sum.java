package com.james.practice.demo;

import java.util.stream.Stream;

public class Sum {

    static long iterativeSum(long n) {
        long result = 0;
        for (long i = 1L; i <= n; i++) {
            result += i;
        }
        return result;
    }

    static long sequentialSum(long n) {
        return Stream.iterate(1L, i -> i + 1)
                     .limit(n)
                     .reduce(0L, Long::sum);
    }

    static long parallelSum(long n) {
        return Stream.iterate(1L, i -> i + 1)
                     .limit(n)
                     .parallel()
                     .reduce(0L, Long::sum);
    }
}
