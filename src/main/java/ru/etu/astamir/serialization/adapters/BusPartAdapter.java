package ru.etu.astamir.serialization.adapters;

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
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 10.02.14
 * Time: 22:16
 * To change this template use File | Settings | File Templates.
 */
public class BusPartAdapter extends XmlAdapter<String, Bus.BusPart> {
    Marshaller edgeMarshaller;
	Element toAddBus;
	List<Bus> busRefs;

    public BusPartAdapter(List<Bus> busRefs) {
//		toAddBus = root;
		this.busRefs = busRefs;
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(Edge.class);
            edgeMarshaller = jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert a value type to a bound type.
     *
     * @param partString The value to be converted. Can be null.
     * @throws Exception if there's an error during the conversion. The caller is responsible for
     *                   reporting the error to the user through {@link javax.xml.bind.ValidationEventHandler}.
     */
    @Override
    public Bus.BusPart unmarshal(String partString) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document build = builder.build(new StringReader(partString));
        Element rootElement = build.getRootElement();

		Element material = rootElement.getChild("material");
		Element widthElem = rootElement.getChild("width");
		double width = Double.parseDouble(widthElem.getText());

		Bus busParent = null;
		Element busRef = rootElement.getChild("busRef");
		if (busRef != null) {
			Bus bus = tryFind(busRef.getText());
			if (bus != null) {
				busParent = bus;
			}
		} else {
			busParent = new Bus(null, Point.of(-1, -1), Material.valueOf(material.getValue().toUpperCase()), width);
			busRefs.add(busParent);
		}



        JAXBContext edgeContext = JAXBContext.newInstance(Edge.class);
        Unmarshaller edgeUnmarshaller = edgeContext.createUnmarshaller();

		Element axisElem = rootElement.getChild("axis");
		axisElem.setName("edge");
		XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
		Edge unmarshal = (Edge) edgeUnmarshaller.unmarshal(new StringReader(outputter.outputString(axisElem)));
		Bus.BusPart part = busParent.new BusPart(unmarshal);
		part.setName(rootElement.getAttributeValue("id"));
		part.setIndex(Integer.parseInt(rootElement.getAttributeValue("index")));

		busParent.setParts(part);

        return part;
    }

	public Bus tryFind(String name) {
		for (Bus bus : busRefs) {
			if (bus.getName().equals(name)) {
				return bus;
			}
		}

		return null;
	}

    /**
     * Convert a bound type to a value type.
     *
     * @param part The value to be convereted. Can be null.
     * @throws Exception if there's an error during the conversion. The caller is responsible for
     *                   reporting the error to the user through {@link javax.xml.bind.ValidationEventHandler}.
     */
    @Override
    public String marshal(Bus.BusPart part) throws Exception {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        Element root = new Element("part");

        String busName = part.getName();
        if (busName == null || busName.isEmpty()) {
            busName = String.valueOf(System.identityHashCode(part));
        }
        root.setAttribute("id", busName);

        root.setAttribute("index", String.valueOf(part.index()));

        org.w3c.dom.Document node = JAXBUtils.newDocument();
        edgeMarshaller.marshal(part.getAxis(), node);
        Document document = JAXBUtils.jaxbTojdom(node);
        Element axis = (Element) document.cloneContent().get(0);
        axis.setName("axis");
        root.addContent(axis);

        Element busRef = new Element("busRef");
        busRef.addContent(part.getParent().getName());
        root.addContent(busRef);

        Element material = new Element("material");
        material.addContent(part.getMaterial().toString().toLowerCase());
        root.addContent(material);

        Element width = new Element("width");
        width.addContent(String.valueOf(part.getWidth()));
        root.addContent(width);

        return outputter.outputString(root);
    }

	public Element marshalToElement(Bus.BusPart part) throws JAXBException, ParserConfigurationException {
		Element root = new Element("part");

		String busName = part.getName();
		if (busName == null || busName.isEmpty()) {
			busName = String.valueOf(System.identityHashCode(part));
		}
		root.setAttribute("id", busName);

		root.setAttribute("index", String.valueOf(part.index()));

		org.w3c.dom.Document node = JAXBUtils.newDocument();
		edgeMarshaller.marshal(part.getAxis(), node);
		Document document = JAXBUtils.jaxbTojdom(node);
		Element axis = (Element) document.cloneContent().get(0);
		axis.setName("axis");
		root.addContent(axis);

		Element busRef = new Element("busRef");
		busRef.addContent(part.getParent().getName());
		root.addContent(busRef);

		Element material = new Element("material");
		material.addContent(part.getMaterial().toString().toLowerCase());
		root.addContent(material);

		Element width = new Element("width");
		width.addContent(String.valueOf(part.getWidth()));
		root.addContent(width);

		return root;
	}
}
