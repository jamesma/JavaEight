Streams
=======

Streams are an update to the Java API that lets you manipulate collections of data in a
*declarative* way - you express a query rather than code an ad hoc implementation for it). In
addition, streams can be processed in parallel *transparently* without you having to write any
multithreaded code!

Compare the following code to return the names of dishes that are low in calories, sorted by
number of calories.

Java 7:
```java
public static List<String> getLowCaloricDishesNamesInJava7(List<Dish> dishes){
    List<Dish> lowCaloricDishes = new ArrayList<>();
    for(Dish d: dishes){
        if(d.getCalories() > 400){
            lowCaloricDishes.add(d);
        }
    }
    List<String> lowCaloricDishesName = new ArrayList<>();
    Collections.sort(lowCaloricDishes, new Comparator<Dish>() {
        public int compare(Dish d1, Dish d2){
            return Integer.compare(d1.getCalories(), d2.getCalories());
        }
    });
    for(Dish d: lowCaloricDishes){
        lowCaloricDishesName.add(d.getName());
    }
    return lowCaloricDishesName;
}
```

Java 8:
```java
public static List<String> getLowCaloricDishesNamesInJava8(List<Dish> dishes){
    return dishes.stream()
                 .filter(d -> d.getCalories() > 400)
                 .sorted(comparing(Dish::getCalories))
                 .map(Dish::getName)
                 .collect(toList());
}
```

For now, you can see that the new approach offers several immediate benefits from a software
engineering point of view:

1. The code is written in a *declarative way* - you specify *what* you want to achieve as opposed
   to specifying *how* to implement an operation (using control-flow blocks such as loops and if
   conditions).
2. You chain together several building-block operations to express a complicated data processing
   pipeline while keeping your code readable and its intent clear.

Because operations such as `filter` (or `sorted`, `map`, and `collect`) are available as
*high-level building blocks* that don't depend on a specific threading model, their internal
implementation could be single-threaded or potentially maximize your multicore architecture
transparently.

What is a stream?
=================

A short definition is "a sequence of elements from a source that supports data processing
operations". Let's break this down:

1. *sequence of elements* - Like a collection, a stream provides an interface to a sequenced set of
   values of a specific element type. Because collections are data structures, they're mostly about
   storing and accessing elements with specific time/space complexities. But streams are about
   expressing computations such as `filter`, `sorted`, `map` that you saw earlier.

   Collections are about data; streams are about computations.

2. *source* - streams consume from a data-providing source such as collections, arrays, or I/O
   resources.

3. *data processing operations* - streams support database-like operations and common operations
   from functional programming languages to manipulate data. Stream operations can be executed
   either sequentially or in parallel.

In additiona, streams have two important characteristics:

1. *pipelining* - many stream operations return a stream themselves, allowing operations to be
   chained and form a larger pipeline. This enables certain optimizations that we explain in detail
   later, such as *laziness* and *short-circuiting*.
2. *internal iteration* - in contrast to collections, which are iterated explicitly using an
   iterator, stream operations do the iteration behind the scenes for you.
3. *traversable only once* - similarly to iterators, a stream can be traversed only once. after
   that, a stream is said to be consumed. you can get a new stream from the initial data source to
   traverse it again just like an iterator, assuming it's a repeatable source like a collection, if
   it's an I/O channel, you're out of luck.

External vs Internal iteration
==============================

External iteration with a for-each loop:
```java
List<String> names = new ArrayList<>();
for (Dish d : menu) {           // explicitly iterate the list of menu sequentially
    names.add(d.getName());     // extract the name and add it to an accumulator
}
```

Internal iteration with streams:
```java
List<String> names = menu.stream()
                         .map(Dish::getName)
                         .collect(toList());    // start executing the pipeline of operations, no iteration!
```

Stream operations
=================

The `Stream` interface defines many operations. They can be classified in two categories. You can
see two groups of operations:

Intermediate operations
-----------------------

Intermediate operations, such as `filter`, `map`, or `sorted` return another stream as the
return type. What's important is that intermediate operations don't perform any processing
until a terminal operation is invoked on the stream pipeline - they're lazy.

```java
return menu.stream()
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
```

This code when executed will print the following:
```
filtering: pork
mapping: pork
filtering: beef
mapping: beef
filtering: chicken
mapping: chicken
```

Terminal operations
-------------------

Terminal operations produce a result from a stream pipeline. A result is any non-stream value as a
`List`, an `Integer`, or even `void`.

```java
menu.stream()
    .forEach(System.out::println); // `void` return type
```
