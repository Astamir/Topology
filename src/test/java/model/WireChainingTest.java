package model;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.common.Pair;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by astamir on 9/16/15.
 */
public class WireChainingTest {
    private final static int TEST_ITERATIONS = 100 ;
    private Wire w1;
    private Wire w2;
    private Wire w3;

    @Before
    public void setUp() {
        w1 = createRandomConnectedWire(100);
        w2 = createChainedWire(100);
        w3 = createChainedWire(100);
    }

    private Wire createRandomConnectedWire(int parts) {
        Wire wire = new Wire(Orientation.VERTICAL);
        SimpleWire.Builder builder = new SimpleWire.Builder(wire);
        Edge prev = Edge.of(0, 0, 10, 0);
        builder.setAxis(prev);
        wire.getParts().add(builder.build());
        for (int i = 0; i < parts; i++) {
            Point commonPoint = prev.getEnd();
            Point otherPoint = commonPoint.clone();
            boolean dx = new Random().nextBoolean();
            otherPoint.move(dx ? Math.random() : 0, dx ? 0 : Math.random());
            Edge axis = new Edge(commonPoint, otherPoint);
            for (int j = 0; j < new Random().nextInt(10);j++) {
                axis.reverse();
            }
            builder.setAxis(axis);
            wire.getParts().add(builder.build());
            prev = axis;
        }
        return wire;
    }

    private Wire createChainedWire(int parts) {
        Wire wire = new Wire(Orientation.VERTICAL);
        Edge axis = Edge.of(0, 0, 10, 0);
        wire.setFirstPart(axis, 0, 1000, true, true, true);
        Direction currentDirection = axis.getDirection();
        for (int i = 0; i < parts - 1; i++) {
            currentDirection = currentDirection.orthogonal();
            wire.addPart(currentDirection, new Random().nextInt(50), 1000, true);
        }
        return wire;
    }

    private static void move(Wire wire) {
        for (SimpleWire simpleWire : wire.getParts()) {
            for (int i = 0; i < new Random().nextInt(5); i++) {
                if (simpleWire.isLink()) {
                    continue;
                }
                Direction direction = Direction.randomDirection(simpleWire.getAxis().getOrientation().getOppositeOrientation());
                wire.movePart(simpleWire, direction, new Random().nextInt(10));
                wire.ensureChained();
            }
        }
    }

    private static void stretch(Wire wire) {
        for (SimpleWire simpleWire : wire.getParts()) {
            if (simpleWire.isLink()) {
                continue; // don't move link
            }
            for (int i = 0; i < new Random().nextInt(5); i++) {
                Direction direction = Direction.randomDirection(simpleWire.getAxis().getOrientation());
                wire.stretch(simpleWire, direction, new Random().nextDouble()*10);
            }
        }
    }

    private static void createLinks(Wire wire) {
        int linksToCreate = wire.getParts().size() / 4 - 5;
        if (linksToCreate <=0){
            linksToCreate = wire.size() / 2;
        }
        List<Pair<Point, Direction>> links = new ArrayList<>();
        for (SimpleWire simpleWire : wire.getParts()) {
            if (simpleWire.isLink()) {
                continue;
            }
            Edge axis = simpleWire.getAxis();
            Point center = axis.getCenter().clone();
            center.setPoint(center.intX(), center.intY());
            links.add(Pair.of(center, axis.getDirection().orthogonal()));
            linksToCreate--;
            if (linksToCreate <= 0) {
                break;
            }
        }

        for (Pair<Point, Direction> link : links) {
            wire.createAnEmptyLink(link.left, link.right);
        }
    }

    @Test
    public void testChainedCreation() {
        Assert.assertEquals(false, w1.isChained());
        Assert.assertEquals(true, w1.isConnected());

        Assert.assertEquals(true, w2.isChained());
        Assert.assertEquals(true, w2.isConnected());
    }

    @Test
    public void testChainedMove() {
        Assert.assertEquals(true, w2.isChained());
        Assert.assertEquals(true, w2.isConnected());
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            move(w2);
            Assert.assertEquals(true, w2.isChained());
            Assert.assertEquals(true, w2.isConnected());
        }
        Assert.assertEquals(true, w2.isChained());
        Assert.assertEquals(true, w2.isConnected());
    }

    @Test
    public void testChainedStretch() {
        Assert.assertEquals(true, w2.isChained());
        Assert.assertEquals(true, w2.isConnected());
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            stretch(w2);
            Assert.assertEquals(true, w2.isChained());
            Assert.assertEquals(true, w2.isConnected());
        }
        Assert.assertEquals(true, w2.isChained());
        Assert.assertEquals(true, w2.isConnected());
    }

    @Test
    public void testChainedWithLinks() {
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            Assert.assertEquals(true, w3.isChained());
            Assert.assertEquals(true, w3.isConnected());
            createLinks(w3);
            Assert.assertEquals(true, w3.isChained());
            Assert.assertEquals(true, w3.isConnected());
        }
    }

    @Test
    public void testChainedWithLinksMove() {
        Assert.assertEquals(true, w3.isChained());
        Assert.assertEquals(true, w3.isConnected());
        createLinks(w3);
        Assert.assertEquals(true, w3.isChained());
        Assert.assertEquals(true, w3.isConnected());
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            move(w3);
            Assert.assertEquals(true, w3.isChained());
            Assert.assertEquals(true, w3.isConnected());
        }
        Assert.assertEquals(true, w3.isChained());
        Assert.assertEquals(true, w3.isConnected());
    }

    @Test
    public void testChainedWithLinksStretch() {
        Assert.assertEquals(true, w3.isChained());
        Assert.assertEquals(true, w3.isConnected());
        createLinks(w3);
        Assert.assertEquals(true, w3.isChained());
        Assert.assertEquals(true, w3.isConnected());
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            stretch(w3);
            Assert.assertEquals(true, w3.isChained());
            Assert.assertEquals(true, w3.isConnected());
        }
        Assert.assertEquals(true, w3.isChained());
        Assert.assertEquals(true, w3.isConnected());
    }
}
