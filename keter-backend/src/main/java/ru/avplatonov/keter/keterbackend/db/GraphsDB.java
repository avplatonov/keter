package ru.avplatonov.keter.keterbackend.db;

import org.springframework.stereotype.Component;
import ru.avplatonov.keter.keterbackend.initialize.NodeTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class GraphsDB {

    private List<Set<NodeTemplate>> listOfGraphs = new ArrayList<>();

    public List<Set<NodeTemplate>> getListOfGraphs() {
        return listOfGraphs;
    }

    public void setListOfGraphs(List<Set<NodeTemplate>> listOfGraphs) {
        this.listOfGraphs = listOfGraphs;
    }

    public void addListOfNodes(Set<NodeTemplate> listOfGraphs){
        this.listOfGraphs.add(listOfGraphs);
    }

}
