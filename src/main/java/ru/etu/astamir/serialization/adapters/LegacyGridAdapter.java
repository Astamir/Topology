package ru.etu.astamir.serialization.adapters;

import com.google.common.collect.Lists;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;
import ru.etu.astamir.model.legacy.LegacyContact;
import ru.etu.astamir.model.legacy.Bus;
import ru.etu.astamir.serialization.JAXBUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public class LegacyGridAdapter {
	LegacyVirtualGrid grid;

	public LegacyGridAdapter(LegacyVirtualGrid grid) {
		this.grid = grid;
	}


	public void marshall(File file) throws Exception {
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		BusAdapter busAdapter = new BusAdapter();
		Element root = new Element("grid");
		List<Bus> marshalledBuses = Lists.newArrayList();

		for (LegacyTopologyElement element : grid.getAllElements()) {
			if (element instanceof Bus) {
				Element bus = busAdapter.marshalToElement((Bus) element);
				root.addContent(bus);
				marshalledBuses.add((Bus) element);
			}

			if (element instanceof LegacyContact) {
				JAXBContext context = JAXBContext.newInstance(LegacyContact.class);
				Marshaller marshaller = context.createMarshaller();
				org.w3c.dom.Document node = JAXBUtils.newDocument();
				marshaller.marshal(element, node);
				root.addContent(JAXBUtils.jaxbTojdom(node).cloneContent().get(0));
			}

			if (element instanceof Bus.BusPart) {
				Bus.BusPart part = (Bus.BusPart) element;
				List<Bus> toPut = Lists.newArrayList(marshalledBuses);
				if (tryFind(marshalledBuses, part.getParent()) == null) {
					Element bus = busAdapter.marshalToElement(((Bus.BusPart) element).getParent());
					root.addContent(bus);
					marshalledBuses.add(((Bus.BusPart) element).getParent());
				}
				toPut.add(part.getParent());
				BusPartAdapter adapter = new BusPartAdapter(toPut);
				Element element1 = adapter.marshalToElement(part);
				root.addContent(element1);
			}

		}

		outputter.output(root, new FileWriter(file));
	}

	public LegacyTopologyElement tryFind(List<? extends LegacyTopologyElement> elements, LegacyTopologyElement element) {
		for (LegacyTopologyElement elem : elements) {
			if (elem.getName() != null && elem.getName().equals(element.getName())) {
				return elem;
			}
		}

		return null;
	}
}
