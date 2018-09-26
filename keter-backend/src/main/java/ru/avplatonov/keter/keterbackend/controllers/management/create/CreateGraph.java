package ru.avplatonov.keter.keterbackend.controllers.management.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.avplatonov.keter.keterbackend.initialize.Graph;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.io.IOException;
import java.util.*;

import static ru.avplatonov.keter.keterbackend.Application.graphsDB;
import static ru.avplatonov.keter.keterbackend.Application.nodesDB;

@RestController
@EnableAutoConfiguration
public class CreateGraph {

    @RequestMapping(value = "/create/graphs",
            headers = {"Content-type=application/json"})
    public String service(@RequestBody Graph graph) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        //listOfNodes.add(graph);
        if(createGraph(graph).isEmpty())
            return "Uuid of node is not found.";
        return "listOfGraphs.size=" + graphsDB.getListOfGraphs().size() + "\n" + mapper.writeValueAsString(createGraph(graph));
    }

    private Set<Node> createGraph(Graph uuidNodesToGraph) {
        Set<Node> listOfGraphsLocal = new HashSet<>();
        for (UUID graphUuid : uuidNodesToGraph.getListOfUuidNodes()) {
            boolean containsNode = false;
            for (Node nodeUuid : nodesDB.getListOfNodes()) {
                if(graphUuid.equals(nodeUuid.getUuid())){
                    containsNode = true;
                    listOfGraphsLocal.add(nodeUuid);
                }
            }
            if(!containsNode)
                return new HashSet<>();
        }
        graphsDB.addListOfNodes(listOfGraphsLocal);
        return listOfGraphsLocal;
    }
}