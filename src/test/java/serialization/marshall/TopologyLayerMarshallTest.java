package serialization.marshall;

import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.TopologyLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 04.11.13
 * Time: 19:30
 * To change this template use File | Settings | File Templates.
 */
public class TopologyLayerMarshallTest extends JAXBTest<TopologyLayer> {
    public TopologyLayerMarshallTest() {
        super(TopologyLayer.class);
    }

    @Override
    protected Collection<TopologyLayer> getTestingInstances() {
        List<TopologyLayer> layers = new ArrayList<TopologyLayer>();
        layers.add(ProjectObjectManager.getLayerFactory().createDefaultTopologyLayer());
        for(Material material : Material.values()) {
            layers.addAll(ProjectObjectManager.getLayerFactory().forMaterial(material));
        }

        return layers;
    }
}
