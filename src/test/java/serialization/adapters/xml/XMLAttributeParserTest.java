package serialization.adapters.xml;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.etu.astamir.serialization.Attribute;
import ru.etu.astamir.serialization.ComplexAttribute;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Collection;
import java.util.Random;

/**
 * @author Artem Mon'ko
 */
public class XMLAttributeParserTest {
    Collection<Attribute> attributes;
    int depth = 20;
    int attributesSize = 100;

    public XMLAttributeParserTest() {
    }

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < new Random().nextInt(depth); i++) {
            ComplexAttribute attribute = new ComplexAttribute("");
            for (int j = 0; j < new Random().nextInt(attributesSize); j++) {

            }
        }
    }

    @After
    public void tearDown() throws Exception {
//        if (!file.delete())
//            Assert.fail("Could not delete marshalling file");
    }

    @Test
    public void testUnmarshall() throws Exception {
//        for (V inst : instances) {
//            System.out.print("Marshalling instance : " + inst);
//            marshaller.marshal(inst, file);
//            Object unmarshall = unmarshaller.unmarshal(file);
//            Assert.assertEquals("Objects are not equal after serializing", inst, unmarshall);
//            System.out.println(" OK");
//        }
    }
}