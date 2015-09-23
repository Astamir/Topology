package ru.etu.astamir.model.legacy;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.graphics.StrokeFactory;
import ru.etu.astamir.model.legacy.Bus;
import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.TopologyLayer;

import javax.xml.bind.annotation.XmlTransient;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.List;

/**
 * Граница некоторой области. На данный момент состоит из четырех кусков границ, представленых
 * отрезками шины.
 */
public class DirectedBounds extends LegacyTopologyElement {
    private Multimap<Direction, Bus.BusPart> directedBounds = ArrayListMultimap.create();

    @XmlTransient
    private transient Bus bus;

    public LegacyVirtualGrid elements = new LegacyVirtualGrid();


    public DirectedBounds(TopologyLayer layer, Rectangle bound) {
        super(layer);
        initEdges(Preconditions.checkNotNull(bound), layer);
    }

    public DirectedBounds(TopologyLayer layer, LegacyVirtualGrid elements, Rectangle bound) {
        super(layer);
        initEdges(Preconditions.checkNotNull(bound), layer);
        this.elements = Preconditions.checkNotNull(elements);
    }

    private void initEdges(Rectangle bounds, TopologyLayer layer) {
        Direction startingDirection = Direction.LEFT;
        bus = new Bus(layer, Point.of(0.0, 0.0), Material.UNKNOWN, 0);
        bus.setMaterial(layer.getMaterial());
        bus.setSketchStroke(StrokeFactory.defaultStroke());
        bus.setFirstPart(bounds.getEdge(startingDirection), Double.MAX_VALUE, true/*startingDirection.isUpOrDown()*/, true/*startingDirection.isLeftOrRight()*/);

        for (Direction dir : Direction.crippledWalk(startingDirection)) {
            Edge axis = bounds.getEdge(dir);
            bus.addPart(dir.clockwise(), axis.length(), Double.MAX_VALUE, true, true);
        }

        ImmutableList<Bus.BusPart> parts = bus.getParts();
        List<Direction> walk = Direction.walk(startingDirection);
        for (int i = 0; i < parts.size(); i++) {
            directedBounds.put(walk.get(i), parts.get(i));
        }
    }

    public boolean moveBound(Direction boundDirection, Direction moveDirection, double d, Border... additionalBorders) {
//        if (!boundDirection.isSameOrientation(moveDirection)) {
//            return false;
//        }
//
//        Collection<Bus.BusPart> parts = directedBounds.get(boundDirection);
//        Border border = Border.emptyBorder(moveDirection.getOrthogonalDirection().toOrientation());
//        if (boundDirection != moveDirection) {
//             border = elements.getBorderWithOffset(moveDirection, 0);
//        }
//
//       // border.overlay(BorderPart.of(directedBounds.get(boundDirection.getOppositeDirection())), moveDirection);
//        for (Border b : additionalBorders) {
//            border.overlay(b.getParts(), moveDirection);
//        }
//
//        for (Bus.BusPart part : parts) {
//            Optional<BorderPart> closestPart = border.getClosestPartWithConstraints(part.getAxis(), part.getParentClass(), moveDirection);
//            if (closestPart.isPresent()) {
//                d = Math.min(d, closestPart.get().getMoveDistance(part.getParentClass(), moveDirection, part.getAxis().getStart()));
//            }
//            if (part.getAxis().getOrientation().isOrthogonal(moveDirection.toOrientation())) {
//                part.move(moveDirection, d);
//            }
//        }

        return true;//Bus.moveParts(parts, moveDirection, d);
    }

    public void imitate(Direction direction, Border... additionalBorders) {
//        Border border = elements.getBorderWithOffset(direction, 1);
//        /*List<BorderPart> of = BorderPart.of(directedBounds.get(direction));
//        border.overlay(of, direction);*/
//        for (Border b : additionalBorders) {
//            border.overlay(b.getParts(), direction);
//        }
//
//        List<Bus.BusPart> imitate = Lists.newArrayList(Iterables.filter(bus.imitate(border, direction), new Predicate<Bus.BusPart>() {
//            @Override
//            public boolean apply(Bus.BusPart input) {
//                return input.index() >= 0 /*&& input.getAxis().getOrientation() != Orientation.BOTH*/;
//            }
//        }));
//
//		directedBounds.replaceValues(direction.getOppositeDirection(), imitate);
    }

    public Multimap<Direction, Bus.BusPart> getDirectedBounds() {
        return directedBounds;
    }

    // TODO
    public LegacyVirtualGrid adjustIndices() {
        elements.insertRow(directedBounds.get(Direction.DOWN), 0);
        elements.addRow(directedBounds.get(Direction.UP));
        elements.addColumn(Lists.newArrayList(directedBounds.get(Direction.RIGHT)));
        elements.insertColumn(directedBounds.get(Direction.LEFT), 0);
        return elements;
    }
    
    public void setBoundsClass(Class<? extends LegacyTopologyElement> actualClass) {
        for (Bus.BusPart part : directedBounds.values()) {
            part.setActualClass(actualClass);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        Graphics2D clone = (Graphics2D) g.create();
        bus.drawAxis(clone);
        elements.draw(clone);
        clone.dispose();
    }
}
