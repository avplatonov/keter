package ru.avplatonov.keter.keterbackend.db;

import org.springframework.stereotype.Component;
import ru.avplatonov.keter.keterbackend.initialize.NodeTemplate;

import java.util.HashSet;
import java.util.Set;

@Component
public class NodesDB {

    private Set<NodeTemplate> listOfNodeTemplates = new HashSet<>();

    public Set<NodeTemplate> getListOfNodeTemplates() {
        return listOfNodeTemplates;
    }

    public void addListOfNodes(NodeTemplate listOfNodes){
        this.listOfNodeTemplates.add(listOfNodes);
    }

    public void setListOfNodeTemplates(Set<NodeTemplate> listOfNodeTemplates) {
        this.listOfNodeTemplates = listOfNodeTemplates;
    }

}
