package ru.etu.astamir.dao;

import ru.etu.astamir.compression.CompressorsPool;
import ru.etu.astamir.compression.DefaultCompressorsPool;
import ru.etu.astamir.gui.painters.*;
import ru.etu.astamir.launcher.Project;
import ru.etu.astamir.launcher.SimpleProject;
import ru.etu.astamir.launcher.VirtualTopology;
import ru.etu.astamir.model.technology.DefaultElementFactory;
import ru.etu.astamir.model.technology.ElementFactory;
import ru.etu.astamir.model.LayerFactory;
import ru.etu.astamir.model.technology.TechnologyFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectObjectManager {
    //Thread-safe static initialization - Brian Goetz "Java Concurrency in Practice", 16.2.3
    //All fields must be "final static" for thread safety

    /**
     * Only used in tests. <br><b>For correct synchronization the list must not be changed (just read) after class
     * (static) initialization.</b>
     */
    private static final List<ObjectManager> manager_list = new ArrayList<ObjectManager>();


    private static final ObjectManager<TechnologyFactory> TECHNOLOGY_FACTORY = new ObjectManager<TechnologyFactory>(TechnologyFactory.class);

    private static final ObjectManager<LayerFactory> LAYER_FACTORY = new ObjectManager<LayerFactory>(LayerFactory.class);

    private static final ObjectManager<ColorFactory> COLOR_FACTORY = new ObjectManager<ColorFactory>(DefaultColorFactory.class);

    private static final ObjectManager<ColorCentral> COLOR_CENTRAL = new ObjectManager<ColorCentral>(DefaultColorCentral.class);

    private static final ObjectManager<ElementFactory> ELEMENT_FACTORY = new ObjectManager<ElementFactory>(DefaultElementFactory.class);

    private static final ObjectManager<PainterCentral> PAINTER_CENTRAL = new ObjectManager<PainterCentral>(DefaultPainterCentral.class);

    private static final ObjectManager<CompressorsPool> COMPRESSORS_POOL = new ObjectManager<CompressorsPool>(DefaultCompressorsPool.class);


    public static TechnologyFactory getTechnologyFactory() {
       return TECHNOLOGY_FACTORY.getInstance();
   }

    public static LayerFactory getLayerFactory() {
        return LAYER_FACTORY.getInstance();
    }

    public static ColorFactory getColorFactory() {
        return COLOR_FACTORY.getInstance();
    }

    public static ColorCentral getColorCentral() {
        return COLOR_CENTRAL.getInstance();
    }

    public static ElementFactory getElementFactory() {
        return ELEMENT_FACTORY.getInstance();
    }

    public static PainterCentral getPainterCentral() {
        return PAINTER_CENTRAL.getInstance();
    }

    public static CompressorsPool getCompressorsPool() {
        return COMPRESSORS_POOL.getInstance();
    }

    /**
     * This method is for test purposes only.
     */
    public static void reset() {
        for (ObjectManager manager : manager_list)
            manager.reset();
    }

    /**
     * This class is thread safe. All field writes made after initialization are guarded by synchronization on object
     * instance.
     */
    private static class ObjectManager<T> {
        private volatile Class<? extends T> clazz;
        private volatile T instance;

        /**
         * This constructor is thread safe since it is only called from static initializers.
         *
         * @param clazz Implementation class.
         */
        public ObjectManager(final Class<? extends T> clazz) {
            this.clazz = clazz;
            manager_list.add(this);
        }

        /**
         * Synchronized for atomicity and mutual exclusiveness.
         */
        public synchronized void setClass(final Class<? extends T> clazz) {
            this.clazz = clazz;
            this.instance = null;
        }

        /**
         * Synchronized for atomicity and mutual exclusiveness.
         */
        public synchronized void setInstance(final T instance) {
            this.instance = instance;
        }

        public T getInstance() {
            //The purpose of "result" is to read non-null value of "instance" only once.
            //See Joshua Bloch "Effective Java, Second Edition", p.282, Item 71.
            //Field\variable read-write order is significant!
            T result = instance;
            if (result == null) {
                synchronized (this) {
                    result = instance;
                    if (result == null) {
                        if (clazz == null)
                            throw new IllegalStateException("Instance should be initialized");
                        try {
                            instance = result = clazz.newInstance();
                        } catch (InstantiationException e) {
                            throw new Error(e);
                        } catch (IllegalAccessException e) {
                            throw new Error(e);
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Synchronized for atomicity and mutual exclusiveness.
         */
        private synchronized void reset() {
            if (clazz != null)
                instance = null;
        }
    }

    private static Project CURRENT_PROJECT;

    public static Project getCurrentProject() {
        if (CURRENT_PROJECT == null) {
            CURRENT_PROJECT = new SimpleProject(new File("default_project.atop"), "default_project"); // TODO temp
            VirtualTopology defaultTopology = new VirtualTopology(VirtualTopology.VIRTUAL_MODE);
            VirtualTopology defaultTopology2 = new VirtualTopology(VirtualTopology.VIRTUAL_MODE);
            CURRENT_PROJECT.getTopologies().put("default_topology", defaultTopology);
            CURRENT_PROJECT.getTopologies().put("default_topology_2", defaultTopology2);
            CURRENT_PROJECT.load();
        }

        return CURRENT_PROJECT;
    }

    public synchronized static void setCurrentProject(Project currentProject) {
        ProjectObjectManager.CURRENT_PROJECT = currentProject;
    }
}