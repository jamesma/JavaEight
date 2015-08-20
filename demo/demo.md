Introduction
============

Topics covered today:

1. Behavior parameterization
2. Lambda expressions
3. Method references
4. Streams API (basic)
5. Default methods

Topics not yet covered:

- Collecting data with streams
- Parallel data processing and performance
- Optional - a better alternative to null
- CompletableFuture - composable async programming
- New Date and Time API
- Misc language and library updates

Behavior Parameterization
=========================

A well known problem in software engineering is that no matter what you do, user requirements will
change. Behavior parameterization is a software development pattern that lets you handle frequent
requirement changes.

In a nutshell, it means taking a block of code and making it available without executing it. This
block of code can be called later by other parts of your pgrogram, which means that you can defer
the execution of that block of code.

Let's walk through an example here that we'll gradually improve, showing some best practices for
making your code more flexible. Imagine an application to help a farmer understand his inventory.

The farmer might want a functionality to find all green apples in his inventory. But the next day
he might tell you "actually I also want to find all applies heavier than 150g". Two days later, the
farmer comes back and adds "it would be really nice if I could find all apples that are green and
heavier than 150g". How can you cope with these changing requirements?

First attempt
-------------

Filtering green apples:
```java
public static List<Apple> filterGreenApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory){
        if ("green".equals(apple.getColor())) {
            result.add(apple);
        }
    }
    return result;
}
```

Second attempt
--------------

Parameterizing the color:
```java
public static List<Apple> filterApplesByColor(List<Apple> inventory,
                                              String color) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory) {
        if (apple.getColor().equals(color)) {
            result.add(apple);
        }
    }
    return result;
}

List<Apple> greenApples = filterApplesByColor(inventory, "green");
List<Apple> redApples = filterApplesByColor(inventory, "red");
```

```java
public static List<Apple> filterApplesByWeight(List<Apple> inventory,
                                               int weight) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory) {
        if (apple.getWeight() > weight) {
            result.add(apple);
        }
    }
    return result;
}
```

Third attempt
-------------

Filtering with every attribute you can think of:
```java
public static List<Apple> filterApples(List<Apple> inventory,
                                       String color,
                                       int weight,
                                       boolean flag) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory) {
        if ((flag && apple.getColor().equals(color)) ||
            (!flag && apple.getWeight() > weight)) {
            result.add(apple);
        }
    }
    return result;
}

List<Apple> greenApples = filterApples(inventory, "green", 0, true);
List<Apple> redApples = filterApples(inventory, "", 150, false);
```

Fourth attempt
--------------

Behavior parameterization - filtering by abstract criteria:
```java
public static List<Apple> filterApples(List<Apple> inventory,
                                       ApplePredicate p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory) {
        if (p.test(apple)) {
            result.add(apple);
        }
    }
    return result;
}

public interface ApplePredicate {
    boolean test(Apple apple);
}

public class AppleRedAndHeavyPredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return "red".equals(apple.getColor()) && apple.getWeight > 150;
    }
}

List<Apple> redAndHeavyApples = filterApples(inventory, new AppleRedAndHeavyPredicate());
```

At this point, we've achieved something really cool: the behavior of the `filterApples` method
depends on the *code you pass* to it via the `ApplePredicate` object. In other words, we've
parameterized the behavior of the `filterApples` method!

However, this process is verbose because you need to declare multiple classes (for each predicate)
that you instantiate only once. Let's see how to improve that.

Fifth attempt
-------------

Using anonymous classes:
```java
List<Apple> redApples = filterApples(inventory, new ApplePredicate() {
    public boolean test(Apple apple) {
        return "red".equals(apple.getColor());
    }
});
```

But anonymous classes are still not good enough. First, they tend to be very bulky because they
take a lot of space. Second, many programmers find them confusing to use.

