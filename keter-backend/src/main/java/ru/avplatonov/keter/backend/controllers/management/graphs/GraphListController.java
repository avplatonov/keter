package ru.avplatonov.keter.backend.controllers.management.graphs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import ru.avplatonov.keter.backend.initialize.NodeTemplate;

import java.util.ArrayList;
import java.util.List;

import static ru.avplatonov.keter.backend.Application.nodesDB;

@RestController
@EnableAutoConfiguration
public class GraphListController {

    @RequestMapping(value = "/graphs/{graphSearch}")
    public String listOfGraphs(
            @PathVariable("graphSearch") @NonNull String value
    ) throws JsonProcessingException {
        return value + "\n" +  searchGraph(value);
    }

    private List<String> searchGraph(String value) throws JsonProcessingException {
        List<String> listOfGraphs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (NodeTemplate nodeTemplate : nodesDB.getListOfNodeTemplates()) {
            if (nodeTemplate.getDescription().contains(value) || nodeTemplate.getTags().contains(value) || nodeTemplate.getName().contains(value)){
                listOfGraphs.add(mapper.writeValueAsString(nodeTemplate));
            }
        }
        return listOfGraphs;
    }
}
