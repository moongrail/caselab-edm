package ru.caselab.edm.backend.controllers;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.caselab.edm.backend.dto.CreateUserDTO;
import ru.caselab.edm.backend.dto.UpdatePasswordDTO;
import ru.caselab.edm.backend.dto.UpdateUserDTO;
import ru.caselab.edm.backend.dto.UserDTO;
import ru.caselab.edm.backend.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@RequestParam("id") UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid CreateUserDTO createdUser) {
        return ResponseEntity.ok(userService.createUser(createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@RequestParam("id") UUID id,
                                              @RequestBody @Valid UpdateUserDTO updatedUser) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@RequestParam("id") UUID id,
                                                  @RequestBody @Valid UpdatePasswordDTO updatePasswordDTO) {
        userService.updatePassword(id, updatePasswordDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@RequestParam("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
