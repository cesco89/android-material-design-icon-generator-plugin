import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.JDOMUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MaterialDesignIconSetDialog extends DialogWrapper {

    private static final String TITLE = "Material Icon Generator";
    private static final String FILE_ICON_COMBOBOX_XML = "template.xml";
    private static final String ICON_CAUTION = "icons/alert/drawable-mdpi/ic_error_black_48dp.png";
    private static final String ICON_DONE = "icons/action/drawable-mdpi/ic_done_black_48dp.png";
    private static final String URL_OVERVIEW = "http://google.github.io/material-design-icons";
    private static final String URL_REPOSITORY = "https://github.com/google/material-design-icons";

    private Project project;
    private IconModel model;
    private boolean canceled;

    private JPanel panelMain;
    private JLabel imageLabel;
    private JComboBox comboBoxDp;
    private JComboBox comboBoxColor;
    private JCheckBox checkBoxMdpi;
    private FilterComboBox comboBoxIcon;
    private JTextField textFieldFileName;
    private JCheckBox checkBoxHdpi;
    private JCheckBox checkBoxXhdpi;
    private JCheckBox checkBoxXxhdpi;
    private JLabel labelOverview;
    private JLabel labelRepository;
    private JCheckBox checkBoxXxxhdpi;

    public MaterialDesignIconSetDialog(@Nullable final Project project) {
        super(project, true);

        this.project = project;

        setTitle(TITLE);
        setResizable(true);

        initIconComboBox();
        initColorComboBox();
        initDpComboBox();
        initFileName();
        initSizeCheckBox();

        initLabelLink(labelOverview, URL_OVERVIEW);
        initLabelLink(labelRepository, URL_REPOSITORY);

        model = createModel();

        showIconPreview();

        init();
    }

    private void initSizeCheckBox() {
        checkBoxMdpi.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (model != null) model.setMdpi(checkBoxMdpi.isSelected());
            }
        });

        checkBoxHdpi.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (model != null) model.setHdpi(checkBoxHdpi.isSelected());
            }
        });

        checkBoxXhdpi.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (model != null) model.setXhdpi(checkBoxXhdpi.isSelected());
            }
        });

        checkBoxXxhdpi.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (model != null) model.setXxhdpi(checkBoxXxhdpi.isSelected());
            }
        });

        checkBoxXxxhdpi.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (model != null) model.setXxxhdpi(checkBoxXxxhdpi.isSelected());
            }
        });
    }

    private void initFileName() {
        textFieldFileName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (model != null) model.setFileName(textFieldFileName.getText());
                showIconPreview();
            }
        });
    }

    private void initDpComboBox() {
        comboBoxDp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                model.setDp((String) comboBoxDp.getSelectedItem());
                showIconPreview();
            }
        });
    }

    private void initColorComboBox() {
        comboBoxColor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                model.setColor((String)comboBoxColor.getSelectedItem());
                showIconPreview();
            }
        });
    }

    private IconModel createModel() {
        final String iconName = (String)comboBoxIcon.getSelectedItem();
        final String color = (String)comboBoxColor.getSelectedItem();
        final String dp = (String)comboBoxDp.getSelectedItem();
        final String fileName = textFieldFileName.getText();
        final boolean mdpi = checkBoxMdpi.isSelected();
        final boolean hdpi = checkBoxHdpi.isSelected();
        final boolean xdpi = checkBoxXhdpi.isSelected();
        final boolean xxdpi = checkBoxXxhdpi.isSelected();
        final boolean xxxdpi = checkBoxXxxhdpi.isSelected();
        return new IconModel(iconName, color, dp, fileName, mdpi, hdpi, xdpi, xxdpi, xxxdpi);
    }

    private void showIconPreview() {
        if (model == null) return;

        try {
            String size = checkBoxXxhdpi.getText();
            ImageIcon icon = new ImageIcon(getClass().getResource(model.getPath(size)));
            imageLabel.setIcon(icon);
        } catch (Exception e) {
            //
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panelMain;
    }

    private void initIconComboBox() {
        Document doc;
        try {
            File templateFile = new File(getClass().getResource(FILE_ICON_COMBOBOX_XML).getFile());
            doc = JDOMUtil.loadDocument(templateFile);

            List<Element> elements = doc.getRootElement().getChildren();
            for (org.jdom.Element element : elements) {
                comboBoxIcon.addItem(element.getText());
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        comboBoxIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (model != null) model.setIconName((String)comboBoxIcon.getSelectedItem());
                textFieldFileName.setText((String) comboBoxIcon.getSelectedItem());
                if (model != null) model.setFileName(textFieldFileName.getText());
                showIconPreview();
            }
        });

        comboBoxIcon.setSelectedIndex(0);
        textFieldFileName.setText((String) comboBoxIcon.getSelectedItem());
    }

    @Override
    protected void doOKAction() {
        if (model == null) return;

        canceled = false;
        boolean success = false;

        if (model.isMdpi() && createIcon(checkBoxMdpi.getText())) {
            success = true;
        }
        if (model.isHdpi() && createIcon(checkBoxHdpi.getText())) {
            success = true;
        }
        if (model.isXhdpi() && createIcon(checkBoxXhdpi.getText())) {
            success = true;
        }
        if (model.isXxhdpi() && createIcon(checkBoxXxhdpi.getText())) {
            success = true;
        }
        if (model.isXxxhdpi() && createIcon(checkBoxXxxhdpi.getText())) {
            success = true;
        }

        if (success) {
            JOptionPane.showConfirmDialog(panelMain,
                    "Icon created successfully.",
                    "Material design icon created",
                    JOptionPane.OK_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    new ImageIcon(getClass().getResource(ICON_DONE)));
        }
    }

    private boolean createIcon(String size) {
        if (canceled) return false;

        File copyFile = new File(model.getCopyPath(project, size));

        if (copyFile.exists() && copyFile.isFile()) {
            int option = JOptionPane.showConfirmDialog(panelMain,
                    copyFile.getName() + " already exists, overwrite ?",
                    "File exists",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    new ImageIcon(getClass().getResource(ICON_CAUTION)));

            switch (option) {
                case JOptionPane.YES_OPTION:
                    String path = model.getPath(size);
                    String originalFile = getClass().getResource(path).getFile();
                    return copyIcon(copyFile, new File(originalFile));
                case JOptionPane.NO_OPTION:
                    return false;
                case JOptionPane.CANCEL_OPTION:
                    canceled = true;
                    return false;
                default:
                    return false;
            }
        } else {
            String path = model.getPath(size);
            String originalFile = getClass().getResource(path).getFile();
            return copyIcon(copyFile, new File(originalFile));
        }
    }

    private boolean copyIcon(File copyFile, File originalFile) {
        try {
            new File(copyFile.getParent()).mkdirs();
            copyFile(originalFile, copyFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void copyFile(File srcFile, File destFile) throws IOException {
        InputStream in = new FileInputStream(srcFile);
        OutputStream os = new FileOutputStream(destFile);

        int len = -1;
        byte[] b = new byte[1000 * 1024];
        try {
            while ((len = in.read(b, 0, b.length)) != -1) {
                os.write(b, 0, len);
            }
            os.flush();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initLabelLink(JLabel label, final String url) {
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() > 0) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            URI uri = new URI(url);
                            desktop.browse(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

}