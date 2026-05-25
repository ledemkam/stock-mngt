package com.kte.backend.services.auth;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.requests.UserRequest;
import com.kte.backend.dto.responses.UserResponse;
import com.kte.backend.entities.Tenant;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void createAdminUser(final Tenant tenant);

    void createUser(final String userId, final UserRequest request);

     void updateUser(final String userId, final UserRequest request);

     void deleteUser(final String userId);

     UserResponse getUserById(final String userId);

     PageReponse<UserResponse> getAllUsers(final int page, final int size);

     void enableUser(final  String userId);

     void disableUser(final String userId);
}
