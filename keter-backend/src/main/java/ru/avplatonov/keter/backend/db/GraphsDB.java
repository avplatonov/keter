package ru.avplatonov.keter.backend.db;

import org.springframework.stereotype.Component;
import ru.avplatonov.keter.backend.initialize.managet.GraphTemplate;

import java.util.Set;

@Component
public class GraphsDB {

    private Set<GraphTemplate> listOfGraphs;

    public Set<GraphTemplate> getListOfGraphs() {
        return listOfGraphs;
    }

    public void addListOfGraphTemplate(GraphTemplate listOfGraphs){
        this.listOfGraphs.add(listOfGraphs);
    }

}
