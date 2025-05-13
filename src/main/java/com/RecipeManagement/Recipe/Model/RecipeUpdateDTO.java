package com.RecipeManagement.Recipe.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for recipe updates.
 * All fields are optional to allow partial updates.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecipeUpdateDTO {
    private String title;
    private List<String> ingredients;
    private String instructions;
    private Integer cookingTime;
    private String category;

    /**
     * Updates the given recipe with the non-null fields from this DTO.
     *
     * @param recipe The recipe to update
     * @return The updated recipe
     */
    public Recipe updateRecipe(Recipe recipe) {
        if (title != null) {
            recipe.setTitle(title);
        }

        if (ingredients != null) {
            recipe.setIngredients(ingredients);
        }

        if (instructions != null) {
            recipe.setInstructions(instructions);
        }

        if (cookingTime != null) {
            recipe.setCookingTime(cookingTime);
        }

        if (category != null) {
            recipe.setCategory(category);
        }

        return recipe;
    }

    public Integer getCookingTime() {
        return cookingTime;
    }
}