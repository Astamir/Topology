package serialization.marshall;

import ru.etu.astamir.geom.common.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 04.11.13
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */
public class PolygonMarshallTest extends JAXBTest<Polygon>{
    private static final int SIZE = 100;

    public PolygonMarshallTest() {
        super(Polygon.class);
    }

    @Override
    protected Collection<Polygon> getTestingInstances() {
        List<Polygon> polygons = new ArrayList<Polygon>();
        PointMarshallTest pointTest = new PointMarshallTest();

        for (int i = 0; i < new Random().nextInt(SIZE); i++) {
            Polygon poly = Polygon.of(pointTest.getTestingInstances());
            polygons.add(poly);
        }

        return polygons;
    }
}
