package com.kte.backend.utils;

public class NameUtils {

    private NameUtils() {}

    public static String extractFirstName(String fullName) {
        return fullName.split(" ")[0];
    }

    public static String extractLastName(String fullName) {
        String[] parts = fullName.split(" ");
        return parts.length > 1 ? parts[1] : fullName;
    }
}