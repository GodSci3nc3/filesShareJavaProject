package com.poliworks.fileshareservice;

import com.poliworks.fileshareservice.server.FileServer;
import com.poliworks.fileshareservice.server.FileServer;


import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("server")) {
            String dir = args.length > 1 ? args[1] : "./shared";
            int port = args.length > 2 ? Integer.parseInt(args[2]) : 7000;
            Files.createDirectories(Paths.get(dir));
            new FileServer(dir).start(port);
            System.out.println("Server on port " + port);
        } else {
            new com.poliworks.fileshareservice.client.FileClientCLI().start();
        }
    }
}
