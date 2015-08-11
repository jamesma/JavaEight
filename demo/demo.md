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
