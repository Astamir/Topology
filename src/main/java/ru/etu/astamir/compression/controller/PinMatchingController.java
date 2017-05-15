package ru.etu.astamir.compression.controller;

import com.google.common.primitives.Ints;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.TopologyCompressor;
import ru.etu.astamir.compression.TopologyParser;
import ru.etu.astamir.compression.commands.compression.ActiveBorder;
import ru.etu.astamir.compression.commands.compression.CompressContactCommand;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.compression.virtual.ConvertException;
import ru.etu.astamir.compression.virtual.Converter;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.gui.editor.MainFrame;
import ru.etu.astamir.launcher.VirtualTopology;
import ru.etu.astamir.math.MathUtils;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.technology.CMOSTechnology;
import ru.etu.astamir.model.technology.CharacteristicParser;
import ru.etu.astamir.model.technology.DefaultTechnologicalCharacteristics;
import ru.etu.astamir.model.technology.Technology;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by jesa on 24.02.2017.
 */
public class PinMatchingController {
    private MainFrame rightTopologyFrame;
    private MainFrame leftTopologyFrame;
    private static List<PinSimpleBean> currentProcessingPins = new ArrayList<>();
    private static List<PinSimpleBean> allProcessedPins = new ArrayList<>();

    public class PinSimpleBean {
        private String name;
        private Point coordinates;
        private Double constraint;

        public PinSimpleBean() {
            this.name = "-1";
        }

        public PinSimpleBean(String name, Point coordinates, Double constraint) {
            this.name = name;
            this.coordinates = coordinates;
            this.constraint = constraint;
        }

        public int compareTo(final @Nonnull PinSimpleBean p) {
            return this.coordinates.compareTo(p.coordinates);
        }

        public int compareToY(final @Nonnull PinSimpleBean p) {
            return this.coordinates.compareToY(p.coordinates);
        }

        @Override
        public String toString() {
            return "PinSimpleBean{" +
                    "name='" + name + '\'' +
                    ", coordinates=" + coordinates +
                    ", constraint=" + constraint +
                    '}';
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Point getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Point coordinates) {
            this.coordinates = coordinates;
        }

        public Double getConstraint() {
            return constraint;
        }

        public void setConstraint(Double constraint) {
            this.constraint = constraint;
        }
    }

    public static boolean pinProcessed = false;

    public PinMatchingController(MainFrame leftTopologyFrame, MainFrame rightTopologyFrame) {
        this.setLeftTopologyFrame(leftTopologyFrame);
        this.setRightTopologyFrame(rightTopologyFrame);
    }

    public PinMatchingController() {
    }

