package ru.avplatonov.keter.backend.initialize.managet;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.avplatonov.keter.backend.Application.nodesDB;

public class NodesKeter {
    private UUID uuidNode = UUID.randomUUID();
    private UUID idNodeTemplate;
    private int localIdNode;
    private String name;
    private String description;
    private List<String> tags;
    private String script;
    private Map<String, Type> parameters;
    private Map<String, Type> hardware;
    private Map<String, URI> files;
    private Map<Integer, String> connections;

    public void setConnections(Map<Integer, String> connections) {
        this.connections = connections;
    }

    public UUID getUuidNode() {
        return uuidNode;
    }

    public Map<Integer, String> getConnections() {
        return connections;
    }

    public void setIdNodeTemplate(UUID idNodeTemplate) {
        this.idNodeTemplate = idNodeTemplate;
    }

    public void setLocalIdNode(int localIdNode) {
        this.localIdNode = localIdNode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setParameters(Map<String, Type> parameters) {
        this.parameters = parameters;
    }

    public void setHardware(Map<String, Type> hardware) {
        this.hardware = hardware;
    }

    public void setFiles(Map<String, URI> files) {
        this.files = files;
    }

    public UUID getIdNodeTemplate() {
        return idNodeTemplate;
    }

    public int getLocalIdNode() {
        return localIdNode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getScript() {
        return script;
    }

    public Map<String, Type> getParameters() {
        return parameters;
    }

    public Map<String, Type> getHardware() {
        return hardware;
    }

    public Map<String, URI> getFiles() {
        return files;
    }


    public NodesKeter(UUID idNodeTemplate, int localIdNode, String name, String description, List<String> tags, String script, Map<String, Type> parameters, Map<String, Type> hardware, Map<String, URI> files, Map<Integer, String> connections) {
        this.idNodeTemplate = idNodeTemplate;
        this.localIdNode = localIdNode;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.script = script;
        this.parameters = parameters;
        this.hardware = hardware;
        this.files = files;
        this.connections = connections;
    }

    public NodesKeter(UUID idNodeTemplate, int localIdNode, Map<String, Type> parameters,
                      Map<String, Type> hardware, Map<Integer,String> connections) {
        NodeTemplate nodeTemplateSearchUuid = null;
        for (NodeTemplate node : nodesDB.getListOfNodeTemplates()) {
            if(node.getUuid().equals(idNodeTemplate)) {
                nodeTemplateSearchUuid=node;
                return;
            }
        }

        this.idNodeTemplate = idNodeTemplate;
        this.localIdNode = localIdNode;
        this.name = nodeTemplateSearchUuid.getName();
        this.description = nodeTemplateSearchUuid.getDescription();
        this.tags = nodeTemplateSearchUuid.getTags();
        this.script = nodeTemplateSearchUuid.getScript();
        this.parameters = parameters;
        this.hardware = hardware;
        this.files = nodeTemplateSearchUuid.getFiles();
        this.connections = connections;
    }
}
