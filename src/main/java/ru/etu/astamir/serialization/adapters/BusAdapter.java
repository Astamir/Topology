package ru.etu.astamir.serialization.adapters;

import com.google.common.collect.Lists;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.legacy.Bus;
import ru.etu.astamir.serialization.JAXBUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.StringReader;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public class BusAdapter extends XmlAdapter<String, Bus> {
    Marshaller edgeMarshaller;

    public BusAdapter() {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(Edge.class);
            edgeMarshaller = jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Bus unmarshal(String v) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document build = builder.build(new StringReader(v));
        Element rootElement = build.getRootElement();
        Element material = rootElement.getChild("material");
        Bus bus = new Bus(null, Point.of(-1, -1), Material.valueOf(material.getValue().toUpperCase()), 0);
        List<Element> parts = rootElement.getChild("parts").getChildren("part");
        List<Bus.BusPart> busParts = Lists.newArrayList();
        JAXBContext edgeContext = JAXBContext.newInstance(Edge.class);
        Unmarshaller edgeUnmarshaller = edgeContext.createUnmarshaller();


        for (Element part : parts) {
            Element axisElem = part.getChild("axis");
            axisElem.setName("edge");
            XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
            Edge unmarshal = (Edge) edgeUnmarshaller.unmarshal(new StringReader(outputter.outputString(axisElem)));
            busParts.add(bus.new BusPart(unmarshal));            
        }
        
        bus.setParts(busParts);

        return bus;
    }

//    public InputStream fromDocument(Document doc) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        Source xmlSource = new DOMSource(doc);
//        Result outputTarget = new StreamResult(outputStream);
//        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
//        return new ByteArrayInputStream(outputStream.toByteArray());
//    }

    @Override
    public String marshal(Bus bus) throws Exception {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        Element root = new Element("bus");

        String busName = bus.getName();
        if (busName.isEmpty()) {
            busName = String.valueOf(System.identityHashCode(bus));
        }
        root.setAttribute("id", busName);

        Element parts = new Element("parts");
        for (Bus.BusPart part : bus.getParts()) {
            Element p = new Element("part");
            p.setAttribute("index", String.valueOf(part.index()));

            org.w3c.dom.Document node = JAXBUtils.newDocument();
            edgeMarshaller.marshal(part.getAxis(), node);
            Document document = JAXBUtils.jaxbTojdom(node);
            Element axis = (Element) document.cloneContent().get(0);
            axis.setName("axis");
            p.addContent(axis);

            Element busRef = new Element("busRef");
            busRef.addContent(busName);
            p.addContent(busRef);

            parts.addContent(p);
        }

        root.addContent(parts);

        Element material = new Element("material");
        material.addContent(bus.getMaterial().toString().toLowerCase());
        root.addContent(material);
        return outputter.outputString(root);
    }

	public Element marshalToElement(Bus bus) throws Exception {
		Element root = new Element("bus");

		String busName = bus.getName();
		if (busName == null || busName.isEmpty()) {
			busName = String.valueOf(System.identityHashCode(bus));
		}
		bus.setName(busName);
		root.setAttribute("id", busName);

		Element parts = new Element("parts");
		for (Bus.BusPart part : bus.getParts()) {
			Element p = new Element("part");
			p.setAttribute("index", String.valueOf(part.index()));

			org.w3c.dom.Document node = JAXBUtils.newDocument();
			edgeMarshaller.marshal(part.getAxis(), node);
			Document document = JAXBUtils.jaxbTojdom(node);
			Element axis = (Element) document.cloneContent().get(0);
			axis.setName("axis");
			p.addContent(axis);

			Element busRef = new Element("busRef");
			busRef.addContent(busName);
			p.addContent(busRef);

			parts.addContent(p);
		}

		root.addContent(parts);

		Element material = new Element("material");
		material.addContent(bus.getMaterial().toString().toLowerCase());
		root.addContent(material);

		return root;
	}
}
