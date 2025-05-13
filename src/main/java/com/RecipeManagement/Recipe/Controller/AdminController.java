package com.RecipeManagement.Recipe.Controller;

import com.RecipeManagement.Recipe.Model.AuthDTO;
import com.RecipeManagement.Recipe.Model.AuthDTO.MessageResponse;
import com.RecipeManagement.Recipe.Model.User;
import com.RecipeManagement.Recipe.Repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final List<String> VALID_ROLES = List.of("ROLE_USER", "ROLE_ADMIN");

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String id,
            @RequestBody AuthDTO.RoleUpdateRequest request) {

        if (!VALID_ROLES.contains(request.getRole())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid role: ALLOWED_ROLES are " + VALID_ROLES));
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setRole(request.getRole());
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User role updated to " + request.getRole()));
    }
}
