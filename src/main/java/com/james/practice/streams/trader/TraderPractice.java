package com.james.practice.streams.trader;

import java.util.Arrays;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class TraderPractice {
    Trader raoul = new Trader("Raoul", "Cambridge");
    Trader mario = new Trader("Mario", "Milan");
    Trader alan = new Trader("Alan", "Cambridge");
    Trader brian = new Trader("Brian", "Cambridge");

    List<Transaction> transactions = Arrays.asList(
        new Transaction(brian, 2011, 300),
        new Transaction(raoul, 2012, 1000),
        new Transaction(raoul, 2011, 400),
        new Transaction(mario, 2012, 710),
        new Transaction(mario, 2012, 700),
        new Transaction(alan, 2012, 950)
    );

    private void process() {
        // 1.
        List<Transaction> ans1 =
            transactions.stream()
                        .filter(t -> t.getYear() == 2011)
                        .sorted(comparing(Transaction::getValue))
                        .collect(toList());

        System.out.println("ans1");
        System.out.println(ans1);

        // 2.
        List<String> ans2 =
            transactions.stream()
                        .map(t -> t.getTrader().getCity())
                        .distinct()
                        .collect(toList());

        System.out.println("ans2");
        System.out.println(ans2);

        // 3.
        List<Trader> ans3 =
            transactions.stream()
                        .map(Transaction::getTrader)
                        .filter(trader -> trader.getCity().equals("Cambridge"))
                        .distinct()
                        .sorted(comparing(Trader::getName))
                        .collect(toList());

        System.out.println("ans3");
        System.out.println(ans3);

        // 4.
        String ans4 =
            transactions.stream()
                        .map(t -> t.getTrader().getName())
                        .distinct()
                        .sorted()
                        .collect(joining());

        System.out.println("ans4");
        System.out.println(ans4);

        // 5.
        boolean ans5 =
            transactions.stream()
                        .anyMatch(transaction -> transaction.getTrader().getCity().equals("Milan"));

        System.out.println("ans5");
        System.out.println(ans5);

        // 6.
        System.out.println("ans6");
        transactions.stream()
                    .filter(transaction -> transaction.getTrader().getCity().equals("Cambridge"))
                    .map(Transaction::getValue)
                    .forEach(System.out::println);

        // 7.
        int ans7 =
            transactions.stream()
                        .map(Transaction::getValue)
                        .reduce(Integer::max)
                        .get();

        System.out.println("ans7");
        System.out.println(ans7);

        Transaction ans8 =
            transactions.stream()
                        .min(comparing(Transaction::getValue))
                        .get();

        System.out.println("ans8");
        System.out.println(ans8);
    }

    public static void main(String[] args) {
        TraderPractice traderPractice = new TraderPractice();
        traderPractice.process();
    }
}
