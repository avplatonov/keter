package ru.avplatonov.keter.keterbackend.controllers.management.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import ru.avplatonov.keter.keterbackend.controllers.management.create.CreateNode;
import ru.avplatonov.keter.keterbackend.db.GraphsDB;
import ru.avplatonov.keter.keterbackend.db.NodesDB;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ru.avplatonov.keter.keterbackend.Application.nodesDB;

@RestController
@EnableAutoConfiguration
public class NodeListController {

    @RequestMapping(value = "/nodes/all")
    public Set<Node> service() throws JsonProcessingException {
        return nodesDB.getListOfNodes();
    }

    @RequestMapping(value = "/nodes/{nodeSearch}")
    public String service(@PathVariable("nodeSearch") @NonNull final String value) throws JsonProcessingException {
        return value + "\n" +  searchNode(value); //.toLowerCase() ?
    }

    private List<String> searchNode(String value) throws JsonProcessingException {
        List<String> listOfNodes = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (Node node : nodesDB.getListOfNodes()) {
            if (node.getDescription().contains(value) || node.getTags().contains(value) || node.getName().contains(value)){
                listOfNodes.add(mapper.writeValueAsString(node));
            }
        }
        return listOfNodes;
    }
}
