package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.*;
import ru.caselab.edm.backend.mapper.DocumentMapper;
import ru.caselab.edm.backend.mapper.DocumentVersionMapper;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.SignatureService;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "Document", description = "Document management operations")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final SignatureService signatureService;
    private final DocumentVersionMapper documentVersionMapper;

    @PostMapping("/{id}/sign")
    @ResponseStatus(HttpStatus.OK)
    public void signDocument(@Valid @RequestBody SignatureCreateDTO signatureCreateDTO, @PathVariable Long id) {
        signatureService.sign(signatureCreateDTO, id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentVersionDTO createDocumentType(@Valid @RequestBody DocumentCreateDTO documentTypeCreateDTO) {
        return documentVersionMapper.toDto(documentService.saveDocument(documentTypeCreateDTO));
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public DocumentPageDTO getAllDocuments(@RequestParam(name = "page", defaultValue = "0") @Min(value = 0) int page,
                                           @RequestParam(name = "size", defaultValue = "10") @Min(value = 1) @Max(value = 100) int size) {
        return documentMapper.toDtoPage(documentService.getAllDocuments(page, size));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentVersionDTO getDocumentById(@PathVariable Long id) {
        return documentVersionMapper.toDto(documentService.getDocumentVersion(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentVersionDTO updateDocumentType(@PathVariable Long id,
                                          @RequestBody @Valid DocumentUpdateDTO updateDocumentType) {
        return documentVersionMapper.toDto(documentService.updateDocument(id, updateDocumentType));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocumentType(@PathVariable Long id) {
        documentService.deleteDocument(id);
    }
}
