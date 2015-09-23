package ru.etu.astamir.model.technology;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author Astamir
 */
public class CharacteristicParser {
    private static final Pattern HAS_SPACES_PATTERN = Pattern.compile("\\s+");
    private static final String OTHER_PROPERTIES_TOKEN = "other";
    private static final String[] DISTANCE_TOKENS = {"_D_SLICE", "_D_SLICE_", "D_SLICE", "D_SLICE_"};
    private static final String[] DISTANCE_EQUIPOTENTIAL_TOKENS = {"_DE_SLICE", "_DE_SLICE_", "DE_SLICE", "DE_SLICE_"};
    private static final String[] OVERLAP_TOKENS = {"_P_SLICE", "_P_SLICE_", "P_SLICE", "P_SLICE_"};
    private static final String[] INCLUDE_TOKENS = {"_I_SLICE", "_I_SLICE_", "I_SLICE", "I_SLICE_"};
    private static final String[] INCLUDE_EQUIPOTENTIAL_TOKENS = {"_IE_SLICE", "_IE_SLICE_", "IE_SLICE", "IE_SLICE_"};
    private static final String[] WIDTH_TOKENS = {"_W_SLICE", "_W_SLICE_", "W_SLICE", "W_SLICE_"};
    private static final String[] WIDTH_ALTERNATIVE_TOKENS = {"_W2_SLICE", "_W2_SLICE_", "W2_SLICE", "W2_SLICE_"};
    private static final String[] SYMBOL_TOKENS = {"_SLICES", "_SLICES_", "SLICES", "SLICES_"};

    private File file;

    private Table<String, String, Double> distances = HashBasedTable.create();
    private Table<String, String, Double> overlaps = HashBasedTable.create();
    private Table<String, String, Double> includes = HashBasedTable.create();
    private Map<String, Double> widths = Maps.newHashMap();
    private Map<String, Double> alternativeWidths = Maps.newHashMap();
    private Map<String, Double> symbols = Maps.newHashMap();
    private Map<String, String> other = Maps.newHashMap();

    private Technology.TechnologicalCharacteristics.Base characteristics;

    public CharacteristicParser(File file, Technology.TechnologicalCharacteristics.Base characteristics) {
        this.file = file;
        this.characteristics = Preconditions.checkNotNull(characteristics);
    }

    public void parse() throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            Scanner scanner = new Scanner(inputStream);
            Map<String, Map<String, String>> properties = Maps.newLinkedHashMap();
            Map<String, String> curMap = Maps.newHashMap();
            String curProperty = OTHER_PROPERTIES_TOKEN;
            properties.put(curProperty, curMap);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.contains("//")) { // Вырезать комментарии
                    line = line.substring(0, line.indexOf("//")).trim();
                }

                line = line.replaceAll("\uFEFF", ""); // Удалить символ пустой строки, который считаются за символ(иногда встречается)

                if (line.isEmpty()) { // Пропускаем пустые строки
                    continue;
                }

                if (!HAS_SPACES_PATTERN.matcher(line).find()) { // Новый список свойств
                    curProperty = line;
                    curMap = Maps.newHashMap();
                    properties.put(curProperty, curMap);
                } else { // Свойства
                    line = line.replaceAll("_", "");
                    String[] tokens = line.split("\\s+");
                    if (tokens.length < 2) {
                        continue;
                    }

                    if (tokens.length == 2) {
                        curMap.put(tokens[0], tokens[1]);
                    } else {
                        StringBuilder key = new StringBuilder();
                        for (int i = 0; i < tokens.length - 1; i++) {
                            key.append(tokens[i]);
                            if (i != tokens.length - 2) {
                                key.append(",");
                            }
                        }

                        curMap.put(key.toString(), tokens[tokens.length - 1]);
                    }
                }
            }

            putProperties(properties); // Кладем свойства по разным мапам.
        }
    }

    private void putProperties(Map<String, Map<String, String>> properties) throws NumberFormatException {
        // _SLICES - названия слоев
        fillPropertyMap(properties, symbols, SYMBOL_TOKENS);
        characteristics.setSymbols(symbols);

        // W_SLICE - топлогические ширины
        fillPropertyMap(properties, widths, WIDTH_TOKENS);
        characteristics.setWidths(widths);

        // W2_SLICE - ТОПОЛОГИЧЕСКАЯ ШИРИНА - альтернативный размер в контактах
        fillPropertyMap(properties, alternativeWidths, WIDTH_ALTERNATIVE_TOKENS);
        characteristics.setAlternativeWidths(alternativeWidths);

        // Перекрытия
        fillPropertyTable(properties, overlaps, OVERLAP_TOKENS);
        characteristics.setOverlaps(overlaps);

        // Включения
        fillPropertyTable(properties, includes, INCLUDE_TOKENS);

        // Включения эквипотенциальные
        fillPropertyTable(properties, includes, INCLUDE_EQUIPOTENTIAL_TOKENS);
        characteristics.setIncludes(includes);

        // D_SLICE - расстояния
        fillPropertyTable(properties, distances, DISTANCE_TOKENS);

        //DE_SLICE - эквипотеницальные расстояния
        fillPropertyTable(properties, distances, DISTANCE_EQUIPOTENTIAL_TOKENS);
        characteristics.setDistances(distances);

        // Другие свойства
        fillCommonPropertyMap(properties, other, new String[]{OTHER_PROPERTIES_TOKEN});
        characteristics.setOther(other);
    }

    private Optional<Map<String, String>> findProperty(Map<String, Map<String, String>> properties, String[] tokens) {
        for (String token : tokens) {
            if (properties.containsKey(token)) {
                return Optional.of(properties.get(token));
            }
        }

        return Optional.absent();
    }

    private void fillPropertyTable(Map<String, Map<String, String>> properties, Table<String, String, Double> toFill, String[] tokens) {
        Preconditions.checkNotNull(toFill);
        Optional<Map<String, String>> property = findProperty(properties, tokens);
        if (property.isPresent()) {
            Map<String, String> distance = property.get();
            for (Map.Entry<String, String> entry : distance.entrySet()) {
                String[] elements = entry.getKey().split(",");
                if (elements.length != 2) {
                    continue;
                }

                toFill.put(elements[0], elements[1], Double.parseDouble(entry.getValue()));
            }
        }
    }

    private void fillPropertyMap(Map<String, Map<String, String>> properties, Map<String, Double> toFill, String[] tokens) {
        Preconditions.checkNotNull(toFill);
        Optional<Map<String, String>> property = findProperty(properties, tokens);
        if (property.isPresent()) {
            Map<String, String> prop = property.get();
            for (Map.Entry<String, String> entry : prop.entrySet()) {
                toFill.put(entry.getKey(), Double.parseDouble(entry.getValue()));
            }
        }
    }

    private void fillCommonPropertyMap(Map<String, Map<String, String>> properties, Map<String, String> toFill, String[] tokens) {
        Preconditions.checkNotNull(toFill);
        Optional<Map<String, String>> property = findProperty(properties, tokens);
        if (property.isPresent()) {
            toFill.putAll(property.get());
        }
    }

    public static void main(String... args) throws IOException {
        CharacteristicParser parser = new CharacteristicParser(new File("/Users/amonko/tech.txt"), null);
        parser.parse();
    }
}
