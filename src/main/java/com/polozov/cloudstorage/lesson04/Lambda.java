package com.polozov.cloudstorage.lesson04;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Lambda {
    public static void main(String[] args) {
        // SAM-тип || замыкание
        // () -> sout  ..... ;
        // () -> {
        //     ...
        //     ...
        // };

        Callback c = new Callback() {
            @Override
            public void call(String value) {
                System.out.println(value);
            }
        };

        Callback c1 = value -> System.out.println(value);
        Callback c2 = System.out::println;

        System.out.println(c1.getClass());
        c.call("some text 0");
        c1.call("some text 1");
        c2.call("some text 2");

        Callback c3= Lambda::signal;
        c3.call("some text 3");

        // ============================================================

        CallbackDouble cd = new CallbackDouble() {
            @Override
            public String callDouble(String a, String b) {
                return String.format("Input-1: %s\tInput-2: %s", a, b);
            }
        };

        CallbackDouble cd1 = (a, b) -> String.format("Input-1: %s\tInput-2: %s", a, b);
        CallbackDouble cd2 = (a, b) -> a + " - " + b;

        System.out.println(cd.callDouble("test1", "test2"));
        System.out.println(cd1.callDouble("test3", "test4"));
        System.out.println(cd2.callDouble("test5", "test6"));

        // ============================================================

        Consumer<Integer> consumer = a -> {
            a++;
            System.out.println(a);
        };
        consumer = consumer.andThen(arg -> {
            arg *= 2;
            System.out.println(arg);
        });
        consumer.accept(10);


        Predicate<Integer> predicate = value -> value % 2 == 0;
        predicate = predicate.and(val -> val > 6).or(v -> v == 5);
        System.out.println(predicate.test(4));


        Function<Integer, String> converter = a -> "test".repeat(a);
        System.out.println(converter.apply(3));

        Function<String, Integer> converterStringToInt = arg -> arg.length();
        System.out.println(converterStringToInt.apply("test"));


        Supplier<List<Integer>> getList = ArrayList::new;
        System.out.println(getList.get());
    }

    private static void signal(String text) {
        System.out.println("!!! " + text + " !!!");
    }
}
