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

    public static void main(String[] args) {
        DishPractice dishPractice = new DishPractice();

        System.out.println(dishPractice.threeHighCaloricDishNames());

        System.out.println(dishPractice.threeHighCaloricDishNamesDebug());
    }
}
