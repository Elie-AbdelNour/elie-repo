package com.RecipeManagement.Recipe.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank
        @Size(min = 3, max = 20)
        private String username;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 6)
        private String password;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private String id;
        private String username;
        private String email;
        private String role;
        public JwtResponse(String token, String id, String username, String email, String role) {
            this.token = token;
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
            this.type = "Bearer"; // set default manually
        }

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageResponse {
        private String message;
    }
    @Getter
    @Setter
    public static class RoleUpdateRequest {
        private String role; // e.g., "ROLE_ADMIN"
    }

}