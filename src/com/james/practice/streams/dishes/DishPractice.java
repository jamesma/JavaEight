package com.james.practice.streams.dishes;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class DishPractice {

    List<Dish> menu() {
        return Arrays.asList(
            new Dish("pork", false, 800, Dish.Type.MEAT),
            new Dish("beef", false, 700, Dish.Type.MEAT),
            new Dish("chicken", false, 400, Dish.Type.MEAT),
            new Dish("french fries", true, 530, Dish.Type.OTHER),
            new Dish("rice", true, 350, Dish.Type.OTHER),
            new Dish("season fruit", true, 120, Dish.Type.OTHER),
            new Dish("pizza", true, 550, Dish.Type.OTHER),
            new Dish("prawns", false, 300, Dish.Type.FISH),
            new Dish("salmon", false, 450, Dish.Type.FISH)
        );
    }

    List<String> threeHighCaloricDishNames() {
        return menu().stream()
                     .filter(d -> d.getCalories() > 300)
                     .map(Dish::getName)
                     .limit(3)
                     .collect(toList());
    }

    List<String> threeHighCaloricDishNamesDebug() {
        return menu().stream()
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
