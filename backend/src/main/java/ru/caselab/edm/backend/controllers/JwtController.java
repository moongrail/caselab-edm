package ru.caselab.edm.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.dto.JwtDto;
import ru.caselab.edm.backend.dto.RefreshTokenDto;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.service.JwtService;
import ru.caselab.edm.backend.service.RefreshTokenService;

@RestController
@RequestMapping("/jwt")
public class JwtController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;


    @Autowired
    public JwtController(RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/update")
    public ResponseEntity<JwtDto> updateJwtToken(@RequestBody RefreshTokenDto refreshTokenDto) throws ResourceNotFoundException {
        System.out.println(refreshTokenDto);
        User user = refreshTokenService.getUserByToken(refreshTokenDto.refreshToken());

        UserInfoDetails userInfoDetails = new UserInfoDetails(user);

        JwtDto jwtDto = new JwtDto(refreshTokenDto.refreshToken(),
                jwtService.generateToken(userInfoDetails));

        return ResponseEntity.ok(jwtDto);
    }
}
