package ru.etu.astamir.gui.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;

/**
 * Implements DropDown panel controlled via ArrowButton.
 *
 * @author Nick Evgeniev
 * @author Denis Kislovsky
 */
public class DropDownPanel extends JPanel implements ActionListener {

    public static final String EXPANDED = "DropDownPanel.Expanded";
    public static final String COLLAPSED = "DropDownPanel.Collapsed";

    protected final JPanel top; // Includes "arrow_button" and "title" components.
    protected final JToggleButton arrow_button;

    protected JComponent title;
    protected JComponent content;

    public DropDownPanel(String button_text, JComponent title, JComponent content) {
        this(button_text, title, content, -1, false);
    }

    public DropDownPanel(String button_text, JComponent title, JComponent content, int button_width) {
        this(button_text, title, content, button_width, false);
    }

    public DropDownPanel(String button_text, JComponent title, JComponent content, int button_width, boolean button_fill) {
        this.title = title;
        this.content = content;
        //this.top_background = UIManager.getColor("DropDownPanel.topBackground");

        top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        arrow_button = createExpandButton(button_width, button_text);
        gbc.fill = button_fill ? GridBagConstraints.BOTH : GridBagConstraints.NONE;
        top.add(arrow_button, gbc);

        title.setOpaque(false);
        gbc.fill = gbc.BOTH;
        gbc.gridwidth = gbc.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        top.add(title, gbc);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        content.setVisible(arrow_button.isSelected());
        arrow_button.addActionListener(this);
    }

    protected JToggleButton createExpandButton(int width, String text) {
        JToggleButton button = new JToggleButton(text);
        //button.setBackground(top_background);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
/*
		Insets insets = button.getMargin();
		insets.left = 0;
		insets.right = 0;
		button.setMargin(insets);
*/
        if (width > 0) {
            button.setMinimumSize(new Dimension(width, button.getMinimumSize().height));
            button.setMaximumSize(new Dimension(width, button.getMaximumSize().height));
            button.setPreferredSize(new Dimension(width, button.getPreferredSize().height));
        }
        return button;
    }

    // :TODO remove. Temporary fix for valid layouting old code
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, super.getMaximumSize().height);
    }
    /*
        public void setTopBackground(Color top_background) {
            this.top_background = top_background;
            top.setBackground(top_background);
            arrow_button.setBackground(top_background);
            title.setBackground(top_background);
        }

    */
    public String getButtonText() {
        return arrow_button.getText();
    }

    public void setButtonText(String button_text) {
        arrow_button.setText(button_text);
    }

    public JComponent getTitle() {
        return title;
    }

    public void setTitle(JComponent new_title) {
        new_title.setOpaque(false);
        GridBagConstraints gbc = ((GridBagLayout)top.getLayout()).getConstraints(getTitle());
        top.remove(getTitle());
        top.add(title = new_title, gbc);
    }

    public JComponent getContent() {
        return content;
    }

    public void setContent(JComponent new_content) {
        new_content.setVisible(arrow_button.isSelected());
        remove(content);
        add(content = new_content, BorderLayout.CENTER);
    }

    public boolean isExpanded() {
        return arrow_button.isSelected();
    }

    public void setExpanded(boolean b) {
        if (content.isVisible() == b)
            return;
        arrow_button.setSelected(b);
        content.setVisible(b);
        fireDropDownActionCommand(new ActionEvent(this, 0, b ? EXPANDED : COLLAPSED));
        revalidate();
        // The code below is workaround (speedup) for delayed Swing
        // reaction when JScrollPane is used to wrap this drop down
        // (because JScrollPane.isValidateRoot() == true).
        Container c;
        if ((c = getParent()) instanceof JViewport &&
                (c = c.getParent()) instanceof JScrollPane &&
                (c = c.getParent()) instanceof JComponent)
        {
//			revalidate();
            ((JComponent)c).revalidate();
        } else if ((c = getParent()) instanceof JSplitPane) {
            ((JComponent)c).revalidate();
            ((JComponent)c.getParent()).revalidate();
        }

    }

    /**
     * Determines the ability to expand or collapse this panel through gui by
     * making arrow_button visible or invisible.
     *
     * @param expandable true if u want to be able to collapse/expand this panel through gui
     */
    public void setExpandable(boolean expandable) {
        arrow_button.setVisible(expandable);
    }

    public boolean isExpandable() {
        return arrow_button.isVisible();
    }

    public boolean isEnabled() {
        return arrow_button.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        arrow_button.setEnabled(enabled);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == arrow_button) {
            setExpanded(arrow_button.isSelected());
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
