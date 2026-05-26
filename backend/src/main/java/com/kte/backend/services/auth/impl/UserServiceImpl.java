package com.kte.backend.services.auth.impl;

import com.kte.backend.common.PageReponse;
import com.kte.backend.config.tenantConfig.TenantContext;
import com.kte.backend.dto.requests.UserRequest;
import com.kte.backend.dto.responses.UserResponse;
import com.kte.backend.entities.Tenant;
import com.kte.backend.entities.User;
import com.kte.backend.enums.UserRole;
import com.kte.backend.exceptions.DuplicateEntityException;
import com.kte.backend.exceptions.InvalidRequestException;
import com.kte.backend.mappers.UserMapper;
import com.kte.backend.repositories.TenantRepository;
import com.kte.backend.repositories.UserRepository;
import com.kte.backend.services.auth.UserService;
import com.kte.backend.utils.NameUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

 //UserService(CRUD Management) method implementations
 @Override
 public void createUser(final UserRequest request) {
     final String tenantId = TenantContext.getCurrentTenant();
     log.info("Creating user for tenant: {}", tenantId);

     // validate if username exists
     if (userRepository.existsByUsername(request.getUsername())) {
         throw new DuplicateEntityException("Username already exists");
     }

     // check if email exists
     if (userRepository.existsByEmail(request.getEmail())) {
         throw new DuplicateEntityException("Email already exists");
     }

     // validate role (cannot be PLATFORM_ADMIN)
     if (request.getRole() == UserRole.ROLE_PLATFORM_ADMIN) {
         throw new InvalidRequestException("Role is required");
     }

     final User user = this.userMapper.toEntity(request);
     user.setPassword(passwordEncoder.encode(request.getPassword()));
     user.setTenant(Tenant.builder().id(tenantId).build());

     userRepository.save(user);

     log.info("User created successfully");
 }

    @Override
    public void updateUser(final String userId, final UserRequest request) {
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating user for tenant: {}", tenantId);

        final User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        // check if user belongs to the tenant
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new InvalidRequestException("User does not belong to the tenant");
        }

        // check if username is being changed and if it is already taken
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEntityException("Username already exists");
        }

        // check if email is being changed and if it is already taken
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntityException("Email already exists");
        }

        // validate role (cannot be PLATFORM_ADMIN)
        if (request.getRole() == UserRole.ROLE_PLATFORM_ADMIN) {
            throw new InvalidRequestException("Role is required");
        }

        // update user details
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        userRepository.save(user);
        log.info("User updated successfully");
    }

    @Override
    public void deleteUser(final String userId) {
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting user for tenant: {}", tenantId);

        final User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        // check if user belongs to the tenant
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new InvalidRequestException("User does not belong to the tenant");
        }

        // soft delete user
        user.setDeleted(true);
        userRepository.save(user);
        log.info("User deleted successfully");
    }

    @Override
    public UserResponse getUserById(final String userId) {
        final String tenantId = TenantContext.getCurrentTenant();
        final User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        // check if user belongs to the tenant
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new InvalidRequestException("User does not belong to the tenant");
        }
        return this.userMapper.toResponse(user);
    }

    @Override
    public PageReponse<UserResponse> getAllUsers(final int page, final int size) {
        final String tenantId = TenantContext.getCurrentTenant();
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<User> userPage = userRepository.findAllByTenantId(tenantId, pageRequest);
        final Page<UserResponse> userResponses = userPage.map(this.userMapper::toResponse);
        return PageReponse.of(userResponses);
    }

    @Override
    public void enableUser(final String userId) {
        final String tenantId = TenantContext.getCurrentTenant();
        final User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        // check if user belongs to the tenant
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new InvalidRequestException("User does not belong to the tenant");
        }

        user.setEnable(true);
        userRepository.save(user);
        log.info("User enabled successfully");
    }

    @Override
    public void disableUser(final String userId) {
        final String tenantId = TenantContext.getCurrentTenant();
        final User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        // check if user belongs to the tenant
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new InvalidRequestException("User does not belong to the tenant");
        }

        user.setEnable(false);
        userRepository.save(user);
        log.info("User disabled successfully");
    }


    // UserDetailsService method implementation

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    @Override
    public void createAdminUser(Tenant tenant) {
        if (userRepository.existsByUsername(tenant.getAdminUserName())) {
            throw new DuplicateEntityException("Admin username already exists");
        }
        final User adminUser = User.builder()
                .username(tenant.getAdminUserName())
                .email(tenant.getAdminEmail())
                .password(passwordEncoder.encode(tenant.getAdminPassword()))
                .firstName(NameUtils.extractFirstName(tenant.getAdminFullName()))
                .lastName(NameUtils.extractLastName(tenant.getAdminFullName()))
                .role(UserRole.ROLE_COMPAGNY_ADMIN)
                .tenant(tenant)
                .enable(true)
                .build();
        userRepository.save(adminUser);
    }
}
