package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;

public interface SignatureService {
    void sign(SignatureCreateDTO createDTO, Long documentVersionId, UserInfoDetails authenticatedUser);
}
