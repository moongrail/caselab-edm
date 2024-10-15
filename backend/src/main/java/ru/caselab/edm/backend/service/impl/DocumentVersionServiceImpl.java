package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.service.DocumentVersionService;

@Service
@RequiredArgsConstructor
public class DocumentVersionServiceImpl implements DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;

    @Override
    public DocumentVersion getDocumentVersion(long id) {
        return documentVersionRepository.getById(id);
    }
}
