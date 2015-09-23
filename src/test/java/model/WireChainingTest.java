package model;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.gui.editor.MainFrame;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by astamir on 9/16/15.
 */
public class WireChainingTest {
    Wire w1;
    Wire w2;
    Wire w3;

    @Before
    public void setUp() {
        w1 = createRandomConnectedWire(100);
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

    @Test
    public void testChained() {
        Assert.assertEquals(false, w1.isChained());
        Assert.assertEquals(true, w1.isConnected());

//        Assert.assertEquals(false, w2.isChained());
//        w2.ensureChained();
//        Assert.assertEquals(true, w2.isChained());
//
//        Assert.assertEquals(false, w3.isChained());
//        w3.ensureChained();
//        Assert.assertEquals(true, w3.isChained());
    }
}
