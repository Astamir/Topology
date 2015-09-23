package ru.etu.astamir.geom.common;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 17.10.13
 * Time: 23:36
 * To change this template use File | Settings | File Templates.
 */
public class MarshallTest {
    public static void main(String[] args) {
        Edge p = Edge.of(12.3, 4.2, 4.1, 22.3);

        try {

            File file = new File("C:\\file.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Edge.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(p, file);
            jaxbMarshaller.marshal(p, System.out);


        } catch (JAXBException e) {
            e.printStackTrace();
        }

        try {

            File file = new File("C:\\file.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Edge.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Edge unp = (Edge) jaxbUnmarshaller.unmarshal(file);
            System.out.println(unp);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
