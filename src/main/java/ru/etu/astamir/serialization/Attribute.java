package ru.etu.astamir.serialization;

import java.util.Objects;

/**
 * Created by Astamir on 03.03.14.
 */
public interface Attribute {
    String getName();

    Object getValue();

    boolean isLocal();

    boolean isReference();

    boolean isSimple();
}
