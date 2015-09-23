package serialization.marshall;

import ru.etu.astamir.model.Material;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 04.11.13
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class MaterialMarshallTest extends EnumJAXBTest<Material> {
    public MaterialMarshallTest() {
        super(Material.class);
    }
}
