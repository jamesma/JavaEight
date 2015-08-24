package com.james.practice.demo;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class SumBenchmark {

    private static final long NUMBER = 100;

    @Benchmark
    public long iterativeSum() {
        return Sum.iterativeSum(NUMBER);
    }

    @Benchmark
    public long sequentialSum() {
        return Sum.sequentialSum(NUMBER);
    }

    @Benchmark
    public long parallelSum() {
        return Sum.parallelSum(NUMBER);
    }

    // improved

//    @Benchmark
//    public long sequentialSumImproved() {
//        return SumImproved.sequentialSum(NUMBER);
//    }
//
//    @Benchmark
//    public long parallelSumImproved() {
//        return SumImproved.parallelSum(NUMBER);
//    }

    // with delay

//    @Benchmark
//    public long iterativeSumWithDelay() throws InterruptedException {
//        return SumWithDelay.iterativeSum(NUMBER);
//    }
//
//    @Benchmark
//    public long sequentialSumWithDelay() {
//        return SumWithDelay.sequentialSum(NUMBER);
//    }
//
//    @Benchmark
//    public long parallelSumWithDelay() {
//        return SumWithDelay.parallelSum(NUMBER);
//    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .warmupIterations(5)
                .measurementIterations(10)
                .forks(1)
                .include(SumBenchmark.class.getSimpleName())
                .build();

        new Runner(options).run();
    }
}
