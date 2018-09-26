package ru.avplatonov.keter.keterbackend.controllers.management.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.keterbackend.initialize.NodeTemplate;

import java.io.IOException;

import static ru.avplatonov.keter.keterbackend.Application.nodesDB;

@RestController
@EnableAutoConfiguration
public class CreateNode {

    @RequestMapping(value = "/create/nodes",
            headers = {"Content-type=application/json"})
    public String service(@RequestBody NodeTemplate nodeTemplate) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        nodesDB.addListOfNodes(nodeTemplate);
        return "listOfNode.size=" + nodesDB.getListOfNodeTemplates().size() + "\n" + mapper.writeValueAsString(nodeTemplate) ;
    }
}
