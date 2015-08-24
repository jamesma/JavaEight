package com.james.practice.demo;

import java.util.stream.LongStream;

public class SumImproved {

    static long sequentialSum(long n) {
        return LongStream.rangeClosed(1, n)
                         .reduce(0L, Long::sum);
    }

    static long parallelSum(long n) {
        return LongStream.rangeClosed(1, n)
                         .parallel()
                         .reduce(0L, Long::sum);
    }
}
