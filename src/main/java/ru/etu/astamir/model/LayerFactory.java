package ru.etu.astamir.model;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 11/4/12
 * Time: 1:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class LayerFactory {
    private Map<String, TopologyLayer> map = Maps.newHashMap();

    {
        map.put("LL", new TopologyLayer(Material.UNKNOWN, "Schema layer", TopologyLayer.ORDINARY_LAYER, "LL"));
        map.put("SI", new TopologyLayer(Material.POLYSILICON, "Polysilicon", TopologyLayer.POLYSILICON_LAYER, "SI"));
        map.put("M1", new TopologyLayer(Material.METAL, "Metal lower 1", TopologyLayer.METAL_LAYER, "M1"));
        map.put("M2", new TopologyLayer(Material.METAL, "Metal upper 2", TopologyLayer.METAL_LAYER, "M2"));
        map.put("M3", new TopologyLayer(Material.METAL, "Metal 3", TopologyLayer.METAL_LAYER, "M3"));
        map.put("M4", new TopologyLayer(Material.METAL, "Metal 4", TopologyLayer.METAL_LAYER, "M4"));
        map.put("M5", new TopologyLayer(Material.METAL, "Metal 5", TopologyLayer.METAL_LAYER, "M5"));
    }

    public TopologyLayer forName(String symbol) {
        return map.containsKey(symbol) ? map.get(symbol) : createDefaultTopologyLayer();
    }

    public TopologyLayer createDefaultTopologyLayer() {
        return forName("LL");
    }

    @Deprecated
    public TopologyLayer createLayerForMaterialType(Material material) {
        TopologyLayer layer = new TopologyLayer(material, material != null ? material.name() : "unknown " + " LAYER", 0, "");
        return layer;
    }


    public TopologyLayer createLayerForMaterialType(Material material, String shortName) {
        if (map.containsKey(shortName)) {
            return map.get(shortName);
        }

        TopologyLayer layer = new TopologyLayer(material, material != null ? material.name() : "unknown " + " LAYER", 0, shortName);
        map.put(shortName, layer);
        return layer;
    }

    public Collection<TopologyLayer> forMaterial(Material material) {
        List<TopologyLayer> result = new ArrayList<>();
        for (TopologyLayer layer : map.values()) {
            if (layer.getMaterial() == material) {
                result.add(layer);
            }
        }
        return result;
    }

    public Collection<TopologyLayer> getAvailableLayers() {
        return map.values();
    }
}
