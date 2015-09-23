package ru.etu.astamir.compression;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.technology.ElementFactory;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.model.wires.Gate;
import ru.etu.astamir.model.wires.Wire;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Artem Mon'ko
 */
public class TopologyParser {
    private static final String SEPARATOR_PATTERN = ";,";
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("([\\w]+)\\(([\\s*\\d+\\.?\\d*\\s*["+ SEPARATOR_PATTERN +"]?]*)\\)?");
    private File file;
    private List<Pair<String, List<Double>>> symbols = Lists.newArrayList();

    public TopologyParser(File file) {
        this.file = file;
    }

    public void parse() throws IOException{
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            Scanner scanner = new Scanner(inputStream);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.contains("//")) { // Вырезать комментарии
                    line = line.substring(0, line.indexOf("//")).trim();
                }

                line = line.replaceAll("\uFEFF", ""); // Удалить символ пустой строки, который считаются за символ(иногда встречается)

                if (line.isEmpty()) { // Пропускаем пустые строки
                    continue;
                }

                symbols.addAll(parseSymbolLine(line)); // кидаем все в одну кучу.
            }
        }

        System.out.println(symbols);
    }

    public void write(VirtualGrid grid) throws IOException {
        Collection<TopologyElement> elements = grid.getAllElements();

        try (PrintWriter writer = new PrintWriter(file)) {
            for (TopologyElement element : elements) {
                writer.print(element.getSymbol() + "(");
                writer.print(Joiner.on(",").join(Iterables.concat(Iterables.transform(element.getCoordinates(), new Function<Point, List<Double>>() {
                    @Override
                    public List<Double> apply(Point input) {
                        return Lists.newArrayList(input.x(), input.y());
                    }
                }))));
                if (element instanceof Wire && !(element instanceof Gate)) {
                    Wire wire = (Wire) element;
                    writer.print("," + wire.getWidth());
                }
                writer.print(")");
                writer.println();
            }
        }
    }

    public VirtualGrid getElements() {
        VirtualGrid grid = new VirtualGrid();
        List<TopologyElement> elements = Lists.newArrayList();
        for (Pair<String, List<Double>> symbol : symbols) {
            List<Point> points = toPoints(symbol.right);
            ElementFactory elementFactory = ProjectObjectManager.getElementFactory();
            grid.addElement(elementFactory.getElement(symbol.left, points.toArray(new Point[points.size()]), new HashMap<String, Object>()));
        }

        return grid;
    }

    private void combineWires() {
        // несколько шин в одну большую
    }

    private void combineContacts() {
        // контакты и контактные окна
    }

    private void initializeRegions() {
        // проставить пренадлежность элементов к областям
    }

    private static List<Pair<String, List<Double>>> parseSymbolLine(String line) throws NumberFormatException {
        List<Pair<String, List<Double>>> result = Lists.newArrayList();
        Matcher matcher = SYMBOL_PATTERN.matcher(line);
        while (matcher.find()) {
            String symbol = matcher.group(1);
            String[] coordinateString = matcher.group(2).split("[;,]");
            List<Double> coordinates = Lists.newArrayList(Lists.transform(Arrays.asList(coordinateString), new Function<String, Double>() {
                @Override
                public Double apply(String input) {
                    return Double.parseDouble(input);
                }
            }));
            result.add(Pair.of(symbol, coordinates));
        }

        return result;
    }

    private static List<Point> toPoints(List<Double> coordinates) {
        List<Point> result = Lists.newArrayList();
        int size = coordinates.size();
        if (size < 2) { // we have no coordinates here
            return Collections.emptyList();
        }

        if (Utils.isOdd(size)) { // we need to have even number of coordinates
            size--;
        }

        for (int i = 0; i < size; i+=2) {
            result.add(Point.of(coordinates.get(i), coordinates.get(i + 1)));
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        TopologyParser parser = new TopologyParser(new File("/Users/amonko/Dropbox/ATopology/topology.txt"));
        parser.parse();
    }
}
