package com.poliworks.fileshareservice.server;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import com.poliworks.fileshareservice.model.FileInfo;


public class FileService {
    
    public List<FileInfo> listFiles(String directoryPath) throws IOException {
        List<FileInfo> fileList = new ArrayList<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath));
        for (Path path : directoryStream) {
            FileInfo fileInfo = new FileInfo(path.getFileName().toString(), Files.size(path), path.toString());
            fileInfo.setFileName(path.getFileName().toString());
            fileInfo.setFileSize(Files.size(path));
            fileList.add(fileInfo);
        }
        return fileList;
    }

    public byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    public void writeFile(String filePath, byte[] data) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, data);
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.delete(path);
    }



}
