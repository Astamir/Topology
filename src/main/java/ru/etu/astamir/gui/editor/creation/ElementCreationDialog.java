package ru.etu.astamir.gui.editor.creation;

import net.miginfocom.swing.MigLayout;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Point;
import ru.etu.astamir.model.TopologyElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElementCreationDialog extends JDialog {
    public static final int CREATION = 1;
    public static final int EDITING = 2;

    private JLabel nameLabel;
    private JTextField nameTextField;

    private JLabel stepsLabel;
    private JTextField stepsTextField;

    private JPanel buttonPanel;

    private JPanel contentPanel;
    private TopologyElement result;

    public ElementCreationDialog(Frame owner, int type) {
        super(owner, title(type), true);

        initComponents();
    }

    private static String title(int type) {
        if (type != CREATION && type != EDITING) {
            throw new IllegalArgumentException("type should be one of ContactCreationDialog.CREATION, ContactCreationDialog.EDITING");
        }

        return type == CREATION ? "Добавить новый элемент" : "Редактирование элемента";
    }

    private void initComponents() {
        contentPanel = createContentPanel();
        setContentPane(contentPanel);
        setLocationRelativeTo(getOwner());
        pack();
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("hidemode 3", "[]5[]"));

        nameLabel = new JLabel("Имя: ");
        panel.add(nameLabel);
        nameTextField = new JTextField(10);
        panel.add(nameTextField, "growx, pushx, wrap");

        stepsLabel = new JLabel("Координаты: ");
        panel.add(stepsLabel);
        stepsTextField = new JTextField(10);
        panel.add(stepsTextField, "growx, pushx, wrap");

        buttonPanel = createButtonPanel();
        panel.add(buttonPanel, "growx, pushx");
        return panel;
    }

    private static java.util.List<Point> readCoordinates(String text) {
        java.util.List<Point> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+\\.*\\d*");
        Matcher m = pattern.matcher(text);
        double x = 0;
        double y = 0;
        int count = 0;
        while (m.find()) {
            if (count == 0) {
                x = Double.parseDouble(m.group());
            } else {
                y = Double.parseDouble(m.group());
            }
            count++;
            if (count == 2) {
                result.add(ru.etu.astamir.geom.common.Point.of(x, y));
                count = 0;
            }

        }

        return result;
    }

    private TopologyElement createElement() {
        String symbol = nameTextField.getText();
        if (symbol.replaceAll("\\s", "").isEmpty()) {
            JOptionPane.showMessageDialog(this, "Тип элемента не задан", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        List<Point> points;
        try {
            points = readCoordinates(stepsTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректно введены координаты", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (points.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите хотя бы одну координату", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return ProjectObjectManager.getElementFactory().getElement(symbol, points.toArray(new Point[points.size()]), new HashMap<String, Object>());
    }

    private JPanel createButtonPanel() {
        buttonPanel = new JPanel(new MigLayout("align right"));
        JButton okButton = new JButton("ОК");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = createElement();
                if (result != null) {
                    setVisible(false);
                }
            }
        });
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    public TopologyElement getResult() {
        return result;
    }

}
