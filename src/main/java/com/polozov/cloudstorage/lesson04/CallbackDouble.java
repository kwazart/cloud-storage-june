package com.polozov.cloudstorage.lesson04;

@FunctionalInterface
public interface CallbackDouble {
    String callDouble(String a, String b);

    default  void testCD() {
        System.out.println("default method of CallbackDouble");
    }
}
