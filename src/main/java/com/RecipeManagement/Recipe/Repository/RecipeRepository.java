package com.RecipeManagement.Recipe.Repository;

import com.RecipeManagement.Recipe.Model.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RecipeRepository extends MongoRepository<Recipe, String> {

    // Title search (case-insensitive contains)
    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Category filter
    Page<Recipe> findByCategoryIgnoreCase(String category, Pageable pageable);

    // Cooking time filters
    Page<Recipe> findByCookingTimeLessThanEqual(int maxCookingTime, Pageable pageable);
    Page<Recipe> findByCookingTimeGreaterThanEqual(int minCookingTime, Pageable pageable);
    Page<Recipe> findByCookingTimeBetween(int minCookingTime, int maxCookingTime, Pageable pageable);

    // Ingredient search (finds recipes containing ANY of the specified ingredients)
    @Query("{ 'ingredients': { $in: ?0 } }")
    Page<Recipe> findByIngredientsIn(List<String> ingredients, Pageable pageable);

    // Ingredient search (finds recipes containing ALL the specified ingredients)
    @Query("{ 'ingredients': { $all: ?0 } }")
    Page<Recipe> findByIngredientsAll(List<String> ingredients, Pageable pageable);

    // Combined title and category search
    Page<Recipe> findByTitleContainingIgnoreCaseAndCategoryIgnoreCase(String title, String category, Pageable pageable);

    // Full text search across title and instructions
    @Query("{ $text: { $search: ?0 } }")
    Page<Recipe> fullTextSearch(String searchText, Pageable pageable);

}
