package ru.etu.astamir.gui.common;

import javax.swing.*;
import java.awt.*;

/**
 * @author Artem Mon'ko
 */
public class WindowUtils {
    public static Window getWindowForComponent(Component c) {
        if (c == null)
            return null;
        if (c instanceof Window)
            return (Window) c;
        return SwingUtilities.getWindowAncestor(c);
    }
}
