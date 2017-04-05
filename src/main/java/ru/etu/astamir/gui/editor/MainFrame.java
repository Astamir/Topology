package ru.etu.astamir.gui.editor;

import com.google.common.base.Preconditions;
import net.miginfocom.swing.MigLayout;
import org.jdom2.JDOMException;
import org.xml.sax.InputSource;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.CompressionUtils;
import ru.etu.astamir.compression.TopologyCompressor;
import ru.etu.astamir.compression.TopologyParser;
import ru.etu.astamir.compression.commands.Command;
import ru.etu.astamir.compression.commands.compression.CompressCommand;
import ru.etu.astamir.compression.commands.compression.CompressWireCommand;
import ru.etu.astamir.compression.grid.Grid;
import ru.etu.astamir.compression.grid.VirtualGrid;
import ru.etu.astamir.compression.virtual.ConvertException;
import ru.etu.astamir.compression.virtual.Converter;
import ru.etu.astamir.dao.ProjectObjectManager;
import ru.etu.astamir.geom.common.Direction;
import ru.etu.astamir.gui.editor.creation.ContactCreationDialog;
import ru.etu.astamir.gui.editor.creation.ElementCreationDialog;
import ru.etu.astamir.launcher.Project;
import ru.etu.astamir.launcher.Topology;
import ru.etu.astamir.launcher.VirtualTopology;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.exceptions.UnexpectedException;
import ru.etu.astamir.model.technology.Technology;
import ru.etu.astamir.model.wires.Wire;
import ru.etu.astamir.serialization.Attribute;
import ru.etu.astamir.serialization.AttributeAdapter;
import ru.etu.astamir.serialization.AttributeContainer;
import ru.etu.astamir.serialization.AttributeFactory;
import ru.etu.astamir.serialization.xml.XMLAttributeParser;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by Astamir on 10.03.14.
 */
public class MainFrame extends JFrame {
    JToolBar toolBar;

    TopologyElement editing;

    VirtualGridPanel paintPanel;
    String currentTopology = "default_topology";
    VirtualTopology defaultTopology;
    boolean borderPainted = false;

