package ru.etu.astamir.common.test;

import com.google.common.collect.*;
import ru.etu.astamir.geom.common.Edge;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 03.02.14
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
public class Test {
    public static void main(String... args) throws IOException, PrinterException {
        Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.putAll(1, Lists.asList("one", "two", new String[]{"three"}));
        multimap.putAll(2, Lists.asList("one", "four", new String[]{"five"}));

        Table<Integer, Integer, Double> table = HashBasedTable.create();
        table.put(1, 1, 1.0);
        table.put(1, 2, 2.0);
        table.put(1, 3, 3.0);

        Edge edge = Edge.of(0, 0, 5, 0);
        edge.shrink(-2);
        System.out.println(edge);

    }
}
