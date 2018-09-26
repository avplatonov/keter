package ru.avplatonov.keter.keterbackend.initialize;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GraphTemplate {

    private UUID uuidGraph = UUID.randomUUID();
    private List<UUID> listOfUuidNodes = new ArrayList<>();

    public UUID getUuid() {
        return uuidGraph;
    }

    public List<UUID> getListOfUuidNodes() {
        return listOfUuidNodes;
    }

    public List<UUID> listOfUuidNodes() {
        return listOfUuidNodes;
    }

    public void setListOfNodes(List<UUID> listOfNodes) {
        this.listOfUuidNodes = listOfNodes;
    }

    public GraphTemplate(){

    }

    public GraphTemplate(List<UUID> listOfNodes) {
        this.listOfUuidNodes = listOfNodes;
    }
}
