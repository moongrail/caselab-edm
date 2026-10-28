package ru.caselab.edm.backend.testutils.fixtures;

import ru.caselab.edm.backend.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Small object-mother for service tests. Keeps domain setup readable and consistent.
 */
public final class TestFixtures {

    public static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID SIGNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID THIRD_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final long DOCUMENT_ID = 1L;
    public static final long VERSION_ID = 10L;

    private TestFixtures() {
    }

    public static User user(UUID id, String login) {
        return User.builder()
                .id(id)
                .login(login)
                .email(login + "@example.com")
                .password("encoded-password")
                .firstName("First")
                .lastName("Last")
                .position("Engineer")
                .build();
    }

    public static DocumentType documentType(Long id, Attribute... attributes) {
        DocumentType type = new DocumentType();
        type.setId(id);
        type.setName("Type " + id);
        type.setAttributes(new HashSet<>(Set.of(attributes)));
        return type;
    }

    public static Attribute attribute(Long id, boolean required) {
        return Attribute.builder()
                .id(id)
                .name("Attribute " + id)
                .dataType("STRING")
                .isRequired(required)
                .build();
    }

    public static Document document(Long id, User owner, DocumentType type) {
        Document document = new Document();
        document.setId(id);
        document.setUser(owner);
        document.setDocumentType(type);
        document.setDocumentVersion(new ArrayList<>());
        return document;
    }

    public static DocumentVersion version(Long id, Document document, DocumentStatus status) {
        DocumentVersion version = new DocumentVersion();
        version.setId(id);
        version.setDocument(document);
        version.setDocumentName("Document name");
        version.setContentUrl("content/" + id);
        version.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        version.setStatus(status);
        version.setDocumentAttributeValue(new ArrayList<>());
        version.setApprovementProcessItems(new ArrayList<>());
        version.setApprovementProcesses(new ArrayList<>());
        document.getDocumentVersion().add(version);
        return version;
    }

    public static ApprovementProcessItem approvementItem(Long id,
                                                          User user,
                                                          DocumentVersion version,
                                                          ApprovementProcess process,
                                                          ApprovementProcessItemStatus status) {
        return ApprovementProcessItem.builder()
                .id(id)
                .user(user)
                .documentVersion(version)
                .approvementProcess(process)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ApprovementProcess approvementProcess(Long id,
                                                         DocumentVersion version,
                                                         ApprovementProcessStatus status) {
        return ApprovementProcess.builder()
                .id(id)
                .documentVersion(version)
                .status(status)
                .deadline(LocalDateTime.now().plusDays(1))
                .agreementProcent(50f)
                .approvementProcessItems(new ArrayList<>())
                .build();
    }

    public static SignatureCreateDTO signature(String status) {
        SignatureCreateDTO dto = new SignatureCreateDTO();
        dto.setStatus(status);
        return dto;
    }
}
