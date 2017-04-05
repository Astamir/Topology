package ru.etu.astamir.compression;

import ru.etu.astamir.launcher.Topology;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class DefaultCompressorsCache implements CompressorsCache {
    Map<Integer, TopologyCompressor> compressorsPool = new HashMap<>();

    public DefaultCompressorsCache() {
    }

    public TopologyCompressor getCompressor(Topology topology) {
        int key = System.identityHashCode(topology);
        if (compressorsPool.containsKey(key)) {
           return compressorsPool.get(key);
        } else {
            TopologyCompressor compressor = new TopologyCompressor(topology);
            compressorsPool.put(key, compressor);
            return compressor;
        }
    }

    public void dropFor(Topology topology) {
        if (null == topology) {
            return;
        }
        compressorsPool.remove(String.valueOf(System.identityHashCode(topology)));
    }

    public void clearPool() {
        compressorsPool.clear();
    }
}
