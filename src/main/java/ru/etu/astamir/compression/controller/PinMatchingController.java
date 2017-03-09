package ru.etu.astamir.compression.controller;

import ru.etu.astamir.compression.TopologyCompressor;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.gui.editor.MainFrame;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.contacts.Contact;

import java.util.*;

/**
 * Created by jesa on 24.02.2017.
 */
public class PinMatchingController {
    private MainFrame rightTopologyFrame;
    private MainFrame leftTopologyFrame;
    private static List<Map<Point, Double>> currentProcessingPins;

    public static boolean pinProcessed = false;

    public PinMatchingController(MainFrame leftTopologyFrame, MainFrame rightTopologyFrame) {
        this.setLeftTopologyFrame(leftTopologyFrame);
        this.setRightTopologyFrame(rightTopologyFrame);
    }

    public void matchCells() {
        List<Point> leftPins = new ArrayList<>();
        List<Point> rightPins = new ArrayList<>();
        List<Map<Point, Double>> leftOutPins = new ArrayList<>();
        List<Map<Point, Double>> rightOutPins = new ArrayList<>();
        //full compressing first
        this.leftTopologyFrame.convertAction();
        this.rightTopologyFrame.convertAction();
        this.leftTopologyFrame.fullCompress();
        this.rightTopologyFrame.fullCompress();
        //get pin coordinates
        //System.out.println(getPinsFromTopologyCompressor(this.leftTopologyFrame).toString());
        //System.out.println(getPinsFromTopologyCompressor(this.rightTopologyFrame).toString());
        leftPins = getPinsForMatching(getPinsFromTopologyCompressor(this.leftTopologyFrame), 0);
        rightPins = getPinsForMatching(getPinsFromTopologyCompressor(this.rightTopologyFrame), 1);

        //create matching query
        pinProcessed = true;
        for (int i = 0; i < leftPins.size(); i++) {
            Map<Point, Double> currPointLeft = new HashMap<>();
            Map<Point, Double> currPointRight = new HashMap<>();
            if (leftPins.get(i).y()-rightPins.get(i).y() < 0) {
                currPointLeft.put(leftPins.get(i), (double) 0);
                leftOutPins.add(currPointLeft);
                currPointRight.put(rightPins.get(i), leftPins.get(i).y()-rightPins.get(i).y());
                rightOutPins.add(currPointRight);
            } else if (leftPins.get(i).y()-rightPins.get(i).y() > 0) {
                currPointLeft.put(leftPins.get(i), (double) 0);
                leftOutPins.add(currPointLeft);
                currPointRight.put(rightPins.get(i), leftPins.get(i).y()-rightPins.get(i).y());
                rightOutPins.add(currPointRight);
            }
            else {
                currPointLeft.put(leftPins.get(i), (double) 0);
                leftOutPins.add(currPointLeft);
                currPointRight.put(rightPins.get(i), (double) 0);
                rightOutPins.add(currPointRight);
            }
            //if ()
//            System.out.println("//////"+leftPins.get(i).compareTo(rightPins.get(i)));
//            System.out.println("//////"+Point.distance(leftPins.get(i), rightPins.get(i)));
            //System.out.println("//////"+(leftPins.get(i).y()-rightPins.get(i).y()));
        }

        //compress again
        this.leftTopologyFrame.convertAction();
        this.rightTopologyFrame.convertAction();
        this.leftTopologyFrame.convertAction();
        this.rightTopologyFrame.convertAction();

        currentProcessingPins = leftOutPins;
        this.leftTopologyFrame.fullCompress();
        currentProcessingPins = rightOutPins;
        this.rightTopologyFrame.fullCompress();
    }

    private List<Collection<Point>> getPinsFromTopologyCompressor(MainFrame topologyFrame) {
        List<Collection<Point>> outPins = new ArrayList<>();
        TopologyCompressor compressor = ProjectObjectManager.getCompressorsPool().getCompressor(topologyFrame.getDefaultTopology());
        for (Map.Entry<TopologyElement, Integer> processedElements : compressor.getProcessed_elements().entrySet()) {
            if (processedElements.getKey() instanceof Contact) {
                outPins.add(processedElements.getKey().getCoordinates());
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
    private List<Point> getPinsForMatching(List<Collection<Point>> pins, int mode) {
        double[] xArray = new double[20];
        int i = 0;
        List<Point> outPins = new ArrayList<>();
        //собираем все координаты по x
        for (Collection<Point> pointCollection : pins) {
            for (Point point : pointCollection) {
                xArray[i] = point.x();
                i++;
            }
        }
        //отрезаем лишнее и сортируем
        xArray = Arrays.copyOfRange(xArray, 0, i);
        Arrays.sort(xArray);
        //проходим по всем, заполняем выходной массив в завистимости от мода (правее/левее середины)
        for (Collection<Point> pointCollection : pins) {
            for (Point point : pointCollection) {
                if (mode == 0) {
                    if (point.x() > (xArray[0] + xArray[xArray.length - 1]) / 2) {
                        outPins.add(point);
                    }
                } else {
                    if (point.x() < (xArray[0] + xArray[xArray.length - 1]) / 2) {
                        outPins.add(point);
                    }
                }
            }
        }
        //System.out.println(outPins.toString());
        outPins.sort(Point::compareTo);
        return outPins;
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

    public static List<Map<Point, Double>> getCurrentProcessingPins() {
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
}
