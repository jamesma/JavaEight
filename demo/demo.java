Collections.sort(inventory, new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2) {
        return a1.getWeight().compareTo(a2.getWeight());
    }
});

inventory.sort(comparing(Apple::getWeight));

//
//
//

File[] hiddenFiles = new File(".").listFiles(new FileFilter() {
    public boolean accept(File file) {
        return file.isHidden();
    }
})

File[] hiddenFiles = new File(".").listFiles(File::isHidden);

//
//
//

public static List<Apple> filterGreenApples(List<Apple> inventory){
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory){
        if ("green".equals(apple.getColor())) {
            result.add(apple);
        }
    }
    return result;
}

// but next, somebody would like the list of heavy apples (say over 150g), and so, with a heavy
// heart, you'd write the following method to achieve this (perhaps even using copy and paste):

public static List<Apple> filterHeavyApples(List<Apple> inventory){
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory){
        if (apple.getWeight() > 150) {
            result.add(apple);
        }
    }
    return result;
}

// we all know the dangers of copy and paste for software engineering (updates and bug fixes to
// one variant but not the other), and hey, these two methods vary only in one line.
// but as we mentioned, java 8 makes it possible to pass the code of the condition as an argument,
// thus avoiding code duplication of the filter method. you can now write this:

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

// and to use this, you call either of the two below:

List<Apple> greenApples = filterApples(inventory, FilteringApples::isGreenApple);

List<Apple> heavyApples = filterApples(inventory, FilteringApples::isHeavyApple);

// passing methods as values is clearly useful, but it's a bit annoying having a write a definition
// for short methods such as isHeavyApple and isGreenApple when they're used perhaps only once/twice
// here's where you can use lambdas:

List<Apple> greenApples2 = filterApples(inventory, (Apple a) -> "green".equals(a.getColor()));

List<Apple> heavyApples2 = filterApples(inventory, (Apple a) -> a.getWeight() > 150);

List<Apple> weirdApples = filterApples(inventory, (Apple a) -> a.getWeight() < 80 ||
                                                               "brown".equals(a.getColor()));

// but if such a lambda exceeds a few lines in length so that its behavior isn't instantly clear,
// then you should use a method reference to a method with a descriptive name instead. code clarity
// should be your guide.

//
//
//

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

// using the streams API, you can solve this problem as follows:

import static java.util.streams.Collectors.groupingBy;
Map<Currency, List<Transaction>> transactionsByCurrencies =
    transactions.stream()
                .filter((Transaction t) -> t.getPrice() > 1000)
                .collect(groupingBy(Transaction::getCurrency));

// parallelism almost for free

import static java.util.streams.Collectors.groupingBy;
Map<Currency, List<Transaction>> transactionsByCurrencies =
    transactions.parallelStream() // s/stream/parallelStream/g
                .filter((Transaction t) -> t.getPrice() > 1000)
                .collect(groupingBy(Transaction::getCurrency));
