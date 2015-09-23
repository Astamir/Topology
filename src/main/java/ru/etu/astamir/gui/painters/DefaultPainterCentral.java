package ru.etu.astamir.gui.painters;

import ru.etu.astamir.compression.Border;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Orientation;
import ru.etu.astamir.model.Entity;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.regions.Contour;
import ru.etu.astamir.model.wires.Gate;
import ru.etu.astamir.model.wires.Wire;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Artem Mon'ko
 */
public class DefaultPainterCentral implements PainterCentral {
    private static final Painter<Entity> EMPTY_PAINTER = new EmptyPainter();
    private Map<Class<? extends Entity>, Painter<? extends Entity>> defaultPainters = new HashMap<>();
    private Map<String, Painter<?>> customPainters = new HashMap<>();


    public DefaultPainterCentral() {
        init();
    }

    private void init() {
        defaultPainters.put(TopologyElement.class, new TopologyElementPainter());
        defaultPainters.put(Wire.class, new WirePainter());
        defaultPainters.put(Contact.class, new ContactPainter());
        defaultPainters.put(Contour.class, new ContourPainter());
    }

    @Override
    public Painter getEntityPainter(Entity entity) {
        Class<? extends Entity> aClass = entity.getClass();
        if (defaultPainters.containsKey(aClass)) {
            return defaultPainters.get(entity.getClass());
        }

        Class superClass = aClass;
        do {
            superClass = superClass.getSuperclass();
            if (defaultPainters.containsKey(superClass)) {
                return defaultPainters.get(superClass);
            }
        } while (superClass != Object.class && superClass != Entity.class);

        return EMPTY_PAINTER;
    }

    @Override
    public void addCustomPainter(String key, Painter painter) {
        customPainters.put(key, painter);
    }

    @Override
    public Painter getCustomPainter(String key) {
        return customPainters.get(key);
    }

    public static void main(String[] args) {
        PainterCentral painterCentral = ProjectObjectManager.getPainterCentral();
        System.out.println(painterCentral.getEntityPainter(new Gate("", Orientation.BOTH)).getClass());
    }
}
