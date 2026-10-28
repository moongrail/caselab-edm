package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.auth.JwtDTO;
import ru.caselab.edm.backend.dto.auth.LoginUserDTO;

public interface AuthService {

    JwtDTO authenticate(LoginUserDTO loginUserDTO);
}
