package ru.avplatonov.keter.backend.initialize;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NodeTemplate {

    private UUID uuidNode = UUID.randomUUID();
    private String name = "KETER";
    private String description = "KETER";
    private List<String> tags = null;
    private String script = "KETER";
    private Map<String,String> parameters = null;
    private List<List<String>> hardware = null;
    private List<String> files = null;

    public UUID getUuid() {
        return uuidNode;
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

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public List<List<String>> getHardware() {
        return hardware;
    }

    public void setHardware(List<List<String>> hardware) {
        this.hardware = hardware;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }


    public NodeTemplate(){

    }

    public NodeTemplate(String name, String description, List<String> tags, String script, Map<String, String> parameters, List<List<String>> hardware, List<String> files) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.script = script;
        this.parameters = parameters;
        this.hardware = hardware;
        this.files = files;
    }
}
