package ru.avplatonov.keter.keterbackend.controllers.management.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.keterbackend.db.GraphsDB;
import ru.avplatonov.keter.keterbackend.db.NodesDB;
import ru.avplatonov.keter.keterbackend.initialize.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.avplatonov.keter.keterbackend.Application.nodesDB;

@RestController
@EnableAutoConfiguration
public class CreateNode {

    @RequestMapping(value = "/create/nodes",
            headers = {"Content-type=application/json"})
    public String service(@RequestBody Node node) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        nodesDB.addListOfNodes(node);
        return "listOfNode.size=" + nodesDB.getListOfNodes().size() + "\n" + mapper.writeValueAsString(node) ;
    }
}
