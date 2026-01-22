package com.poliworks.fileshareservice.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.poliworks.fileshareservice.client.FileClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileManagerGUI extends JFrame {
    private final FileClient client;
    private final DefaultTableModel model;

    public FileManagerGUI(String url) {
        this.client = new FileClient(url);
        setTitle("File Manager");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new Object[]{"Name", "Size"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton[] btns = {new JButton("↻"), new JButton("↑"), new JButton("↓"), new JButton("✕"), new JButton("✎")};
        btns[0].addActionListener(e -> refresh());
        btns[1].addActionListener(e -> upload());
        btns[2].addActionListener(e -> download(table.getSelectedRow()));
        btns[3].addActionListener(e -> delete(table.getSelectedRow()));
        btns[4].addActionListener(e -> write(table.getSelectedRow()));
        for (JButton b : btns) panel.add(b);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
        refresh();
    }

    private void refresh() {
        new Thread(() -> {
            try {
                model.setRowCount(0);
                client.listFiles().forEach(f -> model.addRow(new Object[]{f.getFileName(), f.getFileSize()}));
            } catch (Exception e) {
                error(e.getMessage());
            }
        }).start();
    }

    private void upload() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            new Thread(() -> {
                try {
                    File f = fc.getSelectedFile();
                    client.writeFile(f.getName(), Files.readAllBytes(f.toPath()));
                    refresh();
                } catch (Exception e) { error(e.getMessage()); }
            }).start();
        }
    }

    private void download(int row) {
        if (row == -1) return;
        String name = (String) model.getValueAt(row, 0);
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(name));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            new Thread(() -> {
                try {
                    Files.write(fc.getSelectedFile().toPath(), client.readFile(name));
                } catch (Exception e) { error(e.getMessage()); }
            }).start();
        }
    }

    private void delete(int row) {
        if (row == -1) return;
        String name = (String) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete " + name + "?") == 0) {
            new Thread(() -> {
                try {
                    client.deleteFile(name);
                    refresh();
                } catch (Exception e) { error(e.getMessage()); }
            }).start();
        }
    }

    private void write(int row) {
        String name;
        if (row != -1) {
            name = (String) model.getValueAt(row, 0);
        } else {
            name = JOptionPane.showInputDialog(this, "File Name:");
            if (name == null || name.trim().isEmpty()) return;
        }

        JTextArea ta = new JTextArea(20, 50);
        if (row != -1) {
            try {
                ta.setText(new String(client.readFile(name), StandardCharsets.UTF_8));
            } catch (Exception e) { error(e.getMessage()); return; }
        }

        if (JOptionPane.showConfirmDialog(this, new JScrollPane(ta), "Edit " + name, JOptionPane.OK_CANCEL_OPTION) == 0) {
            new Thread(() -> {
                try {
                    client.writeFile(name, ta.getText().getBytes(StandardCharsets.UTF_8));
                    refresh();
                } catch (Exception e) { error(e.getMessage()); }
            }).start();
        }
    }

    private void error(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Error", 0));
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> 
            new FileManagerGUI(args.length > 0 ? args[0] : "http://localhost:7000").setVisible(true));
    }
}
