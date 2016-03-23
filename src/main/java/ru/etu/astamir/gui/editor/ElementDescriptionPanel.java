package ru.etu.astamir.gui.editor;


import net.miginfocom.swing.MigLayout;
import ru.etu.astamir.gui.common.ElementContainer;
import ru.etu.astamir.model.TopologyElement;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ElementDescriptionPanel extends JPanel implements ElementContainer{

    private JList<TopologyElement> list;
    private ElementListModel model = new ElementListModel();

    public ElementDescriptionPanel() {
        super(new MigLayout("insets 0"));
        initComponents();
    }

    private void initComponents() {
        list = new JList<>(model);
        add(new JScrollPane(list), "grow, push, wrap");
        add(new JLabel("Status bar"), "growx, pushx");
    }

    @Override
    public void addElement(TopologyElement element) {
        model.addElement(element);
    }

    @Override
    public void removeElement(TopologyElement element) {
        model.removeElement(element);
    }

    private static class ElementListModel extends AbstractListModel<TopologyElement> {
        private List<TopologyElement> elements = new ArrayList<>();
        @Override
        public int getSize() {
            return elements.size();
        }

        @Override
        public TopologyElement getElementAt(int index) {
            return elements.get(index);
        }

        public void addElement(TopologyElement element) {
            if (!elements.contains(element)) {
                elements.add(element);
                int index = elements.indexOf(element);
                fireContentsChanged(this, index, index);
            }
        }

        public void removeElement(TopologyElement element) {
            if (elements.remove(element)) {
                fireContentsChanged(this, 0, elements.size() - 1);
            }
        }
    }
}
