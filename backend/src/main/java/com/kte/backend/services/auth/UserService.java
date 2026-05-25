package com.kte.backend.services.auth;

import com.kte.backend.entities.Tenant;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void createAdminUser(Tenant tenant);
}
