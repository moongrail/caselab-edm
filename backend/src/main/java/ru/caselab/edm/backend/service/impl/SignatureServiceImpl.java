package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.signature.SignatureCreateDTO;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.Signature;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.SignatureRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.SignatureService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignatureServiceImpl implements SignatureService {
    private final SignatureRepository signatureRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;
    private final ApprovementItemRepository approvementItemRepository;
    private final DocumentService documentService;

    @Override
    @Transactional
    public void sign(SignatureCreateDTO createDTO, Long documentId, UserInfoDetails authenticatedUser) {

        DocumentVersion documentVersion = documentService.getLastVersionDocumentWhereUserSignatories(documentId, authenticatedUser.getId());

        Optional<ApprovementProcessItem> approvementProcessItemOptional = approvementItemRepository.findByDocumentVersionIdAndUserId(documentVersion.getId(), authenticatedUser.getId());

        if (approvementProcessItemOptional.isEmpty()) {
            throw new ResourceNotFoundException("Signing request not found");
        }

        ApprovementProcessItem approvementProcessItem = approvementProcessItemOptional.get();
        approvementProcessItem.setStatus(ApprovementProcessItemStatus.valueOf(createDTO.getStatus()));

        //Изменение статуса версии документа после подписи
        if (isAuthorSign(documentVersion, authenticatedUser.getId())) {
            //проверка, что документ в данном состоянии может подписать автор
            documentVersion.getState().signAuthor(documentVersion);
        } else {
            //проверка, что документ в данном состоянии может подписать контрагент
            documentVersion.getState().signContractor(approvementProcessItem);

        }

        Signature signature = new Signature();
        signature.setCreatedAt(LocalDateTime.now());
        signature.setApprovementProcessItem(approvementProcessItem);
        signature.setHash(hash(authenticatedUser.getId(), documentVersion.getId()));

        approvementItemRepository.save(approvementProcessItem);
        signatureRepository.save(signature);
    }

    private boolean isAuthorSign(DocumentVersion version, UUID userId) {
        return version.getDocument().getUser().getId().equals(userId);
    }

    private String hash(UUID userId, Long documentVersionId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String combined = userId.toString() + documentVersionId.toString();

            byte[] hashBytes = digest.digest(combined.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }
}
