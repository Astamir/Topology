package ru.etu.astamir.launcher;

import ru.etu.astamir.compression.TopologyParser;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.gui.editor.MainFrame;

import javax.swing.*;
import java.io.*;

/**
 * @author Astamir
 */
public class ApplicationLauncher {

    public static void main(String[] args) throws IOException {
        // init object manager
        // init look and feel

        loadProject();

        // open main frame
        MainFrame mw = new MainFrame();
        mw.setTitle(ProjectObjectManager.getCurrentProject().getName());
        mw.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mw.setSize(600, 600);
        mw.setVisible(true);
    }

    private static void loadProject() throws IOException {
        Project project = ProjectObjectManager.getCurrentProject();

        try (ObjectOutputStream create = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(project.getProjectFilePath().toFile())))) {
            create.writeObject(project);
            create.flush();
        }

        project.load();
        // load default topology
        File topologyFile = new File("default_topology.txt");
        if (topologyFile.exists()) {
            TopologyParser parser = new TopologyParser(topologyFile);
            parser.parse();
            VirtualGrid elements = parser.getElements();
            VirtualTopology default_topology = (VirtualTopology) project.getTopologies().get("default_topology");
            default_topology.setMode(VirtualTopology.VIRTUAL_MODE);
            default_topology.setVirtual(elements);
        }
    }
}
