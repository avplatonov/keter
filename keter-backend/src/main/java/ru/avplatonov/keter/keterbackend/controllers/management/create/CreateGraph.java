package ru.avplatonov.keter.keterbackend.controllers.management.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.keterbackend.initialize.Graph;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.avplatonov.keter.keterbackend.controllers.management.create.CreateNode.listOfNodes;

@RestController
@EnableAutoConfiguration
public class CreateGraph {

    public static List<List<Node>> listOfGraphs = new ArrayList<>();

    @RequestMapping(value = "/create/graphs",
            headers = {"Content-type=application/json"})
    public String service(@RequestBody Graph graph) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        //listOfNodes.add(graph);
        if(createGraph(graph).isEmpty())
            return "Name of node is not found.";
        return "listOfGraphs.size=" + listOfGraphs.size() + "\n" + mapper.writeValueAsString(createGraph(graph));
    }

    private List<Node> createGraph(Graph uuidNodesToGraph) {
        List<Node> listOfGraphsLocal = new ArrayList<>();
        for (UUID graphUuid : uuidNodesToGraph.getListOfUuidNodes()) {
            boolean containsNode = false;
            for (Node nodeUuid : listOfNodes) {
                if(graphUuid.equals(nodeUuid.getUuid())){
                    containsNode = true;
                    listOfGraphsLocal.add(nodeUuid);
                }
            }
            if(!containsNode)
                return new ArrayList<>();
        }
        listOfGraphs.add(listOfGraphsLocal);
        return listOfGraphsLocal;
    }
}