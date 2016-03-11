package ru.etu.astamir.launcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public interface Project {
    Path getProjectFilePath();

    void saveProject() throws IOException;

    void load();

    Map<String, Topology> getTopologies();

    String getName();


}
