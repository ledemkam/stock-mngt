package com.kte.backend.entities;

import com.kte.backend.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;


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

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;



    @LastModifiedBy
    @Column(name = "update_by", insertable = false)
    private String updateBy;

    @Column(name = "deleted", updatable = false, nullable = false)
    private Boolean deleted;

    @PrePersist
    protected void oncreate() {
        if (this.deleted == null) {
            this.deleted = Boolean.FALSE;
        }


    }



}
