package ru.etu.astamir.compression;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.compression.commands.compression.ActiveBorder;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.ComplexElement;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.connectors.ConnectionUtils;
import ru.etu.astamir.model.regions.ActiveRegion;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.wires.Gate;

import java.util.*;
import java.util.stream.Collectors;

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

    public static ActiveBorder getMovingLength(TopologyElement element, Direction direction, Border border) {
        ActiveBorder result = ActiveBorder.NAN;
        for (Point coordinate : element.getCoordinates()) {
            final Optional<BorderPart> closest = border.getClosestPartWithConstraints(coordinate, element.getSymbol(), direction);
            if (closest.isPresent()) {
                double l = border.getMoveDistance(closest.get(), element.getSymbol(), direction, coordinate);
                result = Utils.assignIfSmaller(result, ActiveBorder.of(closest.get(), l));
            }
        }

        return result;
    }

    public static ActiveBorder getMovingLength(TopologyElement element, Point coordinate, Direction direction, Border border) {
        ActiveBorder result = ActiveBorder.NAN;
        BorderPart part = null;
        final Optional<BorderPart> closest = border.getClosestPartWithConstraints(coordinate, element.getSymbol(), direction);
        if (closest.isPresent()) {
            double l = border.getMoveDistance(closest.get(), element.getSymbol(), direction, coordinate);
            result = ActiveBorder.of(closest.get(), l);
        }

        return result;
    }

    /**
     * Constructs a border without connected elements of some particular element.
     *
     * @param border base border to remove connected elements from
     * @param grid all the elements in the grid
     * @return border without connected elements
     */
    public static Border borderWithoutConnectedElements(final TopologyElement element, Border border, Grid grid) {
        boolean isGate = element instanceof Gate;

        final Collection<String> connected_names = ConnectionUtils.getElementsNames(element);

        List<BorderPart> parts = border.getParts().stream().filter(part-> { // filter out active regions if element is gate
            if (isGate) {
                TopologyElement e = part.getElement();
                if (e instanceof ActiveRegion) {
                    return ((ActiveRegion)e).contains(e);
                }
            }
            return true;
        }).filter(part -> {
            TopologyElement e = part.getElement();
            return e == null || !connected_names.contains(e.getName());
        }).collect(Collectors.toList());

        Border result = new Border(border.getOrientation(), border.getTechnology());
        result.setParts(parts);
        return result;
    }
}
