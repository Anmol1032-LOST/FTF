package main.gui;

import main.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransferLogicEdditor extends JDialog {
    private final GridBagConstraints gbc = new GridBagConstraints();
    private int tableSize = 0;
    private final ArrayList<JTextField[]> data = new ArrayList<>();

    public TransferLogicEdditor(Frame owner) {
        super(owner, "<Transfer Logic>");
        setLayout(new GridBagLayout());

        int width = 600;
        int height = 400;

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());

        for (Map.Entry<String, File> entry : Main.locations.entrySet()) {
            String string = entry.getKey();
            String file = entry.getValue().getAbsolutePath();

            addRow(string, file, p, tableSize++);
        }

        JButton addRow = new JButton("Add Row");
        addRow.addActionListener(e -> {
            addRow("<Pattern>", "<File>", p, tableSize++);
            invalidate();
            revalidate();
            repaint();
        });

        JButton submit = new JButton("Submit");
        submit.addActionListener(e -> {
            HashMap<String, String> validatedData = new HashMap<>(data.size());
            AtomicBoolean error = new AtomicBoolean(false);
            data.forEach(textFields -> {
                JTextField pattern = textFields[0];
                JTextField file = textFields[1];

                String patternString = pattern.getText();
                String fileString = file.getText();
                File fileFile = new File(fileString);

                if (!fileFile.exists() || !fileFile.isDirectory()) {
                    file.setBorder(MainGui.errorBorder);
                    error.set(true);
                }

                if (patternString.contains(Main.separator)) {
                    pattern.setBorder(MainGui.errorBorder);
                    error.set(true);
                }

                for (String string : validatedData.keySet()) {
                    if (patternString.endsWith(string)) {
                        pattern.setBorder(MainGui.errorBorder);
                        error.set(true);
                        break;
                    }
                }

                if (validatedData.put(patternString, fileFile.getAbsolutePath()) != null) {
                    pattern.setBorder(MainGui.errorBorder);
                    error.set(true);
                }
            });

            if (!error.get()) {
                save(validatedData);
            }
        });

        add(this, new JScrollPane(p), 0, 0, 1, 1, 1, 1);
        add(this, addRow, 0, 1, 1, 1, 1, 0);
        add(this, submit, 0, 2, 1, 1, 1, 0);

        setSize(new Dimension(width, height));
        setLocationRelativeTo(null);
    }

    private void save(HashMap<String, String> validatedData) {
        Main.locations = new HashMap<>(validatedData.size());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Main.dataFile, false))) {
            for (Map.Entry<String, String> d : validatedData.entrySet()) {
                writer.write(d.getKey());
                writer.write(Main.separator);
                writer.write(d.getValue());
                writer.newLine();

                Main.locations.put(d.getKey(), new File(d.getValue()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dispose();
    }

    private void addRow(String string, String file, JPanel p, int i) {
        JTextField patternField = new JTextField(string);
        patternField.setBorder(BorderFactory.createRaisedBevelBorder());

        JTextField fileField = new JTextField(file);

        JTextField[] asArray = new JTextField[]{
                patternField,
                fileField
        };
        data.add(asArray);

        fileField.setBorder(BorderFactory.createRaisedBevelBorder());
        fileField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                fileField.setBorder(BorderFactory.createLoweredBevelBorder());
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                MainGui.validateFileField(fileField);
            }
        });

        patternField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                patternField.setBorder(BorderFactory.createLoweredBevelBorder());
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                patternField.setBorder(BorderFactory.createRaisedBevelBorder());
            }
        });

        JButton fileChooserButton = new JButton(UIManager.getIcon("FileView.directoryIcon"));
        JFileChooser fileChooser = new JFileChooser(file);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.addActionListener(e -> {
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                fileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
            MainGui.validateFileField(fileField);
        });
        fileChooserButton.addActionListener(e -> fileChooser.showOpenDialog(this));

        JButton deleteButton = new JButton("Delete");

        deleteButton.addActionListener(e -> {
            data.remove(asArray);
            p.remove(patternField);
            p.remove(fileField);
            p.remove(fileChooserButton);
            p.remove(deleteButton);
            invalidate();
            revalidate();
            repaint();
        });

        add(p, patternField, 0, i, 1, 1, 0.2, 0);
        add(p, fileField, 1, i, 1, 1, 0.6, 0);
        add(p, fileChooserButton, 2, i, 1, 1, 0.1, 0);
        add(p, deleteButton, 3, i, 1, 1, 0.1, 0);
    }

    protected void add(Container p, JComponent c, int x, int y, int width, int height, double weightx, double weighty) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        p.add(c, gbc);
    }
}
