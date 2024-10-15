package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.SignatureCreateDTO;

public interface SignatureService {
    void sign(SignatureCreateDTO createDTO, Long documentVersionId);
}