For example, here's a
classic Java puzzler that catches most of us off guard, try your hand at it:
```java
// Q: What will be the output when doIt() is executed, 4, 5, 6, or 7?
public class Wuuuut {
    public final int value = 4;
    public void doIt() {
        int value = 6;
        Runnable r = new Runnable() {
            public final int value = 5;
            public void run() {
                int value = 7;
                System.out.println(this.value);
            }
        };
        r.run();
    }
}
```

Verbosity in general is bad: it discourages the use of a language feature because it takes a long
time to write and maintain verbose code. Good code should be easy to comprehend at a glance.

Sixth attempt
-------------

Using a lambda expression:
```java
List<Apple> redApples = filterApples(inventory, (Apple apple) -> "red".equals(apple.getColor()));
```

Seventh attempt
---------------

Abstracting over `List` type:
```java
public interface Predicate<T> {
    boolean test(T t);
}

public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> result = new ArrayList<>();
    for (T e : list) {
        if (p.test(e)) {
            result.add(e);
        }
    }
    return result;
}

List<Apple> redApples = filter(inventory, (Apple apple) -> "red".equals(apple.getColor()));
List<Apple> evenNumbers = filter(numbers, (Integer i) -> i % 2 == 0);
```

Isn't it cool? You've found the sweet spot between flexibility and conciseness, which wasn't
possible before Java 8!

Lambda Expressions
==================

A *lambda expression* can be understood as a concise representation of an anonymous function that
can be passed around: it doesn't have a name, but it has a list of parameters, a body, a return
type, and possibly a list of exceptions that can be thrown.

Let's break it down:

1. *Anonymous* - It doesn't have an explicit name like a method would normally have, less to write
                 and think about!
2. *Function* - We say *function* because a lambda isn't associated with a particular class like a
                method is. But like a method, it has a list of parameters, a body, a return
                type, and possibly a list of exceptions that can be thrown.
3. *Passed around* - It can be passed as an argument to a method or stored in a variable.
4. *Concise* - You don't need to write a lot of boilerplate like you do for anonymous classes.

If you're wondering where the term *lambda* comes from, it originates from a system developed in
academia called *lambda calculus*, which is used to describe computations.

Before:
```java
Comparator<Apple> byWeight = new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
}
```

After:
```java
Comparator<Apple> byWeight =
    (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight);
```

<!-- show figure 3.1 -->

The basic syntax of a lambda is either:
```
(parameters) -> expression
```
or
```
(parameters) -> { statements; }
```

To illustrate further, here's five examples of valid lambda expressions:
```java
(String s) -> s.length()

(Apple a) -> a.getWeight() > 150

(int x, int y) -> {
    System.out.println("Result: ");
    System.out.println(x + y);
}

() -> 42

(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight())
```

Here's a list of example lambdas with examples of use cases:

Use case                      | Examples of lambdas
----------------------------- | -------------------
A boolean expression          | `(List<String> list) -> list.isEmpty()`
Creating objects              | `() -> new Apple(10)`
Consuming from an object      | `(Apple a) -> { System.out.println(a.getWeight()); }`
Select/extract from an object | `(String s) -> s.length()`
Combine two values            | `(int a, int b) -> a * b`
Compare two objects           | `(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight())`

Where and how to use lambdas
============================

You can use a lambda expression in the context of a functional interface.

In a nutshell, a *functional interface* is an interface that specifies exactly one abstract method.
You already know several other functional interfaces in the Java API such as:

```java
public interface Comparator<T> { // java.util.Comparator
    int compare(T o1, T o2);
}

public interface Runnable { // java.lang.Runnable
    void run();
}

public interface ActionListener extends EventListener { // java.awt.event.ActionListener
    void actionPerformed(ActionEvent e);
}

public interface Callable<V> { // java.util.concurrent.Callable
    V call();
}

public interface PrivilegedAction<V> { // java.security.PrivilegedAction
    T run();
}
```

Note: An interface is still a functional interface if it has many default methods as long as it
      specifies only one *abstract method*.

Lambda expressions let you provide the implementation of the abstract method of a functional
interface directly inline and *treat the whole expression as an instance of a functional interface*
(more technically speaking, an instance of a *concrete implementation* of the functional interface).

