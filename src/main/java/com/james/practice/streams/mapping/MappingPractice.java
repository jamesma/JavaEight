package com.james.practice.streams.mapping;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MappingPractice {

    List<Integer> squaresOfNumbers(List<Integer> numbers) {
        return numbers.stream()
                      .map(n -> n * n)
                      .collect(toList());
    }

    // A pair is represented as an array with two elements.
    List<int[]> pairsOfNumbers(List<Integer> numbers1, List<Integer> numbers2) {
        return numbers1.stream()
                       .flatMap(n1 -> numbers2.stream()
                                              .map(n2 -> new int[]{n1, n2})
                               )
                       .collect(toList());
    }

    List<int[]> pairsOfNumbersSumDivisibleByThree(List<Integer> numbers1, List<Integer> numbers2) {
        return numbers1.stream()
                       .flatMap(n1 -> numbers2.stream()
                                              .filter(n2 -> (n1 + n2) % 3 == 0)
                                              .map(n2 -> new int[]{n1, n2})
                               )
                       .collect(toList());
    }

    public static void main(String[] args) {
        MappingPractice practice = new MappingPractice();

        System.out.println("\nsquaresOfNumbers:");
        System.out.println(practice.squaresOfNumbers(Arrays.asList(1, 2, 3)));

        System.out.println("\npairsOfNumbers:");
        practice.pairsOfNumbers(Arrays.asList(1, 2, 3), Arrays.asList(3, 4))
                .stream()
                .forEach(pair -> System.out.println(Integer.toString(pair[0]) + ", " + Integer.toString(pair[1])));

        System.out.println("\npairsOfNumbersSumDivisibleByThree:");
        practice.pairsOfNumbersSumDivisibleByThree(Arrays.asList(1, 2, 3), Arrays.asList(3, 4))
                .stream()
                .forEach(pair -> System.out.println(Integer.toString(pair[0]) + ", " + Integer.toString(pair[1])));
    }
}
