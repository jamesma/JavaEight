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
