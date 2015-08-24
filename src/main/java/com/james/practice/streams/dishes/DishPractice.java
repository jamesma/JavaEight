package com.james.practice.streams.dishes;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class DishPractice {

    List<String> threeHighCaloricDishNames() {
        return Dish.menu.stream()
                        .filter(d -> d.getCalories() > 300)
                        .map(Dish::getName)
                        .limit(3)
                        .collect(toList());
    }

    List<String> threeHighCaloricDishNamesDebug() {
        return Dish.menu.stream()
                         .filter(d -> {
                             System.out.println("filtering: " + d.getName());
                             return d.getCalories() > 300;
                         })
                         .map(d -> {
                             System.out.println("mapping: " + d.getName());
                             return d.getName();
                         })
                         .limit(3)
                         .collect(toList());
    }

    List<String> dishNames() {
        return Dish.menu.stream()
                        .map(Dish::getName)
                        .collect(toList());
    }

    List<Integer> dishNamesLengths() {
        return Dish.menu.stream()
                        .map(Dish::getName)
                        .map(String::length)
                        .collect(toList());
    }

    List<String> dishNamesUniqueCharacters() {
        return Dish.menu.stream()                   // Stream<Dish>
                        .map(Dish::getName)         // Stream<String>
                        .map(w -> w.split(""))      // Stream<String[]>
                        .flatMap(Arrays::stream)    // Stream<String>
                        .distinct()                 // Stream<String>
                        .collect(toList());         // List<String>
    }

    public static void main(String[] args) {
        DishPractice dishPractice = new DishPractice();

        System.out.println("\nthreeHighCaloricDishNames:");
        System.out.println(dishPractice.threeHighCaloricDishNames());

        System.out.println("\nthreeHighCaloricDishNamesDebug:");
        System.out.println(dishPractice.threeHighCaloricDishNamesDebug());

        System.out.println("\ndishNames:");
        System.out.println(dishPractice.dishNames());

        System.out.println("\ndishNamesLengths:");
        System.out.println(dishPractice.dishNamesLengths());

        System.out.println("\ndishNamesUniqueCharacters:");
        System.out.println(dishPractice.dishNamesUniqueCharacters());
    }
}
