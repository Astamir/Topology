package ru.etu.astamir.compression.commands;

/**
 * @author Artem Mon'ko
 */
public interface DescribableCommand extends Command {
    Object getDescription();
}