You can achieve the same thing with an anonymous inner class, although it's clumsier.

Function descriptor
===================

The signature of the *abstract method* of the functional interface essentially describes the
signature of the lambda expression. We call this abstract method a *function descriptor*.

For example, the `Runnable` interface can be viewed as the signature of a function that accepts
nothing (`void`) because it has only one abstract method called `run`, which accepts nothing and
returns nothing (`void`).

You may already be wondering how lambda expressions are type checked. We'll talk about this later.
For now, it suffices to understand that a lambda expression can be assigned to a variable or
passed to a method expecting a functional interface as argument, provided the lambda expression
has the same signature as the abstract method of the functional interface.

```java
public interface Runnable { // java.lang.Runnable
    void run(); // function descriptor: () -> void
}

class Demo {
    private static void process(Runnable r) {
        r.run();
    }

    public static void main(String[] args) {
        // the following lambda expression has the same signature as the abstract method of Runnable
        process(() -> System.out.println("Hello World!"));
    }
}
```

<!-- Might be also appropriate to mention @FunctionalInterface annotation -->

The Java library designers have helped us by introducing several new functional interfaces inside
the `java.util.function` package. Here's a few that are commonly used:

```java
// Accepts an object of generic type T and returns a boolean.
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}

// Accepts an object of generic type T and returns no result (void).
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}

// Accepts an object of generic type T as input and returns an object of generic type R.
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}
```

Here's how you use a `Predicate`:
```java
public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> results = new ArrayList<>();
    for (T s : list) {
        if (p.test(s)) {
            results.add(s);
        }
    }
    return results;
}

// ["foo", "bar"]
List<String> nonEmpty = filter(Arrays.asList("foo", "", "bar"),
                               (String s) -> !s.isEmpty());
```

Here's how you use a `Consumer`:
```java
public static <T> void forEach(List<T> list, Consumer<T> c) {
    for (T i : list) {
        c.accept(i);
    }
}

// 1, 2, 3
forEach(Arrays.asList(1, 2, 3),
        (Integer i) -> System.out.println(i));
```

Here's how you use a `Function`:
```java
public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
    List<R> result = new ArrayList<>();
    for (T s : list) {
        result.add(f.apply(s));
    }
    return result;
}

// [3, 3]
List<Integer> ints = map(Array.asList("foo", "bar"),
                         (String s) -> s.length());
```

Auto-boxing concerns
====================

What about auto-boxing? It comes with a performance cost after all. Java 8 brings a specialized
version of the functional interfaces we described earlier in order to avoid autoboxing operations
when the inputs or outputs are primitives.

```java
@FunctionalInterface
public interface IntPredicate {
    boolean test(int t);
}
```

In general, the names of functional interfaces that have a specialization for the input type
parameter are preceded by the appropriate primitive type, for example, `DoublePredicate`,
`IntConsumer`, `IntFunction`, and so on. The `Function` has also variants for the output type
parameter, such as `ToIntFunction<T>`, `IntToDoubleFunction`, and so on.

What about exceptions?
======================

Note that none of the functional interfaces allow for a checked exception to be thrown. You have two
options if you need a lambda expression to throw an exception:

1. Define your own functional interface that declares the checked exception.
2. Wrap the lambda with a try/catch block.

Type Checking
=============

The type of a lambda is deduced from the context in which the lambda is used. The type expected for
the lambda expression inside the context is called the *target type*. Let's look at an example to
see what happens behind the scenes when you use a lambda expression:

```java
List<Apple> heavierThan150g = filter(inventory, (Apple a) -> a.getWeight() > 150);
```

1. You look up the declaration of the `filter` method
2. It expects as the second formal parameter an object of type `Predicate<Apple>` (the target type)
3. `Predicate<Apple>` is a functional interface defining a single abstract method called `test`
4. The method `test` describes a function descriptor that accepts an `Apple` and returns a `boolean`

Note that if the lambda expression were throwing an exception, then the declared `throws` clause of
the abstract method would also have to match.

