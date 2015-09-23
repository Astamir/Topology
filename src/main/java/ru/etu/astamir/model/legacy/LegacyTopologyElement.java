package ru.etu.astamir.model.legacy;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.ColorAdapter;
import ru.etu.astamir.StrokeAdapter;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.graphics.StrokeFactory;
import ru.etu.astamir.model.*;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Comparator;

/**
 * Элемент топологии. Некоторый абстрактный класс, который умеет
 * себя отрисовывать на чертеже. Все, что будет подвержену анализу,
 * должно реализовывать этот интерфейс. Так как у нас инвариантная
 * топология, то поидее элемент должен знать свое место в виртуальной
 * сетке.
 */
public abstract class LegacyTopologyElement implements Drawable, Cloneable {
    /**
     * Координаты элемента в сетке.
     */
    private Point coordinates;

    /**
     * Граница элемента. Для отрисовки.
     */
    private Polygon bounds = new Polygon();

    /**
     * Слой, к которому принадлежит элемент.
     */
    private TopologyLayer layer = ProjectObjectManager.getLayerFactory().createDefaultTopologyLayer();

    /**
     * Материал элемента. Часто нам он не важен, либо совпадает с
     * материалом слоя.
     */
    private Material material = Material.UNKNOWN;

    /**
     * Тип проводимости элемента.
     */
    private ConductionType conductionType = ConductionType.UNKNOWN;

    /**
     * Цвет элемента.
     */
    private Color color = Color.BLACK;

    /**
     * Способ отрисовки граничных линий элемента.
     */
    private Stroke stroke = StrokeFactory.defaultStroke();

    /**
     * Способ отрисовки осевых линий или точек центра.
     */
    private Stroke sketchStroke = StrokeFactory.dashedStroke();


    private String name;

    private boolean isEmpty = false;

    private Class<? extends LegacyTopologyElement> clazz = null;

    protected LegacyTopologyElement() {
        coordinates = Point.of(-1, -1);
		name = String.valueOf(System.identityHashCode(this));
    }

    protected LegacyTopologyElement(int x, int y, TopologyLayer layer) {
        this.coordinates = new Point(x, y);
        this.layer = layer;
    }

    protected LegacyTopologyElement(Point coordinates, TopologyLayer layer) {
        this.coordinates = coordinates;
        this.layer = layer;
    }

    protected LegacyTopologyElement(Point coordinates, TopologyLayer layer, Polygon bounds) {
        this.coordinates = coordinates;
        this.bounds = bounds;
        this.layer = layer;
    }

    protected LegacyTopologyElement(TopologyLayer layer) {
        this(-1, -1, layer);
    }

    @XmlID
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActualClass(Class<? extends LegacyTopologyElement> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends LegacyTopologyElement> getActualClass() {
        return clazz != null ? clazz : getClass();
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public ConductionType getConductionType() {
        return conductionType;
    }

    public void setConductionType(ConductionType conductionType) {
        this.conductionType = conductionType;
    }

    /**
     * Координата х элемента.
     * @return
     */
    public double gridX() {
        return coordinates.x();
    }

    /**
     * Координата у элемента.
     * @return
     */
    public double gridY() {
        return coordinates.y();
    }

    public static Comparator<LegacyTopologyElement> verticalComparator() {
        return new Comparator<LegacyTopologyElement>() {
            @Override
            public int compare(LegacyTopologyElement one, LegacyTopologyElement another) {
                if (one != null && another != null) {
                    double oneX = one.coordinates.x();
                    double anotherX = another.coordinates.x();

                    return Doubles.compare(oneX, anotherX);
                }

                return 0;
            }
        };
    }

    public static Comparator<LegacyTopologyElement> horizontalComparator() {
        return new Comparator<LegacyTopologyElement>() {
            @Override
            public int compare(LegacyTopologyElement one, LegacyTopologyElement another) {
                if (one != null && another != null) {
                    double oneY = one.coordinates.y();
                    double anotherY = another.coordinates.y();

                    return Doubles.compare(oneY, anotherY);
                }

                return 0;
            }
        };
    }

    public static Comparator<LegacyTopologyElement> gridComparator() {
        return new Comparator<LegacyTopologyElement>() {
            @Override
            public int compare(LegacyTopologyElement one, LegacyTopologyElement another) {
                if (one != null && another != null) {
                    one.coordinates.compareTo(another.coordinates);
                }

                return 0;
            }
        };
    }

    /**
     * Установка координат. Не стоит вызывать напрямую, сетка должна контролировать изменение
     * координат в элементе.
     *
     * @param x
     * @param y
     */
    public void setCoordinates(double x, double y) {
        coordinates.setPoint(x, y);
    }

    public Point getCoordinates() {
        return Point.of(coordinates.x(), coordinates.y());
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public TopologyLayer getLayer() {
        return layer;
    }

    public void setLayer(TopologyLayer layer) {
        this.layer = layer;
    }

    /**
     * Границы элемента. Надо переопределять в каждой новой сущности,
     * @return
     */
    public Polygon getBounds() {
        return bounds;
    }

    public void setBounds(Polygon bounds) {
        this.bounds = bounds;
    }

    @XmlJavaTypeAdapter(ColorAdapter.class)
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @XmlJavaTypeAdapter(StrokeAdapter.class)
    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    @XmlJavaTypeAdapter(StrokeAdapter.class)
    public Stroke getSketchStroke() {
        return sketchStroke;
    }

    public void setSketchStroke(Stroke sketchStroke) {
        this.sketchStroke = sketchStroke;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    /**
     * Те слои топологии, которые нас как-то затрагивают.
     * То есть, элементы которых препятствуют движению нашего элемента.
     *
     * @return Затрагиваемые этим элементом слои топологии.
     */
    public ImmutableList<TopologyLayer> affectedLayers() {
        return ImmutableList.of(layer);
    }
}
