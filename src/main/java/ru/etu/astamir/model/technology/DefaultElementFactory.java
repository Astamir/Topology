package ru.etu.astamir.model.technology;

import com.google.common.collect.Lists;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.contacts.ContactType;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.regions.*;
import ru.etu.astamir.model.wires.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class DefaultElementFactory implements ElementFactory {
    private static Map<String, ElementCreator> cache = new HashMap<>();

    public DefaultElementFactory() {
        initElementCache();
    }

    private void initElementCache() {
        /* Карманы p- и n- типа */
        cache.put("KP", new ElementCreator() { // p- карман, включающий n- транзисторы
            @Override
            public Entity create(Point... coordinates) {
                Bulk bulk = new Bulk("KP", Polygon.of(coordinates));
                bulk.setConductionType(ConductionType.P);
                return bulk;
            }
        });
        cache.put("KN", new ElementCreator() { // n- карман, включающий p- транзисторы
            @Override
            public Entity create(Point... coordinates) {
                Bulk bulk = new Bulk("KN", Polygon.of(coordinates));
                bulk.setConductionType(ConductionType.N);

                return bulk;
            }
        });

        /* Активные области транзисторов n- и p- типа */
        cache.put("NA", new ElementCreator() { // n++ - активная область n- транзистора
            @Override
            public Entity create(Point... coordinates) {
                ActiveRegion region = new ActiveRegion("NA", Polygon.of(coordinates));
                region.setConductionType(ConductionType.NNN);
                region.setLayer(ProjectObjectManager.getLayerFactory().forName("SI"));

                return region;
            }
        });
        cache.put("PA", new ElementCreator() { // p++ - активная область p- транзистора
            @Override
            public Entity create(Point... coordinates) {
                ActiveRegion region = new ActiveRegion("PA", Polygon.of(coordinates));
                region.setConductionType(ConductionType.PPP);
                region.setLayer(ProjectObjectManager.getLayerFactory().forName("SI"));

                return region;
            }
        });

        /* Контактные области к карманам */
        cache.put("NK", new ElementCreator() { // n++ - контактная область к n- карману
            @Override
            public Entity create(Point... coordinates) {
                ContactRegion region = new ContactRegion("NK", Polygon.of(coordinates));
                region.setConductionType(ConductionType.NNN);

                return region;
            }
        });
        cache.put("PK", new ElementCreator() {// p++ - контактная область к p- карману
            @Override
            public Entity create(Point... coordinates) {
                ContactRegion region = new ContactRegion("PK", Polygon.of(coordinates));
                region.setConductionType(ConductionType.PPP);

                return region;
            }
        });

        /* Области легирования */
        cache.put("N", new ElementCreator() { // n++ - область легирования
            @Override
            public Entity create(Point... coordinates) {
                AlloyRegion region = new AlloyRegion("N", Polygon.of(coordinates));
                region.setConductionType(ConductionType.NNN);

                return region;
            }
        });
        cache.put("P", new ElementCreator() {// p++ - область легирования
            @Override
            public Entity create(Point... coordinates) {
                AlloyRegion region = new AlloyRegion("P", Polygon.of(coordinates));
                region.setConductionType(ConductionType.PPP);

                return region;
            }
        });

        /* Затворы транзистора */
        cache.put("SN", new ElementCreator() { // n - затвор
            @Override
            public Entity create(Point... coordinates) {
                List<Edge> edges = WireUtils.fromPoints(Lists.newArrayList(coordinates));
                List<SimpleWire> parts = Lists.newArrayList();
                for (Edge edge : edges) {
                    SimpleWire wire = new SimpleWire(edge);
                    parts.add(wire);
                }

                Gate gate = new Gate(WireUtils.getOrientation(edges));
                gate.setSymbol("SN");
                gate.setLayer(ProjectObjectManager.getLayerFactory().forName("SI"));
                gate.setConductionType(ConductionType.N);
                gate.setParts(parts);

                return gate;
            }
        });
        cache.put("SP", new ElementCreator() {// p - затвор
            @Override
            public Entity create(Point... coordinates) {
                List<Edge> edges = WireUtils.fromPoints(Lists.newArrayList(coordinates));
                List<SimpleWire> parts = Lists.newArrayList();
                for (Edge edge : edges) {
                    SimpleWire wire = new SimpleWire(edge);
                    parts.add(wire);
                }

                Gate gate = new Gate(WireUtils.getOrientation(edges));
                gate.setSymbol("SP");
                gate.setLayer(ProjectObjectManager.getLayerFactory().forName("SI"));
                gate.setConductionType(ConductionType.P);
                gate.setParts(parts);

                return gate;
            }
        });

        cache.put("M2", new ElementCreator() {
            @Override
            public Entity create(Point... coordinates) {
                List<Edge> edges = WireUtils.fromPoints(Lists.newArrayList(coordinates));
                List<SimpleWire> parts = Lists.newArrayList();
                for (Edge edge : edges) {
                    SimpleWire wire = new SimpleWire(edge);
                    parts.add(wire);
                }

                Wire wire = new Wire(WireUtils.getOrientation(edges));
                wire.setSymbol("M2");
                wire.setLayer(ProjectObjectManager.getLayerFactory().forName("M2"));
                wire.setMaterial(Material.METAL);
                wire.setParts(parts);

                return wire;
            }
        });

        cache.put("M1", new ElementCreator() {
            @Override
            public Entity create(Point... coordinates) {
                List<Edge> edges = WireUtils.fromPoints(Lists.newArrayList(coordinates));
                List<SimpleWire> parts = Lists.newArrayList();
                for (Edge edge : edges) {
                    SimpleWire wire = new SimpleWire(edge);
                    parts.add(wire);
                }

                Wire wire = new Wire(WireUtils.getOrientation(edges));
                wire.setSymbol("M1");
                wire.setMaterial(Material.METAL);
                wire.setLayer(ProjectObjectManager.getLayerFactory().forName("M1"));
                wire.setParts(parts);

                return wire;
            }
        });

        /* Контактные окна*/
        cache.put("CNA", new ElementCreator() {//Контактное окно между М1C_  и NA_
            @Override
            public Entity create(Point... coordinates) {
                if (coordinates.length == 0) {
                    throw new UnexpectedException("there are no coordinates");
                }

                Edge center;
                if (coordinates.length == 1) {
                    center = Edge.of(coordinates[0]);
                } else {
                    center = new Edge(coordinates[0], coordinates[1]);
                }

                Contact contact = new Contact(center);
                contact.setSymbol("CNA");
                contact.setConductionType(ConductionType.N);
                contact.setType(ContactType.USUAL);

                ContactWindow window1 = new ContactWindow("CNA", Rectangle.of(center, 0, 0));
                window1.setMaterial(Material.POLYSILICON);
                window1.setLayer(ProjectObjectManager.getLayerFactory().forName("SI"));
                contact.getContactWindows().put(window1.getMaterial(), window1);

                ContactWindow window2 = new ContactWindow("M1", Rectangle.of(center, 0, 0));
                window2.setMaterial(Material.METAL);
                window2.setLayer(ProjectObjectManager.getLayerFactory().forName("M1"));
                contact.getContactWindows().put(window2.getMaterial(), window2);

                return contact;
            }
        });
        cache.put("CNK", new ElementCreator() {//Контактное окно между М1C_  и NK_
            @Override
            public Entity create(Point... coordinates) {

                //ContactWindow window = new ContactWindow(id, "CNA", Rectangle.createFor));

                return null;
            }
        });
        cache.put("CNE", new ElementCreator() {//Контактное окно между М1C_  и NE_
            @Override
            public Entity create(Point... coordinates) {

                //ContactWindow window = new ContactWindow(id, "CNA", Rectangle.createFor));

                return null;
            }
        });
        cache.put("CPA", new ElementCreator() {//Контактное окно между М1C_  и PA_
            @Override
            public Entity create(Point... coordinates) {

                //ContactWindow window = new ContactWindow(id, "CNA", Rectangle.createFor));

                return null;
            }
        });
        cache.put("CPK", new ElementCreator() {//Контактное окно между М1C_  и PK_
            @Override
            public Entity create(Point... coordinates) {

                //ContactWindow window = new ContactWindow(id, "CNA", Rectangle.createFor));

                return null;
            }
        });
        cache.put("CPE", new ElementCreator() {//Контактное окно между М1C_  и PE_
            @Override
            public Entity create(Point... coordinates) {

                //ContactWindow window = new ContactWindow(id, "CNA", Rectangle.createFor));

                return null;
            }
        });
        cache.put("CSI", new ElementCreator() {//Контактное окно между M1C_  и SI_
            @Override
            public Entity create(Point... coordinates) {

                //ContactWindow window = new ContactWindow(id, "CNA", Rectangle.createFor));

                return null;
            }
        });
        cache.put("SI", new ElementCreator() {//Поликремниевая шина
            @Override
            public Entity create(Point... coordinates) {
                List<Edge> edges = WireUtils.fromPoints(Lists.newArrayList(coordinates));
                List<SimpleWire> parts = Lists.newArrayList();
                for (Edge edge : edges) {
                    SimpleWire wire = new SimpleWire(edge);
                    parts.add(wire);
                }

                Wire wire = new Wire(WireUtils.getOrientation(edges));
                wire.setSymbol("SI");
                wire.setMaterial(Material.POLYSILICON);
                wire.setLayer(ProjectObjectManager.getLayerFactory().forName("SI"));
                wire.setParts(parts);

                return wire;
            }
        });
    }

    public ElementCreator getElementCreator(String symbol) {
        if (!cache.containsKey(symbol)) {
            throw new UnexpectedException("There is no creator for " + symbol);
        }

        return cache.get(symbol);
    }

    @Override
    public TopologyElement getElement(String symbol, Point[] coordinates, Map<String, Object> properties) {
        ElementCreator<TopologyElement> creator = getElementCreator(symbol);
        return creator.create(coordinates);
    }

    public Class<? extends TopologyElement> getEntityClass(String symbol) {
        if (cache.containsKey(symbol)) {
            ElementCreator creator = getElementCreator(symbol);
            return (Class<? extends TopologyElement>) creator.create(new Point()).getClass();
        }

        return TopologyElement.class;
    }

    public interface ElementCreator<V extends Entity> {
        V create(Point... coordinates);
    }
}
