package ru.avplatonov.keter.backend.controllers.management.graphs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.backend.initialize.managet.GraphTemplate;

import java.util.ArrayList;
import java.util.List;

import static ru.avplatonov.keter.backend.Application.graphsDB;

@RestController
@EnableAutoConfiguration
public class GraphTemplateListController {

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(value = "/graphs/{graphSearch}")
    public String listOfGraphs(
            @PathVariable("graphSearch") @NonNull String value
    ) throws JsonProcessingException {
        return value + "\n" +  searchGraph(value);
    }

    private List<GraphTemplate> searchGraph(String value) throws JsonProcessingException {
        List<GraphTemplate> listOfGraphs = new ArrayList<>();
        for (GraphTemplate graphTemplate : graphsDB.getListOfGraphs()) {
            if (graphTemplate.getGraphName().contains(value) || graphTemplate.getOutputs().contains(value)){
                listOfGraphs.add(graphTemplate);
            }
        }
        return listOfGraphs;
    }
}
