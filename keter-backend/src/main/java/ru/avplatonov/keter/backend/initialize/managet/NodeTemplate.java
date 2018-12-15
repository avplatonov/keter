package ru.avplatonov.keter.backend.initialize.managet;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NodeTemplate {



    private UUID uuidNode = UUID.randomUUID();
    private String name = "KETER";
    private String description = "KETER";
    private List<String> tags = null;
    private String script = "KETER";
    private Map<String, Type> parameters = null;
    private Map<String, Type> hardware = null;
    private Map<String, URI> files = null;
    private List<String> outputs;
    private List<String> inputs;

    public UUID getUuidNode() {
        return uuidNode;
    }
    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public List<String> getInputs() {
        return inputs;
    }

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

    public Map<String, Type> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Type> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Type> getHardware() {
        return hardware;
    }

    public void setHardware(Map<String, Type> hardware) {
        this.hardware = hardware;
    }

    public void setFiles(Map<String, URI> files) {
        this.files = files;
    }

    public Map<String, URI> getFiles() {
        return files;
    }


    public NodeTemplate(){

    }

    public NodeTemplate(String name, String description, List<String> tags, String script, Map<String, Type> parameters, Map<String, Type> hardware, Map<String, URI> files, List<String> outputs, List<String> inputs) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.script = script;
        this.parameters = parameters;
        this.hardware = hardware;
        this.files = files;
        this.outputs = outputs;
        this.inputs = inputs;
    }
}
