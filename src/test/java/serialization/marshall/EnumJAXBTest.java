package serialization.marshall;

import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @param <V>
 */
public abstract class EnumJAXBTest<V extends Enum<V>> extends JAXBTest<V> {
    public EnumJAXBTest(Class<V> clazz) {
        super(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<V> getTestingInstances() {
        return EnumSet.allOf((Class<V>) clazz);
    }
}
