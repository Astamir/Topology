package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.CompressionUtils;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Команда сжатия. Содержит все частоколы и сетку, а так же направления сжатия.
 */
public abstract class CompressCommand implements Command {
    /**
     * Grid before executing this command
     */
    protected VirtualGrid grid;

    /**
     * All borders. Only actual before executing this command.
     */
    protected Map<TopologyLayer, Map<Direction, Border>> borders = new HashMap<>();
    protected String elementName;
    protected Direction direction;

    public CompressCommand(VirtualGrid grid, Map<TopologyLayer, Map<Direction, Border>> borders, String elementName, Direction direction) {
        this.grid = grid;
        this.borders = borders;
        this.elementName = elementName;
        this.direction = direction;
    }

    public TopologyElement getElement() {
        Optional<TopologyElement> foundElement = grid.findElementByName(elementName);
        if (!foundElement.isPresent()) {
            throw new UnexpectedException("There is no foundElement with the name=" + elementName + " in the grid");
        }

        return foundElement.get();
    }

    public Collection<Border> getAffectedBorders() {
        return CompressionUtils.getAffectedBorders(getElement(), borders, direction);
    }
}
