package ru.etu.astamir.compression.commands.compression;

import ru.etu.astamir.compression.commands.DescribableCommand;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.util.concurrent.Callable;

/**
 * @author Artem Mon'ko
 */
public class SimpleDescribableCommand implements DescribableCommand {
    private Callable<Boolean> function;
    private Callable<Boolean> undo;
    private String description;

    public SimpleDescribableCommand(Callable<Boolean> function, Callable<Boolean> undo, String description) {
        this.function = function;
        this.undo = undo;
        this.description = description;
    }

    @Override
    public Object getDescription() {
        return description;
    }

    @Override
    public boolean execute() {
        try {
            return function.call();
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public boolean unexecute() {
        try {
            return undo.call();
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    public static SimpleDescribableCommand of(final Runnable func, String desc) {
        return new SimpleDescribableCommand(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                func.run();
                return true;
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }, desc);
    }
}
