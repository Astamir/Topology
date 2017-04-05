package ru.etu.astamir.compression;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ru.etu.astamir.common.Utils;
import ru.etu.astamir.common.collections.EntitySet;
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
        Set<TopologyLayer> elementLayers = Sets.newHashSet(element.getLayer());
        if (element instanceof ComplexElement) {
            elementLayers.addAll(Collections2.transform(((ComplexElement) element).getElements(), new Function<TopologyElement, TopologyLayer>() {
                @Override
                public TopologyLayer apply(TopologyElement element) {
                    TopologyLayer layer = element.getLayer();
                    if (layer == null) {
                        Collection<TopologyLayer> materialLayers = ProjectObjectManager.getLayerFactory().forMaterial(element.getMaterial());
                        if (!materialLayers.isEmpty()) {
                            layer = materialLayers.iterator().next();
                        }
                    }
                    return layer;
                }
            }));
        }

        if (element instanceof Contour && !(element instanceof ActiveRegion)) {
            for (TopologyElement containingElement : ((Contour) element).getElements()) {
                TopologyLayer layer = containingElement.getLayer();
                if (layer != null)
                    elementLayers.add(layer);
            }
        }

        for (Map.Entry<TopologyLayer, Map<Direction, Border>> borderMap : borders.entrySet()) {
            TopologyLayer borderLayer = borderMap.getKey();
            if (elementLayers.contains(borderLayer)) {
                result.add(borderMap.getValue().get(direction));
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

        final Collection<String> elementNames = ConnectionUtils.getElementsNames(element);
        List<String> connectedNames = ConnectionUtils.getConnectedElements(element, grid).stream().map(ConnectionUtils::getElementsNames).flatMap(Collection::stream).collect(Collectors.toList());

        List<BorderPart> parts = border.getParts().stream().filter(part -> {
            TopologyElement e = part.getElement();
            return e == null || (!elementNames.contains(e.getName()) && !connectedNames.contains(e.getName()));
        }).collect(Collectors.toList());

        Border result = new Border(border.getOrientation(), border.getTechnology());
        result.setParts(parts);
        return result;
    }
}
