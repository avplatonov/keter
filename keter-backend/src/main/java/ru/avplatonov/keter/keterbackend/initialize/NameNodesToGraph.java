package ru.avplatonov.keter.keterbackend.initialize;

import java.util.ArrayList;
import java.util.List;

public class NameNodesToGraph {
    public List<String> getListOfNameNodes() {
        return listOfNameNodes;
    }

    public void setListOfNameNodes(List<String> listOfNameNodes) {
        this.listOfNameNodes = listOfNameNodes;
    }

    public List<String> listOfNameNodes = new ArrayList<>();

    public NameNodesToGraph(){

    }

    public NameNodesToGraph(List<String> listOfNameNodes) {
        this.listOfNameNodes = listOfNameNodes;
    }
}
