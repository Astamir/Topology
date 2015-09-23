package ru.etu.astamir.launcher;


import ru.etu.astamir.model.technology.CMOSTechnology;
import ru.etu.astamir.model.technology.CharacteristicParser;
import ru.etu.astamir.model.technology.DefaultTechnologicalCharacteristics;
import ru.etu.astamir.model.technology.Technology;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Astamir
 */
public class SimpleProject implements Project, Serializable{
    private File projectFile;
    private String projectName;

    private Map<String, Topology> topologies = new HashMap<>();


    public SimpleProject(File projectFile, String projectName) {
        this.projectFile = projectFile;
        this.projectName = projectName;
    }

    @Override
    public Path getProjectFilePath() {
        return projectFile.toPath();
    }

    @Override
    public void saveProject() throws IOException {
        try (ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(projectFile)))) {
            stream.writeObject(this);
            stream.flush();
            System.out.println("writing project \"" + projectName +"\" to " + projectFile.getAbsolutePath());
        }
    }

    @Override
    public void load() {
        try (ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(projectFile)))){
            SimpleProject project = (SimpleProject) stream.readObject();
            topologies = project.topologies;
            VirtualTopology topology = (VirtualTopology) topologies.get("default_topology");
            if (topology != null) {
                Technology technology = topology.getTechnology();
                if (technology == null) {
                    Technology.TechnologicalCharacteristics.Base base = new DefaultTechnologicalCharacteristics();
                    CharacteristicParser parser = new CharacteristicParser(new File("tehnol.txt"), base);
                    parser.parse();

                    topology.setTechnology(new CMOSTechnology("def", base));
                }

            }
            projectName = project.projectName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Topology> getTopologies() {
        return topologies;
    }

    @Override
    public String getName() {
        return projectName;
    }

    // get elements cache
    // get painters
    // get distances cache
    // get serialization subsystem
    // ...
}
