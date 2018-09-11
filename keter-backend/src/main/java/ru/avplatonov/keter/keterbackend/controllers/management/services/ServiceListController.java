package ru.avplatonov.keter.keterbackend.controllers.management.services;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.keterbackend.initialize.GraphInitialize;

@RestController
@EnableAutoConfiguration
public class ServiceListController {

    @GetMapping("/services")
    public String service() {
        return String.valueOf(GraphInitialize.getSmallList());
    }
}
