package ru.etu.astamir.compression;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.ComplexElement;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.connectors.ConnectionPoint;
import ru.etu.astamir.model.connectors.ConnectionUtils;
import ru.etu.astamir.model.regions.ActiveRegion;
import ru.etu.astamir.model.regions.Bulk;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.wires.Wire;

import java.util.*;

/**
 * @author Artem Mon'ko
 */
public class CompressionUtils {
    public static Collection<Border> getAffectedBorders(TopologyElement element, Map<TopologyLayer, Map<Direction, Border>> borders, Direction direction) {
        List<Border> result = Lists.newArrayList();
        Set<TopologyLayer> element_layers = Sets.newHashSet(element.getLayer());
        if (element instanceof ComplexElement) {
            element_layers.addAll(Collections2.transform(((ComplexElement) element).getElements(), new Function<TopologyElement, TopologyLayer>() {
                @Override
                public TopologyLayer apply(TopologyElement element) {
                    TopologyLayer layer = element.getLayer();
                    if (layer == null) {
                        Collection<TopologyLayer> material_layers = ProjectObjectManager.getLayerFactory().forMaterial(element.getMaterial());
                        if (!material_layers.isEmpty()) {
                            layer = material_layers.iterator().next();
                        }
                    }
                    return layer;
                }
            }));
        }

        if (element instanceof Contour && !(element instanceof ActiveRegion)) {
            for (TopologyElement containing_element : ((Contour) element).getElements()) {
                TopologyLayer layer = containing_element.getLayer();
                if (layer != null)
                    element_layers.add(layer);
            }
        }

        for (Map.Entry<TopologyLayer, Map<Direction, Border>> border_map : borders.entrySet()) {
            TopologyLayer border_layer = border_map.getKey();
            if (element_layers.contains(border_layer)) {
                result.add(border_map.getValue().get(direction));
            }
        }
        return result;
    }

    public static double getMovingLength(TopologyElement element, Direction direction, Border border) {
        double length = 0.0;
        for (Point coordinate : element.getCoordinates()) {
            final Optional<BorderPart> closest = border.getClosestPartWithConstraints(coordinate, element.getSymbol(), direction);
            if (closest.isPresent()) {
                double l = border.getMoveDistance(closest.get(), element.getSymbol(), direction, coordinate);
                length = l < length || length == 0 ? l : length;
            }
        }

        return length;
    }

    public static Border borderWithoutConnectedElements(Wire wire, Border border) {
        final List<String> connected_names = new ArrayList<>(ConnectionUtils.getConnectedNames(wire.getConnections()));
        Border result = new Border(border.getOrientation(), border.getTechnology());
        Collection<BorderPart> parts = Collections2.filter(border.getParts(), new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                TopologyElement element = input.getElement();
                return element == null || !connected_names.contains(element.getName());
            }
        });

        result.setParts(Lists.newArrayList(parts));
        return result;
    }
}
