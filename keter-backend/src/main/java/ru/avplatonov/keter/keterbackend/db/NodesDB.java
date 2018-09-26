package ru.avplatonov.keter.keterbackend.db;

import org.springframework.stereotype.Component;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class NodesDB {

    private Set<Node> listOfNodes = new HashSet<>();

    public Set<Node> getListOfNodes() {
        return listOfNodes;
    }

    public void addListOfNodes(Node listOfNodes){
        this.listOfNodes.add(listOfNodes);
    }

    public void setListOfNodes(Set<Node> listOfNodes) {
        this.listOfNodes = listOfNodes;
    }

}
