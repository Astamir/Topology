package ru.etu.astamir.model.wires;

import com.google.common.primitives.Ints;
import ru.etu.astamir.geom.common.*;
import ru.etu.astamir.model.*;
import ru.etu.astamir.serialization.LookIntoAttribute;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * Часть шины. Объект, который представляет собой
 * осевую линию и прямоугольную границу определяемую
 * некоторым расстоянием от оси.
 *
 * @author astamir
 * @version 1.0
 */
public class SimpleWire extends TopologyElement implements Movable, Stretchable, Serializable, Comparable<SimpleWire> {
	private static final long serialVersionUID = 1L;

	/**
	 * Осевая линия шины.
	 */
	Edge axis;

	/**
	 * Ширина шины.
	 */
	double width;

	/**
	 * Расстояние от осевой линии на границе шины.
	 */
	double widthAtBorder;

	/**
	 * Максимальная протяженность шины.
	 */
	double maxLength = Double.MAX_VALUE;

    /**
     * Минимальная длина шины.
     */
    double minLength;

	/**
	 * Говорит о том, может ли данный фрагмент шины растягиваться.
	 */
	boolean stretchable;

	/**
	 * Говорит о том, может ли данный кусочек шины менять свое местоположение в реальных координатах.
	 */
	boolean movable = true;

	/**
	 * Говорит о том, можно ли данный кусок шины деформировать.
	 */
	boolean deformable = true;

	Polygon bounds;

	transient Wire wire;

	private SimpleWire() {
	}

    public SimpleWire(Edge axis, double width) {
		super();
		this.axis = axis;
		this.width = width;
		this.widthAtBorder = width;
	}

    public SimpleWire(Edge axis) {
        super();
        this.axis = axis;
    }

	public boolean isPartOfComplex() {
		return wire != null;
	}

    public boolean isLink() {
        return isPartOfComplex() && axis.isPoint();
    }

	public Edge getAxis() {
		return axis;
	}

	public void setAxis(Edge axis) {
		this.axis = axis;
	}

