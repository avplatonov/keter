package ru.avplatonov.keter.backend.controllers;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class Index {

    @RequestMapping("/")
    public String index() {
        return "Welcome to Keter";
    }
}
