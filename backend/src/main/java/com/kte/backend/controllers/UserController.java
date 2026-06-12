package com.kte.backend.controllers;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.requests.UserRequest;
import com.kte.backend.dto.responses.UserResponse;
import com.kte.backend.controllers.uicontrollers.UIUserController;
import com.kte.backend.services.auth.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UIUserController {

    private final UserService userService;

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_COMPAGNY_ADMIN')")
    public ResponseEntity<Void> createUser(
            @Valid
            @RequestBody
            final UserRequest request) {
        this.userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_COMPAGNY_ADMIN', 'ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<PageReponse<UserResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        final PageReponse<UserResponse> response = this.userService.getAllUsers(page, size);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{user-id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPAGNY_ADMIN', 'ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("user-id")
            final String id) {
        final UserResponse response = this.userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{user-id}")
    @PreAuthorize("hasAuthority('ROLE_COMPAGNY_ADMIN')")
    public ResponseEntity<Void> updateUser(
            @PathVariable("user-id")
            final String id,
            @Valid
            @RequestBody
            final UserRequest request) {
        this.userService.updateUser(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }

    @Override
    @DeleteMapping("/{user-id}")
    @PreAuthorize("hasAuthority('ROLE_COMPAGNY_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("user-id")
            final String id) {
        this.userService.deleteUser(id);
        return ResponseEntity.noContent()
                .build();
    }

    @Override
    @PutMapping("/{user-id}/enable")
    @PreAuthorize("hasAuthority('ROLE_COMPAGNY_ADMIN')")
    public ResponseEntity<Void> enableUser(
            @PathVariable("user-id")
            final String id) {
        this.userService.enableUser(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }

    @Override
    @PutMapping("/{user-id}/disable")
    @PreAuthorize("hasAuthority('ROLE_COMPAGNY_ADMIN')")
    public ResponseEntity<Void> disableUser(
            @PathVariable("user-id")
            final String id) {
        this.userService.disableUser(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }
}
