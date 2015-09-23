package ru.etu.astamir.compression.commands.compression;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ru.etu.astamir.common.collections.CollectionUtils;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.CompressionUtils;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.ComplexElement;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.Contour;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Astamir on 11.01.2015.
 */
public abstract class CompressCommand implements Command {
    protected VirtualGrid grid;
    protected Map<TopologyLayer, Map<Direction, Border>> borders = new HashMap<>();
    protected String element_name;
    protected Direction direction;

    public CompressCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String element_name, Direction direction) {
        this.grid = grid;
        this.borders = borders;
        this.element_name = element_name;
        this.direction = direction;
    }

    public TopologyElement getElement() {
        Optional<TopologyElement> found_element = grid.findElementByName(element_name);
        if (!found_element.isPresent()) {
            throw new UnexpectedException("There is no found_element with the name=" + element_name + " in the grid");
        }

        return found_element.get();
    }

    public Collection<Border> getAffectedBorders() {
        return CompressionUtils.getAffectedBorders(getElement(), borders, direction);
    }
}
