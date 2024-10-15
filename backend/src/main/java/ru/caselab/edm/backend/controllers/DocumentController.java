package ru.caselab.edm.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.*;
import ru.caselab.edm.backend.mapper.DocumentMapper;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.SignatureService;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final SignatureService signatureService;

    @PostMapping("/{id}/sign")
    @ResponseStatus(HttpStatus.OK)
    public void signDocument(@Valid @RequestBody SignatureCreateDTO signatureCreateDTO, @PathVariable Long id) {
        signatureService.sign(signatureCreateDTO, id);
    }

    private final UserRepository userRepository;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDTO createDocumentType(@Valid @RequestBody DocumentCreateDTO documentTypeCreateDTO) {
        return documentMapper.toDto(documentService.saveDocument(documentTypeCreateDTO));
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public DocumentPageDTO getAllDocuments(@RequestParam(name = "page", defaultValue = "0") @Min(value = 0) int page,
                                           @RequestParam(name = "size", defaultValue = "10") @Min(value = 1) @Max(value = 100) int size,
                                           Principal principal) {
        UUID userId = getUserByPrincipal(principal).getId();
        return documentMapper.toDtoPage(documentService.getAllDocumentForUser(page, size, userId));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDTO getDocumentById(@PathVariable Long id,
                                       Principal principal) {
        UUID userId = getUserByPrincipal(principal).getId();
        return documentMapper.toDto(documentService.getDocumentForUser(id, userId));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDTO updateDocumentType(@PathVariable Long id,
                                          @RequestBody @Valid DocumentUpdateDTO updateDocumentType) {
        return documentMapper.toDto(documentService.updateDocument(id, updateDocumentType));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocumentType(@PathVariable Long id) {
        documentService.deleteDocument(id);
    }

    private final User getUserByPrincipal(Principal principal) {
        return userRepository
                .findUserByLogin(principal.getName())
                .get();
    }
}
