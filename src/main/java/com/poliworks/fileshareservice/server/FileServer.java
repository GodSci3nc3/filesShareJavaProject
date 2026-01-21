package com.poliworks.fileshareservice.server;

import io.javalin.Javalin;
import java.util.Map;

public class FileServer {
    FileService fs;
    String dir;

    public FileServer(String sharedDirectory) {
        this.fs = new FileService();
        this.dir = sharedDirectory;
    }

    public void start(int port) {
        Javalin.create().start(port)
            .get("/files", ctx -> ctx.json(fs.listFiles(dir)))
            .get("/files/{name}", ctx -> ctx.result(fs.readFile(dir + "/" + ctx.pathParam("name"))))
            .post("/files/{name}", ctx -> fs.writeFile(dir + "/" + ctx.pathParam("name"), ctx.bodyAsBytes()))
            .delete("/files/{name}", ctx -> fs.deleteFile(dir + "/" + ctx.pathParam("name")))
            .exception(Exception.class, (e, ctx) -> ctx.status(404).json(Map.of("error", e.getMessage())));
    }
}
