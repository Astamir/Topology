package geom.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 12/8/12
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class EdgeCommonTest {
    private Edge edge;

    @Before
    public void setUp() {
        edge = Edge.of(100, 100, 200, 200);
    }

    @Test
    public void centerTest() {
        Assert.assertEquals(Point.of(150, 150), edge.getCenter());
    }
}
