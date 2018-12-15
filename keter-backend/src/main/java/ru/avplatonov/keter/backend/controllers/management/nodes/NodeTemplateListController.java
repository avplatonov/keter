package ru.avplatonov.keter.backend.controllers.management.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.backend.initialize.managet.NodeTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ru.avplatonov.keter.backend.Application.nodesDB;

@RestController
@EnableAutoConfiguration
public class NodeTemplateListController {

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(value = "/nodes/all")
    public Set<NodeTemplate> service() throws JsonProcessingException {
        return nodesDB.getListOfNodeTemplates();
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(value = "/nodes/{nodeSearch}")
    public String service(
            @PathVariable("nodeSearch") @NonNull String value
    ) throws JsonProcessingException {
        return value + "\n" +  searchNode(value); //.toLowerCase() ?
    }

    private List<NodeTemplate> searchNode(String value) throws JsonProcessingException {
        List<NodeTemplate> listOfNodes = new ArrayList<>();
        //ObjectMapper mapper = new ObjectMapper();
        for (NodeTemplate nodeTemplate : nodesDB.getListOfNodeTemplates()) {
            if (nodeTemplate.getDescription().contains(value) || nodeTemplate.getTags().contains(value) || nodeTemplate.getName().contains(value)){
                listOfNodes.add(nodeTemplate);
            }
        }
        return listOfNodes;
    }
}
