package ru.caselab.edm.backend.approval.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.approval.api.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.approval.application.SignatureService;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.approval.domain.Signature;
import ru.caselab.edm.backend.approval.exception.SignatureAlreadyExistsException;
import ru.caselab.edm.backend.approval.infrastructure.ApprovementItemRepository;
import ru.caselab.edm.backend.approval.infrastructure.SignatureRepository;
import ru.caselab.edm.backend.document.application.DocumentService;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.shared.exception.ResourceNotFoundException;
import ru.caselab.edm.platform.security.AuthenticatedUser;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignatureServiceImpl implements SignatureService {
    private final SignatureRepository signatureRepository;
    private final ApprovementItemRepository approvementItemRepository;
    private final DocumentService documentService;

    @Override
    @Transactional
    public void sign(SignatureCreateDTO createDTO, Long documentId,
                     AuthenticatedUser authenticatedUser) {
        DocumentVersion documentVersion = documentService
                .getLastVersionDocumentWhereUserSignatories(documentId, authenticatedUser.id());

        ApprovementProcessItem approvementProcessItem = approvementItemRepository
                .findByDocumentVersionIdAndSignerId(documentVersion.getId(), authenticatedUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Signing request not found"));

        if (signatureRepository.existsByApprovementProcessItemId(approvementProcessItem.getId())) {
            throw new SignatureAlreadyExistsException(
                    "Document has already been signed by user with id = %s"
                            .formatted(authenticatedUser.id()));
        }

        approvementProcessItem.setStatus(
                ApprovementProcessItemStatus.valueOf(createDTO.getStatus()));

        if (isAuthorSign(documentVersion, authenticatedUser.id())) {
            documentVersion.getStateMachine().signAuthor(documentVersion);
        } else {
            documentVersion.getStateMachine().signContractor(approvementProcessItem);
        }

        Signature signature = new Signature();
        signature.setCreatedAt(LocalDateTime.now());
        signature.setApprovementProcessItem(approvementProcessItem);
        signature.setHash(hash(authenticatedUser.id(), documentVersion.getId()));

        approvementItemRepository.save(approvementProcessItem);
        signatureRepository.save(signature);
    }

    private boolean isAuthorSign(DocumentVersion version, UUID userId) {
        return version.getDocument().getOwnerId().equals(userId);
    }

    private String hash(UUID userId, Long documentVersionId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = userId.toString() + documentVersionId;
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm SHA-256 is not available", e);
        }
    }
}
