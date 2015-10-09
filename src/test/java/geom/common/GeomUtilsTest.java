package geom.common;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.GeomUtils;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.wires.SimpleWire;

import javax.rmi.CORBA.Util;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by amonko on 09.10.2015.
 */
public class GeomUtilsTest {
    double[] toPointsInitialArray;

    @Before
    public void setUp() {
        toPointsInitialArray = new double[new Random().nextInt(100)];
        for (int i = 0; i < toPointsInitialArray.length; i++) {
            toPointsInitialArray[i] = new Random().nextDouble();
        }
    }

    @Test
    public void testToPoints() {
        Point[] points = GeomUtils.toPoints(toPointsInitialArray);
        Assert.assertEquals("Point array length should be the same as coordinates array", toPointsInitialArray.length / 2, points.length);

        int k = 0;
        System.out.println("coordinates = " + Arrays.toString(toPointsInitialArray));
        System.out.println("points = " + Arrays.toString(points));
        for (Point point : points) {
            Assert.assertEquals("index = " + k, point.x(), Utils.round(toPointsInitialArray[k]));
            Assert.assertEquals("index = " + k, point.y(), Utils.round(toPointsInitialArray[k + 1]));
            k += 2;
        }
    }
}
