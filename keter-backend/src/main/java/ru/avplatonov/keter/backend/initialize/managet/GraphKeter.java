package ru.avplatonov.keter.backend.initialize.managet;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GraphKeter {
    private UUID uuidGraph = UUID.randomUUID();
    private int idRun = 2;
    private String status;
    private List<NodesKeter> nodesList;
    private String graphName;
    private String graphDescription;
    private Map<Integer, Map<String,String>> parametrs;

    public void setIdRun(int idRun) {
        this.idRun = idRun;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNodesList(List<NodesKeter> nodesList) {
        this.nodesList = nodesList;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public void setGraphDescription(String graphDescription) {
        this.graphDescription = graphDescription;
    }

    public void setParametrs(Map<Integer, Map<String, String>> parametrs) {
        this.parametrs = parametrs;
    }

    public UUID getUuidGraph() {
        return uuidGraph;
    }

    public int getIdRun() {
        return idRun;
    }

    public String getStatus() {
        return status;
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

    public Map<Integer, Map<String, String>> getParametrs() {
        return parametrs;
    }

    public GraphKeter(int idRun, String status, List<NodesKeter> nodesList, String graphName, String graphDescription, Map<Integer, Map<String, String>> parametrs) {
        this.idRun = idRun;
        this.status = status;
        this.nodesList = nodesList;
        this.graphName = graphName;
        this.graphDescription = graphDescription;
        this.parametrs = parametrs;
    }
}
