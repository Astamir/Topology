package ru.etu.astamir.model.legacy;

import com.google.common.base.Preconditions;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.geom.common.Rectangle;
import ru.etu.astamir.model.Drawable;
import ru.etu.astamir.model.Material;
import ru.etu.astamir.model.legacy.LegacyContactFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Контактное окно. Область контакта используемая для подлкючения ??.
 * На эскизе не отображается.
 */
public class LegacyContactWindow implements Drawable {
    private Rectangle window;

    private Point center;

    private Material material;

    private Stroke stroke = LegacyContactFactory.createDefaultContactStroke();

    private Color color = Color.BLACK;

    public LegacyContactWindow(Point center, Rectangle window) {
        this.window = window;
        this.center = center;
    }

    public LegacyContactWindow(Rectangle window) {
        this.window = Preconditions.checkNotNull(window);
        this.center = window.getCenter();
    }

    public Rectangle getWindow() {
        return window;
    }

    public void setWindow(Rectangle window) {
        this.window = window;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    @Override
    public void draw(Graphics2D g) {
        Graphics2D ourGraphics = (Graphics2D) g.create();
        ourGraphics.setStroke(stroke);
        ourGraphics.setColor(color);
        window.draw(g);
        ourGraphics.dispose();
    }
}
