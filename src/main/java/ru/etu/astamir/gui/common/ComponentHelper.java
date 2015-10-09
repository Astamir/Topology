package ru.etu.astamir.gui.common;

import java.awt.*;

/**
 * Utility methods for UI components.
 */
public class ComponentHelper {
    /**
     * Set component minimum, preferred and maximum width.
     */
    public static void setFixedComponentWidth(Component component, int width) {
        component.setMinimumSize(new Dimension(width, component.getMinimumSize().height));
        component.setPreferredSize(new Dimension(width, component.getPreferredSize().height));
        component.setMaximumSize(new Dimension(width, component.getMaximumSize().height));
    }

    public static <T extends Component> T withMinWidth(int width, T component) {
        component.setMinimumSize(new Dimension(width, component.getMinimumSize().height));
        return component;
    }

    public static <T extends Component> T withFixedWidth(int width, T component) {
        component.setMinimumSize(new Dimension(width, component.getMinimumSize().height));
        component.setPreferredSize(new Dimension(width, component.getPreferredSize().height));
        component.setMaximumSize(new Dimension(width, component.getMaximumSize().height));
        return component;
    }

    /**
     * Set component minimum, preferred and maximum width and height.
     */
    public static void setFixedComponentSize(Component component, int width, int height) {
        component.setMinimumSize(new Dimension(width, height));
        component.setPreferredSize(new Dimension(width, height));
        component.setMaximumSize(new Dimension(width, height));
    }

    public static void setMinAndPrefComponentSize(Component component, int width, int height) {
        component.setMinimumSize(new Dimension(width, height));
        component.setPreferredSize(new Dimension(width, height));
    }

    /**
     * @return the predecessor of the specified level for the given component or null
     */
    public static Component getParent(Component component, int level) {
        for (; level > 0; level--)
            if (component.getParent() == null)
                return null;
            else
                component = component.getParent();
        return component;
    }
}