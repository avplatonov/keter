package ru.avplatonov.keter.keterbackend.controllers.management.graphs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import ru.avplatonov.keter.keterbackend.controllers.management.create.CreateNode;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.util.ArrayList;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class GraphListController {

    @RequestMapping(value = "/graphs/{graphSearch}")
    public String listOfGraphs(
            @PathVariable("graphSearch") @NonNull final String value) throws JsonProcessingException {
        return value + "\n" +  searchGraph(value);
    }

    private List<String> searchGraph(String value) throws JsonProcessingException {
        List<String> listOfGraphs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (Node node:CreateNode.listOfNodes) {
            if (node.getDescription().contains(value) || node.getTags().contains(value) || node.getName().contains(value)){
                listOfGraphs.add(mapper.writeValueAsString(node));
            }
        }
        return listOfGraphs;
    }
}
