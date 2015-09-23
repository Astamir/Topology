package serialization.marshall;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Collection;

/**
 * Класс тестировщик jaxb сериализации.
 *
 * @param <V>
 */
public abstract class JAXBTest<V> {
    private Collection<V> instances;
    private File file = new File("test_marshall.xml");
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;

    protected Class<?> clazz;

    public JAXBTest(Class<?> clazz) {
        this.clazz = clazz;
    }

    protected abstract Collection<V> getTestingInstances();

    @Before
    public void setUp() throws Exception {
        instances = getTestingInstances();
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
            Assert.fail("Some marshalling problems : ");
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!file.delete())
            Assert.fail("Could not delete marshalling file");
    }

    @Test
    public void testUnmarshall() throws Exception {
        for (V inst : instances) {
            System.out.print("Marshalling instance : " + inst);
            marshaller.marshal(inst, file);
            Object unmarshall = unmarshaller.unmarshal(file);
            Assert.assertEquals("Objects are not equal after serializing", inst, unmarshall);
            System.out.println(" OK");
        }
    }
}
