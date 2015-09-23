package ru.etu.astamir.compression;

import ru.etu.astamir.launcher.Topology;

/**
 * @author Artem Mon'ko
 */
public interface CompressorsPool {
    TopologyCompressor getCompressor(Topology topology);
    void dropFor(Topology topology);
    void clearPool();
}
