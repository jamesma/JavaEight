package com.james.practice.futures;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;

public class Shop {
    private static final Random random = new Random();

    private final String name;

    public Shop(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private double calculatePrice(String product) {
        Delays.delay();
        return random.nextDouble() * product.charAt(0) + product.charAt(1);
    }

    public double getPrice(String product) {
        return calculatePrice(product);
    }

    public Future<Double> getPriceAsync(String product) {
        return CompletableFuture.supplyAsync(() -> calculatePrice(product));
    }

    public List<String> findPrices(String product) {
        List<Shop> shops = Arrays.asList(new Shop("BestPrice"),
                                         new Shop("LetsSaveBig"),
                                         new Shop("MyFavoriteShop"),
                                         new Shop("BuyItAll"));

        List<CompletableFuture<String>> priceFutures =
            shops.stream()
                 .map(s ->
                          CompletableFuture.supplyAsync(() -> s.getName() + " price is " + s.getPrice(product),
                                                        Executors.EXECUTOR))
                 .collect(toList());

        return priceFutures.stream()
                           .map(CompletableFuture::join)
                           .collect(toList());
    }

    public static void main(String[] args) {
        Shop shop = new Shop("BestShop");
        long start = System.nanoTime();

        Future<Double> futurePrice = shop.getPriceAsync("my favorite product");
        long invocationTime = ((System.nanoTime()) - start) / 1_000_000;
        System.out.println("Invocation returned after " + invocationTime + " ms");
    }
}
