package ru.etu.astamir.compression.legacy;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.CompressionStrategy;
import ru.etu.astamir.compression.grid.legacy.LegacyVirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.legacy.LegacyContact;
import ru.etu.astamir.model.contacts.Contactable;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.legacy.Bus;
import ru.etu.astamir.model.legacy.LegacyGate;
import ru.etu.astamir.model.legacy.LegacyTopologyElement;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 4/1/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopologyCompressor {
    /**
     * Какая-то сетка, содержащая наши элементы.
     */
    public LegacyTopologyScheme scheme;

    TopologyLayer busLayer = ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.SILICON);

    private CompressionStrategy strategy; // = StrategyFactory.defaultStrategy();

    public Map<TopologyLayer, Map<Direction, Border>> borders = Maps.newHashMap();
    
    public Border currentBorder = Border.emptyBorder(Orientation.VERTICAL, null);

    public TopologyCompressor(LegacyVirtualGrid grid, ru.etu.astamir.geom.common.Rectangle bounds) {
        this.scheme = new LegacyTopologyScheme(null, grid, bounds);
        initComponents();
    }

    public TopologyCompressor(LegacyTopologyScheme scheme) {
        this.scheme = scheme;
        initComponents();
    }

    public void moveElements(Direction direction, int steps, final Graphics2D graphics) {
//        int k = 0;
//        for (List<LegacyTopologyElement> column : scheme.elements.walk(direction)) { // TODO
//            //Border border = transistor.grid.getBorder(direction, k, new BorderPart(((Rectangle)transistor.getBounds()).getEdge(direction), TransistorActiveRegion.class));
//            for (LegacyTopologyElement element : column){
//                if (element.isEmpty()) {
//                    continue;
//                }
//                if (k >= steps) {
//                    return;
//                }
//
//                if (element instanceof Bus.BusPart) {
//
//                    Bus.BusPart part = (Bus.BusPart) element;
//                    final Border metalBorder = borders.get(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(part.getMaterial())).get(direction);
//                    ru.etu.astamir.geom.common.Rectangle bounds = (ru.etu.astamir.geom.common.Rectangle) element.getBounds();
//                    Class<? extends LegacyTopologyElement> actualClass = part.getActualClass();
//                    if (actualClass.equals(Bus.BusPart.class)) {
//                        actualClass = part.getParentClass();
//                    }
//
//                    Edge partAxis = (Edge) part.getAxis().clone();
//                    if (partAxis.getOrientation() == direction.toOrientation()) {
//                        continue;
//                    }
//                    partAxis.correct();
//                    Optional<BorderPart> closestPart = metalBorder.getClosestPartWithConstraints(partAxis, actualClass, direction);
//                    if (!closestPart.isPresent()) {
//                        //throw new UnexpectedException();
//                        continue;
//                    }
//
//                    double dist = closestPart.get().getMoveDistance(/*element.getClass()*/actualClass, direction,
//                            partAxis.getStart()/*bounds.getEdge(direction).getStart()*/);
//
//                    List<LegacyContact> contacts = Lists.transform(part.getContacts(), new Function<Contact, LegacyContact>() {
//						Map<String, LegacyContact> cache;
//						@Override
//						public LegacyContact apply(Contact contact) {
//							if (cache.containsKey(contact.getName())) {
//								return cache.get(contact.getName());
//							}
//
//							LegacyContact c = new LegacyContact(contact.getCenter().getStart(), contact.getMaterial());
//							cache.put(contact.getName(), c);
//
//							return c;
//						}
//					});
//                    for (LegacyContact contact : contacts) {
//                        double d = distToMoveContact(contact, direction);
//                        dist = Math.min(dist, d);
//                        //  double dist = distToMoveContact(contact, direction);
//                        GeomUtils.move(contact, direction, dist);
//                    }
//
//                    part.move(direction, dist);
//                    metalBorder.overlay(Lists.newArrayList(BorderPart.of(part)), direction);
//                    currentBorder = metalBorder;
//                    currentBorder.draw(graphics);
//                    if (dist == 0) {
//                        steps++;
//                    }
//
//                    /*Bus gate = ((Transistor) element).getGate();
//                    // border.imitate(gate, direction);
//                    gate.imitate(border, direction);
//                    border.overlay(BorderPart.of(gate), direction);
//                    k++;*/
//                }
//
//                if (element instanceof LegacyContact) {
//                    LegacyContact contact = (LegacyContact) element;
//                    Border border = new Border(direction.getOrthogonalDirection().toOrientation());
//                    Border metallBorder = borders.get(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL)).get(direction);
//                    border.overlay(metallBorder.getParts(), direction); // TODO костыль
//                    Border polisiliconBorder = borders.get(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.POLYSILICON)).get(direction);
//                    border.overlay(polisiliconBorder.getParts(), direction);
//                    currentBorder = border;
//                    Optional<BorderPart> closestPart = border.getClosestPartWithConstraints(Edge.of(contact.getCenter(), contact.getCenter()),
//                            LegacyContact.class, direction);
//                    if (!closestPart.isPresent()) {
//                        //throw new UnexpectedException();
//                        continue;
//                    }
//
//                    double dist = closestPart.get().getMoveDistance(element.getClass(), direction,
//                            /*bounds.getEdge(direction).getStart()*/contact.getCenter());
//                    //  double dist = distToMoveContact(contact, direction);
//
//
//                    List<Contactable> contactables = contact.getContactables();
//
//                    for (Contactable contactable : contactables) {
//                        LegacyTopologyElement elem = (LegacyTopologyElement) contactable;
//                        if (elem == null) {
//                            continue;
//                        }
//                        border = borders.get(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(elem.getMaterial())).get(direction);
//                        ru.etu.astamir.geom.common.Point center = elem.getBounds().getCenter();
//                        Optional<BorderPart> cl = border.getClosestPartWithConstraints(Edge.of(center, center),
//                                LegacyContact.class, direction);
//                        if (!cl.isPresent()) {
//                            throw new UnexpectedException();
//                        }
//
//                        double d = cl.get().getMoveDistance(element.getClass(), direction,
//                                /*bounds.getEdge(direction).getStart()*/center);
//                        dist = Math.min(dist, d);
//                       // GeomUtils.move((Movable) elem, direction, d);
//                        //  double dist = distToMoveContact(contact, direction);
//
//                    }
//
//
//                   // if (contactables.isEmpty())
//                    //GeomUtils.move((Movable) element, direction, dist);
//
//                    List<BorderPart> newParts = Lists.newArrayList();
//                    for (Edge edge : element.getBounds().edges()) {
//                        BorderPart part = new BorderPart(edge, LegacyContact.class);
//                        newParts.add(part);
//                    }
//
//                    if (dist == 0) {
//                        steps++;
//                    }
//
//                    metallBorder.overlay(newParts, direction);
//                    polisiliconBorder.overlay(newParts, direction);
//                    currentBorder.draw(graphics);
//                   // k++;
//                }
//
//                if (element instanceof LegacyGate) {
//                    LegacyGate gate = (LegacyGate) element;
//                    Border border = borders.get(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.POLYSILICON)).get(direction);
//                    gate.imitate(border, direction);
//                    //gate.removeInfidelParts();
//                    border.overlay(BorderPart.of(gate), direction);
//                    k++;
//                    steps++;
//                    continue;
//
//                }
//
//                k++;
//            }
//
//        }
    }

    public void moveBound(Direction direction) {
//        scheme.moveBound(direction.getOppositeDirection(), direction, Double.MAX_VALUE);
//        for (TopologyLayer layer : borders.keySet()) {
//            Border border = new Border(direction.getOrthogonalDirection().toOrientation(),
//                    BorderPart.of(scheme.getDirectedBounds().get(direction.getOppositeDirection())));
//            Map<Direction, Border> layerBorders = borders.get(layer);
//            layerBorders.put(direction.getOppositeDirection(), border);
//        }
    }

    private double distToMoveContact(LegacyContact contact, Direction direction) {
//        Border border = new Border(direction.getOrthogonalDirection().toOrientation());
//        Border metallBorder = borders.get(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.METAL)).get(direction);
//        border.overlay(metallBorder.getParts(), direction); // TODO костыль
//        Border polisiliconBorder = borders.get(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(Material.POLYSILICON)).get(direction);
//        border.overlay(polisiliconBorder.getParts(), direction);
//        currentBorder = border;
//        Optional<BorderPart> closestPart = border.getClosestPartWithConstraints(Edge.of(contact.getCenter(), contact.getCenter()),
//                LegacyContact.class, direction);
//        if (!closestPart.isPresent()) {
//            throw new UnexpectedException();
//        }
//
//        return closestPart.get().getMoveDistance(contact.getClass(), direction,
//                /*bounds.getEdge(direction).getStart()*/contact.getCenter());
            return 0.0;
    }

    public void straightenGates(Direction direction) {
//        for (List<LegacyTopologyElement> column : scheme.elements.walk(direction)) {
//            for (final LegacyTopologyElement element : column) {
//                if (element instanceof Bus) {
//                    List<LegacyTopologyElement> allElements = Lists.newArrayList(Iterables.filter(scheme.elements.getAllElements(), new Predicate<LegacyTopologyElement>() {
//                        @Override
//                        public boolean apply(LegacyTopologyElement input) {
//                            return input instanceof LegacyContact;
//                        }
//                    }));
//                    allElements.remove(element);
//                    Bus bus = ((Bus) element);
//                    bus.straighten(allElements, Border.emptyBorder(direction.getOrthogonalDirection().toOrientation())/*borders.get(direction)*/, direction); //TODO
//                }
//            }
//        }
    }


    private void initComponents() {
//        ImmutableList<TopologyLayer> topologyLayers = scheme.affectedLayers();
//        //topologyLayers.add(busLayer);
//        Multimap<Direction,Bus.BusPart> directedBounds = scheme.getDirectedBounds();
//        for (TopologyLayer layer : topologyLayers) {
//            Map<Direction, Border> borderMap = Maps.newHashMap();
//            for (Direction direction : Direction.values()) {
//                Border border = new Border(direction.getOrthogonalDirection().toOrientation());
//                border.overlay(BorderPart.of(directedBounds.get(direction)), direction);
//                borderMap.put(direction, border);
//            }
//
//            borders.put(layer, borderMap);
//        }
    }
}
