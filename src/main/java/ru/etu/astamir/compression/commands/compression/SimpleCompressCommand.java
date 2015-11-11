package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.compression.commands.DescribableCommand;

/**
 * @author Artem Mon'ko
 */
public class SimpleCompressCommand implements DescribableCommand{
    private BorderPart borderPart;
    private DescribableCommand targetCommand;

    public SimpleCompressCommand(DescribableCommand targetCommand, BorderPart borderPart) {
        this.borderPart = borderPart;
        this.targetCommand = targetCommand;
    }

    @Override
    public Object getDescription() {
        return targetCommand.getDescription() + " with border = " + borderPart;
    }

    @Override
    public boolean execute() {
        return targetCommand.execute();
    }

    @Override
    public boolean unexecute() {
        return targetCommand.unexecute();
    }

    public DescribableCommand getTargetCommand() {
        return targetCommand;
    }

    public BorderPart getBorderPart() {
        return borderPart;
    }

    public void setBorderPart(BorderPart borderPart) {
        this.borderPart = borderPart;
    }
}
