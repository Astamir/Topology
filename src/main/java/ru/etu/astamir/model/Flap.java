package ru.etu.astamir.model;

import ru.etu.astamir.geom.common.Edge;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.contacts.Contact;

import java.util.HashMap;
import java.util.Map;

/**
 * Закрылок затвора.
 */
public class Flap extends Contact {
    public static enum Position{START, END}

    private Position position;

	public Flap(String name, Point center, Position position) {
		super(name, Edge.of(center));
		this.position = position;
	}

    public Flap(Position position) {
        this(Point.of(0, 0), position);
    }

    public Flap(Point center, Position position) {
        super(Edge.of(center));
        this.position = position;
    }

    public static Map<Position, Flap> createFlaps() {
        Map<Position, Flap> result = new HashMap<>();
        result.put(Position.START, new Flap(Position.START));
        result.put(Position.END, new Flap(Position.END));

        return result;
    }

	public Position getPosition() {
        return position;
    }

    @Override
    public Flap clone() {
        Flap clone = (Flap) super.clone();
        clone.position = position;

        return clone;
    }
}
