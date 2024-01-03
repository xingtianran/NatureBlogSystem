package net.nature;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = "12346";
        // String encode = passwordEncoder.encode(password);
        // $2a$10$4F7sXFywh4WeSUVNdl3PiulJFOS9BZMiRAiMrYWPA59gT1j5LY.uS
        // $2a$10$YRW8sUh.f9oMNuZb9p1JnuOzigbp.0Gn05bMIACIfW5TT6dOgFXQy
        // System.out.println(encode);
        boolean result = passwordEncoder.matches(password, "$2a$10$4F7sXFywh4WeSUVNdl3PiulJFOS9BZMiRAiMrYWPA59gT1j5LY.uS");
        System.out.println("result == " + result);
    }
}
