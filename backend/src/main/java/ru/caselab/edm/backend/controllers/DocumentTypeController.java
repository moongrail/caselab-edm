package ru.caselab.edm.backend.controllers;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.service.DocumentTypeService;

@RequiredArgsConstructor
@Data
@RestController
@RequestMapping("/document_type")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentTypeDTO createDocumentType(@Valid @RequestBody DocumentTypeCreateDTO documentTypeCreateDTO) {
        return documentTypeService.createDocumentType(documentTypeCreateDTO);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentTypeDTO> showAllDocumentTypes(@RequestParam(value = "page", defaultValue = "0") int page,
                                                      @RequestParam(value = "size", defaultValue = "10") int size) {
        return documentTypeService.getAllDocumentType(page, size);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentTypeDTO showDocumentType(@PathVariable Long id) {
        return documentTypeService.getDocumentTypeById(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentTypeDTO updateDocumentType(@PathVariable Long id, @RequestBody @Valid DocumentTypeUpdateDTO updateDocumentType) {
        return documentTypeService.updateDocumentType(id, updateDocumentType);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocumentType(@PathVariable Long id) {
        documentTypeService.deleteDocumentType(id);
    }
}
