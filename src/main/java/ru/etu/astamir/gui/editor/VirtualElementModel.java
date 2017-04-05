package ru.etu.astamir.gui.editor;

import com.google.common.base.Preconditions;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.launcher.VirtualTopology;
import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Astamir
 */
public class VirtualElementModel implements ElementModel{
    VirtualTopology topology;

    public VirtualElementModel(VirtualTopology topology) {
        this.topology = Preconditions.checkNotNull(topology);
    }

    @Override
    public boolean addElement(TopologyElement element) {
        topology.getGrid().addElement(element);
        return true;
    }

    @Override
    public Optional<? extends Entity> getElementByName(String name) {
        throw new UnexpectedException("not implemented yet");
    }

    @Override
    public boolean hasElement(String name) {
        throw new UnexpectedException("not implemented yet");
    }

    @Override
    public Collection<TopologyElement> getAllElements() {
        return topology.getGrid().getAllElements();
    }

    @Override
    public Grid grid() {
        return topology.getGrid();
    }
}
