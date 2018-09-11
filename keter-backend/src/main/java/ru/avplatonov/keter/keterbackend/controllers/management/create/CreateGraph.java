package ru.avplatonov.keter.keterbackend.controllers.management.create;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.keterbackend.initialize.NameNodesToGraph;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.avplatonov.keter.keterbackend.controllers.management.create.CreateNode.listOfNodes;

@RestController
@EnableAutoConfiguration
public class CreateGraph {

    public static List<List<Node>> listOfGraphs = new ArrayList<>();

    @RequestMapping(value = "/create/graphs",
            headers = {"Content-type=application/json"})
    public String service(@RequestBody NameNodesToGraph nameNodesToGraph) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        //listOfNodes.add(nameNodesToGraph);
        if(createGraph(nameNodesToGraph).isEmpty())
            return "Name of node is not found.";
        return "listOfGraphs.size=" + listOfGraphs.size() + "\n" + mapper.writeValueAsString(createGraph(nameNodesToGraph));
    }

    private List<Node> createGraph(NameNodesToGraph nameNodesToGraph) {
        List<Node> listOfGraphsLocal = new ArrayList<>();
        for (String graph : nameNodesToGraph.listOfNameNodes) {
            boolean containsNode = false;
            for (Node node : listOfNodes) {
                if(graph.equals(node.getName())){
                    containsNode = true;
                    listOfGraphsLocal.add(node);
                }
            }
            if(!containsNode)
                return new ArrayList<>();
        }
        listOfGraphs.add(listOfGraphsLocal);
        return listOfGraphsLocal;
    }
}