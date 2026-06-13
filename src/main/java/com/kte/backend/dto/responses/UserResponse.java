package com.kte.backend.dto.responses;

import com.kte.backend.enums.UserRole;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;
}
