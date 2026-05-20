package com.kte.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;



public enum UserRole {

   PLATFORM_ADMIN,
   COMPAGNY_ADMIN,
   USER,
   SALES_OPERATOR
}