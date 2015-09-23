package ru.etu.astamir.compression.commands;


/**
 * Command interface contains do and undo methods.
 */
public interface Command {
    /**
     * Execute the command.
     */
    boolean execute();

    /**
     * Undo the execution.
     */
    boolean unexecute();
}
