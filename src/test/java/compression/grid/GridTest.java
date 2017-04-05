package compression.grid;

import com.google.common.base.Predicate;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.reflections.ReflectionUtils;
import ru.etu.astamir.common.collections.AbstractEntitySet;
import ru.etu.astamir.common.collections.EntitySet;
import ru.etu.astamir.common.reflect.ReflectUtils;
import ru.etu.astamir.model.Entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by astamir on 9/12/14.
 */
public class GridTest {
    List<AbstractEntitySet<?>> sets = new ArrayList<>();

    @Before
    public void setUp() throws IllegalAccessException, InstantiationException {
        List<Class<? extends AbstractEntitySet>> setClasses = new ArrayList<>();
        setClasses.add(EntitySet.class);

        for (Class<? extends AbstractEntitySet> aClass : setClasses) {
            sets.add(aClass.newInstance());
        }
    }

    @Test
    public void testBackingMaps() {
        for (AbstractEntitySet set : sets) {

        }
    }
}
