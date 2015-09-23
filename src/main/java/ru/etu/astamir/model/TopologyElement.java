package ru.etu.astamir.model;

import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Polygon;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Artem Mon'ko
 */
public abstract class TopologyElement extends Entity implements Serializable, Cloneable {
	/**
	 * Слой, к которому принадлежит элемент.
	 */
	private TopologyLayer layer = ProjectObjectManager.getLayerFactory().createDefaultTopologyLayer();

	/**
	 * Материал элемента. Часто нам он не важен, либо совпадает с
	 * материалом слоя.
	 */
	private Material material = Material.UNKNOWN;

	/**
	 * Тип проводимости элемента.
	 */
	private ConductionType conductionType = ConductionType.UNKNOWN;

    protected String symbol;

	public TopologyElement(String name) {
		super(name);
	}

    public TopologyElement(String name, String symbol) {
        super(name);
        this.symbol = symbol;
    }

	protected TopologyElement() {
	}

	public TopologyLayer getLayer() {
		return layer;
	}

	public void setLayer(TopologyLayer layer) {
		this.layer = layer;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public ConductionType getConductionType() {
		return conductionType;
	}

	public void setConductionType(ConductionType conductionType) {
		this.conductionType = conductionType;
	}

	public abstract Collection<Point> getCoordinates();

    public abstract void setCoordinates(Collection<Point> coordinates);

    public abstract Polygon getBounds();

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public TopologyElement clone()  {
        TopologyElement clone = (TopologyElement) super.clone();
        clone.setConductionType(conductionType);
        clone.setMaterial(material);
        clone.setLayer(layer.clone());

        return clone;
    }
}
