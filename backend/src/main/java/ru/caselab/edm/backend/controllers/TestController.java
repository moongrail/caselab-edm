package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.JwtDto;
import ru.caselab.edm.backend.dto.LoginUserDto;
import ru.caselab.edm.backend.service.impl.TestService;

@RestController
@RequestMapping("/check")
@AllArgsConstructor
@CrossOrigin
public class TestController {

    TestService testService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ИЛИ НЕ ЖИВОЕ????????")
    public String checkController(){
         return "It's ALIVE!!!!!!";
    }

    @PostMapping("/jwtTest")
    @Operation(summary = "JWT TEST")
    public ResponseEntity<JwtDto> jwtTest(@RequestBody LoginUserDto dto) {
        return ResponseEntity.ok(testService.testJwt(dto));
    }
}
