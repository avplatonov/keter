package ru.avplatonov.keter.keterbackend.db;

import org.springframework.stereotype.Component;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.util.ArrayList;
import java.util.List;

@Component
public class GraphsDB {

    private List<List<Node>> listOfGraphs = new ArrayList<>();

    public List<List<Node>> getListOfGraphs() {
        return listOfGraphs;
    }

    public void setListOfGraphs(List<List<Node>> listOfGraphs) {
        this.listOfGraphs = listOfGraphs;
    }

    public void addListOfNodes(List<Node> listOfGraphs){
        this.listOfGraphs.add(listOfGraphs);
    }

}
