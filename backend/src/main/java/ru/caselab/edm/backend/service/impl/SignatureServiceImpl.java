package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.SignatureCreateDTO;
import ru.caselab.edm.backend.entity.*;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.SignatureRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.SignatureService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
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

    @Override
    @Transactional
    public void sign(SignatureCreateDTO createDTO, Long documentVersionId, UserInfoDetails authenticatedUser) {
        Optional<DocumentVersion> documentVersionOptional = documentVersionRepository.findById(documentVersionId);
        if (documentVersionOptional.isEmpty())
            throw new ResourceNotFoundException("Document version not found with id = %d".formatted(documentVersionId));


        Optional<User> userOptional = userRepository.findById(createDTO.getUserId());
        if (userOptional.isEmpty())
            throw new ResourceNotFoundException("User not found with id = %s".formatted(createDTO.getUserId()));

        User user = userOptional.get();
        DocumentVersion documentVersion = documentVersionOptional.get();
        if (!documentVersion.getDocument().getUser().getId().equals(authenticatedUser.getId()))
            throw new DocumentForbiddenAccess("You don't have a access to document with id = %d".formatted(documentVersionId));

        if (!approvementItemRepository.existsByDocumentVersionIdAndUserId(documentVersionId, user.getId()))
            throw new ResourceNotFoundException("Signing request not found");

        ApprovementProcessItem approvementProcessItem = approvementItemRepository.findByDocumentVersionIdAndUserId(documentVersionId, user.getId()).get();
        approvementProcessItem.setStatus(ApprovementProcessItemStatus.valueOf(createDTO.getStatus()));

        Signature signature = new Signature();
        signature.setCreatedAt(LocalDateTime.now());
        signature.setApprovementProcessItem(approvementProcessItem);
        signature.setHash(hash(user.getId(), documentVersionId));

        approvementItemRepository.save(approvementProcessItem);
        signatureRepository.save(signature);
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
