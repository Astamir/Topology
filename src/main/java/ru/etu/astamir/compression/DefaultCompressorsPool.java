package ru.etu.astamir.compression;

import ru.etu.astamir.launcher.Topology;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class DefaultCompressorsPool implements CompressorsPool {
    Map<Integer, TopologyCompressor> compressors_pool = new HashMap<>();

    public DefaultCompressorsPool() {
    }

    public TopologyCompressor getCompressor(Topology topology) {
        int key = System.identityHashCode(topology);
        if (compressors_pool.containsKey(key)) {
           return compressors_pool.get(key);
        } else {
            TopologyCompressor compressor = new TopologyCompressor(topology);
            compressors_pool.put(key, compressor);
            return compressor;
        }
    }

    public void dropFor(Topology topology) {
        if (null == topology) {
            return;
        }
        compressors_pool.remove(String.valueOf(System.identityHashCode(topology)));
    }

    public void clearPool() {
        compressors_pool.clear();
    }
}
