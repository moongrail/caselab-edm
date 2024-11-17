package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.dto.documenttype.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.mapper.user.UserMapper;
import ru.caselab.edm.backend.service.ReplacementManagementService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/replacement_managers")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('USER')")
@Tag(name = "Replacement", description = "Replacement managers operations")
public class ReplacementManagementController {
    private final ReplacementManagementService replacementManagementService;
    private final UserMapper userMapper;

    @Operation(
            summary = "Output of all users available for replacement",
            description = "Get a list of all users with pagination. " +
                          "Specify the page number and the number of users per page."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Getting the list of users is successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DocumentTypeDTO.class)))
    })
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public UserPageDTO getAllUsersForReplacement(@RequestParam(name = "page", defaultValue = "0")
                                                 @Min(value = 0) int page,
                                                 @RequestParam(name = "size", defaultValue = "10")
                                                 @Min(value = 1) @Max(value = 100) int size,
                                                 @AuthenticationPrincipal UserInfoDetails user) {
        Page<User> userForReplacement = replacementManagementService.getAllUsersForReplacement(page, size, user.getId());
        return userMapper.toPageDTO(userForReplacement);
    }
}