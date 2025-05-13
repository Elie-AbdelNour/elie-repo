package com.RecipeManagement.Recipe.Model;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "Recipe")
public class Recipe {

    @Id
    private String id;
    @NotNull
    private String title;
    @NotNull
    private List<String> ingredients ;
    @NotNull
    private String instructions;
    @NotNull
    private int cookingTime;
    @NotNull
    private String category;

    public Recipe(String id, String title, String instructions, int cookingTime, String category) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
        this.cookingTime = cookingTime;
        this.category = category;
    }
    public void addIngredient(String ingredient) {
        if(!ingredients.contains(ingredient))
        {
            this.ingredients.add(ingredient);
        }
        else
        {
            System.out.println("Ingredient already exists in the recipe.");
        }
    }

}
