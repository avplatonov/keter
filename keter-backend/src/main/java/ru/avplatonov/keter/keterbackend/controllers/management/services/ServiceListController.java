package ru.avplatonov.keter.keterbackend.controllers.management.services;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class ServiceListController {

    @GetMapping("/services")
    public String service() {
        return null;
    }
}
