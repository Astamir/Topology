package ru.etu.astamir.gui;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.event.*;


/**
 * Created with IntelliJ IDEA.
 * User: astamir
 * Date: 06.05.12
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class MainWindow extends JFrame {
    TestPanel paintPanel = new TestPanel();
//    PaintPanel paintPanel = new PaintPanel();

    double x = 0;
    double y = 0;

    public MainWindow() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                paintPanel.change(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                paintPanel.placePoint(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
              //  paintPanel.drag(e, x, y);
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
              // paintPanel.zoom(e);
            }
        });
        initComponents();
    }

    private void initComponents() {
        /*setJMenuBar(initMainMenu());
        JToolBar toolBar = new JToolBar("tool bar");
        toolBar.add(new Button("some button"));
        add(toolBar, BorderLayout.PAGE_START);*/

        add(paintPanel);
    }

    private JMenuBar initMainMenu() {
        JMenuBar mainMenu = new JMenuBar();
        mainMenu.add(new JMenu("Файл"));

        return mainMenu;
    }

    public static void main(String[] args) {
        MainWindow mw = new MainWindow();
        mw.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mw.setSize(1000, 1500);
        mw.setVisible(true);
    }
}
