package serialization.marshall;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import ru.etu.astamir.model.ConductionType;
import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.contacts.ContactType;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 04.11.13
 * Time: 18:30
 * To change this template use File | Settings | File Templates.
 */
public class EnumMarshallTests {
    final Set<Class<? extends Enum>> enumTests = new HashSet<Class<? extends Enum>>();

    @Before
    public void setUp() throws Exception {
        enumTests.add(Material.class);
        enumTests.add(ConductionType.class);
        enumTests.add(ContactType.class);
    }

    @Test
    public void testEnums() throws Exception {
        JUnitCore core = new JUnitCore();
        for (final Class<? extends Enum> aClass : enumTests) {
            TestWrap testWrap = new TestWrap() {
                {
                    setCl(aClass);
                }
            };
            Class<? extends TestWrap> aClass1 = testWrap.getClass();
            Result result = core.run(aClass1);
            if (!result.wasSuccessful()) {
                Assert.fail(aClass.getSimpleName() + " test failed");
            }
        }
    }
    
    private static class TestWrap extends EnumJAXBTest {
        public TestWrap() {
            super(null);
        }

        public void setCl(Class<? extends Enum> cl) {
            this.clazz = cl;
        }
    }
    
}
