package ru.caselab.edm.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.JwtDto;
import ru.caselab.edm.backend.dto.LoginUserDto;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.JwtService;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestService {
    // здесь вставляем все роли юзера и т.д крч тестовый сервис

    JwtService jwtService;
    UserRepository userRepository;
    RoleRepository roleRepository;

    @Autowired
    public TestService(JwtService jwtService, UserRepository userRepository,
                       RoleRepository roleRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public JwtDto testJwt(LoginUserDto dto) {
        User user = userRepository.findUserByLogin(dto.login()).orElseThrow(() -> new RuntimeException("Not found"));

        UserInfoDetails userInfoDetails = new UserInfoDetails(user);

        JwtDto jwtDto = new JwtDto(jwtService.generateToken(userInfoDetails));

        return jwtDto;
    }
}
