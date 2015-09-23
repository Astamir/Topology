package ru.etu.astamir.model.contacts;

import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.legacy.Bus;
import ru.etu.astamir.model.legacy.LegacyContact;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 28.06.12
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */
public class EquipotentialContact extends LegacyTopologyElement implements Connector, Movable{
    private LegacyContact one;
    private LegacyContact another;

    protected EquipotentialContact(TopologyLayer layer) {
        super(layer);
    }

    /**
     * Подсоединить элемент. Все элементы, к которым с которыми
     * соединитель может соединятся напрямую это шины(или затворы).
     *
     * @param element Элемент, который мы соединяем с нашим коннектором.
     */
    @Override
    public void connect(Bus element) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Отсоединить элемент.
     *
     * @param element Элемент, который мы хотим отсоединить.
     */
    @Override
    public void disconnect(Bus element) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    public void draw(Graphics2D g) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Перемещение объекта с заданным смещением по двум координатам.
     *
     * @param dx Смещение по оси абсцисс.
     * @param dy Смещение по оси ординат.
     * @return true, если получилось сместить объект.
     */
    @Override
    public boolean move(double dx, double dy) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
