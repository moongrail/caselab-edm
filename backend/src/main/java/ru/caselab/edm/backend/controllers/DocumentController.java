package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.*;
import ru.caselab.edm.backend.mapper.DocumentMapper;
import ru.caselab.edm.backend.service.DocumentService;
import ru.caselab.edm.backend.service.SignatureService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "Document", description = "Document management operations")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final SignatureService signatureService;

    @PostMapping("/{id}/sign")
    @ResponseStatus(HttpStatus.OK)
    public void signDocument(@Valid @RequestBody SignatureCreateDTO signatureCreateDTO, @PathVariable Long id) {
        signatureService.sign(signatureCreateDTO, id);
    }

    @Operation(
            summary = "Send document for signature",
            description = "Send document for signature to users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document sent for signature", content = @Content),
            @ApiResponse(responseCode = "404", description = "Document with provided version ID not found",
                    content = @Content)
    })
    @PostMapping("/{id}/send_for_signature")
    @ResponseStatus(HttpStatus.OK)
    public void sendForSignature(
            @Parameter(description = "Document version ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "List of user IDs", required = true, example = "550e8400-e29b-41d4-a716-446655440000,550e8400-e29b-41d4-a716-446655440001")
            @NotEmpty @RequestParam List<UUID> userIds) {
        documentService.sendForSign(userIds, id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDTO createDocument(@Valid @RequestBody DocumentCreateDTO documentCreateDTO) {
        return documentMapper.toDto(documentService.saveDocument(documentCreateDTO));
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public DocumentPageDTO getAllDocuments(@RequestParam(name = "page", defaultValue = "0") @Min(value = 0) int page,
                                           @RequestParam(name = "size", defaultValue = "10") @Min(value = 1) @Max(value = 100) int size) {
        return documentMapper.toDtoPage(documentService.getAllDocuments(page, size));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDTO getDocumentById(@PathVariable Long id) {
        return documentMapper.toDto(documentService.getDocument(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDTO updateDocument(@PathVariable Long id,
                                          @RequestBody @Valid DocumentUpdateDTO updateDocument) {
        return documentMapper.toDto(documentService.updateDocument(id, updateDocument));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
    }
}
