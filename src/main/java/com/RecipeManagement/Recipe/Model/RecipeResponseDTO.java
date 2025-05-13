package com.RecipeManagement.Recipe.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object for recipe responses.
 * Does not include MongoDB ID to avoid exposing internal identifiers.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecipeResponseDTO {
    private String title;
    private List<String> ingredients;
    private String instructions;
    private int cookingTime;
    private String category;

    /**
     * Creates a RecipeResponseDTO from a Recipe entity.
     *
     * @param recipe The recipe entity
     * @return RecipeResponseDTO without the MongoDB ID
     */
    public static RecipeResponseDTO fromEntity(Recipe recipe) {
        RecipeResponseDTO dto = new RecipeResponseDTO();
        dto.setTitle(recipe.getTitle());
        dto.setIngredients(recipe.getIngredients());
        dto.setInstructions(recipe.getInstructions());
        dto.setCookingTime(recipe.getCookingTime());
        dto.setCategory(recipe.getCategory());
        return dto;
    }
}