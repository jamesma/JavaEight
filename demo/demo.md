Introduction
============
Prior to Java 8, you have to use threads to use these cores. The problem is that working with
threads is difficult and error prone. Java has followed an evolutionary path of continually
trying to make concurrency easier and less error prone.

- Java 1.0: threads, locks, and even a memory model
- Java 5: industrial strength building blocks like thread pools, concurrent collections
- Java 7: fork/join framework
- Java 8: ???

Streams API supports many parallel operations to process data and resembles the way you might think
in database query languages - you express what you want in a higher-level manner, and the
implementation (the Streams library) chooses the best low-level execution mechanism.

- It avoids the need for you to write code that uses `synchronized`, which is not only highly error
  prone but is also more expensive than you may realize on multicore CPUs.

The Java 8 feature of passing code to methods (and also being able to return it and incorporate it
into data structures) also provides access to a whole range of additional techniques that are
commonly referred to as functional-style programming.

---

Java 8 *method reference* `::` syntax - means use this method as a value.

```java
File[] hiddenFiles = new File(".").listFiles(new FileFilter() {
    public boolean accept(File file) {
        return file.isHidden();
    }
})

File[] hiddenFiles = new File(".").listFiles(File::isHidden);
```

In Java 8 when you write `File::isHidden` you create a *method reference*, which can similarly be
passed around like an object reference.

---

Lambdas
=======

As well as allowing (named) methods to be first-class values, Java 8 allows a richer idea of
*functions as values*, including *lambdas*. For example, you can now write `(int x) -> x + 1` to
mean "the function that, when called with an argument x, returns the value x + 1".

You might wonder why this is necessary because you could define a method `add1` inside a class
`MathUtils` and then write `MathUtils::add1`! Yes you could, but the new lambda syntax is more
concise for cases where you don't have a convenient method and class available.

Programs using these concepts are said to be written in functional programming style - this phrase
means "writing programs that pass functions around as first-class values".

```java
public static List<Apple> filterGreenApples(List<Apple> inventory){
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory){
        if ("green".equals(apple.getColor())) {
            result.add(apple);
        }
    }
    return result;
}
```

But next, somebody would like the list of heavy apples (say over 150g), and so, with a heavy
heart, you'd write the following method to achieve this (perhaps even using copy and paste):

```java
public static List<Apple> filterHeavyApples(List<Apple> inventory){
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory){
        if (apple.getWeight() > 150) {
            result.add(apple);
        }
    }
    return result;
}
```

We all know the dangers of copy and paste for software engineering (updates and bug fixes to
one variant but not the other), and hey, these two methods vary only in one line.
But as we mentioned, java 8 makes it possible to pass the code of the condition as an argument,
thus avoiding code duplication of the filter method. you can now write this:

```java
public static boolean isGreenApple(Apple apple) {
    return "green".equals(apple.getColor());
}

public static boolean isHeavyApple(Apple apple) {
    return apple.getWeight() > 150;
}

public interface Predicate<T> {
    boolean test(T t);
}

public static List<Apple> filterApples(List<Apple> inventory,
                                       Predicate<Apple> p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
        if (p.test(apple)) {
            result.add(apple);
        }
    }
    return result;
}
```

And to use this, you call either of the two below:

```java
List<Apple> greenApples = filterApples(inventory, FilteringApples::isGreenApple);

List<Apple> heavyApples = filterApples(inventory, FilteringApples::isHeavyApple);
```

Passing methods as values is clearly useful, but it's a bit annoying having a write a definition
for short methods such as isHeavyApple and isGreenApple when they're used perhaps only once/twice
Here's where you can use lambdas:

```java
List<Apple> greenApples2 = filterApples(inventory, (Apple a) -> "green".equals(a.getColor()));

List<Apple> heavyApples2 = filterApples(inventory, (Apple a) -> a.getWeight() > 150);

List<Apple> weirdApples = filterApples(inventory, (Apple a) -> a.getWeight() < 80 ||
                                                               "brown".equals(a.getColor()));
```

But if such a lambda exceeds a few lines in length so that its behavior isn't instantly clear,
then you should use a method reference to a method with a descriptive name instead. Code clarity
should be your guide.

---

Streams
=======

Nearly every Java application *makes* and *processes* collections. But working with collections
isn't always ideal. For e.g. let's say you need to filter expensive transactions from a list and
then group them by currency. You'd need to write a lot of boilerplate code to implement this data
processing query, as shown here:

```java
Map<Currency, List<Transaction>> transactionsByCurrencies = new HashMap<>();
for (Transaction transaction : transactions) {
    if (transaction.getPrice() > 1000) {
        Currency currency = transaction.getCurrency();
        List<Transaction> transactionsForCurrency = transactionsByCurrencies.get(currency);
        if (transactionsForCurrency == null) {
            transactionsForCurrency = new ArrayList<>();
            transactionsByCurrencies.put(currency, transactionsForCurrency);
        }
        transactionsForCurrency.add(transaction);
    }
}
```

Using the streams API, you can solve this problem as follows:

```java
import static java.util.streams.Collectors.groupingBy;
Map<Currency, List<Transaction>> transactionsByCurrencies =
    transactions.stream()
                .filter((Transaction t) -> t.getPrice() > 1000)
                .collect(groupingBy(Transaction::getCurrency));
```

For now it's worth noting that the Streams API provides a very different way to process data in
comparison to the Collections API. Using a collection, you're managing the iteration process
yourself. You need to iterate through each element one by one using a `for-each` loop and then
process the elements. We call this way of iterating over data *external iteration*.

In contrast, using the Streams API, you don't need to think in terms of loops at all. The data
processing happens internally inside the library. We call this idea *internal iteration*.

---

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

---

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

I'll talk about this later, there are some restrictions that prevent issues such as the
infamous *diamond inheritance problem* in C++.

---

Other good ideas from functional programming
============================================

In Java 8 there's an `Optional<T>` class that, if used consistently, can help you avoid
`NullPointerExceptions`. It's a container object that may or may not contain a value. `Optional<T>`
also includes methods to explicitly deal with the case where a value is absent. In other words, it
uses the type system to allow you to indicate when a variable is anticipated to potentially have a
missing value.

Lambda Expressions
==================

A *lambda expression* can be understand as a concise representation of an anonymous function that
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

A:
```java
1. Function<String, Integer> stringToInteger = Integer::parseInt;
2. BiPredicate<List<String>, String> contains = List::contains;
```

Putting lambdas and method references into practice!
====================================================

We'll try refactoring the problem of sorting a list of `Apples` with different ordering
strategies and show how you can progressively evolve a naive solution into a concise one.

Step 1:
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

Step 2:
Use an anonymous class.

```java
inventory.sort(new Comparator<Apple>(){
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
});
```

Step 3:
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
inventory.sort(comparing((a) -> a.getWeight()));
```

Step 4:
Use method references:

```java
inventory.sort(comparing(Apple::getWeight));
```

Congratulations! This is your final solution! Why is this code better than code prior to Java 8?
It's obvious what it means, and the code reads like the problem statement "sort inventory comparing
the weight of the apples".
