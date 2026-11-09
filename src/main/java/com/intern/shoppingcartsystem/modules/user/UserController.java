package com.intern.shoppingcartsystem.modules.user;

import com.intern.shoppingcartsystem.common.pagination.PageResponse;
import com.intern.shoppingcartsystem.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(required = false, defaultValue = "") String email,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        PageResponse<UserResponse> responses = userService.getAllUsers(email, role, active, page, size);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        UserResponse response = userService.updateUserStatus(id, request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        Long id = principal.getUserId();
        UserResponse response = userService.getMyProfile(id);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProfileRequest request
    ) {
        Long id = principal.getUserId();
        UserResponse response = userService.updateMyProfile(id, request);
        return ResponseEntity.ok(response);
    }

}

