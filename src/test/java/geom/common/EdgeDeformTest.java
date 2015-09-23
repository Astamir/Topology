package geom.common;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;

import java.util.List;

/**
 * Тест деформации отрезка.
 */
public class EdgeDeformTest {
    List<Edge> verticalDeform = Lists.newArrayList();
    List<Edge> horizontalDeform = Lists.newArrayList();

    Edge verticalEdge;
    Edge horizontalEdge;

    Point horizontalDeformPoint = Point.of(100, 100);
    Point verticalDeformPoint = Point.of(100, 150);

    double deformWidth = 50;


    @Before
    public void setUp() {
        verticalEdge = Edge.of(100, 100, 100, 200);
        horizontalEdge = Edge.of(50, 100, 150, 100);


    }

    @Test
    public void horizontalDeformTest() {
        horizontalDeform = horizontalEdge.deform(horizontalDeformPoint, Direction.UP, Direction.RIGHT, deformWidth);
        Assert.assertEquals(3, horizontalDeform.size());
        Assert.assertEquals(Edge.of(50, 100, 100, 100), horizontalDeform.get(0));
        Assert.assertEquals(Edge.of(100, 100, 100, 150), horizontalDeform.get(1));
        Assert.assertEquals(Edge.of(100, 150, 150, 150), horizontalDeform.get(2));

        horizontalDeform = horizontalEdge.deform(horizontalDeformPoint, Direction.UP, Direction.LEFT, deformWidth);
        Assert.assertEquals(3, horizontalDeform.size());
        Assert.assertEquals(Edge.of(50, 150, 100, 150), horizontalDeform.get(0));
        Assert.assertEquals(Edge.of(100, 150, 100, 100), horizontalDeform.get(1));
        Assert.assertEquals(Edge.of(100, 100, 150, 100), horizontalDeform.get(2));

        horizontalDeform = horizontalEdge.deform(horizontalDeformPoint, Direction.DOWN, Direction.RIGHT, deformWidth);
        Assert.assertEquals(3, horizontalDeform.size());
        Assert.assertEquals(Edge.of(50, 50, 100, 50), horizontalDeform.get(0));
        Assert.assertEquals(Edge.of(100, 50, 100, 100), horizontalDeform.get(1));
        Assert.assertEquals(Edge.of(100, 100, 150, 100), horizontalDeform.get(2));

        horizontalDeform = horizontalEdge.deform(horizontalDeformPoint, Direction.DOWN, Direction.LEFT, deformWidth);
        Assert.assertEquals(3, horizontalDeform.size());
        Assert.assertEquals(Edge.of(50, 100, 100, 100), horizontalDeform.get(0));
        Assert.assertEquals(Edge.of(100, 100, 100, 50), horizontalDeform.get(1));
        Assert.assertEquals(Edge.of(100, 50, 150, 50), horizontalDeform.get(2));

    }

    @Test
    public void verticalDeformTest() {
        verticalDeform = verticalEdge.deform(verticalDeformPoint, Direction.RIGHT, Direction.RIGHT, deformWidth);
        Assert.assertEquals(3, verticalDeform.size());
        Assert.assertEquals(Edge.of(150, 100, 150, 150), verticalDeform.get(0));
        Assert.assertEquals(Edge.of(150, 150, 100, 150), verticalDeform.get(1));
        Assert.assertEquals(Edge.of(100, 150, 100, 200), verticalDeform.get(2));

        verticalDeform = verticalEdge.deform(verticalDeformPoint, Direction.RIGHT, Direction.LEFT, deformWidth);
        Assert.assertEquals(3, verticalDeform.size());
        Assert.assertEquals(Edge.of(100, 100, 100, 150), verticalDeform.get(0));
        Assert.assertEquals(Edge.of(100, 150, 150, 150), verticalDeform.get(1));
        Assert.assertEquals(Edge.of(150, 150, 150, 200), verticalDeform.get(2));

        verticalDeform = verticalEdge.deform(verticalDeformPoint, Direction.LEFT, Direction.RIGHT, deformWidth);
        Assert.assertEquals(3, verticalDeform.size());
        Assert.assertEquals(Edge.of(100, 100, 100, 150), verticalDeform.get(0));
        Assert.assertEquals(Edge.of(100, 150, 50, 150), verticalDeform.get(1));
        Assert.assertEquals(Edge.of(50, 150, 50, 200), verticalDeform.get(2));

        verticalDeform = verticalEdge.deform(verticalDeformPoint, Direction.LEFT, Direction.LEFT, deformWidth);
        Assert.assertEquals(3, verticalDeform.size());
        Assert.assertEquals(Edge.of(50, 100, 50, 150), verticalDeform.get(0));
        Assert.assertEquals(Edge.of(50, 150, 100, 150), verticalDeform.get(1));
        Assert.assertEquals(Edge.of(100, 150, 100, 200), verticalDeform.get(2));
    }
}
