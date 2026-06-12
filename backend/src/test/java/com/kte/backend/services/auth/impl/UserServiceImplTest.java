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
import com.kte.backend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String TENANT_ID = "tenant-001";
    private static final String USER_ID = "user-001";

    private UserRequest request;
    private User user;
    private Tenant tenant;
    private UserResponse response;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TENANT_ID);

        tenant = new Tenant();
        tenant.setId(TENANT_ID);

        request = UserRequest.builder()
                .username("john.doe")
                .email("john@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.ROLE_USER)
                .build();

        user = User.builder()
                .username("john.doe")
                .email("john@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.ROLE_USER)
                .enable(true)
                .tenant(tenant)
                .build();
        user.setId(USER_ID);

        response = UserResponse.builder()
                .id(USER_ID)
                .username("john.doe")
                .email("john@example.com")
                .role(UserRole.ROLE_USER)
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ---- createUser ----

    @Test
    void createUser_shouldSaveUser_whenUsernameAndEmailAreUnique() {
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        userService.createUser(request);

        verify(userRepository).save(user);
        assertThat(user.getTenant().getId()).isEqualTo(TENANT_ID);
    }

    @Test
    void createUser_shouldThrowDuplicateEntityException_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldThrowDuplicateEntityException_whenEmailAlreadyExists() {
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldThrowInvalidRequestException_whenRoleIsPlatformAdmin() {
        request.setRole(UserRole.ROLE_PLATFORM_ADMIN);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(InvalidRequestException.class);

        verify(userRepository, never()).save(any());
    }

    // ---- updateUser ----

    @Test
    void updateUser_shouldUpdateUser_whenAllChecksPass() {
        // username et email identiques à l'existant → les checks de duplicate ne sont pas appelés
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        userService.updateUser(USER_ID, request);

        verify(userRepository).save(user);
        assertThat(user.getUsername()).isEqualTo(request.getUsername());
        assertThat(user.getEmail()).isEqualTo(request.getEmail());
        assertThat(user.getRole()).isEqualTo(request.getRole());
    }

    @Test
    void updateUser_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(USER_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User does not exist");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldThrowInvalidRequestException_whenUserBelongsToAnotherTenant() {
        Tenant otherTenant = new Tenant();
        otherTenant.setId("other-tenant");
        user.setTenant(otherTenant);

        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateUser(USER_ID, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("User does not belong to the tenant");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldThrowDuplicateEntityException_whenNewUsernameAlreadyTaken() {
        request.setUsername("new.username");
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new.username")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(USER_ID, request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldThrowDuplicateEntityException_whenNewEmailAlreadyTaken() {
        request.setEmail("new@example.com");
        // username inchangé → existsByUsername non appelé, pas de stub nécessaire
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(USER_ID, request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldThrowInvalidRequestException_whenRoleIsPlatformAdmin() {
        request.setRole(UserRole.ROLE_PLATFORM_ADMIN);
        // username et email inchangés → existsByUsername/existsByEmail non appelés
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateUser(USER_ID, request))
                .isInstanceOf(InvalidRequestException.class);

        verify(userRepository, never()).save(any());
    }

    // ---- deleteUser ----

    @Test
    void deleteUser_shouldSoftDeleteUser_whenUserExistsAndBelongsToTenant() {
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        userService.deleteUser(USER_ID);

        assertThat(user.getDeleted()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User does not exist");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldThrowInvalidRequestException_whenUserBelongsToAnotherTenant() {
        Tenant otherTenant = new Tenant();
        otherTenant.setId("other-tenant");
        user.setTenant(otherTenant);

        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.deleteUser(USER_ID))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("User does not belong to the tenant");

        verify(userRepository, never()).save(any());
    }

    // ---- getUserById ----

    @Test
    void getUserById_shouldReturnUserResponse_whenUserExistsAndBelongsToTenant() {
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.getUserById(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getUsername()).isEqualTo("john.doe");
    }

    @Test
    void getUserById_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User does not exist");
    }

    @Test
    void getUserById_shouldThrowInvalidRequestException_whenUserBelongsToAnotherTenant() {
        Tenant otherTenant = new Tenant();
        otherTenant.setId("other-tenant");
        user.setTenant(otherTenant);

        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getUserById(USER_ID))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("User does not belong to the tenant");
    }

    // ---- getAllUsers ----

    @Test
    void getAllUsers_shouldReturnPagedResponse_forCurrentTenant() {
        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.findAllByTenantId(eq(TENANT_ID), any(PageRequest.class))).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(response);

        PageReponse<UserResponse> result = userService.getAllUsers(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ---- enableUser ----

    @Test
    void enableUser_shouldSetEnableTrue_whenUserExistsAndBelongsToTenant() {
        user.setEnable(false);
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        userService.enableUser(USER_ID);

        assertThat(user.getEnable()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void enableUser_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.enableUser(USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User does not exist");
    }

    @Test
    void enableUser_shouldThrowInvalidRequestException_whenUserBelongsToAnotherTenant() {
        Tenant otherTenant = new Tenant();
        otherTenant.setId("other-tenant");
        user.setTenant(otherTenant);

        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.enableUser(USER_ID))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("User does not belong to the tenant");
    }

    // ---- disableUser ----

    @Test
    void disableUser_shouldSetEnableFalse_whenUserExistsAndBelongsToTenant() {
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        userService.disableUser(USER_ID);

        assertThat(user.getEnable()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void disableUser_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.disableUser(USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User does not exist");
    }

    @Test
    void disableUser_shouldThrowInvalidRequestException_whenUserBelongsToAnotherTenant() {
        Tenant otherTenant = new Tenant();
        otherTenant.setId("other-tenant");
        user.setTenant(otherTenant);

        when(userRepository.findByIdAndNotDeleted(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.disableUser(USER_ID))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("User does not belong to the tenant");
    }

    // ---- loadUserByUsername ----

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUsernameExists() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("john.doe");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john.doe");
    }

    @Test
    void loadUserByUsername_shouldThrowEntityNotFoundException_whenUsernameNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with username: unknown");
    }

    // ---- createAdminUser ----

    @Test
    void createAdminUser_shouldSaveAdminUser_whenUsernameIsUnique() {
        tenant.setAdminUserName("admin.user");
        tenant.setAdminEmail("admin@example.com");
        tenant.setAdminPassword("adminPass123");
        tenant.setAdminFullName("Admin User");

        when(userRepository.existsByUsername("admin.user")).thenReturn(false);
        when(passwordEncoder.encode("adminPass123")).thenReturn("encodedAdminPass");

        userService.createAdminUser(tenant);

        verify(userRepository).save(argThat(u ->
                "admin.user".equals(u.getUsername()) &&
                "admin@example.com".equals(u.getEmail()) &&
                UserRole.ROLE_COMPAGNY_ADMIN.equals(u.getRole()) &&
                Boolean.TRUE.equals(u.getEnable())
        ));
    }

    @Test
    void createAdminUser_shouldThrowDuplicateEntityException_whenAdminUsernameAlreadyExists() {
        tenant.setAdminUserName("admin.user");

        when(userRepository.existsByUsername("admin.user")).thenReturn(true);

        assertThatThrownBy(() -> userService.createAdminUser(tenant))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Admin username already exists");

        verify(userRepository, never()).save(any());
    }
}