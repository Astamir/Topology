package serialization.marshall;

import ru.etu.astamir.geom.common.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 04.11.13
 * Time: 19:03
 * To change this template use File | Settings | File Templates.
 */
public class PointMarshallTest extends JAXBTest<Point>{

    private static final int SIZE = 100;

    public PointMarshallTest() {
        super(Point.class);
    }

    @Override
    protected Collection<Point> getTestingInstances() {
        List<Point> points = new ArrayList<Point>();
        for (int i = 0; i < new Random().nextInt(SIZE); i++) {
            Point point = Point.of(new Random().nextDouble(), new Random().nextDouble());
            points.add(point);
        }

        return points;
    }
}
