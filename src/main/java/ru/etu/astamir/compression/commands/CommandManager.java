package ru.etu.astamir.compression.commands;

import com.google.common.base.Preconditions;

import java.util.*;

// todo add cursor;
public class CommandManager<T extends Command> {
    public static final int STOP_ON_FAIL = 1;
    public static final int CONTINUE_ON_FAIL = 2;

    Deque<T> commands = new LinkedList<>();
    Deque<T> history = new LinkedList<>();
    Deque<T> failed_commands = new LinkedList<>();

    private int fail_policy = CONTINUE_ON_FAIL;

    public CommandManager(Collection<T> commands) {
        this.commands.addAll(commands);
    }

    public CommandManager() {
    }

    public void addCommand(T command) {
        commands.add(Preconditions.checkNotNull(command));
    }

    public void executeNext() {
        T next_command = commands.poll();
        if (next_command != null) {
            boolean success = next_command.execute();
            log(next_command); // todo
            if (!success) {
                next_command.unexecute();
            }
        }
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
        failed_commands.clear();
    }

    private void log(T command) {
        StringBuilder log = new StringBuilder();
        log.append(command.getClass().getSimpleName()).append(" was executed - ");
        log.append(command);
        System.out.println(log.toString());
    }

    public void executeAll() {

    }

    public void rollback() {

    }


}
