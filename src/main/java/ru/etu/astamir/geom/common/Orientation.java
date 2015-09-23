package ru.etu.astamir.geom.common;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 18.09.12
 * Time: 18:59
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
public enum Orientation {
    HORIZONTAL, VERTICAL, BOTH;

    public Orientation getOppositeOrientation() {
		if (this == BOTH) {
			return BOTH;
		}

        return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
    }

    public boolean isOrthogonal(Orientation orientation) {
        return this != BOTH && orientation.getOppositeOrientation().equals(this);
    }

    public Direction toDirection(double sign) {
		if (this == BOTH) {
			return Direction.UNDETERMINED;
		}

        if (sign >= 0) {
            return this == HORIZONTAL ? Direction.RIGHT : Direction.UP;
        } else {
            return this == HORIZONTAL ? Direction.LEFT : Direction.DOWN;
        }
    }

    public Collection<Direction> getDirections() {
        switch (this) {
           case HORIZONTAL: return Lists.newArrayList(Direction.LEFT, Direction.RIGHT);
           case VERTICAL: return Lists.newArrayList(Direction.UP, Direction.DOWN);
           default:return Collections.emptyList();
        }
    }
}
