package com.james.practice.functions;

import java.util.Arrays;
import java.util.List;

class Predicates {

    static final class Apple {
        String color;
        Double weight;

        Apple(String color, double weight) {
            this.color = color;
            this.weight = weight;
        }

        String getColor() {
            return color;
        }

        Double getWeight() {
            return weight;
        }
    }

    public static void main(String[] args) {
        List<Apple> apples = Arrays.asList(new Apple("red", 1.1), new Apple("green", 1.5));
        apples.sort((a1, a2) -> a1.getWeight().compareTo(a2.getWeight()));
    }
}
