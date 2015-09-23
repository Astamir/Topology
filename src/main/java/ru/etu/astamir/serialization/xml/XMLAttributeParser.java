package ru.etu.astamir.serialization.xml;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.serialization.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class XMLAttributeParser {
	public static Collection<Attribute> parse(InputSource source) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document build = builder.build(source);

		return (Collection<Attribute>) toAttribute(build.getRootElement()).getValue();
	}

    public static Collection<Attribute> fromDocument(Document document) {
        return (Collection<Attribute>) toAttribute(document.getRootElement()).getValue();
    }

	public static void write(Attribute attribute, OutputStream stream) throws IOException {
		try(BufferedOutputStream bos = new BufferedOutputStream(stream)) {
			XMLOutputter writer = new XMLOutputter(Format.getPrettyFormat());
			Document root = new Document(toElement(null, attribute));
			writer.output(root, bos);
		}
	}

	static Attribute toAttribute(Element root) {
		List<Attribute> rootAttributes = new ArrayList<>();
        for (org.jdom2.Attribute attribute : root.getAttributes()) {
            rootAttributes.add(AttributeFactory.createAttribute(attribute.getName(), attribute.getValue()));
        }
        if (root.getChildren().isEmpty()) { // has no children, so attributes and value
            String value = root.getValue();
            if (!value.isEmpty()) {
                rootAttributes.add(root.getName().contains("_ref") ? AttributeFactory.createReferenceAttribute(root.getName(), value)
                        : AttributeFactory.createAttribute(root.getName(), value));
            }

			if (!root.getAttributes().isEmpty()) {
				Collection<Attribute> xmlAttributes = getXMLAttributes(root);
				for (Attribute a : xmlAttributes) {
					rootAttributes.add(a);
				}
			} else {
                return AttributeContainer.findAttribute(rootAttributes, root.getName()).get();
            }
		} else {
			for (Element element : root.getChildren()) {
				Attribute child = toAttribute(element);
                rootAttributes.add(child);
			}
		}

		return AttributeFactory.createAttribute(root.getName(), rootAttributes);
	}

	static Element toElement(Element parent, Attribute attribute) {
		List<Element> children = new ArrayList<>();
        String name = attribute.isReference() ? attribute.getName() + "_ref" : attribute.getName();
		Element element = new Element(name);
		if (attribute.isSimple()) {
			SimpleAttribute sAttribute = (SimpleAttribute) attribute;
			if (attribute.isLocal()) {
				if (parent == null) {
					throw new UnexpectedException("parent should not be null for local attribute : " + attribute);
				}

                parent.setAttribute(name, sAttribute.getValue());
			} else {
				element.setText(sAttribute.getValue());
			}

			return element;
		} else {
			ComplexAttribute cAttribute = (ComplexAttribute) attribute;
			for (Attribute atr : cAttribute.getValue()) {
				Element elem = toElement(element, atr);
				if (!elem.getContent().isEmpty() || !elem.getAttributes().isEmpty()) {
					children.add(elem);
				}
			}
		}

		element.setContent(children);
		return element;
	}

	static Collection<Attribute> getXMLAttributes(Element root) {
		Map<String, Attribute> attributeMap = new HashMap<>();
		for (org.jdom2.Attribute attribute : root.getAttributes()) {
			String name = attribute.getName();
			attributeMap.put(name, AttributeFactory.createAttribute(name, attribute.getValue()));
		}

		return attributeMap.values();
	}
}
