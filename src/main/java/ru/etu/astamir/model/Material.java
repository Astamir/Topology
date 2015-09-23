package ru.etu.astamir.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Перечисление возможных материалов и их свойств.
 * Например металл, кремний, поликремний и т.д.
 */
@XmlRootElement
public enum Material {
    METAL, SILICON, POLYSILICON, UNKNOWN
}
