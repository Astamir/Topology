package ru.etu.astamir.gui.widgets;

import com.google.common.collect.Sets;
import net.miginfocom.swing.MigLayout;
import ru.etu.astamir.gui.IconFactory;
import ru.etu.astamir.gui.common.ComponentHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Some player like behaviour. To use it, simply add it to layout and register listener
 * to get player actions.
 * @author Artem Mon'ko
 */
public class PlayerPanel extends JPanel implements ActionListener{
    private JButton firstButton;
    private JButton previousButton;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton nextButton;
    private JButton lastButton;

    private List<PlayerListener> listeners = new ArrayList<>();

    private enum State {
        PLAYING, PAUSED, STOPPED
    }
    private State currentState = State.STOPPED;

    private static final String PLAY_COMMAND = "player_panel_play_command";
    private static final String PAUSE_COMMAND = "player_panel_pause_command";
    private static final String STOP_COMMAND = "player_panel_stop_command";
    private static final String NEXT_COMMAND = "player_panel_next_command";
    private static final String PREVIOUS_COMMAND = "player_panel_previous_command";
    private static final String FIRST_COMMAND = "player_panel_first_command";
    private static final String LAST_COMMAND = "player_panel_last_command";

    private Set<String> availableCommands = Sets.newHashSet(PREVIOUS_COMMAND, NEXT_COMMAND, FIRST_COMMAND, LAST_COMMAND);

    private static interface PlayerListener {
        void play();
        void pause();
        void next();
        void previous();
        void first();
        void last();
        void stop();
    }

    public static abstract class PlayerAdapter implements PlayerListener{
        @Override
        public void play() {}

        @Override
        public void pause() {}

        @Override
        public void next(){}

        @Override
        public void previous() {}

        @Override
        public void first() {}

        @Override
        public void last() {}

        @Override
        public void stop() {}
    }

    public PlayerPanel() {
        super(new MigLayout("insets 0 0 0 0, hidemode 3"));
        initComponents();
    }

    private void initComponents() {
        firstButton = new JButton(IconFactory.makeImageIcon(PlayerPanel.class, "player_panel_first_item.png"));
        ComponentHelper.setFixedComponentSize(firstButton, 32, 32);
        firstButton.addActionListener(this);
        firstButton.setActionCommand(FIRST_COMMAND);
        add(firstButton);

        previousButton = new JButton(IconFactory.makeImageIcon(PlayerPanel.class, "player_panel_previous_item.png"));
        ComponentHelper.setFixedComponentSize(previousButton, 32, 32);
        previousButton.addActionListener(this);
        previousButton.setActionCommand(PREVIOUS_COMMAND);
        add(previousButton, "gapleft 1");

        playButton = new JButton(IconFactory.makeImageIcon(PlayerPanel.class, "player_panel_play_item.png"));
        ComponentHelper.setFixedComponentSize(playButton, 32, 32);
        playButton.addActionListener(this);
        playButton.setActionCommand(PLAY_COMMAND);
        add(playButton, "gapleft 1");

        pauseButton = new JButton(IconFactory.makeImageIcon(PlayerPanel.class, "player_panel_pause_item.png"));
        ComponentHelper.setFixedComponentSize(pauseButton, 32, 32);
        pauseButton.addActionListener(this);
        pauseButton.setActionCommand(PAUSE_COMMAND);
        add(pauseButton, "gapleft 1");

        stopButton = new JButton(IconFactory.makeImageIcon(PlayerPanel.class, "player_panel_stop_item.png"));
        ComponentHelper.setFixedComponentSize(stopButton, 32, 32);
        stopButton.addActionListener(this);
        stopButton.setActionCommand(STOP_COMMAND);
        add(stopButton, "gapleft 1");

        nextButton = new JButton(IconFactory.makeImageIcon(PlayerPanel.class, "player_panel_next_item.png"));
        ComponentHelper.setFixedComponentSize(nextButton, 32, 32);
        nextButton.addActionListener(this);
        nextButton.setActionCommand(NEXT_COMMAND);
        add(nextButton, "gapleft 1");

        lastButton = new JButton(IconFactory.makeImageIcon(PlayerPanel.class, "player_panel_last_item.png"));
        ComponentHelper.setFixedComponentSize(lastButton, 32, 32);
        lastButton.addActionListener(this);
        lastButton.setActionCommand(LAST_COMMAND);
        add(lastButton, "gapleft 1");

        updateVisibility();
    }

    private void updateVisibility() {
        playButton.setVisible(currentState == State.PAUSED);
        pauseButton.setVisible(currentState == State.PLAYING);
        stopButton.setEnabled(currentState != State.STOPPED);

        for (Component component : getComponents()) {
            if (component instanceof JButton) {
                component.setVisible(availableCommands.contains(((JButton)component).getActionCommand()));
            }
        }
    }

    public void addPlayerListener(PlayerListener listener) {
        listeners.add(listener);;
    }

    public void removePlayerListener(PlayerListener listener) {
        listeners.remove(listener);
    }

    private void commandExecuted(String command) {
        switch (command) {
            case FIRST_COMMAND: {
                if (currentState == State.PLAYING) {
                    currentState = State.PAUSED;
                }
                for (PlayerListener listener : listeners) {
                    listener.first();
                }
                break;
            }
            case PREVIOUS_COMMAND:{
                if (currentState == State.PLAYING) {
                    currentState = State.PAUSED;
                }
                for (PlayerListener listener : listeners) {
                    listener.previous();
                }
                break;
            }
            case PLAY_COMMAND:{
                if (currentState != State.PLAYING) {
                    currentState = State.PLAYING;
                }
                for (PlayerListener listener : listeners) {
                    listener.play();
                }
                break;
            }
            case PAUSE_COMMAND:{
                if (currentState != State.PAUSED) {
                    currentState = State.PAUSED;
                }
                for (PlayerListener listener : listeners) {
                    listener.pause();
                }
                break;
            }
            case STOP_COMMAND:{
                currentState = State.STOPPED;
                for (PlayerListener listener : listeners) {
                    listener.stop();
                }
                break;
            }
            case NEXT_COMMAND:{
                if (currentState == State.PLAYING) {
                    currentState = State.PAUSED;
                }
                for (PlayerListener listener : listeners) {
                    listener.next();
                }
                break;
            }
            case LAST_COMMAND:{
                if (currentState == State.PLAYING) {
                    currentState = State.PAUSED;
                }
                for (PlayerListener listener : listeners) {
                    listener.last();
                }
                break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        commandExecuted(e.getActionCommand());
        updateVisibility();
    }
}
