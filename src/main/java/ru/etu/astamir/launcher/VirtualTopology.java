package ru.etu.astamir.launcher;

import com.google.common.base.Preconditions;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.model.technology.CMOSTechnology;

import java.io.Serializable;

/**
 * @author Artem Mon'ko
 */
public class VirtualTopology implements Topology, Serializable {
    public static final int VIRTUAL_MODE = 0;
    public static final int REAL_MODE = 1;

    VirtualGrid virtual = new VirtualGrid();
    VirtualGrid real = new VirtualGrid();

    CMOSTechnology technology;

    int mode = VIRTUAL_MODE;

    public VirtualTopology(int mode) {
        this(new VirtualGrid(), new VirtualGrid(), null, mode);
    }

    public VirtualTopology(VirtualGrid virtual, VirtualGrid real, CMOSTechnology technology, int mode) {
        this.virtual = virtual;
        this.real = real;
        this.technology = technology;

        Preconditions.checkArgument(mode == VirtualTopology.VIRTUAL_MODE || mode == VirtualTopology.REAL_MODE);
        this.mode = mode;
    }

    public static VirtualTopology of(Grid grid) {
        return new VirtualTopology(new VirtualGrid(), new VirtualGrid(), new CMOSTechnology("unknown cmos technology"), VIRTUAL_MODE);
    }

    @Override
    public VirtualGrid getGrid() {
        return mode == VIRTUAL_MODE ? virtual : real;
    }

    public VirtualGrid getVirtual() {
        return virtual;
    }

    public void setVirtual(VirtualGrid virtual) {
        this.virtual = virtual;
    }

    public VirtualGrid getReal() {
        return real;
    }

    public void setReal(VirtualGrid real) {
        this.real = real;
    }

    public CMOSTechnology getTechnology() {
        return technology;
    }

    public void setTechnology(CMOSTechnology technology) {
        this.technology = technology;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