    public void matchCells() {
        TopologyCompressor compressorLeft, compressorRight;
        CompressContactCommand peekLeft, peekRight;
        Contact contactLeft, contactRight;
        Collection<Border> affectedBordersLeft, affectedBordersRight;
        Double distanceLeft = 0.0, distanceRight = 0.0;

        List<PinSimpleBean> leftPins, rightPins;
        //List<Map<Point, Double>> leftOutPins = new ArrayList<>();
        //List<Map<Point, Double>> rightOutPins = new ArrayList<>();

        //full compressing first
        this.leftTopologyFrame.convertAction();
        this.rightTopologyFrame.convertAction();
        /*this.leftTopologyFrame.fullCompress();
        this.rightTopologyFrame.fullCompress();*/

        //get pin coordinates
        //System.out.println(getPinsFromTopologyCompressor(this.leftTopologyFrame).toString());
        //System.out.println(getPinsFromTopologyCompressor(this.rightTopologyFrame).toString());
        leftPins = getPinsForMatching(getPinsFromTopologyCompressor(this.leftTopologyFrame), 0);
        rightPins = getPinsForMatching(getPinsFromTopologyCompressor(this.rightTopologyFrame), 1);
        pinProcessed = true;

        Method m = null;
        try {
            m = CompressContactCommand.class.getDeclaredMethod("getContactMoveDistance", Contact.class, Collection.class, Direction.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        m.setAccessible(true);

        for (int i = 0; i < leftPins.size(); i++) {
            //добавляем текущие пины одного уровня
            currentProcessingPins.add(leftPins.get(i));
            currentProcessingPins.add(rightPins.get(i));

            //начинаем сжатие, которое прервется на команде перемещения пина
            this.leftTopologyFrame.fullCompress();
            this.rightTopologyFrame.fullCompress();

            //получаем компрессоры и текущие команды
            compressorLeft = ProjectObjectManager.getCompressorsPool().getCompressor(this.leftTopologyFrame.getDefaultTopology());
            compressorRight = ProjectObjectManager.getCompressorsPool().getCompressor(this.rightTopologyFrame.getDefaultTopology());
            peekLeft = (CompressContactCommand) compressorLeft.commands.peek();
            peekRight = (CompressContactCommand) compressorRight.commands.peek();

            //получаем расстояние, на которое будет перемещаться пин (как в CompressContactCommand)
            contactLeft = (Contact) peekLeft.getElement();
            contactRight = (Contact) peekRight.getElement();
            affectedBordersLeft = peekLeft.getAffectedBorders();
            affectedBordersRight = peekRight.getAffectedBorders();
            try {
                distanceLeft = ((ActiveBorder) m.invoke(peekLeft, contactLeft, affectedBordersLeft, peekLeft.getDirection())).getLength();
                distanceRight = ((ActiveBorder) m.invoke(peekRight, contactRight, affectedBordersRight, peekRight.getDirection())).getLength();
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            if (distanceLeft - distanceRight < 0) {
                allProcessedPins.add(new PinSimpleBean(currentProcessingPins.get(0).getName(), currentProcessingPins.get(0).getCoordinates(), 0.0));
                allProcessedPins.add(new PinSimpleBean(currentProcessingPins.get(1).getName(), currentProcessingPins.get(1).getCoordinates(), distanceLeft - distanceRight));
            } else if (distanceLeft - distanceRight > 0) {
                allProcessedPins.add(new PinSimpleBean(currentProcessingPins.get(0).getName(), currentProcessingPins.get(0).getCoordinates(), distanceLeft - distanceRight));
                allProcessedPins.add(new PinSimpleBean(currentProcessingPins.get(1).getName(), currentProcessingPins.get(1).getCoordinates(), 0.0));
            } else {
                allProcessedPins.add(new PinSimpleBean(currentProcessingPins.get(0).getName(), currentProcessingPins.get(0).getCoordinates(), 0.0));
                allProcessedPins.add(new PinSimpleBean(currentProcessingPins.get(1).getName(), currentProcessingPins.get(1).getCoordinates(), 0.0));
            }

            //очищяем текущие пины
            currentProcessingPins.clear();

            //конвертируем топологию чтобы начать сжатие сначала
            this.leftTopologyFrame.convertAction();
            this.rightTopologyFrame.convertAction();
            this.leftTopologyFrame.convertAction();
            this.rightTopologyFrame.convertAction();
        }

        this.leftTopologyFrame.fullCompress();
        this.rightTopologyFrame.fullCompress();

    }

    private List<PinSimpleBean> getPinsFromTopologyCompressor(MainFrame topologyFrame) {
        List<PinSimpleBean> outPins = new ArrayList<>();
        TopologyCompressor compressor = ProjectObjectManager.getCompressorsPool().getCompressor(topologyFrame.getDefaultTopology());
        for (Map.Entry<TopologyElement, Integer> processedElements : compressor.getProcessedElements().entrySet()) {
            if (processedElements.getKey() instanceof Contact) {
                outPins.add(new PinSimpleBean(processedElements.getKey().getName(), processedElements.getKey().getCoordinates().iterator().next(), 0.0));
            }
        }
        return outPins;
    }

    /**
     * Возвращаем список пинов, которые необходимо стыковать, для правой и левой ячейки
     *
     * @param pins список всех пинов ячейки
     * @param mode 0 - обработка для  левой ячейки, 1 - для правой
     * @return
     */
    private List<PinSimpleBean> getPinsForMatching(List<PinSimpleBean> pins, int mode) {
        double[] xArray = new double[20];
        int i = 0;
        List<PinSimpleBean> outPins = new ArrayList<>();
        //собираем все координаты по x
        for (PinSimpleBean pointCollection : pins) {
            xArray[i] = pointCollection.coordinates.x();
            i++;
        }
        //отрезаем лишнее и сортируем
        xArray = Arrays.copyOfRange(xArray, 0, i);
        Arrays.sort(xArray);
        //проходим по всем, заполняем выходной массив в завистимости от мода (правее/левее середины)
        for (PinSimpleBean pointCollection : pins) {
            if (mode == 0) {
                if (pointCollection.coordinates.x() > (xArray[0] + xArray[xArray.length - 1]) / 2) {
                    outPins.add(new PinSimpleBean(pointCollection.name, pointCollection.coordinates, 0.0));
                }
            } else {
                if (pointCollection.coordinates.x() < (xArray[0] + xArray[xArray.length - 1]) / 2) {
                    outPins.add(new PinSimpleBean(pointCollection.name, pointCollection.coordinates, 0.0));
                }
            }
        }
        //System.out.println(outPins.toString());
        outPins.sort(PinSimpleBean::compareTo);
        return outPins;
    }

    public void multipleTopologiesCompression() throws IOException {
        //System.out.format("toString: ", Paths.get("").toAbsolutePath().toString());

        class PriorityBean {
            //массив приоритетов
            public int[] priority;// = new int[3];
            //массив ярусов для стыкуемых ячеек
            public int[] levels;// = new int[3];

            public PriorityBean(int num) {
                this.priority = new int[num - 1];
                this.levels = new int[num];
            }
        }

        int topologyListSize = 3; //temporary
        //кол-во ярусов в левой и правой обрабатываемых ячейках
        int leftLevelsCount = 0, rightLevelsCount = 0;

        //финальная таблица приоритетов
        int[][] outPriotityTable;

        PinSimpleBean leftPin, rightPin;
        PriorityBean priorityBean = new PriorityBean(topologyListSize);
        List<List<PinSimpleBean>> pinTable = new ArrayList<>();
        TopologyCompressor compressor;
        // текущий приоритет обработки
        int currentPriority = 0;

        for (int i = 0; i < topologyListSize; i++) {
            List<PinSimpleBean> pinList = new ArrayList<>();
            String fileName = Paths.get("").toAbsolutePath().toString() + File.separator + "topologies" + File.separator + "default_topology_" + (i + 1) + ".txt";
            File topologyFile = new File(fileName);
            if (topologyFile.exists()) {
                TopologyParser parser = new TopologyParser(topologyFile);
                parser.parse();
                VirtualGrid elements = parser.getElements();
                VirtualTopology topology = VirtualTopology.of(elements);
                Technology.TechnologicalCharacteristics.Base base = new DefaultTechnologicalCharacteristics();
                CharacteristicParser characteristicParser = new CharacteristicParser(new File("tehnol.txt"), base);
                characteristicParser.parse();
                topology.setTechnology(new CMOSTechnology("def", base));
                topology.setMode(VirtualTopology.VIRTUAL_MODE);
                topology.setVirtual(elements);
                Converter converter = new Converter(topology);
                try {
                    converter.convert();
                    topology.setMode(VirtualTopology.REAL_MODE);
                } catch (ConvertException e1) {
                    e1.printStackTrace();
                }
                compressor = ProjectObjectManager.getCompressorsPool().getCompressor(topology);
                compressor.compress();
                /*VirtualTopology default_topology = (VirtualTopology) project.getTopologies().get("default_topology");
                default_topology.setMode(VirtualTopology.VIRTUAL_MODE);
                default_topology.setVirtual(elements);*/
                for (Map.Entry<TopologyElement, Integer> processedElements : compressor.getProcessedElements().entrySet()) {
                    if (processedElements.getKey() instanceof Contact) {
                        pinList.add(new PinSimpleBean(processedElements.getKey().getName(), processedElements.getKey().getCoordinates().iterator().next(), 0.0));
                    }
                }
                /*for (TopologyElement element : *//*elements.getAllElements()*//*) {
                    if (element instanceof Contact) {
                        pinList.add(new PinSimpleBean(element.getName(), element.getCoordinates().iterator().next(), 0.0));
                    }
                }*/
                //заполняем кол-во ярусов
                if (pinList.isEmpty()) {
                    pinList.add(new PinSimpleBean());
                    priorityBean.levels[i] = 0;
                } else {
                    //рассмотрим 3 варианта - начало последовательности ячеек, середина и конец
                    if ((i == 0) || (i == (topologyListSize - 1))) {
                        //leftLevelsCount = getPinsForMatching(pinList, 0).size();
                        //continue;
                        priorityBean.levels[i] = 0;
                    } else if (i < (topologyListSize - 1)) {
                        leftLevelsCount = getPinsForMatching(pinList, 0).size();
                        rightLevelsCount = getPinsForMatching(pinList, 1).size();
                        priorityBean.levels[i] = Math.max(leftLevelsCount, rightLevelsCount);
                        //leftLevelsCount = getPinsForMatching(pinList, 0).size();
                    }/* else {
                        //continue;
                        *//*rightLevelsCount = getPinsForMatching(pinList, 1).size();
                        priorityBean.levels[i] = Math.max(leftLevelsCount, rightLevelsCount);*//*
                    }*/
                }
                pinList.sort(PinSimpleBean::compareToY);
                pinTable.add(i, pinList);
            }
        }
        //priority = new int[pinTable.size()];
        outPriotityTable = new int[Ints.max(priorityBean.levels)][priorityBean.priority.length];
        //проходим по всем ярусам
        for (int currentLevelCount = 0; currentLevelCount < Ints.max(priorityBean.levels); currentLevelCount++) {
            currentPriority = 0;
            //проходим по всей строке ячеек и расставляем приоритеты
            for (int currentCellCount = 0; currentCellCount < pinTable.size() - 1; currentCellCount++) {
                //обработаем ячейки, в которых нет контактов, а также первую ячейку
                if ((priorityBean.levels[currentCellCount] == 0) || (currentCellCount == 0)) {
                    priorityBean.priority[currentCellCount] = currentPriority;
                    continue;
                } else {
                    //проверим, что не превышаем счетчик ярусов для данной пары ячеек
                    if ((getPinsForMatching(pinTable.get(currentCellCount), 1).size() <= currentLevelCount) ||
                            (getPinsForMatching(pinTable.get(currentCellCount), 0).size() <= currentLevelCount)) {
                        priorityBean.priority[currentCellCount] = currentPriority;
                        continue;
                    }
                    //расставим приоритеты
                    leftPin = getPinsForMatching(pinTable.get(currentCellCount), 1).get(currentLevelCount);
                    rightPin = getPinsForMatching(pinTable.get(currentCellCount), 0).get(currentLevelCount);
                    if (leftPin.getCoordinates().y() > rightPin.getCoordinates().y()) {
                        priorityBean.priority[currentCellCount] = --currentPriority;
                    } else if (leftPin.getCoordinates().y() < rightPin.getCoordinates().y()) {
                        priorityBean.priority[currentCellCount] = ++currentPriority;
                    } else {
                        priorityBean.priority[currentCellCount] = currentPriority;
                    }
                }
                //нормализуем приоритеты
                int norm = Math.abs(Ints.min(priorityBean.priority));
                for (int i = 0; i < priorityBean.priority.length; i++) {
                    outPriotityTable[currentLevelCount][i] = priorityBean.priority[i] += norm;
                }

            }

            System.out.format("toString: ", Paths.get("").toAbsolutePath().toString());
        }
    }

    public MainFrame getRightTopologyFrame() {
        return rightTopologyFrame;
    }

    public void setRightTopologyFrame(MainFrame rightTopologyFrame) {
        this.rightTopologyFrame = rightTopologyFrame;
    }

    public MainFrame getLeftTopologyFrame() {
        return leftTopologyFrame;
    }

    public void setLeftTopologyFrame(MainFrame leftTopologyFrame) {
        this.leftTopologyFrame = leftTopologyFrame;
    }

    public static List<PinSimpleBean> getCurrentProcessingPins() {
        return currentProcessingPins;
    }

    /*public void setCurrentProcessingPins(List<Point> currentProcessingPins) {
        this.currentProcessingPins = currentProcessingPins;
    }*/

    public boolean isPinProcessed() {
        return pinProcessed;
    }

    /*public void setPinProcessed(boolean pinProcessed) {
        this.pinProcessed = pinProcessed;
    }*/

    public static List<PinSimpleBean> getAllProcessedPins() {
        return allProcessedPins;
    }
}
