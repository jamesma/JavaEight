package com.james.practice.futures;

public final class Delays {
    static void delay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
