package ru.etu.astamir.compression.commands;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This command consists of several commands, that should be treated as single command by
 * command manager.
 * @author Artem Mon'ko
 */
public class CompositeCommand implements Command{
    Queue<Command> commands = new LinkedList<>();
    Queue<Command> executed_commands = new LinkedList<>();

    public CompositeCommand(Command... commands) {
        this(Arrays.asList(commands));
    }

    public CompositeCommand(Iterable<Command> commands) {
        this.commands = Lists.newLinkedList(commands);
    }

    @Override
    public boolean execute() {
        for (Command command : commands) {
            if (!command.execute()) {
                unexecute();
                return false;
            }

            executed_commands.add(command);
        }
        return true;
    }

    @Override
    public boolean unexecute() {
        boolean success = true;
        for (Command executed_command : executed_commands) {
            success &= executed_command.unexecute();
        }

        return success;
    }
}
