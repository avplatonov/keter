package ru.avplatonov.keter.keterbackend.db;

import org.springframework.stereotype.Component;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class GraphsDB {

    private List<Set<Node>> listOfGraphs = new ArrayList<>();

    public List<Set<Node>> getListOfGraphs() {
        return listOfGraphs;
    }

    public void setListOfGraphs(List<Set<Node>> listOfGraphs) {
        this.listOfGraphs = listOfGraphs;
    }

    public void addListOfNodes(Set<Node> listOfGraphs){
        this.listOfGraphs.add(listOfGraphs);
    }

}
