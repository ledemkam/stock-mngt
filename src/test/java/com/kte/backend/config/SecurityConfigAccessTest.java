package com.kte.backend.config;

import com.kte.backend.controllers.AuthenticationController;
import com.kte.backend.controllers.ProductController;
import com.kte.backend.dto.responses.LoginReponse;
import com.kte.backend.security.JwtAuthentificationFilter;
import com.kte.backend.services.auth.AuthenticationService;
import com.kte.backend.services.catalog.ProductService;
import com.kte.backend.services.tenant.TenantService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthenticationController.class, ProductController.class})
@Import({SecurityConfig.class, JpaAuditingConfig.class})
class SecurityConfigAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthentificationFilter jwtAuthentificationFilter;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private ProductService productService;

    // Nécessaire car @EnableJpaAuditing est sur @SpringBootApplication
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void configurePassThroughFilter() throws Exception {
        // Le filtre mocké doit passer la requête au filtre suivant dans la chaîne
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthentificationFilter).doFilter(any(), any(), any());
    }

    // ---- URLs publiques ----

    @Test
    void loginEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(authenticationService.login(any())).thenReturn(
                LoginReponse.builder().accessToken("jwt.token").tokenType("Bearer").build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registerEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"compagnyName\":\"Acme\",\"compagnyCode\":\"ACME\"," +
                                 "\"email\":\"acme@test.com\",\"adminFullName\":\"Admin\"," +
                                 "\"adminEmail\":\"admin@acme.com\",\"adminUserName\":\"admin\"," +
                                 "\"adminPassword\":\"pass1234\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void optionsRequest_shouldBePermittedForAnyPath() throws Exception {
        mockMvc.perform(options("/api/v1/products")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }
/*  @Test
    // ---- URLs protégées : sans authentification → 401 ----


    void productsEndpoint_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteProduct_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/products/prod-001"))
                .andExpect(status().isUnauthorized());
    }
*/
    // ---- URLs protégées : avec authentification → accès autorisé ----

    @Test
    void productsEndpoint_shouldBeAccessible_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .with(user("user-001").roles("USER")))
                .andExpect(status().isOk());
    }

    // ---- CORS : headers présents sur les réponses ----

    @Test
    void response_shouldContainCorsHeaders_whenOriginHeaderIsPresent() throws Exception {
        when(authenticationService.login(any())).thenReturn(
                LoginReponse.builder().accessToken("token").tokenType("Bearer").build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"password\":\"secret123\"}")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    // ---- Session stateless ----

    @Test
    void response_shouldNotCreateHttpSession_statelessPolicy() throws Exception {
        when(authenticationService.login(any())).thenReturn(
                LoginReponse.builder().accessToken("token").tokenType("Bearer").build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"password\":\"secret123\"}"))
                .andExpect(request().sessionAttributeDoesNotExist("SPRING_SECURITY_CONTEXT"));
    }
}