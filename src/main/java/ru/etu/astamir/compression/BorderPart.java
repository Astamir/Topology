package ru.etu.astamir.compression;

import com.google.common.base.Preconditions;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Polygon;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.regions.ContactWindow;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.wires.SimpleWire;
import ru.etu.astamir.model.wires.Wire;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Элемент частокола.
 */
public class BorderPart {
    /**
     * Отрезок в реальных координатах.
     */
    private Edge axis;

    /**
     * Сам элемент, на случай если нужны дополнительные проверки на расстояние.
     */
    private TopologyElement element;

    /**
     * Символ элемента.
     */
    private String symbol;


    public BorderPart(Edge axis, TopologyElement element, String symbol) {
        this.axis = axis;
        this.element = element;
        this.symbol = Preconditions.checkNotNull(symbol);
    }

    public BorderPart(Edge axis, String symbol) {
        this.axis = axis;
        this.symbol = Preconditions.checkNotNull(symbol);
    }

    static List<BorderPart> of(TopologyElement element) {
        return of(element, Direction.UNDETERMINED);
    }

    public static List<BorderPart> of(TopologyElement element, Direction direction) {
        List<BorderPart> parts = new ArrayList<>();
        if (element instanceof Contact) {
            for (ContactWindow contactWindow : ((Contact) element).getContactWindows().values()) {
                parts.addAll(of(contactWindow));
            }
        } else if (element instanceof SimpleWire) {
            parts.add(of((SimpleWire)element, (String) null));
        } else if (element instanceof Wire) {
            for (SimpleWire wire : ((Wire) element).getParts()) {
                parts.add(of(wire, element.getSymbol()));
            }
        } else if (element instanceof Contour) {
            if (direction.isDetermined()) {
                Polygon bounds = element.getBounds();
                if (bounds.isRectangle()) {
                    Rectangle rectangle = new Rectangle(bounds);
                    parts.add(new BorderPart(rectangle.getEdge(direction), element, element.getSymbol()));
                }
            } else {
                for (Edge edge : ((Contour) element).toEdges()) {
                    parts.add(new BorderPart(edge, element, element.getSymbol()));
                }
            }
        } else {
            throw new NotImplementedException();
        }
        return parts;
    }

    public static BorderPart of(SimpleWire wire, String symbol) {
        return new BorderPart(wire.getAxis(), wire, wire.getSymbol() != null ? wire.getSymbol() : symbol);
    }

    public Edge getAxis() {
        return axis;
    }

    public TopologyElement getElement() {
        return element;
    }

    public void setElement(TopologyElement element) {
        this.element = element;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void correct() {
        if (!axis.getDirection().isUpOrRight()) {
            axis.reverse();
        }
    }

    public boolean isOfType(Class<? extends TopologyElement> clazz) {
        if (element != null) {
            return clazz.equals(element.getClass());
        } else {
            Class<? extends TopologyElement> entityClass = ProjectObjectManager.getElementFactory().getEntityClass(symbol);
            return clazz.equals(entityClass);
        }
    }

    public static Comparator<BorderPart> getAxisComparator(final Comparator<Edge> edgeComparator) {
        return (o1, o2) -> {
            if (o1 != null && o2 != null) {
                return edgeComparator.compare(o1.getAxis(), o2.getAxis());
            }

            return 0;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BorderPart that = (BorderPart) o;

        if (axis != null ? !axis.equals(that.axis) : that.axis != null) return false;
        if (element != null ? !element.equals(that.element) : that.element != null) return false;
        if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (axis != null ? axis.hashCode() : 0);
        result = 31 * result + (element != null ? element.hashCode() : 0);
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{" + axis + "}[" + symbol + "]";
    }
}
