package ru.avplatonov.keter.keterbackend.db;

import org.springframework.stereotype.Component;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.util.ArrayList;
import java.util.List;

@Component
public class NodesDB {

    private List<Node> listOfNodes = new ArrayList<>();

    public List<Node> getListOfNodes() {
        return listOfNodes;
    }

    public void addListOfNodes(Node listOfNodes){
        this.listOfNodes.add(listOfNodes);
    }

    public void setListOfNodes(List<Node> listOfNodes) {
        this.listOfNodes = listOfNodes;
    }

}
