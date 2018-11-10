package ru.avplatonov.keter.backend.controllers.management.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.avplatonov.keter.backend.initialize.GraphTemplate;
import ru.avplatonov.keter.backend.initialize.NodeTemplate;

import java.io.IOException;
import java.util.*;

import static ru.avplatonov.keter.backend.Application.graphsDB;
import static ru.avplatonov.keter.backend.Application.nodesDB;

@RestController
@EnableAutoConfiguration
public class CreateGraph {

    @RequestMapping(value = "/create/graphs",
            headers = {"Content-type=application/json"})
    public String service(
            @RequestBody GraphTemplate graphTemplate
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        //listOfNodes.add(graphTemplate);
        if(createGraph(graphTemplate).isEmpty())
            return "Uuid of node is not found.";
        return "listOfGraphs.size=" + graphsDB.getListOfGraphs().size() + "\n" + mapper.writeValueAsString(createGraph(graphTemplate));
    }

    private Set<NodeTemplate> createGraph(GraphTemplate uuidNodesToGraphTemplate) {
        Set<NodeTemplate> listOfGraphsLocal = new HashSet<>();
        for (UUID graphUuid : uuidNodesToGraphTemplate.getListOfUuidNodes()) {
            boolean containsNode = false;
            for (NodeTemplate nodeTemplateUuid : nodesDB.getListOfNodeTemplates()) {
                if(graphUuid.equals(nodeTemplateUuid.getUuid())){
                    containsNode = true;
                    listOfGraphsLocal.add(nodeTemplateUuid);
                }
            }
            if(!containsNode)
                return new HashSet<>();
        }
        graphsDB.addListOfNodes(listOfGraphsLocal);
        return listOfGraphsLocal;
    }
}