    public MainFrame() {
        this.defaultTopology = (VirtualTopology) Preconditions.checkNotNull(ProjectObjectManager.getCurrentProject().getTopologies().get(currentTopology));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (editing != null) {

                }
            }
        });


        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("ins 0"));
        setJMenuBar(createMainMenu());
        toolBar = createToolBar();
        add(toolBar, "growx, wrap");
        paintPanel = new VirtualGridPanel(defaultTopology, 20);
        add(paintPanel, "push, grow");
        ElementDescriptionPanel description = new ElementDescriptionPanel();
        paintPanel.setDetailsPanel(description);
        //add(description, "growy, pushy, wrap");
    }

    private JToolBar createToolBar() {
        JToolBar tb = new JToolBar(SwingConstants.HORIZONTAL);
        tb.add(new AddElementAction());
        tb.add(new ConvertAction());
        tb.add(new CompressAction());
        return tb;
    }
    private JMenuBar createMainMenu() {
        JMenuBar mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        fileMenu.add(new SaveProjectAction("Сохранить проект"));
        fileMenu.add(new SaveTopologyAction("Сохранить топологию"));
        fileMenu.add(new ExportToXMLAction("Экспортировать в XML"));
        fileMenu.add(new ImportFromXMLAction("Импортировать из XML"));
        fileMenu.add(new ImportFromFileAction("Импорт из сокращенного файла"));

        JMenu projectMenu = new JMenu("Проект");
        projectMenu.add(new CharacteristicEditAction("Редактировать технологические нормы"));


        mainMenu.add(fileMenu);
        mainMenu.add(projectMenu);

        return mainMenu;
    }

    private class ConvertAction extends AbstractAction {
        public ConvertAction() {
            super("Преобразование координат");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int mode = defaultTopology.getMode();
            if (mode == VirtualTopology.REAL_MODE) {
                defaultTopology.setMode(VirtualTopology.VIRTUAL_MODE);
            } else {
                Converter converter = new Converter(defaultTopology);
                try {
                    converter.convert();
                    defaultTopology.setMode(VirtualTopology.REAL_MODE);
                    TopologyCompressor compressor = ProjectObjectManager.getCompressorsPool().getCompressor(defaultTopology);
                    compressor.compress();
                } catch (ConvertException e1) {
                    JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage(), "Ошибка перевода координат", JOptionPane.ERROR_MESSAGE);
                }
            }

            paintPanel.setModel(new VirtualElementModel(defaultTopology));
        }
    }

    private class CompressAction extends AbstractAction {
        public CompressAction() {
            super("Сжатие топологии");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int mode = defaultTopology.getMode();
            if (mode != VirtualTopology.REAL_MODE) {
                Converter converter = new Converter(defaultTopology);
                try {
                    converter.convert();
                    defaultTopology.setMode(VirtualTopology.REAL_MODE);
                } catch (ConvertException e1) {
                    JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage(), "Ошибка перевода координат", JOptionPane.ERROR_MESSAGE);
                }
            }
            TopologyCompressor compressor = ProjectObjectManager.getCompressorsPool().getCompressor(defaultTopology);
            Command peek = compressor.commands.peek();
            if (peek instanceof CompressCommand) {
                Collection<Border> affectedBorders = ((CompressCommand) peek).getAffectedBorders();
                if (peek instanceof CompressWireCommand) {
                    Border border = affectedBorders.iterator().next();
                    border = CompressionUtils.borderWithoutConnectedElements((Wire)((CompressWireCommand) peek).getElement(), border, defaultTopology.getGrid());
                    border = border.getOverlay(Direction.LEFT);
                    paintPanel.setBorders(Collections.singletonList(border));
                } else {
                    paintPanel.setBorders(affectedBorders);
                }
                if (!borderPainted) {
                    paintPanel.repaint();
                    borderPainted = true;
                    return;
                }
                borderPainted = false;
            }
            compressor.step(1);



            paintPanel.setModel(new VirtualElementModel(defaultTopology));
        }
    }

    private class AddElementAction extends AbstractAction {
        public AddElementAction() {
            super("Элемент");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ElementCreationDialog dialog = new ElementCreationDialog(MainFrame.this, ContactCreationDialog.CREATION);
            dialog.setResizable(false);
            dialog.setVisible(true);
            TopologyElement result = dialog.getResult();
            if (result != null) {
                paintPanel.model.addElement(result);
                paintPanel.repaint();
            }
        }
    }

    private class SaveProjectAction extends AbstractAction {

        private SaveProjectAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Project currentProject = ProjectObjectManager.getCurrentProject();
            try {
                currentProject.saveProject();
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(MainFrame.this, "Не удалось сохранить проект", "Ошибка сохранения проекта", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        }
    }

    private class SaveTopologyAction extends AbstractAction {

        private SaveTopologyAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Project currentProject = ProjectObjectManager.getCurrentProject();
            try {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showSaveDialog(MainFrame.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    Topology topology = currentProject.getTopologies().get(currentTopology);
                    if (topology != null) {
                        TopologyParser parser = new TopologyParser(file);
                        parser.write(topology.getGrid());
                        JOptionPane.showMessageDialog(MainFrame.this, "Топология успешно сохранена в " + file.getAbsolutePath(), "Сохранение", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(MainFrame.this, "Не удалось сохранить топологию", "Ошибка сохранения топологии", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        }
    }

    private class ExportToXMLAction extends AbstractAction {

        private ExportToXMLAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(MainFrame.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                    XMLAttributeParser.write(
                            AttributeFactory.createAttribute("topology", AttributeContainer.getAttributesFor(paintPanel.model.grid())), os);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Не удалось записать данные в файл", "Ошибка записи", JOptionPane.ERROR_MESSAGE);
                }
            }

        }
    }

    private class ImportFromXMLAction extends AbstractAction {

        private ImportFromXMLAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(MainFrame.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
                    Collection<Attribute> parse = XMLAttributeParser.parse(new InputSource(is));
                    Optional<AttributeAdapter<Object>> adapter = AttributeContainer.getAdapterSafely(Grid.class);
                    if (!adapter.isPresent()) {
                        throw new UnexpectedException("no adapter for grid in cache ?");
                    }

                    Grid entity = (Grid) adapter.get().getEntity(parse);
                    paintPanel.setModel(new VirtualElementModel(VirtualTopology.of(entity)));
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Не удалось прочитать данные из файла", "Ошибка чтения", JOptionPane.ERROR_MESSAGE);
                } catch (JDOMException e1) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка разбора XML файла", "Некорректная структура файла", JOptionPane.ERROR_MESSAGE);
                }
            }

        }
    }

    private class ImportFromFileAction extends AbstractAction {

        private ImportFromFileAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(MainFrame.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                TopologyParser parser = new TopologyParser(file);
                try {
                    parser.parse();
                    VirtualGrid entity = parser.getElements();
                    defaultTopology.setVirtual(entity);
                    paintPanel.setModel(new VirtualElementModel(defaultTopology));
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Не удалось прочитать данные из файла", "Ошибка чтения", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }

        }
    }

    private class CharacteristicEditAction extends AbstractAction {

        private CharacteristicEditAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            CharacteristicEditorPanel editorPanel = new CharacteristicEditorPanel((Technology.TechnologicalCharacteristics.Base) defaultTopology.getTechnology().getCharacteristics()/*(Technology.TechnologicalCharacteristics.Base) topology.getTechnology().getCharacteristics()*/);
            JDialog dialog = new JDialog(MainFrame.this, "Редактирвоание технологических норм", true);
            dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            dialog.setLocationRelativeTo(MainFrame.this);
            dialog.setContentPane(editorPanel);
            dialog.setSize(400, 500);
            dialog.setVisible(true);
        }
    }
}
