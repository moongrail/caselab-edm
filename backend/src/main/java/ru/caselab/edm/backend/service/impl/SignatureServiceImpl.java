package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.SignatureCreateDTO;
import ru.caselab.edm.backend.entity.Signature;
import ru.caselab.edm.backend.exceptions.SignatureAlreadyExistsException;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.SignatureRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.SignatureService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignatureServiceImpl implements SignatureService {
    private final SignatureRepository signatureRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sign(SignatureCreateDTO createDTO, Long documentVersionId) {
        Signature signature = new Signature();

        if(signatureRepository.existsByUserIdAndDocumentVersionId(createDTO.getUserId(), documentVersionId)) {
            throw new SignatureAlreadyExistsException("Signature already exists");
        }

        signature.setUser(userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found")));
        if (createDTO.getCreationDate() == null) {
            createDTO.setCreationDate(LocalDateTime.now());
        }
        signature.setDocumentVersion(documentVersionRepository.findById(documentVersionId)
                .orElseThrow(() -> new NoSuchElementException("DocumentVersion not found"))
        );
        signature.setCreatedAt(createDTO.getCreationDate());
        signature.setHash(hash(createDTO.getUserId(), documentVersionId));

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
