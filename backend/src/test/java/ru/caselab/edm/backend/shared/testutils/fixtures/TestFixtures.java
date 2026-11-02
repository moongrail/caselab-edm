package ru.caselab.edm.backend.shared.testutils.fixtures;

import ru.caselab.edm.backend.approval.api.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.approval.domain.ApprovementProcess;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessStatus;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentType;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;
import ru.caselab.edm.backend.shared.directory.UserSummary;

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

    public static UserSummary user(UUID id, String login) {
        return new UserSummary(id, login, login + "@example.com", "First", "Last",
                null, "Engineer", 1L, "Development", null, Set.of("USER"));
    }

    public static UserSummary manager(UUID id, String login, Long departmentId) {
        return new UserSummary(id, login, login + "@example.com", "First", "Last",
                null, "Manager", departmentId, "Development", departmentId, Set.of("USER"));
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

    public static Document document(Long id, UUID ownerId, DocumentType type) {
        Document document = new Document();
        document.setId(id);
        document.setOwnerId(ownerId);
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
                                                          UUID signerId,
                                                          DocumentVersion version,
                                                          ApprovementProcess process,
                                                          ApprovementProcessItemStatus status) {
        return ApprovementProcessItem.builder()
                .id(id)
                .signerId(signerId)
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
