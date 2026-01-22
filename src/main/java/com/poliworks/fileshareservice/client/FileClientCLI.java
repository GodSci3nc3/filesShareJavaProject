package com.poliworks.fileshareservice.client;

import com.poliworks.fileshareservice.model.FileInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class FileClientCLI {
    private FileClient client;
    private String currentFile;
    private final Scanner scanner;

    public FileClientCLI() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("File Share CLI Started");
        System.out.println("Commands: connect <url>, disconnect, open <file>, close, read, write <content>, ls, exit");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String args = parts.length > 1 ? parts[1] : "";

            try {
                switch (cmd) {
                    case "exit":
                        return;
                    case "connect":
                        if (args.isEmpty()) args = "http://localhost:7000";
                        client = new FileClient(args);
                        System.out.println("Connected to " + args);
                        break;
                    case "disconnect":
                        client = null;
                        currentFile = null;
                        System.out.println("Disconnected");
                        break;
                    case "ls":
                        ensureConnected();
                        List<FileInfo> files = client.listFiles();
                        System.out.println("Files:");
                        for (FileInfo f : files) {
                            System.out.printf("- %s (%d bytes)%n", f.getFileName(), f.getFileSize());
                        }
                        break;
                    case "open":
                        ensureConnected();
                        if (args.isEmpty()) throw new Exception("Usage: open <filename>");
                        currentFile = args;
                        System.out.println("File '" + currentFile + "' is now open");
                        break;
                    case "close":
                        currentFile = null;
                        System.out.println("File closed");
                        break;
                    case "read":
                        ensureConnected();
                        ensureFileOpen();
                        byte[] content = client.readFile(currentFile);
                        System.out.println("--- Content of " + currentFile + " ---");
                        System.out.println(new String(content, StandardCharsets.UTF_8));
                        System.out.println("---------------------------");
                        break;
                    case "write":
                        ensureConnected();
                        ensureFileOpen();
                        if (args.isEmpty()) throw new Exception("Usage: write <content>");
                        client.writeFile(currentFile, args.getBytes(StandardCharsets.UTF_8));
                        System.out.println("Wrote " + args.length() + " bytes to " + currentFile);
                        break;
                    default:
                        System.out.println("Unknown command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void ensureConnected() throws Exception {
        if (client == null) throw new Exception("Not connected. Use 'connect <url>' first.");
    }

    private void ensureFileOpen() throws Exception {
        if (currentFile == null) throw new Exception("No file open. Use 'open <filename>' first.");
    }
}