You may have the same lambda, different functional interfaces because of the idea of target typing.

Type Inference
==============

For lambda expressions up to this point, we can simplify them further. The Java compiler deduces
what functional interface to associate with a lambda expression from its surrounding context. The
compiler has access to the types of the parameters of a lambda expression and they can be omitted
in the lambda syntax.

```java
// without type inference
Comparator<Apple> c = (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());

// with type inference
Comparator<Apple> c = (a1, a2) -> a1.getWeight().compareTo(a2.getWeight());
```

There's no rule for which way is better - developers must make their own choices.

Using local variables
=====================

Lambda expressions are allowed to use *free variables* just like anonymous classes can - variables
that aren't the parameters and defined in an outer scope. They're called *capturing lambdas*. Local
variables have to be explicitly declared final or are effectively final.

<!-- instance variables are stored on heap, local variables are stored on stack, if a lambda
     could access the local variable directly and the lambda were used in a thread, then the thread
     using the lambda could try to access the variable after the thread that allocated the variable
     had de-allocated it. hence Java implements access to a free local variable as access to a copy
     of it rather than access to the original. allowing capture of mutable local variables opens
     new thread-unsafe opportunities, hence the restriction. -->

Method References
=================

Why should you care about method references? Method references can be seen as a short hand for
lambdas calling only a specific method. The basic idea is that if a lambda represents
"call this method directly", it's best to refer to the method by name rather than by a description
of how to call it. Through this your code can gain *better readability*.

Lambda                                | Method reference equivalent
------------------------------------- | ---------------------------
`(Apple a) -> a.getWeight()`          | `Apple::getWeight`
`(String s) -> System.out.println(s)` | `System.out::println`
`(str, i) -> str.substring(i)`        | `String::substring`

Q: What are equivalent method references for the following lambdas?
```java
1. Function<String, Integer> stringToInteger = (String s) -> Integer.parseInt(s);
2. BiPredicate<List<String>, String> contains = (list, element) -> list.contains(element);
```

Answer:
```java
1. Function<String, Integer> stringToInteger = Integer::parseInt;
2. BiPredicate<List<String>, String> contains = List::contains;
```

Putting lambdas and method references into practice!
====================================================

We'll try refactoring the problem of sorting a list of `Apples` with different ordering
strategies and show how you can progressively evolve a naive solution into a concise one.

**Step 1:**
You already have `List.sort` method in Java 8, with the following signature:
```java
void sort(Comparator<? super E> c);
```

Your first solution looks like:
```java
public class AppleComparator implements Comparator<Apple> {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
}
appleList.sort(new AppleComparator());
```

**Step 2:**
Use an anonymous class.

```java
inventory.sort(new Comparator<Apple>(){
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
});
```

**Step 3:**
Use lambda expressions.

```java
inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));

// with type inference
inventory.sort((a1, a2) -> a1.getWeight().compareTo(a2.getWeight()));
```

`Comparator` has a static helper method called `comparing` that takes a `Function` extracting a
`Comparable` key and produces a `Comparator` object.

```java
Comparator<Apple> c = Comparator.comparing((a) -> a.getWeight());
```

You can now rewrite your solution in a more compact form:
```java
import static java.util.Comparator.comparing;
inventory.sort(comparing(a -> a.getWeight()));
```

**Step 4:**
Use method references:

```java
inventory.sort(comparing(Apple::getWeight));
```

Congratulations! This is your final solution! Why is this code better than code prior to Java 8?
It's obvious what it means, and the code reads like the problem statement "sort inventory comparing
the weight of the apples".

What if requirements change?
============================

Reversed order (i.e. decreasing weight):
```java
inventory.sort(comparing(Apple::getWeight)
         .reversed());                       // sorting by decreasing weight
```

Reversed order, and additional priority if two apples are of equal weight:
```java
inventory.sort(comparing(Apple::getWeight))
         .reversed()                         // sorting by decreasing weight
         .thenComparing(Apple::getCountry)); // sorting further by country when two apples have same weight
```

Composing Predicates
====================

