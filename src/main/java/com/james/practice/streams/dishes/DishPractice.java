package com.james.practice.streams.dishes;

import java.util.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

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

    long numberOfDishes() {
        return Dish.menu.stream()
                        .collect(counting()); // can be simplified to count()
    }

    Optional<Dish> findMaxCaloricDish() {
        return Dish.menu.stream()
                        .collect(maxBy(comparingInt(Dish::getCalories)));
    }

    Optional<Dish> findMaxCaloricDishUsingReduce() {
        return Dish.menu.stream()
                        .collect(reducing((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2));
    }

    int totalCalories() {
        return Dish.menu.stream()
                        .collect(summingInt(Dish::getCalories));
    }

    int totalCaloriesUsingReduce() {
        return Dish.menu.stream()
                        .collect(reducing(0, Dish::getCalories, (i, j) -> i + j));
    }

    IntSummaryStatistics caloriesSummaryStatistics() {
        return Dish.menu.stream()
                        .collect(summarizingInt(Dish::getCalories));
    }

    String shortMenu() {
        return Dish.menu.stream()
                        .map(Dish::getName)
                        .collect(joining());
    }

    String shortMenuDelimited() {
        return Dish.menu.stream()
                        .map(Dish::getName)
                        .collect(joining(", "));
    }

    Map<Dish.Type, List<Dish>> dishesByType() {
        return Dish.menu.stream()
                        .collect(groupingBy(Dish::getType));
    }

    enum CaloricLevel {
        DIET,
        NORMAL,
        FAT;

        static CaloricLevel valueOf(Dish dish) {
            int calories = dish.getCalories();
            if (calories <= 400) {
                return CaloricLevel.DIET;
            } else if (calories <= 700) {
                return CaloricLevel.NORMAL;
            } else {
                return CaloricLevel.FAT;
            }
        }
    }

    Map<CaloricLevel, List<Dish>> dishesByCaloricLevel() {
        return Dish.menu.stream()
                        .collect(groupingBy(CaloricLevel::valueOf));
    }

    Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel() {
        return Dish.menu.stream()
                        .collect(groupingBy(Dish::getType,
                                            groupingBy(CaloricLevel::valueOf)));
    }

    Map<Dish.Type, Long> typesCount() {
        return Dish.menu.stream()
                        .collect(groupingBy(Dish::getType,
                                            counting()));
    }

    Map<Dish.Type, Dish> mostCaloricByType() {
        return Dish.menu.stream()
                        .collect(groupingBy(Dish::getType,
                                            collectingAndThen(maxBy(comparingInt(Dish::getCalories)),
                                                              Optional::get)));
    }

    Map<Dish.Type, Set<CaloricLevel>> caloricLevelsByType() {
        return Dish.menu.stream()
                        .collect(groupingBy(Dish::getType,
                                            mapping(CaloricLevel::valueOf, toSet())));
    }

    Map<Boolean, List<Dish>> partitionedMenu() {
        return Dish.menu.stream()
                        .collect(partitioningBy(Dish::isVegetarian));
    }

    List<Dish> vegetarianDishes() {
        return partitionedMenu().get(true);
    }

    Map<Boolean, Map<Dish.Type, List<Dish>>> vegetarianDishesByType() {
        return Dish.menu.stream()
                        .collect(partitioningBy(Dish::isVegetarian,
                                                groupingBy(Dish::getType)));
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

        System.out.println("\nshortMenu:");
        System.out.println(dishPractice.shortMenu());

        System.out.println("\nshortMenuDelimited:");
        System.out.println(dishPractice.shortMenuDelimited());

        System.out.println("\ndishesByType:");
        System.out.println(dishPractice.dishesByType());

        System.out.println("\ndishesByCaloricLevel:");
        System.out.println(dishPractice.dishesByCaloricLevel());

        System.out.println("\ndishesByTypeCaloricLevel:");
        System.out.println(dishPractice.dishesByTypeCaloricLevel());

        System.out.println("\ntypesCount:");
        System.out.println(dishPractice.typesCount());

        System.out.println("\nmostCaloricByType:");
        System.out.println(dishPractice.mostCaloricByType());

        System.out.println("\ncaloricLevelsByType:");
        System.out.println(dishPractice.caloricLevelsByType());
    }
}
