package ru.avplatonov.keter.backend.controllers.management.create;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.backend.initialize.managet.GraphTemplate;

import static ru.avplatonov.keter.backend.Application.graphsDB;

@RestController
@EnableAutoConfiguration
public class CreateGraphTemplate {

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(value = "/create/graphs",
            headers = {"Content-type=application/json"})
    public String service(
            @RequestBody GraphTemplate graphTemplate
    ){
        try {
            graphsDB.addListOfGraphTemplate(graphTemplate);
            return "Success";
        }catch (Exception ex){
            return "Failed";
        }
    }
}