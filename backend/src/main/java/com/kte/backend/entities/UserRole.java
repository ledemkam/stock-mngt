package com.kte.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;



public enum UserRole {

   ROLE_PLATFORM_ADMIN,
   ROLE_COMPAGNY_ADMIN,
   ROLE_USER,
   ROLE_SALES_OPERATOR
}