package com.james.practice.futures;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class Executors {
    static final Executor EXECUTOR =
        newFixedThreadPool(Math.min(4, 100),
                           threadFactory -> {
                               Thread t = new Thread();
                               t.setDaemon(true);
                               return t;
                           });
}
