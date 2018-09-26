package ru.avplatonov.keter.backend.controllers.management.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import ru.avplatonov.keter.backend.initialize.NodeTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ru.avplatonov.keter.backend.Application.nodesDB;

@RestController
@EnableAutoConfiguration
public class NodeListController {

    @RequestMapping(value = "/nodes/all")
    public Set<NodeTemplate> service() throws JsonProcessingException {
        return nodesDB.getListOfNodeTemplates();
    }

    @RequestMapping(value = "/nodes/{nodeSearch}")
    public String service(
            @PathVariable("nodeSearch") @NonNull String value
    ) throws JsonProcessingException {
        return value + "\n" +  searchNode(value); //.toLowerCase() ?
    }

    private List<String> searchNode(String value) throws JsonProcessingException {
        List<String> listOfNodes = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (NodeTemplate nodeTemplate : nodesDB.getListOfNodeTemplates()) {
            if (nodeTemplate.getDescription().contains(value) || nodeTemplate.getTags().contains(value) || nodeTemplate.getName().contains(value)){
                listOfNodes.add(mapper.writeValueAsString(nodeTemplate));
            }
        }
        return listOfNodes;
    }
}
