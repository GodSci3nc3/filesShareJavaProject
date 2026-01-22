package com.poliworks.fileshareservice.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.poliworks.fileshareservice.client.FileClient;
import com.poliworks.fileshareservice.model.FileInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileManagerGUI extends JFrame {
    private FileClient client;
    private final DefaultTableModel tableModel;
    private final JTable fileTable;
    private final JTextArea fileContentArea;
    private final JTextField urlField;
    private final JButton btnConnect, btnDisconnect;
    private final JButton btnOpen, btnClose, btnRead, btnWrite;
    private final JLabel statusLabel;
    
    private String currentFileName = null;

    public FileManagerGUI(String defaultUrl) {
        setTitle("File Share Client");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Top Panel: Connection ---
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
        
        urlField = new JTextField(defaultUrl, 20);
        btnConnect = new JButton("Connect");
        btnDisconnect = new JButton("Disconnect");
        
        connectionPanel.add(new JLabel("Server URL:"));
        connectionPanel.add(urlField);
        connectionPanel.add(btnConnect);
        connectionPanel.add(btnDisconnect);

        // --- Left Panel: File List ---
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Files"));
        listPanel.setPreferredSize(new Dimension(300, 0));
        
        tableModel = new DefaultTableModel(new Object[]{"Name", "Size"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPanel.add(new JScrollPane(fileTable), BorderLayout.CENTER);

        // --- Center Panel: File Content (Editor) ---
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(BorderFactory.createTitledBorder("File Content"));
        
        fileContentArea = new JTextArea();
        fileContentArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        editorPanel.add(new JScrollPane(fileContentArea), BorderLayout.CENTER);

        // --- Bottom Panel: Operations & Status ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel operationsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnOpen = new JButton("Open");
        btnClose = new JButton("Close");
        btnRead = new JButton("Read");
        btnWrite = new JButton("Write");
        
        operationsPanel.add(btnOpen);
        operationsPanel.add(btnRead);
        operationsPanel.add(btnWrite);
        operationsPanel.add(btnClose);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        bottomPanel.add(operationsPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        // --- Add to Frame ---
        add(connectionPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.WEST);
        add(editorPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        btnConnect.addActionListener(e -> connect());
        btnDisconnect.addActionListener(e -> disconnect());
        btnOpen.addActionListener(e -> openFile());
        btnClose.addActionListener(e -> closeFile());
        btnRead.addActionListener(e -> readFile());
        btnWrite.addActionListener(e -> writeFile());

        // Initial State
        setConnected(false);
    }

    private void setConnected(boolean connected) {
        btnConnect.setEnabled(!connected);
        urlField.setEnabled(!connected);
        btnDisconnect.setEnabled(connected);
        
        fileTable.setEnabled(connected);
        fileContentArea.setEnabled(connected);
        
        btnOpen.setEnabled(connected);
        // Read/Write/Close depend on having a file open, but we enable them generally when connected for simplicity, 
        // or manage them based on selection. Let's manage them based on "Open" state.
        btnRead.setEnabled(false);
        btnWrite.setEnabled(false);
        btnClose.setEnabled(false);
        
        if (!connected) {
            tableModel.setRowCount(0);
            fileContentArea.setText("");
            currentFileName = null;
            client = null;
            statusLabel.setText("Disconnected");
        } else {
            statusLabel.setText("Connected to " + urlField.getText());
        }
    }

    private void connect() {
        try {
            String url = urlField.getText();
            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a URL");
                return;
            }
            client = new FileClient(url);
            refreshFileList();
            setConnected(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + e.getMessage());
        }
    }

    private void disconnect() {
        setConnected(false);
    }

    private void refreshFileList() {
        if (client == null) return;
        new Thread(() -> {
            try {
                List<FileInfo> files = client.listFiles();
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (FileInfo f : files) {
                        tableModel.addRow(new Object[]{f.getFileName(), f.getFileSize()});
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error listing files: " + e.getMessage()));
            }
        }).start();
    }

    private void openFile() {
        int row = fileTable.getSelectedRow();
        if (row == -1) {
            // Allow opening a "new" file by just enabling the editor if nothing is selected? 
            // Or maybe prompt for a name?
            // For now, let's assume we want to open an existing file from the list OR create a new one.
            // Let's prompt for a name if nothing selected to support creating new files.
            String name = JOptionPane.showInputDialog(this, "Enter file name (to create new or open):");
            if (name != null && !name.trim().isEmpty()) {
                currentFileName = name.trim();
                prepareEditor(true);
            }
            return;
        }
        currentFileName = (String) tableModel.getValueAt(row, 0);
        prepareEditor(true);
    }

    private void prepareEditor(boolean isOpen) {
        btnRead.setEnabled(isOpen);
        btnWrite.setEnabled(isOpen);
        btnClose.setEnabled(isOpen);
        fileContentArea.setEnabled(isOpen);
        statusLabel.setText(isOpen ? "File selected: " + currentFileName : "Ready");
        if (!isOpen) {
            fileContentArea.setText("");
            currentFileName = null;
        }
    }

    private void closeFile() {
        prepareEditor(false);
    }

    private void readFile() {
        if (currentFileName == null) return;
        new Thread(() -> {
            try {
                byte[] data = client.readFile(currentFileName);
                String content = new String(data, StandardCharsets.UTF_8);
                SwingUtilities.invokeLater(() -> {
                    fileContentArea.setText(content);
                    statusLabel.setText("Read " + data.length + " bytes from " + currentFileName);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage()));
            }
        }).start();
    }

    private void writeFile() {
        if (currentFileName == null) return;
        new Thread(() -> {
            try {
                byte[] data = fileContentArea.getText().getBytes(StandardCharsets.UTF_8);
                client.writeFile(currentFileName, data);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Written " + data.length + " bytes to " + currentFileName);
                    refreshFileList(); // Refresh list in case it was a new file
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error writing file: " + e.getMessage()));
            }
        }).start();
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> 
            new FileManagerGUI(args.length > 0 ? args[0] : "http://localhost:7000").setVisible(true));
    }
}
