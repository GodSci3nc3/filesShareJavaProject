package com.poliworks.fileshareservice.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.poliworks.fileshareservice.model.FileInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class FileClient {
    private final String url;
    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public FileClient(String url) { this.url = url; }

    public List<FileInfo> listFiles() throws Exception {
        var req = HttpRequest.newBuilder().uri(URI.create(url + "/files")).GET().build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new Exception("Server error: " + res.body());
        return gson.fromJson(res.body(), new TypeToken<List<FileInfo>>(){}.getType());
    }

    public byte[] readFile(String name) throws Exception {
        var req = HttpRequest.newBuilder().uri(URI.create(url + "/files/" + name)).GET().build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() >= 400) throw new Exception("File not found");
        return res.body();
    }

    public void writeFile(String name, byte[] data) throws Exception {
        var req = HttpRequest.newBuilder().uri(URI.create(url + "/files/" + name))
            .POST(HttpRequest.BodyPublishers.ofByteArray(data)).build();
        http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    public void deleteFile(String name) throws Exception {
        var req = HttpRequest.newBuilder().uri(URI.create(url + "/files/" + name)).DELETE().build();
        http.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
