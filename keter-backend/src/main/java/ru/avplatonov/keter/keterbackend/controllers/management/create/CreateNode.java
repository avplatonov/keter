package ru.avplatonov.keter.keterbackend.controllers.management.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class CreateNode {

    public static List<Node> listOfNodes = new ArrayList<>();

    @RequestMapping(value = "/create/nodes",
            headers = {"Content-type=application/json"})
    public String service(@RequestBody Node node) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        listOfNodes.add(node);
        return "listOfNode.size=" + listOfNodes.size() + "\n" + mapper.writeValueAsString(node) ;
    }
}
