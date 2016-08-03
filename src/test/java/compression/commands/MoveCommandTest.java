package compression.commands;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.commands.CommandManager;
import ru.etu.astamir.compression.commands.MoveCommand;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.GeomUtils;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.technology.ElementFactory;
import ru.etu.astamir.model.wires.Gate;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by amonko on 09.10.2015.
 */
public class MoveCommandTest {
    SimpleWire simpleWire;
    Wire wire;
    Contour contour;
    Contact contact;
    Gate gate;

    List<TopologyElement> elements = new ArrayList<>();

    ElementFactory factory = ProjectObjectManager.getElementFactory();

    @Before
    public void setUp() {
        simpleWire = new SimpleWire(Edge.of(0, 0, 0, 10), 10);
        wire = (Wire) factory.getElement("SI", GeomUtils.toPoints(0, 0, 0, 10, 10, 10), null);
        contour = (Contour) factory.getElement("NA", GeomUtils.toPoints(0,0,0,10, 0, 10, 10, 10, 10, 10, 10, 0, 10, 0, 0, 0), null);
        contact = (Contact) factory.getElement("CNA", GeomUtils.toPoints(0, 0), null);
        gate = (Gate) factory.getElement("SN", GeomUtils.toPoints(0, 0, 0, 10, 10, 10), null);

        elements = Lists.<TopologyElement>newArrayList(simpleWire, wire, contact, contour, gate);
    }

    @Test
    public void test() {
        for (TopologyElement element : elements) {
            testElement(element);
        }
    }

    private void testElement(TopologyElement element) {
        int moveCount = new Random().nextInt(40);
        CommandManager<MoveCommand> manager = new CommandManager<>();
        double dx = 0;
        double dy = 0;
        for (int i = 0; i < moveCount; i++) {
            Direction direction = Direction.randomDirection();
            double length = new Random().nextDouble();
            double signedD = GeomUtils.getSignedLength(direction, length);
            if (direction.isLeftOrRight()) {
                dx += signedD;
            } else {
                dy += signedD;
            }
            MoveCommand move = new MoveCommand(element, direction, length);
            manager.addCommand(move);
        }

        final double finalDx = dx;
        final double finalDy = dy;
        Collection<Point> initialCoordinates = element.clone().getCoordinates();
        Collection<Point> movedCoordinates = Lists.newArrayList(Collections2.transform(initialCoordinates, new Function<Point, Point>() {
            @Override
            public Point apply(Point input) {
                return Point.of(input.x() + finalDx, input.y() + finalDy);
            }
        }));
        manager.executeAll();
        Collection<Point> commandCoordinates = element.getCoordinates();
        Assert.assertArrayEquals("move all, dx = " + dx + ", dy = " + dy, movedCoordinates.toArray(new Point[movedCoordinates.size()]),
                commandCoordinates.toArray(new Point[commandCoordinates.size()]));

        manager.rollback();
        commandCoordinates = element.getCoordinates();
        Assert.assertArrayEquals("move all back, dx = " + -dx + ", dy = " + -dy, commandCoordinates.toArray(new Point[commandCoordinates.size()]),
                initialCoordinates.toArray(new Point[initialCoordinates.size()]));
    }


}
