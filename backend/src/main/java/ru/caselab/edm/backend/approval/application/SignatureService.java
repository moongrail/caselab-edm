package ru.caselab.edm.backend.approval.application;

import ru.caselab.edm.backend.approval.api.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.platform.security.AuthenticatedUser;

public interface SignatureService {
    void sign(SignatureCreateDTO createDTO, Long documentVersionId, AuthenticatedUser authenticatedUser);
}
