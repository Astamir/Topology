package ru.etu.astamir.model.connectors;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.model.ComplexElement;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.wires.Wire;

import java.security.PublicKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Artem Mon'ko
 */
public class ConnectionUtils {
    public static <V extends TopologyElement> EntitySet<V> resolveConnectedElements(ConnectionPoint point, Grid grid) {
        EntitySet<V> result = new EntitySet<>();
        for (String name : getConnectedNames(point)) {
            Optional<TopologyElement> element = grid.findElementByName(name);
            if (element.isPresent()) {
                result.add((V) element.get());
            }
        }

        return result;
    }

    public static <V extends TopologyElement> EntitySet<V> getConnectedElements(Wire wire, Grid grid) {
        EntitySet<V> connected_elements = new EntitySet<>();
        for (ConnectionPoint connection_point : wire.getConnections()) {
            connected_elements.addAll(ConnectionUtils.<V>resolveConnectedElements(connection_point, grid));
        }

        return connected_elements;
    }

    public static Collection<String> getElementsNames(TopologyElement element) {
        if (element instanceof ComplexElement) {
            List<String> children = ((ComplexElement) element).getElements().stream().map(TopologyElement::getName).collect(Collectors.toList());
            children.add(element.getName());
            return children;
        }

        return Collections.singleton(element.getName());
    }

    public static Collection<String> getConnectedNames(ConnectionPoint... connections) {
        Set<String> result = new HashSet<>();
        for (ConnectionPoint connection : connections) {
            result.addAll(connection.getConnectedNames());
            if (connection.isSimple()) {
                result.add(connection.getName());
            }
        }

        return result;
    }

    public static Collection<String> getConnectedNames(Collection<ConnectionPoint> connections) {
        return getConnectedNames(connections.toArray(new ConnectionPoint[connections.size()]));
    }

    public static Collection<Contour> isContainedIn(Grid grid, final TopologyElement element, Class<? extends Contour> contourType) {
        return Lists.newArrayList(Collections2.filter(grid.findAllOfType(contourType), new Predicate<Contour>() {
            @Override
            public boolean apply(Contour contour) {
                return contour.contains(element);
            }
        }));
    }
}
