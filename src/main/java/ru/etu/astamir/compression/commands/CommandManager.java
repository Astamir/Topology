package ru.etu.astamir.compression.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.*;

// todo add cursor;
public class CommandManager<T extends Command> {
    public static final int STOP_ON_FAIL = 1;
    public static final int CONTINUE_ON_FAIL = 2;

    Deque<T> commands = new LinkedList<>();
    Deque<T> history = new LinkedList<>();
    Deque<T> failedCommands = new LinkedList<>();
    Deque<T> executedCommands = new LinkedList<>();

    List<Listener> listeners = new ArrayList<>();

    private int failPolicy = CONTINUE_ON_FAIL;

    public CommandManager(List<T> commands) {
        this.commands.addAll(commands);
        fireCommandAdded(commands);
    }

    public CommandManager() {
    }

    public void addCommand(T command) {
        commands.add(Preconditions.checkNotNull(command));
        fireCommandAdded(Collections.singletonList(command));
    }

    public boolean executeNext() {
        boolean success = false;
        T nextCommand = commands.poll();
        if (nextCommand != null) {
            success = nextCommand.execute();
            log(nextCommand); // todo
            if (!success) {
                nextCommand.unexecute();
                failedCommands.add(nextCommand);
            }

            executedCommands.add(nextCommand);
            fireCommandExecuted(nextCommand);
        }

        return success;
    }

    // todo remove later, debug for now
    public Command peek() {
        return commands.peek();
    }

    public boolean hasNext() {
        return !commands.isEmpty();
    }

    public void clear() {
        commands.clear();
        history.clear();
        failedCommands.clear();
    }

    public ImmutableList<T> getCommands() {
        return ImmutableList.copyOf(commands);
    }

    private void log(T command) {
        StringBuilder log = new StringBuilder();
        log.append(command.getClass().getSimpleName()).append(" was executed - ");
        log.append(command);
        System.out.println(log.toString());
    }

    public void executeAll() {
        while (hasNext()) {
            Command currentCommand = peek();
            boolean success = executeNext();
            if (!success) {
                System.out.println("Unable to execute command: " + currentCommand.toString() + ", rolling back now");
                rollback();
            }
        }
    }

    public void rollback() {
        while (!executedCommands.isEmpty()) {
            T commandToUnexecute = executedCommands.pop();
            commandToUnexecute.unexecute();
        }
        while (!failedCommands.isEmpty()) {
            T command_to_unexecute = failedCommands.pop();
            command_to_unexecute.unexecute();
        }
    }

    private void fireCommandAdded(List<? extends Command> commands) {
        for (Listener listener : listeners) {
            listener.commandAdded(commands);
        }
    }

    private void fireCommandExecuted(T command) {
        for (Listener listener : listeners) {
            listener.commandExecuted(command);
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public static interface Listener {
        void commandAdded(List<? extends Command> commands);
        void commandRemoved(Command command);
        void commandExecuted(Command command);
        void commandUnexecuted(Command command);
    }
}
