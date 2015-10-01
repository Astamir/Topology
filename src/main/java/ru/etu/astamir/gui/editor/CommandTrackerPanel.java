package ru.etu.astamir.gui.editor;

import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.commands.CommandManager;
import ru.etu.astamir.gui.widgets.PlayerPanel;

import javax.swing.*;

/**
 * @author Artem Mon'ko
 */
public class CommandTrackerPanel extends JPanel{
    ElementModel elementModel;
    CommandManager commands;

    JList list;
    JPanel descriptionPanel;
    PlayerPanel commandPlayerPanel;

    public CommandTrackerPanel(ElementModel elementModel, CommandManager commands) {

    }

    private JPanel createDescriptionPanel(Command command) {
        return null;
    }

}
