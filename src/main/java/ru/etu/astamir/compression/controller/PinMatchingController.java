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

    public PinMatchingController(MainFrame leftTopologyFrame, MainFrame rightTopologyFrame) {
        this.setLeftTopologyFrame(leftTopologyFrame);
        this.setRightTopologyFrame(rightTopologyFrame);
    }

    public void matchCells() {
        List<Point> leftPins = new ArrayList<>();
        List<Point> rightPins = new ArrayList<>();
        //full compressing first
        this.leftTopologyFrame.convertAction();
        this.rightTopologyFrame.convertAction();
        this.leftTopologyFrame.fullCompress();
        this.rightTopologyFrame.fullCompress();
        //get pin coordinates
        leftPins = getPinsForMatching(getPinsFromTopologyCompressor(this.leftTopologyFrame), 0);
        rightPins = getPinsForMatching(getPinsFromTopologyCompressor(this.rightTopologyFrame), 1);

        //create matching query

        //compress again

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
        for (Collection<Point> pointCollection : pins) {
            for (Point point : pointCollection) {
                xArray[i] = point.x();
                i++;
            }
        }
        xArray = Arrays.copyOfRange(xArray, 0, i);
        Arrays.sort(xArray);
        for (Collection<Point> pointCollection : pins) {
            for (Point point : pointCollection) {
                if (mode == 0) {
                    if (point.x() > (xArray[0] + xArray[xArray.length-1]) / 2) {
                        outPins.add(point);
                    }
                } else {
                    if (point.x() < (xArray[0] + xArray[xArray.length-1]) / 2) {
                        outPins.add(point);
                    }
                }
            }
        }
        System.out.println(outPins.toString());
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
}
