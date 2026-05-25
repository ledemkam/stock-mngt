package com.kte.backend.dto.requests;

import com.kte.backend.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Username should not be empty")
    private String username;
    @NotBlank(message = "Email should not be empty")
    private String email;
    @NotBlank(message = "Password should not be empty")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    private String password;
    @NotBlank(message = "First name should not be empty")
    private String firstName;
    @NotBlank(message = "Last name should not be empty")
    private String lastName;
    @NotNull(message = "Role should not be empty")
    private UserRole role;
}
