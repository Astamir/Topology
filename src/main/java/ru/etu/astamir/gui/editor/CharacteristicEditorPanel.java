package ru.etu.astamir.gui.editor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.sun.java.swing.plaf.windows.WindowsGraphicsUtils;
import javafx.scene.control.Tab;
import net.miginfocom.swing.MigLayout;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.gui.common.WindowUtils;
import ru.etu.astamir.gui.widgets.DropDownPanel;
import ru.etu.astamir.model.technology.Technology;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Панель просмотра и редактирвоания технологических норм.
 * @author Artem Mon'ko
 */
public class CharacteristicEditorPanel extends JPanel {
    private static final String[] TWO_COLUMNS = new String[]{"Символ1", "Символ2", "Значение"};
    private static final String[] ONE_COLUMNS = new String[]{"Символ", "Значение"};
    private JTable distanceTable;

    private JTable widthsTable;
    private JTable overlapsTable;
    private JTable includesTable;

    private JButton importButton;
    private JButton saveButton;
    private JButton cancelButton;

    private Technology.TechnologicalCharacteristics.Base characteristics;

    public CharacteristicEditorPanel(Technology.TechnologicalCharacteristics.Base characteristics) {
        this.characteristics = characteristics;

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel tablesPanel = new JPanel(new GridBagLayout());
        add(tablesPanel, BorderLayout.CENTER);
        tablesPanel.add(createDropDownTablePanel("Расстояния", new JTable(new DefaultTableModel(TWO_COLUMNS, 0))),
                new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 0));

        tablesPanel.add(createDropDownTablePanel("Ширины", new JTable(new DefaultTableModel(TWO_COLUMNS, 0))),
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 0));

        tablesPanel.add(createDropDownTablePanel("Выходы", new JTable(new DefaultTableModel(TWO_COLUMNS, 0))),
                new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 0));


        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        importButton = new JButton("Загрузить...");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importCharacteristics();
            }
        });
        buttonsPanel.add(importButton);

        saveButton = new JButton("Сохранить");
        buttonsPanel.add(saveButton);

        cancelButton = new JButton("Отмена");
        buttonsPanel.add(cancelButton);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JComponent createDropDownTablePanel(String name, final JTable table) {
        JPanel panel = new JPanel(new GridBagLayout());

        table.setGridColor(Color.BLACK);
        table.setBorder(new LineBorder(Color.BLACK));
        panel.add(new JScrollPane(table), new GridBagConstraints(0, 0, 1, 4, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        JButton addButton = new JButton("+");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((DefaultTableModel) table.getModel()).addRow(new Object[]{});
            }
        });

        JButton deleteButton = new JButton("-");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() >= 0) {
                    ((DefaultTableModel) table.getModel()).removeRow(table.getSelectedRow());
                }
            }
        });
        panel.add(addButton, new GridBagConstraints(1, 0, 1, 1, 0.2, 0.2, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        panel.add(deleteButton, new GridBagConstraints(1, 1, 1, 1, 0.2, 0.2, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

        return new DropDownPanel(name, new JPanel(), panel);
    }

    private void updateWindowComponent() {
        Window windowForComponent = WindowUtils.getWindowForComponent(this);
        if (windowForComponent != null) {
            windowForComponent.pack();
        }
    }

    private void importCharacteristics() {

    }

    private static class DistanceTableModel extends DefaultTableModel {
        Table<String, String, Double> data;
        public DistanceTableModel(Table<String, String, Double> data) {
            this.data = data;
            init();
        }

        private void init() {
            for (String columnKey : data.columnKeySet()) {
                for (String rowKey : data.column(columnKey).keySet()) {
                    addRow(new String[]{columnKey, rowKey, String.valueOf(data.get(rowKey, columnKey))});
                }
            }
        }

        public Table<String, String, Double> getData() {
            Table<String, String, Double> table = HashBasedTable.create();
            for (int i = 0; i < getRowCount(); i++) {
                for (int j = 0; j < getColumnCount(); j++) {
                    table.put((String)getValueAt(i, 0), (String) getValueAt(i, 1), (Double)getValueAt(i, 2));
                }
            }
            return table;
        }
    }
}
