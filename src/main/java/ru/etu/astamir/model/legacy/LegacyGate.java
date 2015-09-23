package ru.etu.astamir.model.legacy;

import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.model.*;
import ru.etu.astamir.model.contacts.Contact;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

/**
 * Затвор. Состоит из набора прямых(кусков). Может располагаться
 * на нескольких координатах сетки(причем может занимать солидное пространство).
 * Секции затвора представлены шинами.
 */
// TODO
public class LegacyGate extends Bus implements Drawable, Movable, Cloneable, Serializable {

    private Flap startFlap;

    private Flap endFlap;

    private Polygon bounds;

    public LegacyGate(TopologyLayer layer, Point gridCoordinates, Material material, double width, double widthAtBorder, double maxLength, Orientation orientation, List<BusPart> parts, List<Contact> contacts) {
        super(layer, gridCoordinates, material, width, widthAtBorder, maxLength, orientation, parts, contacts);
        initFlaps();
    }

    public LegacyGate(TopologyLayer layer, Point gridCoordinates, Material material, double width) {
        super(layer, gridCoordinates, material, width);
    }

    @Override
    public void setFirstPart(Point start, Point end, double maxLength, boolean stretchable) {
        super.setFirstPart(start, end, maxLength, stretchable);
        initFlaps();
    }

    @Override
    public void setFirstPart(Edge axis, double maxLength, boolean stretchable) {
        super.setFirstPart(axis, maxLength, stretchable);    //To change body of overridden methods use File | Settings | File Templates.
        initFlaps();
    }

    @Override
    public void setFirstPart(Edge axis, double maxLength, boolean stretchable, boolean movable) {
        super.setFirstPart(axis, maxLength, stretchable, movable);    //To change body of overridden methods use File | Settings | File Templates.
        initFlaps();
    }

    /**
     * Установка первого элемента шины. Первый элемент шины задает ее будущуюю ориентацию.
     *
     * @param start       Точка начала.
     * @param direction   Направление.
     * @param length      Длина кусочка.
     * @param maxLength   Максимальная длина кусочка.
     * @param stretchable Возможность куска растягиваться.
     * @param movable     Возможность кусочка менять свое местоположение.
     */
    @Override
    public void setFirstPart(Point start, Direction direction, double length, double maxLength, boolean stretchable, boolean movable) {
        super.setFirstPart(start, direction, length, maxLength, stretchable, movable);    //To change body of overridden methods use File | Settings | File Templates.
        initFlaps();
    }

    @Override
    public void setFirstPart(Point start, Direction direction, double length, double maxLength, boolean stretchable) {
        super.setFirstPart(start, direction, length, maxLength, stretchable);    //To change body of overridden methods use File | Settings | File Templates.
        initFlaps();
    }

    /**
     * Добавляет очередной кусок к шине. Для обеспечения ортогональности, все последующие куски прикрепяются к последнему.
     * Нельзя добавлять куски противоположного направления последнему.
     *
     * @param direction   Направление очередного кусочка шины.
     * @param length      Длина кусочка.
     * @param maxLength   Максимальная длина кусочка.
     * @param stretchable Возможность куска растягиваться.
     * @param movable     Возможность кусочка менять свое местоположение.
     */
    @Override
    public void addPart(Direction direction, double length, double maxLength, boolean stretchable, boolean movable) {
        super.addPart(direction, length, maxLength, stretchable, movable);    //To change body of overridden methods use File | Settings | File Templates.
        initFlaps();
    }

    @Override
    public void addPart(Direction direction, double length, double maxLength, boolean stretchable) {
        super.addPart(direction, length, maxLength, stretchable);    //To change body of overridden methods use File | Settings | File Templates.
        initFlaps();
    }

    private void initFlaps() {
//        if (!parts.isEmpty()) {
//            if (startFlap == null) {
//                startFlap = new Flap(parts.get(0).getAxis().getStart(), getMaterial(), Flap.Position.START);
//            } else {
//                startFlap.setCenter(parts.get(0).getAxis().getStart());
//            }
//            if (endFlap == null) {
//                endFlap = new Flap(parts.get(size() - 1).getAxis().getEnd(), getMaterial(), Flap.Position.END);
//            } else {
//                endFlap.setCenter(parts.get(size() - 1).getAxis().getEnd());
//            }
//        }
    }

    public void moveConnectedToStartFlap(Direction dir, double l) {
//        for (Contactable contactable : startFlap.getContactables()) {
//            if (contactable instanceof Bus) {
//                Bus bus = (Bus) contactable;
//                // we should get the last part and move it;
//                if (bus.hasParts()) {
//                    BusPart part = bus.getLastPart();
//                    part.move(dir, l);
//                }
//            }
//        }
    }

    public void moveConnectedToEndFlap(Direction dir, double l) {
//        for (Contactable contactable : endFlap.getContactables()) {
//            if (contactable instanceof Bus) {
//                Bus bus = (Bus) contactable;
//                // we should get the last part and move it;
//                if (bus.hasParts()) {
//                    BusPart part = bus.getFirstPart();
//                    part.move(dir, l);
//                }
//            }
//        }
    }

    /**
     * Перемещение кусочка шины без учета ограничений на длину,
     * только возможность растягиваться и двигаться.
     *
     * @param partIndex
     * @param direction
     * @param width
     * @return true, если получилось передвинуть заданный кусок, false иначе.
     */
    @Override
    public boolean directlyMovePart(int partIndex, Direction direction, double width) {
        /*Preconditions.checkElementIndex(partIndex, size());
        int size = size();
        if (size > 0 && size <= 1) {
            moveConnectedToEndFlap(direction, width);
            moveConnectedToEndFlap(direction, width);
        } else if (partIndex == 0) {
            moveConnectedToStartFlap(direction, width);
        } else if (partIndex == size - 1) {
            moveConnectedToEndFlap(direction, width);
        }*/

        return super.directlyMovePart(partIndex, direction, width);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
    }


    public Polygon getBounds() {
        return super.getBounds();
    }

    public void connectToStartFlap(Bus bus) {
//        if (startFlap != null) {
//            startFlap.addContactable(bus);
//        }
    }
    
    public void connectToEndFlap(Bus bus) {
//        if (endFlap != null) {
//            endFlap.addContactable(bus);
//        }
    }
}
