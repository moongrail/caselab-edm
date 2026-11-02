package ru.caselab.edm.security.service;

import ru.caselab.edm.security.api.dto.auth.JwtDTO;
import ru.caselab.edm.security.api.dto.auth.LoginUserDTO;

public interface AuthService {

    JwtDTO authenticate(LoginUserDTO loginUserDTO);
}
