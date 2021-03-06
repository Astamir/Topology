package ru.etu.astamir.gui.widgets;

import net.miginfocom.swing.MigLayout;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.commands.CommandManager;
import ru.etu.astamir.compression.commands.DescribableCommand;
import ru.etu.astamir.gui.editor.ElementModel;
import ru.etu.astamir.gui.widgets.PlayerPanel;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public class CommandTrackerPanel extends JPanel{
    ElementModel elementModel;
    CommandManager<? extends DescribableCommand> commands;

    JList commandList;
    JPanel descriptionPanel;
    PlayerPanel commandPlayerPanel;

    public CommandTrackerPanel(ElementModel elementModel, CommandManager<? extends DescribableCommand> commands) {
        super(new MigLayout("ins 0"));
        this.elementModel = elementModel;
        this.commands = commands;
        commands.addListener(new CommandManager.Listener() {
            @Override
            public void commandAdded(List<? extends Command> commands) {
                commandList.setModel(new CommandListModel<>(CommandTrackerPanel.this.commands.getCommands()));
                commandList.repaint();
            }

            @Override
            public void commandRemoved(Command command) {

            }

            @Override
            public void commandExecuted(Command command) {

            }

            @Override
            public void commandUnexecuted(Command command) {

            }
        });
        initComponents();
    }

    private void initComponents() {
        commandPlayerPanel = new PlayerPanel();
        add(commandPlayerPanel, "wrap");

        commandList = new JList(new CommandListModel<>((List<DescribableCommand>) commands.getCommands()));
        add(new JScrollPane(commandList), "grow, push");
    }

    private JPanel createDescriptionPanel(Command command) {
        return null;
    }

    private static class CommandListModel<V extends Command> implements ListModel<V> {
        private List<V> commands = new ArrayList<>();
        private List<ListDataListener> listeners = new ArrayList<>();

        public CommandListModel(List<V> commands) {
            this.commands = commands;
        }

        @Override
        public int getSize() {
            return commands.size();
        }

        @Override
        public V getElementAt(int index) {
            return commands.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }
    }
}
