package ru.etu.astamir.serialization;

import com.google.common.collect.Lists;
import org.jdom2.JDOMException;
import org.xml.sax.InputSource;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.model.ConductionType;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.serialization.adapters.*;
import ru.etu.astamir.serialization.xml.XMLAttributeParser;

import java.io.*;
import java.util.Collection;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public class TestSerialization {
	public static void main(String... args) throws JDOMException, IOException {
		Edge p = Edge.of(12, 4, Direction.RIGHT, 10);
		EdgeAdapter adapter = new EdgeAdapter();
		List<Attribute> attributes = adapter.getAttributes(p);
		String xml = "<point x=\"12.4\" y=\"22.4\"/>";
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			XMLAttributeParser.write(AttributeFactory.createAttribute("edge", attributes), os);
			xml = os.toString();
			System.out.println(xml);
		}

		Collection<Attribute> parse = XMLAttributeParser.parse(new InputSource(new StringReader(xml)));
		Edge entity = adapter.getEntity(parse);

		System.out.println(entity);
		System.out.println(p.equals(entity));

		ConductionType type = ConductionType.N;
		EnumAdapter<ConductionType> ad = new EnumAdapter<>(ConductionType.class);

		ConductionType entity1 = (ConductionType) ad.getEntity(ad.getAttributes(type));
		System.out.println(entity1);

        TopologyLayerAdapter layerAdapter = new TopologyLayerAdapter();
        TopologyLayer layer = ProjectObjectManager.getLayerFactory().createDefaultTopologyLayer();
        System.out.println(layer.equals(layerAdapter.getEntity(layerAdapter.getAttributes(layer))));

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            XMLAttributeParser.write(AttributeFactory.createAttribute("edge", layerAdapter.getAttributes(layer)), os);
            xml = os.toString();
            System.out.println(xml);
        }

        Polygon polygon = Rectangle.of(Edge.of(12, 15, Direction.DOWN, 50), 20, 20);
        PolygonAdapter polygonAdapter = new PolygonAdapter();
        System.out.println(polygonAdapter.getEntity(polygonAdapter.getAttributes(polygon)).equals(polygon));

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            XMLAttributeParser.write(AttributeFactory.createAttribute("polygon", polygonAdapter.getAttributes(polygon)), os);
            xml = os.toString();
            System.out.println(xml);
        }


        SimpleWire wire = new SimpleWire(Edge.of(10, 10, Direction.RIGHT, 100), 0);
        Wire wireRef = new Wire("asdasd", Orientation.BOTH);
        wireRef.setParts(Lists.newArrayList(wire));
        wire.setWire(wireRef);
        SimpleWireAdapter1 wireAdapter = new SimpleWireAdapter1();
        BasicAdapter ad1 = new BasicAdapter(Wire.class);
        Collection<Attribute> attributes1 = ad1.getAttributes(wire);
        System.out.println(attributes1);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            XMLAttributeParser.write(AttributeFactory.createAttribute("wire", attributes1), os);
            xml = os.toString();
            System.out.println(xml);
        }

        Grid grid = new VirtualGrid();
        grid.putElement(1,1, wire);
        grid.putElement(1, 2, wireRef);
        AttributeAdapter<Grid> adapterFor = (AttributeAdapter<Grid>) AttributeContainer.getInstance().getAdapterFor(Grid.class);
        adapterFor.getAttributes(grid);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            XMLAttributeParser.write(AttributeFactory.createAttribute("grid", adapterFor.getAttributes(grid)), os);
            xml = os.toString();
            System.out.println(xml);
        }
        System.out.println("SAD");

        Collection<Attribute> p1 = XMLAttributeParser.parse(new InputSource(new StringReader(xml)));
        Grid entity2 = adapterFor.getEntity(p1);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            XMLAttributeParser.write(AttributeFactory.createAttribute("grid", adapterFor.getAttributes(entity2)), os);
            xml = os.toString();
            System.out.println(xml);
        }


//        Contour contour = new Contour("cont");
//        contour.addElement(wire);
//        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
//            XMLAttributeParser.write(AttributeFactory.createAttribute("grid", new BasicAdapter<Contour>(Contour.class).getAttributes(contour)), os);
//            xml = os.toString();
//            System.out.println(xml);
//        }
//
//        wire = wireAdapter.getEntity(XMLAttributeParser.parse(new InputSource(new StringReader(xml))));
//        System.out.println(wire);


    }
}
