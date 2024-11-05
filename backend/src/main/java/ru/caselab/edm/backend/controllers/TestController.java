package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check")
@AllArgsConstructor
@CrossOrigin
@Tag(name = "Test", description = "For testing")
public class TestController {

    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ИЛИ НЕ ЖИВОЕ????????")
    public String checkController() {
        return "It's ALIVE!!!!!!";
    }
}
