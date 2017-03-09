package ru.etu.astamir.model.contacts;

import com.google.common.primitives.Doubles;
import ru.etu.astamir.compression.controller.PinMatchingController;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.math.MathUtils;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.connectors.ConnectionPoint;
import ru.etu.astamir.model.regions.ContactWindow;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.*;

public class Contact extends TopologyElement implements ConnectionPoint, Serializable, ComplexElement, Movable, Edged {
    private static final long serialVersionUID = 1L;

    protected Edge center;

    protected Map<Material, ContactWindow> contactWindows = new HashMap<>();

    protected ContactType type = ContactType.USUAL;

    protected Collection<String> connectedElements = new HashSet<>();

    public Contact(String name, Edge center) {
        super(name);
        this.center = center;
    }

    public Contact(Edge center) {
        super();
        this.center = center;
    }

    protected Contact() {
    }

    public Edge getCenter() {
        return center;
    }

    public void setCenter(Edge center) {
        this.center = center;
    }

    public ContactType getType() {
        return type;
    }

    public void setType(ContactType type) {
        this.type = type;
    }

    public Map<Material, ContactWindow> getContactWindows() {
        return contactWindows;
    }

    public void setContactWindows(Map<Material, ContactWindow> contactWindows) {
        this.contactWindows = contactWindows;
    }

    @Override
    public Collection<Point> getCoordinates() {
        return center.getPoints();
    }

    @Override
    public boolean setCoordinates(Collection<Point> coordinates) {
        throw new NotImplementedException();
    }

    public void setConnectedElements(Collection<String> connectedElements) {
        this.connectedElements = connectedElements;
    }

    public void addConnectedElement(String name) {
        connectedElements.add(name);
    }

    public void removeConnectedElement(String name) {
        connectedElements.remove(name);
    }

    @Override
    public Collection<String> getConnectedNames() {
        return connectedElements;
    }

    @Override
    public Polygon getBounds() {
        //return new Polygon(); // it's heavily dependent on the technology
        return !contactWindows.isEmpty() ? contactWindows.values().stream().max((o1, o2) -> Doubles.compare(o1.getBounds().area(), o2.getBounds().area())).get().getBounds() : Polygon.emptyPolygon();
    }

    @Override
    public Contact clone() {
        Contact clone = (Contact) super.clone();
        clone.setType(type);
        clone.setCenter(center.clone());

        clone.connectedElements = new HashSet<>(connectedElements);

        Map<Material, ContactWindow> windows = new HashMap<>();
        for (Map.Entry<Material, ContactWindow> entry : contactWindows.entrySet()) {
            windows.put(entry.getKey(), (ContactWindow) entry.getValue().clone());
        }
        clone.setContactWindows(windows);

        return clone;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public Collection<? extends TopologyElement> getElements() {
        return contactWindows.values();
    }

    @Override
    public boolean move(double dx, double dy) {

        //System.out.println("1"+this.getCoordinates().toString());
        boolean success = center.move(dx, dy);
        for (ContactWindow contactWindow : contactWindows.values()) {
            success &= contactWindow.move(dx, dy);
        }
        //System.out.println("2"+this.getCoordinates().toString());
        if (PinMatchingController.pinProcessed) {
            for (Point point : this.getCoordinates()) {
                for (Map<Point, Double> pointMapListEl : PinMatchingController.getCurrentProcessingPins()) {
                    for (Map.Entry<Point, Double> pointMap : pointMapListEl.entrySet()) {
                        if (MathUtils.round(point.x()) == pointMap.getKey().x() && MathUtils.round(point.y()) == pointMap.getKey().y()) {
                            //System.out.println("kokokokombo");
                            success = center.move(0, pointMap.getValue());
                            for (ContactWindow contactWindow : contactWindows.values()) {
                                success &= contactWindow.move(0, pointMap.getValue());
                            }
                        }
                    }
                }
            }
            //this.getCoordinates().contains(new Point(8.0, 25.7));
            //System.out.println("kokokokombo");
        } /*else {
            success = center.move(dx, dy);
            for (ContactWindow contactWindow : contactWindows.values()) {
                success &= contactWindow.move(dx, dy);
            }
        }*/
        return success;
    }

    @Override
    public Edge getAxis() {
        return center;
    }
}
