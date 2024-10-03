package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check")
public class TestController {
    @GetMapping
    @Operation(summary = "ИЛИ НЕ ЖИВОЕ????????")
    public String checkController(){
             return "It's ALIVE!!!!!!";
    }
}
