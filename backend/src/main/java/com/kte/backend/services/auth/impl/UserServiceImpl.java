package com.kte.backend.services.auth.impl;

import com.kte.backend.entities.Tenant;
import com.kte.backend.entities.User;
import com.kte.backend.enums.UserRole;
import com.kte.backend.exceptions.DuplicateEntityException;
import com.kte.backend.repositories.UserRepository;
import com.kte.backend.services.auth.UserService;
import com.kte.backend.utils.NameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public void createAdminUser(Tenant tenant) {
        if (userRepository.existsByUsername(tenant.getAdminUserName())) {
            throw new DuplicateEntityException("Admin username already exists");
        }
        final User adminUser = User.builder()
                .username(tenant.getAdminUserName())
                .email(tenant.getAdminEmail())
                .password(tenant.getAdminPassword())
                .firstName(NameUtils.extractFirstName(tenant.getAdminFullName()))
                .lastName(NameUtils.extractLastName(tenant.getAdminFullName()))
                .role(UserRole.ROLE_COMPAGNY_ADMIN)
                .tenant(tenant)
                .enable(true)
                .build();
        userRepository.save(adminUser);
        log.info("Created initial admin user for tenant {}: {}", tenant.getId(), adminUser);
    }
}
