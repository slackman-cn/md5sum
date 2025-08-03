package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;

public class MainFrame extends JFrame {

    public MainFrame() throws HeadlessException {
        super("JavaMD5 (Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750,400);

        initComponents();

        btnCancel.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (task != null && !task.isDone()) {
                    task.cancel(true);
                }
                progressBar.setValue(0);
                tFile.setText("");
                tSize.setText("File Size: n/a");
                btnCancel.setEnabled(false);
            }
        });
        btnSelect.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int i = fc.showOpenDialog(MainFrame.this);
                if (i == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file != null && file.exists()) {
                        btnCancel.setEnabled(true);
                        tFile.setText(file.getAbsolutePath());
                        tSize.setText("File Size: " + file.length() + " bytes");
                        progressBar.setValue(0);

                        task = new CheckSumTask(file, tMD5);
                        task.addPropertyChangeListener(evt -> {
                            if (Objects.equals(evt.getPropertyName(), "progress")) {
                                progressBar.setValue(task.getProgress());
                            }
                            if (Objects.equals(SwingWorker.StateValue.DONE, task.getState())) {
                                btnCancel.setEnabled(false);
                            }
                        });
                        task.execute();
                    }
                }
            }
        });
        btnVerify.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = "Original:\t" + tMD5.getText() + "\nCurrent:\t" + tVerify.getText();
                if (Objects.equals(tMD5.getText(), tVerify.getText())) {
                    JOptionPane.showMessageDialog(MainFrame.this, text,"Matched",JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(MainFrame.this,text,"Not Matched",JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    JButton btnSelect, btnCancel, btnVerify;
    JProgressBar progressBar;
    JTextField tFile, tMD5, tVerify;
    JLabel tSize;

    CheckSumTask task;

    private void initComponents() {
        btnSelect = new JButton("Browse..");
        btnCancel = new JButton("Cancel");
        btnCancel.setEnabled(false);
        btnVerify = new JButton("Verify");
//        btnVerify.setEnabled(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        tFile = new JTextField("");
        tFile.setEditable(false);
        tMD5 = new JTextField("");
        tMD5.setEditable(false);
        tVerify = new JTextField("");
        tSize = new JLabel("File Size: n/a");

        JPanel panelN = new JPanel(new MigLayout("insets 10", "[grow]5[]", "[]1[]5[]"));
        add(panelN, BorderLayout.NORTH);
        panelN.add(new JLabel("Select a file to compute MD5 checksum (or drag and drop a file onto this window)"), "span 2, wrap");
        panelN.add(tFile, "growx");
        panelN.add(btnSelect, "wrap");
        panelN.add(progressBar, "growx");
        panelN.add(btnCancel,"growx,wrap");

        JPanel panelC = new JPanel(new MigLayout("insets 5", "[300]5[]", "[]1[]5[]"));
        add(panelC, BorderLayout.CENTER);
        panelC.add(tSize, "span 2, wrap");
        panelC.add(new JLabel("Current file MD5 checksum value:"), "span 2, wrap");
        panelC.add(tMD5, "growx, wrap");
        panelC.add(new JLabel("Original file MD5 checksum value:"), "span 2, wrap");
        panelC.add(tVerify, "growx");
        panelC.add(btnVerify);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            JFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
