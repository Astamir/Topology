package ru.etu.astamir.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Тип проводимости. По идее это либо p либо n проводимость.
 * Возможны еще некоторые свойства.
 */
@XmlRootElement
public enum ConductionType {
    P, PP, PPP, N, NN, NNN, UNKNOWN
}
