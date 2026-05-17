package com.kte.backend.exceptions;

public class DuplicateCategoryNotFoundException extends RuntimeException {
  public DuplicateCategoryNotFoundException(String message) {
    super(message);
  }
}
