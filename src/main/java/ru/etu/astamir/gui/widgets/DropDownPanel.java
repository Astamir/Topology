package ru.etu.astamir.gui.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DropDownPanel extends JPanel implements ActionListener {

    public static final String EXPANDED = "DropDownPanel.Expanded";
    public static final String COLLAPSED = "DropDownPanel.Collapsed";

    protected final JPanel top;
    protected final JToggleButton arrowButton;

    protected JComponent title;
    protected JComponent content;

    public DropDownPanel(String buttonText, JComponent title, JComponent content) {
        this(buttonText, title, content, -1, false);
    }

    public DropDownPanel(String buttonText, JComponent title, JComponent content, int buttonWidth) {
        this(buttonText, title, content, buttonWidth, false);
    }

    public DropDownPanel(String buttonText, JComponent title, JComponent content, int buttonWidth, boolean buttonFill) {
        this.title = title;
        this.content = content;

        top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        arrowButton = createExpandButton(buttonWidth, buttonText);
        gbc.fill = buttonFill ? GridBagConstraints.BOTH : GridBagConstraints.NONE;
        top.add(arrowButton, gbc);

        title.setOpaque(false);
        gbc.fill = gbc.BOTH;
        gbc.gridwidth = gbc.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        top.add(title, gbc);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        content.setVisible(arrowButton.isSelected());
        arrowButton.addActionListener(this);
    }

    protected JToggleButton createExpandButton(int width, String text) {
        JToggleButton button = new JToggleButton(text);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        if (width > 0) {
            button.setMinimumSize(new Dimension(width, button.getMinimumSize().height));
            button.setMaximumSize(new Dimension(width, button.getMaximumSize().height));
            button.setPreferredSize(new Dimension(width, button.getPreferredSize().height));
        }
        return button;
    }

    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, super.getMaximumSize().height);
    }

    public String getButtonText() {
        return arrowButton.getText();
    }

    public void setButtonText(String buttonText) {
        arrowButton.setText(buttonText);
    }

    public JComponent getTitle() {
        return title;
    }

    public void setTitle(JComponent newTitle) {
        newTitle.setOpaque(false);
        GridBagConstraints gbc = ((GridBagLayout)top.getLayout()).getConstraints(getTitle());
        top.remove(getTitle());
        top.add(title = newTitle, gbc);
    }

    public JComponent getContent() {
        return content;
    }

    public void setContent(JComponent newContent) {
        newContent.setVisible(arrowButton.isSelected());
        remove(content);
        add(content = newContent, BorderLayout.CENTER);
    }

    public boolean isExpanded() {
        return arrowButton.isSelected();
    }

    public void setExpanded(boolean b) {
        if (content.isVisible() == b)
            return;
        arrowButton.setSelected(b);
        content.setVisible(b);
        fireDropDownActionCommand(new ActionEvent(this, 0, b ? EXPANDED : COLLAPSED));
        revalidate();

        Container c;
        if ((c = getParent()) instanceof JViewport &&
                (c = c.getParent()) instanceof JScrollPane &&
                (c = c.getParent()) instanceof JComponent)
        {
            ((JComponent)c).revalidate();
        } else if ((c = getParent()) instanceof JSplitPane) {
            ((JComponent)c).revalidate();
            ((JComponent)c.getParent()).revalidate();
        }

    }

    public void setExpandable(boolean expandable) {
        arrowButton.setVisible(expandable);
    }

    public boolean isExpandable() {
        return arrowButton.isVisible();
    }

    public boolean isEnabled() {
        return arrowButton.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        arrowButton.setEnabled(enabled);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == arrowButton) {
            setExpanded(arrowButton.isSelected());
            return;
        }
    }

    public void addActionListener(ActionListener al) {
        listenerList.add(ActionListener.class, al);
    }

    public void removeActionListener(ActionListener al) {
        listenerList.remove(ActionListener.class, al);
    }

    protected void fireDropDownActionCommand(ActionEvent ae) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            if (listeners[i] == ActionListener.class)
                ((ActionListener)listeners[i + 1]).actionPerformed(ae);
    }
}
