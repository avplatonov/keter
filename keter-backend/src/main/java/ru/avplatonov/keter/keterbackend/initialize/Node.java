package ru.avplatonov.keter.keterbackend.initialize;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Node {

    private UUID uuid = UUID.randomUUID();
    private String name = "KETER";
    private String description = "KETER";
    private List<String> tags = null;
    private String script = "KETER";
    private String parameters = "KETER";
    private List<List<String>> hardware = null;
    private List<List<String>> files = null;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public List<List<String>> getHardware() {
        return hardware;
    }

    public void setHardware(List<List<String>> hardware) {
        this.hardware = hardware;
    }

    public void setFiles(List<List<String>> files) {
        this.files = files;
    }

    public List<List<String>> getFiles() {
        return files;
    }


    public Node(){

    }

    public Node(String name, String description, List<String> tags, String script, String parameters, List<List<String>> hardware, List<List<String>> files) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.script = script;
        this.parameters = parameters;
        this.hardware = hardware;
        this.files = files;
    }
}
