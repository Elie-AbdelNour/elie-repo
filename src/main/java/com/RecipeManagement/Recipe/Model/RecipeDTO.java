package com.RecipeManagement.Recipe.Model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object for recipe creation operations.
 * Does not include ID since it will be generated by the server.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecipeDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotEmpty(message = "At least one ingredient is required")
    private List<String> ingredients;

    @NotBlank(message = "Instructions are required")
    private String instructions;

    @NotNull(message = "Cooking time is required")
    @Min(value = 1, message = "Cooking time should be greater than 0")
    private int cookingTime;

    @NotBlank(message = "Category is required")
    private String category;

    /**
     * Converts this DTO to a Recipe entity.
     * The ID will be null and should be generated by MongoDB.
     *
     * @return Recipe entity without ID
     */
    public Recipe toEntity() {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        recipe.setCookingTime(cookingTime);
        recipe.setCategory(category);
        return recipe;
    }
}