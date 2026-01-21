package com.poliworks.fileshareservice.server;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import com.poliworks.fileshareservice.model.FileInfo;

public class FileService {
    public List<FileInfo> listFiles(String dir) throws IOException {
        List<FileInfo> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            stream.forEach(p -> files.add(new FileInfo(
                p.getFileName().toString(), 
                p.toFile().length(), 
                p.toString()
            )));
        }
        return files;
    }

    public byte[] readFile(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public void writeFile(String path, byte[] data) throws IOException {
        Files.write(Paths.get(path), data);
    }

    public void deleteFile(String path) throws IOException {
        Files.delete(Paths.get(path));
    }
}