	public int getIndex() {
		return wire != null ? wire.indexOf(this) : -1;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getWidthAtBorder() {
		return widthAtBorder;
	}

	public void setWidthAtBorder(double widthAtBorder) {
		this.widthAtBorder = widthAtBorder;
	}

	public double getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(double maxLength) {
		this.maxLength = maxLength;
	}

    public double getMinLength() {
        return minLength;
    }

    public void setMinLength(double minLength) {
        this.minLength = minLength;
    }

    public boolean isStretchable() {
		return stretchable;
	}

	public void setStretchable(boolean stretchable) {
		this.stretchable = stretchable;
	}

	public boolean isMovable() {
		return movable;
	}

	public void setMovable(boolean movable) {
		this.movable = movable;
	}

	public boolean isDeformable() {
		return deformable;
	}

	public void setDeformable(boolean deformable) {
		this.deformable = deformable;
	}

	public void setBounds(Polygon bounds) {
		this.bounds = bounds;
	}

    public Wire getWire() {
        return wire;
    }

    public void setWire(Wire wire) {
        this.wire = wire;
    }

    @Override
	public Collection<Point> getCoordinates() {
		return axis.getPoints();
	}

	@Override
	public TopologyLayer getLayer() {
		return wire != null ? wire.getLayer() : super.getLayer();
	}

	@Override
	public Material getMaterial() {
		return wire != null ? wire.getMaterial() : super.getMaterial();
	}

	@Override
    public boolean setCoordinates(Collection<Point> coordinates) {
        if (coordinates.size() < 2) {
            return false;
        }

        Point[] points = coordinates.toArray(new Point[coordinates.size()]);
        axis.setEdge(points[0], points[1]);

        rebuildBounds();

		return false;
    }

    /**
     * Перемещение куска шины независимо от шины. Использование этого метода
     * влечет за собой некооректное состояние шины, если такая есть.
     */
    boolean moveDirectly(double dx, double dy) {

        boolean success = axis.move(dx, dy);
		//if (success)
        	//rebuildBounds();

		return success;
    }

    /**
     * @see #moveDirectly(double, double)
     */
    void moveDirectly(Direction direction, double d) {
        double signedD = d * direction.getDirectionSign();
        if (direction.isLeftOrRight()) {
            moveDirectly(signedD, 0);
        } else {
            moveDirectly(0, signedD);
        }
    }

	@Override
	public boolean move(double dx, double dy) {
		return moveDirectly(dx, dy);
	}

	/**
	 * use #stretchDirectly(Point, Direction, double)
	 */
	@Deprecated
    boolean stretchDirectly(Direction direction, double d) {
        axis.stretch(direction, d);
        rebuildBounds();
		return true;
    }

    boolean stretchDirectly(Point base, Direction direction, double d) {
        axis.stretch(base, direction, d);
        rebuildBounds();
		return true;
    }

	@Override
	public boolean stretch(Direction direction, double length, Point stretchPoint) {
		return stretchDirectly(stretchPoint, direction, length);
	}

    public double length() {
        return axis.length();
    }

    /**
	 * Update bounds based on the current width and axis.
	 */
	void rebuildBounds() {
		bounds = buildBounds();
	}

	/**
	 * Build new bounds based on current width and axis.
	 */
	public Polygon buildBounds() {
		return Rectangle.of(axis, width, widthAtBorder);
	}

	@Override
	public Polygon getBounds() {
		if (bounds == null) {
			rebuildBounds();
		}

		return bounds;
	}

	@Override
	public SimpleWire clone() {
        SimpleWire clone = (SimpleWire) super.clone();
        clone.setAxis(axis.clone());
        clone.setWidth(width);
        clone.setWidthAtBorder(widthAtBorder);
        clone.setMaxLength(maxLength);
        clone.setMinLength(minLength);
        clone.setStretchable(stretchable);
        clone.setDeformable(deformable);
        clone.setMovable(movable);
        clone.rebuildBounds();

        return clone;
	}

	@Override
	public String toString() {
		return "SimpleWire{" + "axis=" + axis + "index = " + getIndex() + "}";
	}

    @Override
    public int compareTo(SimpleWire o) {
        return Ints.compare(getIndex(), o.getIndex());
    }

	public static class Builder {
		private final SimpleWire WIRE = new SimpleWire();

		public Builder() {
		}

		public Builder(Wire wire) {
			WIRE.wire = wire;
			WIRE.setLayer(wire.getLayer());
			WIRE.setMaterial(wire.getMaterial());
            WIRE.width = wire.width;
            WIRE.widthAtBorder = wire.widthAtBorder;
		}

        public Builder(SimpleWire other) {
            WIRE.wire = other.wire;
            WIRE.axis = other.axis;
            WIRE.width = other.width;
            WIRE.widthAtBorder = other.widthAtBorder;
            WIRE.maxLength = other.maxLength;
            WIRE.minLength = other.minLength;
            WIRE.stretchable = other.stretchable;
            WIRE.movable = other.movable;
            WIRE.deformable = other.deformable;
            WIRE.bounds = other.bounds;
			WIRE.setMaterial(other.getMaterial());
			WIRE.setLayer(other.getLayer());
        }

		public SimpleWire build() {
			return WIRE.clone();
		}

		public Builder setAxis(Edge axis) {
			WIRE.axis = axis;
			return this;
		}

		public Builder setWidth(double width) {
			WIRE.width = width;
			return this;
		}

		public Builder setWidthAtBorder(double widthAtBorder) {
			WIRE.widthAtBorder = widthAtBorder;
			return this;
		}

		public Builder setMaxLength(double maxLength) {
			WIRE.maxLength = maxLength;
			return this;
		}

        public Builder setMinLength(double minLength) {
            WIRE.minLength = minLength;
            return this;
        }

		public Builder setStretchable(boolean stretchable) {
			WIRE.stretchable = stretchable;
			return this;
		}

		public Builder setMovable(boolean movable) {
			WIRE.movable = movable;
			return this;
		}

		public Builder setDeformable(boolean deformable) {
			WIRE.deformable = deformable;
			return this;
		}

		public Builder setBounds(Polygon bounds) {
			WIRE.bounds = bounds;
			return this;
		}

        public Builder setName(String name) {
            WIRE.name = name;
            return this;
        }
	}
}
