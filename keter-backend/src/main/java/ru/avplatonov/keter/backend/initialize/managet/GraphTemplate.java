package ru.avplatonov.keter.backend.initialize.managet;

import java.util.List;
import java.util.UUID;

public class GraphTemplate {

    private UUID uuidGraphTemplate = UUID.randomUUID();
    private List<NodesKeter> nodesList;
    private String graphName;
    private String graphDescription;

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public void setGraphDescription(String graphDescription) {
        this.graphDescription = graphDescription;
    }

    public void setNodesList(List<NodesKeter> nodesList) {
        this.nodesList = nodesList;
    }

    public UUID getUuidGraph() {
        return uuidGraphTemplate;
    }

    public List<NodesKeter> getNodesList() {
        return nodesList;
    }

    public String getGraphName() {
        return graphName;
    }

    public String getGraphDescription() {
        return graphDescription;
    }

    public GraphTemplate(){
    }

    public GraphTemplate(List<NodesKeter> nodesList, String graphName, String graphDescription) {
        this.nodesList = nodesList;
        this.graphName = graphName;
        this.graphDescription = graphDescription;
    }
}
