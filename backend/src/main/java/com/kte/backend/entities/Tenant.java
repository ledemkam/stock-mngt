package com.kte.backend.entities;

import com.kte.backend.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tenants")
public class Tenant extends AbstractEntity{

    @Column(name = "compagny_name", nullable = false)
    private String compagnyName;

    @Column(name = "compagny_code", nullable = false, unique = true)
    private String compagnyCode;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenantStatus status = TenantStatus.PENDING ;


    //initial admin credentials

    @Column(name = "admin_full_name", nullable = false)
    private String adminFullName;

    @Column(name = "admin_email", nullable = false, unique = true)
    private String adminEmail;

    @Column(name = "admin_user_name", nullable = false, unique = true)
    private  String adminUserName;

    @Column(name = "admin_password", nullable = false)
    private String adminPassword;



}
