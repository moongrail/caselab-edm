package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.department.*;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.service.DelegationService;
import ru.caselab.edm.backend.service.DepartmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/department")
@Tag(name = "Department", description = "Operations for department")
@SecurityRequirement(name = "bearer-jwt")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DelegationService delegationService;

    @Autowired
    public DepartmentController(DepartmentService departmentService, DelegationService delegationService) {
        this.departmentService = departmentService;
        this.delegationService = delegationService;
    }

    @Operation(
            summary = "Creating department"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description="Department created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Manager with given id doesn't exists",
                    content = @Content()),
            @ApiResponse(responseCode = "409", description = "Given manager is already leading another department",
                    content = @Content)
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DepartmentDTO> createDepartment(
            @RequestBody
            @Valid CreateDepartmentDTO createDepartmentDTO) {
        return ResponseEntity.ok(departmentService.createDepartment(createDepartmentDTO));
    }

    @Operation(
            summary = "Returning department that this user belongs to"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description="Departments with current user was successfully returned",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentPageDTO.class))),
            @ApiResponse(responseCode = "409", description = "User is not a member of any department",
                    content = @Content)
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<DepartmentDTO>> getDepartmentWithUser(@AuthenticationPrincipal UserInfoDetails user) {
        return ResponseEntity.ok(departmentService.getDepartmentsWithUser(user.getId()));
    }

    @Operation(
            summary = "Returning manager of current department"
    )
    @ApiResponse(responseCode = "200", description = "Managers of current department was successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPageDTO.class)))
    @GetMapping("/{id}/managers")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDTO> getManagerOfDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getManagerOfDepartment(id));
    }

    @Operation(
            summary = "Returning all members of currentDepartment"
    )
    @ApiResponse(responseCode = "200", description = "Members of current department was successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPageDTO.class)))
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserPageDTO> getAllMembersOfDepartment(@RequestParam(name = "page", defaultValue = "0")
                                                                     @Min(value = 0) int page,
                                                                 @RequestParam(name = "size", defaultValue = "10")
                                                                     @Min(value = 1) @Max(value = 100) int size,
                                                                 @PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getAllMembersOfDepartment(page, size, id));
    }

    @Operation(
            summary = "Returns all departments in which this user is the manager"
    )
    @ApiResponse(responseCode = "200", description = "Departments in which this user is the manager was successfully returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartmentPageDTO.class)))
    @GetMapping("/managers")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DepartmentPageDTO> getAllSubordinateDepartments(@RequestParam(name = "page", defaultValue = "0")
                                                                              @Min(value = 0) int page,
                                                                          @RequestParam(name = "size", defaultValue = "10")
                                                                              @Min(value = 1) @Max(value = 100) int size,
                                                                          @AuthenticationPrincipal UserInfoDetails user) {
        return ResponseEntity.ok(departmentService.getAllSubordinateDepartments(page, size, user.getId()));
    }

    @Operation(
            summary = "Adding list of potential members to department members"
    )
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Potential members was successfully added",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticMembersDTO.class))),
            @ApiResponse(responseCode = "404", description = "Department was not found",
                    content = @Content)
        }
    )
    @PostMapping("/add/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StatisticMembersDTO> addMembersToDepartment(@RequestBody
                                                                      @Valid MembersDTO addMembersDTO,
                                                                      @Parameter(description = "Department id", required = true, example = "1")
                                                                      @PathVariable Long id) {
        return ResponseEntity.ok(departmentService.addMembersToDepartment(addMembersDTO, id));
    }

    @Operation(
            summary = "Kicking members from department"
    )
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Members was successfully kicked",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticMembersDTO.class))),
            @ApiResponse(responseCode = "404", description = "Department was not found",
                    content = @Content)
        }
    )
    @PostMapping("/kick/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StatisticMembersDTO> kickMembersFromDepartment(@RequestBody
                                                                         @Valid MembersDTO kickMembersDTO,
                                                                         @Parameter(description = "Department id", required = true, example = "1")
                                                                         @PathVariable Long id) {
        return ResponseEntity.ok(departmentService.kickMembersFromDepartment(kickMembersDTO, id));
    }

    @Operation(
            summary = "Leaving from department"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was successfully leaved",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User was not found",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "User is not a member of any department")
    })
    @PostMapping("/leave/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void leaveFromDepartment(@PathVariable Long id,
                                    @AuthenticationPrincipal UserInfoDetails user) {
        departmentService.leaveFromDepartment(user.getId(), id);
    }

    @Operation(
            summary = "Deleting department"
    )
    @ApiResponse(responseCode = "204", description = "Department was successfully deleted",
            content = @Content)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
    }


    @Operation(
            summary = "Get page of available users for delegation sign document's"
    )
    @ApiResponse(responseCode = "200", description = "List of users",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPageDTO.class)))

    @GetMapping("/document/{documentId}")
    public ResponseEntity<UserPageDTO> getUsersForDelegation(
            @RequestParam(name = "page", defaultValue = "0")
            @Min(value = 0) int page,
            @RequestParam(name = "size", defaultValue = "10")
            @Min(value = 1) @Max(value = 100) int size,
            @AuthenticationPrincipal UserInfoDetails user,
            @Parameter(description = "Document id", required = true, example = "1")
            @PathVariable Long documentId){

        return new ResponseEntity<>(delegationService.getAvailableUsersForDelegation(user.getId(),documentId,size,page), HttpStatus.OK);
    }

    @Operation(
            summary = "Delegate sign document's for another user in yours departments"
    )
    @ApiResponse(responseCode = "200", description = "Delegation was successfully done")

   @GetMapping("/document/{documentId}/delegate")
    public ResponseEntity delegateSignForUser(
           @AuthenticationPrincipal UserInfoDetails user,
           @Parameter(description = "Document id", required = true, example = "1")
           @PathVariable Long documentId,
           @Parameter(description = "User id to delegate", required = true, example = "9858b2f2-1aaf-4dbc-98b2-f21aafedbc2e")
           @RequestParam(name = "userId") UUID userToDelegate
   )
   {
       delegationService.delegateSign(userToDelegate,user.getId(),documentId);
       return new ResponseEntity<>(HttpStatus.OK);
   }
}
