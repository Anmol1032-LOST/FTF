package main.gui;

import main.CopyHelper;
import main.Main;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainGui extends JFrame {
    static final Border errorBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.RED, new Color(1, 0, 0.5f, 1));
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final JTextArea log;
    private final Main main;
    private final JProgressBar fileProgressBar;
    private final JButton fileButton;
    private final JTextField fileField;
    private final BufferedImage iconImage;
    private boolean started = false;
    private JButton runButton;
    private Thread mainThread;

    public MainGui() {
        super("<MAIN>");
        setLayout(new GridBagLayout());

        iconImage = getIcon();
        setIconImage(iconImage);

        log = new JTextArea();
        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        main = new Main(this);

        int width = 1080;
        int height = 810;

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        fileField = new JTextField();
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
                validateFileField(fileField);
            }
        });

        JButton logicButton = getLogicButton();

        fileButton = getFileChooserButton(fileField);

        createRunButton();

        fileProgressBar = new JProgressBar();
        fileProgressBar.setStringPainted(true);

        JLabel loadingImage;
        try {
            File loadingImageFile = new File("LoadingImage.gif");
            if (!loadingImageFile.isFile()) {
                // copying to external location for easy customization
                CopyHelper.rawCopyFile(MainGui.class.getClassLoader().getResourceAsStream("LoadingImage.gif"), loadingImageFile);
            }

            loadingImage = new JLabel(new ImageIcon(new File("LoadingImage.gif").toURI().toURL()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JScrollPane scrollPane = new JScrollPane(log);
        add(scrollPane, 0, 0, 3, 1, 1, 1);
        add(loadingImage, 0, 1, 3, 1, 1, 0);
        add(fileProgressBar, 0, 2, 3, 1, 1, 0);
        add(fileField, 0, 3, 3, 1, 1, 0);
        add(logicButton, 0, 4, 1, 1, 0.25, 0);
        add(fileButton, 1, 4, 1, 1, 0.5, 0);
        add(runButton, 2, 4, 1, 1, 0.25, 0);

        setSize(new Dimension(width, height));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    static void validateFileField(JTextField fileField) {
        File dir = new File(fileField.getText());
        if (!dir.exists() || !dir.isDirectory()) {
            fileField.setBorder(errorBorder);
        } else {
            fileField.setBorder(BorderFactory.createRaisedBevelBorder());
            fileField.setText(dir.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGui::new);
    }

    private JButton getLogicButton() {
        JButton button = new JButton("Edit Transfer logic");
        button.addActionListener(e -> {
            main.readData();
            JDialog dialog = new TransferLogicEdditor(this);
            dialog.setVisible(true);
        });
        return button;
    }

    private JButton getFileChooserButton(JTextField fileField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.addActionListener(e -> {
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                fileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                validateFileField(fileField);
            }
        });

        JButton fileButton = new JButton("Select File", UIManager.getIcon("FileView.directoryIcon"));
        fileButton.addActionListener(e -> fileChooser.showOpenDialog(this));
        return fileButton;
    }

    private BufferedImage getIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(1, 1, 1, 1f));
        g.setStroke(new BasicStroke(5.5f));
        g.drawArc(6, 6, 64 - 12, 64 - 12, -30, 120);
        g.setColor(new Color(0, 1, 0.75f, 1));
        g.drawArc(6, 6, 64 - 12, 64 - 12, 90, 120);
        g.setColor(new Color(1, 0, 0.75f, 1));
        g.drawArc(6, 6, 64 - 12, 64 - 12, 210, 120);
        g.setColor(new Color(0, 0, 0.75f, 1));
        g.drawOval(18, 18, 64 - 36, 64 - 36);
        g.setColor(new Color(1, 1, 0.75f, 1));
        g.drawOval(24, 24, 64 - 48, 64 - 48);
        g.dispose();
        return image;
    }

    private void createRunButton() {
        runButton = new JButton("Run", new ImageIcon(iconImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH | Image.SCALE_AREA_AVERAGING)));
        runButton.addActionListener(e -> {
            if (!started) {
                // run / run again
                validateFileField(fileField);

                File dir = new File(fileField.getText());
                if (!dir.exists() || !dir.isDirectory()) {
                    return;
                }

                fileField.setEditable(false);
                fileButton.setEnabled(false);
                runButton.setText("Force Stop");

                mainThread = new Thread(() -> main.start(dir), "main file transfer");
                mainThread.start();
                started = true;
            } else {
                // force stop
                fileField.setEditable(true);
                fileButton.setEnabled(true);

                mainThread.interrupt();
                started = false;
                runButton.setText("Run again");
            }
        });
    }

    protected void add(JComponent c, int x, int y, int width, int height, double weightx, double weighty) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        this.add(c, gbc);
    }

    public void logs(String s) {
        log.setText(log.getText() + '\n' + "      \t\t" + s);
    }

    public void logd(String s) {
        log.setText(log.getText() + '\n' + "------\t" + s);
    }

    public void logw(String s) {
        log.setText(log.getText() + '\n' + "======\t" + s);
    }

    public void loge(String s) {
        requestFocus();
        log.setText(log.getText() + "\n\n" + ">>>>>>\t" + s + '\n');
    }

    public void logDone() {
        started = false;
        fileField.setEditable(true);
        fileButton.setEnabled(true);

        requestFocus();
        runButton.setText("Run again");
        log.setText(log.getText() + "\n\n\n>>>>>>\tDONE\n");
    }

    public void setFileProgressBarMax(int n) {
        fileProgressBar.setMaximum(n);
    }

    public void setFileProgressBarProgesss(int n) {
        fileProgressBar.setValue(n);
    }
}
