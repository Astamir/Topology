package ru.etu.astamir.common.test;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 03.02.14
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    void foo(List<? super Number> list) {
        list.add(1);
        System.out.println("linked ");
    }

    public static void main(String... args) {
        double d1 = 4.64;
        double d2 = 2;
        double d3 = d1 - d2;
        BigDecimal d4 = BigDecimal.valueOf(d1);
        BigDecimal d5 = BigDecimal.valueOf(d2);
        BigDecimal d6 = BigDecimal.valueOf(d3);
        d6 = d6.setScale(2, BigDecimal.ROUND_HALF_UP);
        System.out.println(d1 - d2 == d3);
        System.out.println(d4.subtract(d5).equals(d6));
    }
}
