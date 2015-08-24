package com.james.practice.demo;

import java.util.stream.LongStream;

public class SumWithDelay {

    static long iterativeSum(long n) throws InterruptedException {
        long result = 0;
        for (long i = 1L; i <= n; i++) {
            Thread.sleep(1); // simulate DB
            result += i;
        }
        return result;
    }

    static long sequentialSum(long n) {
        return LongStream.rangeClosed(1, n)
                         .reduce(0L, (t1, t2) -> {
                             try {
                                 Thread.sleep(1); // simulate DB
                             } catch (InterruptedException e) {
                                 e.printStackTrace();
                             }
                             return t1 + t2;
                         });
    }

    static long parallelSum(long n) {
        return LongStream.rangeClosed(1, n)
                         .parallel()
                         .reduce(0L, (t1, t2) -> {
                             try {
                                 Thread.sleep(1); // simulate DB
                             } catch (InterruptedException e) {
                                 e.printStackTrace();
                             }
                             return t1 + t2;
                         });
    }
}
