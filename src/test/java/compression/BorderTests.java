package compression;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.model.legacy.Bus;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 02.02.13
 * Time: 0:37
 * To change this template use File | Settings | File Templates.
 */
public class BorderTests {
    private BorderPart was;
    private BorderPart added;
    
    private Direction direction;

    @Before
    public void setUp() {
        was = new BorderPart(Edge.of(0, 0, 0, 3), "SN");
        added = new BorderPart(Edge.of(3, -1, 3, 5), "SN");

        direction = Direction.LEFT;
    }

    @Test
    public void centerTest() {
        List<BorderPart> overlay = Border.singleOverlay(was, added, direction.clockwise().toOrientation(), direction);
        Assert.assertEquals(0, overlay.size());
        Assert.assertArrayEquals(Lists.newArrayList().toArray(), overlay.toArray());

        added.getAxis().shiftY(-3);
        overlay = Border.singleOverlay(was, added, direction.clockwise().toOrientation(), direction);
        Assert.assertEquals(1, overlay.size());
        Assert.assertArrayEquals(Lists.newArrayList(new BorderPart(Edge.of(0, 2, 0, 3), "SN")).toArray(), overlay.toArray());

        added.getAxis().shiftY(5);
        overlay = Border.singleOverlay(was, added, direction.clockwise().toOrientation(), direction);
        Assert.assertEquals(1, overlay.size());
        Assert.assertArrayEquals(Lists.newArrayList(new BorderPart(Edge.of(0, 0, 0, 1), "SN")).toArray(), overlay.toArray());

        added.getAxis().shiftY(-2);
        added.getAxis().stretch(Direction.DOWN, 1);
        added.getAxis().shrink(3);
        overlay = Border.singleOverlay(was, added, direction.clockwise().toOrientation(), direction);
        Assert.assertEquals(2, overlay.size());
        Assert.assertArrayEquals(Lists.newArrayList(new BorderPart(Edge.of(0, 2, 0, 3), "SN"),
                new BorderPart(Edge.of(0, 0, 0, 1), "SN")).toArray(), overlay.toArray());
    }
}
