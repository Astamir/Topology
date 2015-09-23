package serialization.marshall;

import ru.etu.astamir.geom.common.Edge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 04.11.13
 * Time: 19:00
 * To change this template use File | Settings | File Templates.
 */
public class EdgeMarshallTest extends JAXBTest<Edge> {
    private static final int SIZE = 100;

    public EdgeMarshallTest() {
        super(Edge.class);
    }

    @Override
    protected Collection<Edge> getTestingInstances() {
        List<Edge> edges = new ArrayList<Edge>();
        for (int i = 0; i < new Random().nextInt(SIZE); i++) {
            Edge edge = Edge.of(new Random().nextDouble(), new Random().nextDouble(),
                    new Random().nextDouble(), new Random().nextDouble());
            edges.add(edge);
        }

        return edges;
    }
}
