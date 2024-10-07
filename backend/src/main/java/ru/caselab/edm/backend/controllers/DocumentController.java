package ru.caselab.edm.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentDTO;
import ru.caselab.edm.backend.dto.DocumentPageDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.mapper.DocumentMapper;
import ru.caselab.edm.backend.service.DocumentService;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDTO createDocumentType(@Valid @RequestBody DocumentCreateDTO documentTypeCreateDTO) {
        return documentMapper.toDto(
                documentService.saveDocument(
                        documentMapper.toEntity(documentTypeCreateDTO)
                )
        );
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public DocumentPageDTO getAllDocuments(@RequestParam(name = "page", defaultValue = "0") @Min(value = 0) int page,
                                           @RequestParam(name = "size", defaultValue = "10") @Min(value = 1) @Max(value = 100) int size) {
        return documentMapper.toDtoPage(
                documentService.getAllDocuments(page, size)
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDTO getDocumentById(@PathVariable Long id) {
        return documentMapper.toDto(
                documentService.getDocument(id)
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDTO updateDocumentType(@PathVariable Long id,
                                          @RequestBody @Valid DocumentUpdateDTO updateDocumentType) {
        return documentMapper.toDto(
                documentService.updateDocument(
                        id,
                        documentMapper.toEntity(updateDocumentType)
                )
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocumentType(@PathVariable Long id) {
        documentService.deleteDocument(id);
    }
}
