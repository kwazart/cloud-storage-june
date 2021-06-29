package com.polozov.cloudstorage.lesson04;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Streams {
    public static void main(String[] args) throws IOException {
        List<Developer> developerList = new ArrayList<>();
        developerList.add(new Developer("Alex", "Java", 4000));
        developerList.add(new Developer("Peter", "Python", 3000));
        developerList.add(new Developer("Michel", "PHP", 5000));
        developerList.add(new Developer("John", "Java", 4500));
        developerList.add(new Developer("Perry", "C++", 4100));
        developerList.add(new Developer("Vivien", "PHP", 3500));
        developerList.add(new Developer("Tomas", "Java", 4200));
        developerList.add(new Developer("Alisa", "Python", 3900));
        developerList.add(new Developer("Barry", "Java", 4000));
        developerList.add(new Developer("Bob", "C++", 4600));
        developerList.add(new Developer("Tom", "Python", 3600));

        List<Developer> filteredDeveloperList = new ArrayList<>();
        for (Developer developer : developerList) {
            if ("Java".equals(developer.getLanguage()) && developer.getSalary() > 4100) {
                filteredDeveloperList.add(developer);
            }
        }

        for (Developer developer : filteredDeveloperList) {
            System.out.println(developer);
        }


        List<Developer> javaDevs = developerList.stream()
                .filter(p -> "Java".equals(p.getLanguage()))
                .filter(d -> d.getSalary() > 4100)
                .collect(Collectors.toList());
        javaDevs.forEach(System.out::println);

        System.out.println("----------------------------------------");

        developerList.stream()
                .filter(k -> "Python".equals(k.getLanguage()))
                .sorted(Comparator.comparingInt(Developer::getSalary))
                .forEach(System.out::println);

        System.out.println("----------------------------------------");

        developerList.stream()
                .filter(k -> "C++".equals(k.getLanguage()))
                .map(Developer::getName)
                .sorted()
                .forEach(System.out::println);

        System.out.println("----------------------------------------");

        // Конвейерные методы:
        // filter
        // map
        // mapToDouble
        // mapToInt
        // mapToLong
        // sorted
        // skip
        // distinct
        // peek
        // limit
        // flatMap
        // flatMapToInt
        // flatMapToDouble
        // flatMapToLong

        // Терминальные методы
        // collect
        // count
        // max
        // reduce
        // toArray
        // findFirst <-----
        // findAny <-----
        // anyMatch <-----
        // noneMatch <-----
        // allMatch <-----
        // forEach
        // forEachOrdered

        // sum
        // average

        // Создание стримов
        List<String> strings = Arrays.asList("one", "two", "three");
        strings.stream();

        Stream<String> one = Stream.of("one", "two", "three");

        Stream<String> stream = Arrays.stream(new String[]{"one", "two", "three"});

        Stream<String> server = Files.lines(Path.of("server", "1.txt"));

        IntStream chars = "123".chars();
        IntStream chars1 = "one".chars();

        Stream<Object> build = Stream.builder()
                .add("one")
                .add("two")
                .build();

        Map<String, Integer> textMap = Files.newBufferedReader(Path.of("server", "1.txt")).lines()
                .flatMap(line -> Arrays.stream(line.split(" +")))
                .map(v -> v.replaceAll("[?!;:,.—]", "").toLowerCase(Locale.ROOT))
                .filter(line -> !line.isBlank())
                .sorted(Comparator.reverseOrder())
//                .distinct()
                .collect(Collectors.toMap(Function.identity(), value -> 1, Integer::sum));

//        System.out.println(textMap);

        textMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((o1, o2) -> o2 - o1))
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
}