The `Predicate` interface includes 3 methods that let you reuse an existing `Predicate` to create
more complicated ones: `negate`, `and`, and `or`.

```java
Predicate<Apple> redApple = a -> "red".equals(a.getColor());

Predicate<Apple> notRedApple = redApple.negate();

Predicate<Apple> redAndHeavyApple = redApple.and(a -> a.getWeight > 150);

Predicate<Apple> redAndHeavyAppleOrGreen = redApple.and(a -> a.getWeight > 150)
                                                   .or(a -> "green".equals(a.getColor()));
```

Why is this great (again)? You can represent more complicated lambda expressions that still read
like the problem statement!

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

In addition, streams have two important characteristics:

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

Multithreading is difficult
===========================

The problem is that exploiting parallelism by writing *multithreaded code* (using the Threads API
from previous versions of Java) is difficult. You have to think differently: threads can access
and update shared variables at the same time. As a result, data could change unexpectedly if not
coordinated properly. This model is harder to think about than a step-by-step sequential model.

There are two design motivations of the Streams API.

The first design motivator is that there are many data processing patterns (similar to
`filterApples`) that occur over and over again and that would benefit from forming part of a
library: *filtering* data based on a criterion, *extracting* data, or *grouping* data, and so on.

The second design motivator is that such operations can be often be parallelized. For instance,
filtering a list on two CPUs could be done by asking one CPU to process the first half of a list and
the second CPU to process the other half of the list. The CPUs then filter their respective
half-lists. Finally, one CPU would join the two results.

Key take-away:

1. Collections API is mostly about storing and accessing data.
2. Streams API is mostly about describing computations on data.

Here's a taste of how you can get "parallelism almost for free":

```java
import static java.util.streams.Collectors.groupingBy;
Map<Currency, List<Transaction>> transactionsByCurrencies =
    transactions.parallelStream() // s/stream/parallelStream/g
                .filter((Transaction t) -> t.getPrice() > 1000)
                .collect(groupingBy(Transaction::getCurrency));
```

Default methods
===============

Default methods are added to Java 8 largely to support library designers by enabling them to write
*more evolvable* interfaces. They're important because you'll increasingly encounter them in
interfaces, but because relatively few programmers will need to write default methods themselves
and because they facilitate program evolution rather than helping write any particular program.

For e.g. the previous Java 8 code has a problem:

```java
import static java.util.streams.Collectors.groupingBy;
Map<Currency, List<Transaction>> transactionsByCurrencies =
    transactions.parallelStream()
                .filter((Transaction t) -> t.getPrice() > 1000)
                .collect(groupingBy(Transaction::getCurrency));
```

`List<T>` prior to Java 8 doesn't have `stream` or `parallelStream` methods, and neither does the
`Collection<T>` interface that it implements! Without these methods this code won't compile.

The simplest solution which you might employ for your own interfaces, would have been for the Java 8
designers to simply add the `stream` method to the `Collection` interface and add the implementation
in the `ArrayList` class.

But this would have been a nightmare for users. There are many alternative collection frameworks
that implement interfaces from the Collections API. Adding a new method to an interface means all
concrete classes must provide an implementation for it. Language designers have no control on all
existing implementations of `Collections`, so you have a bit of a dilemma: how can you evolve
published interfaces without disrupting existing implementations?

Hence *default methods* for interfaces. An interface can now contain method signatures for which an
implementing class doesn't provide an implementation! So who implements them? The missing method
bodies are given as part of the interface rather than in the implementing class.

Java 8 uses the keyword *default* in the *interface* specification to achieve this.

For the above example, the following default method was added in the `List` interface, which
calls the static method `Collections.sort`:

```java
default void sort(Comparator<? super E> c) {
    Collections.sort(this, c);
}
```

But wait, a single class can implement multiple interfaces, right? So does that mean that you can
have multiple default implementations in several interfaces? The answer is Yes, to some extent.

I'll talk about this in the future (or you can look it up), there are some restrictions that prevent
issues such as the infamous *diamond inheritance problem* in C++.
