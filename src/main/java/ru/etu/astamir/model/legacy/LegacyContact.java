package ru.etu.astamir.model.legacy;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.GeomUtils;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.Movable;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.contacts.Connector;
import ru.etu.astamir.model.contacts.ContactType;
import ru.etu.astamir.model.contacts.Contactable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.awt.*;
import java.util.List;

/**
 * Простой одноточечный контакт.
 */
public class LegacyContact extends LegacyTopologyElement implements Movable, Connector {
    /**
     * Центр контакта.
     */
    private Point center;


    /**
     * Элементы, для которых этот контакт является связующим. Этих элементов может и не быть,
     * или отсутствовать один из них. Это означает, что он уточнится позднее.
     */
    @XmlTransient
    private List<Contactable> contactables = Lists.newArrayList();

    /**
     * Тип контакта, по идее он не должен меняться, но пока оставим так.
     */
    protected ContactType type = ContactType.USUAL;

    protected LegacyContact() {
        super();
    }

    public LegacyContact(TopologyLayer layer, Point center, double width, Material material, Contactable oneContactable,
			Contactable anotherContactable) {
        super(layer);
        this.center = Preconditions.checkNotNull(center);
        setBounds(Rectangle.createSquare(center, width));
        setMaterial(material);
        contactables = Lists.newArrayList(oneContactable, anotherContactable);
    }

    public LegacyContact(Point center, Material material) {
        super(ProjectObjectManager.getLayerFactory().createLayerForMaterialType(material));
        this.center = Preconditions.checkNotNull(center);
        setBounds(Rectangle.createSquare(center, LegacyContactFactory.getDefaultContactWidth(getLayer())));
        setMaterial(material);
    }

    public ContactType getType() {
        return type;
    }

    public void setType(ContactType type) {
        this.type = type;
    }

    public void addContactable(Contactable contactable) {
//        contactable.addContact(this);
        contactables.add(contactable);
    }

    public List<Contactable> getContactables() {
        return contactables;
    }

    @XmlElement(name = "center")
    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }


    @Override
    // TODO look through
    public boolean move(double dx, double dy) {
        center.move(dx, dy);
        getBounds().move(dx, dy);
        for (Contactable contactable : contactables) {
            // move contactable somehow
        }

        return true;
    }

    public boolean move(Direction direction, double d) {
        return GeomUtils.move(this, direction, d);
    }

    @Override
    public void connect(Bus element) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disconnect(Bus element) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void draw(Graphics2D g) {
        Graphics2D graphics2D = (Graphics2D) g.create();
        center.draw(graphics2D);
        graphics2D.setColor(getColor());
        getBounds().draw(graphics2D);

        graphics2D.dispose();
    }
}
