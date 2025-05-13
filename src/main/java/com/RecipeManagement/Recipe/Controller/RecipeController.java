package com.RecipeManagement.Recipe.Controller;

import com.RecipeManagement.Recipe.Model.Recipe;
import com.RecipeManagement.Recipe.Model.RecipeDTO;
import com.RecipeManagement.Recipe.Model.RecipeResponseDTO;
import com.RecipeManagement.Recipe.Model.RecipeUpdateDTO;
import com.RecipeManagement.Recipe.Repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private MongoTemplate mongoTemplate;


    // Modified findRecipesWithFilters method
    private Page<Recipe> findRecipesWithFilters(
            String title, String category, Integer minCookingTime, Integer maxCookingTime,
            List<String> ingredients, boolean matchAllIngredients, Pageable pageable) {

        // Always use criteria query approach for cooking time ranges to avoid issues
        Criteria criteria = new Criteria();

        if (title != null && !title.isEmpty()) {
            criteria.and("title").regex(title, "i");
        }

        if (category != null && !category.isEmpty()) {
            criteria.and("category").regex(category, "i");
        }

        // Safer handling of cooking time range
        if (minCookingTime != null && maxCookingTime != null) {
            criteria.and("cookingTime").gte(minCookingTime).lte(maxCookingTime);
        } else if (minCookingTime != null) {
            criteria.and("cookingTime").gte(minCookingTime);
        } else if (maxCookingTime != null) {
            criteria.and("cookingTime").lte(maxCookingTime);
        }

        if (ingredients != null && !ingredients.isEmpty()) {
            if (matchAllIngredients) {
                criteria.and("ingredients").all(ingredients);
            } else {
                criteria.and("ingredients").in(ingredients);
            }
        }

        Query query = new Query(criteria).with(pageable);
        List<Recipe> recipes = mongoTemplate.find(query, Recipe.class);
        long count = mongoTemplate.count(query, Recipe.class);

        return PageableExecutionUtils.getPage(
                recipes,
                pageable,
                () -> count);
    }

    /**
     * Helper method to check if only a specific parameter is present and valid
     */
    private boolean isOnlySpecified(String value) {
        return value != null && !value.isEmpty();
    }

    /**
     * Helper method to check if only a specific Integer parameter is present
     */
    private boolean isOnlySpecified(Integer value) {
        return value != null;
    }

    /**
     * Helper method to check if only two string parameters are present and valid
     */
    private boolean isOnlySpecified(String value1, String value2) {
        return value1 != null && !value1.isEmpty() &&
                value2 != null && !value2.isEmpty();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/getRecipes")
    public ResponseEntity<?> getRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minCookingTime,
            @RequestParam(required = false) Integer maxCookingTime,
            @RequestParam(required = false) List<String> ingredients,
            @RequestParam(required = false) Boolean matchAllIngredients,
            @RequestParam(required = false) String searchText) {

        try {
            Sort sort = direction.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Query query = new Query().with(pageable);
            List<Criteria> criteriaList = new ArrayList<>();

            // Add full-text search if provided
            if (searchText != null && !searchText.isEmpty()) {
                TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matching(searchText);
                query.addCriteria(textCriteria);
            }

            // Add additional filters
            if (title != null && !title.isEmpty()) {
                criteriaList.add(Criteria.where("title").regex(title, "i"));
            }

            if (category != null && !category.isEmpty()) {
                criteriaList.add(Criteria.where("category").regex(category, "i"));
            }

            if (minCookingTime != null) {
                criteriaList.add(Criteria.where("cookingTime").gte(minCookingTime));
            }

            if (maxCookingTime != null) {
                criteriaList.add(Criteria.where("cookingTime").lte(maxCookingTime));
            }

            if (ingredients != null && !ingredients.isEmpty()) {
                if (Boolean.TRUE.equals(matchAllIngredients)) {
                    for (String ing : ingredients) {
                        Pattern regex = Pattern.compile(ing, Pattern.CASE_INSENSITIVE);
                        criteriaList.add(Criteria.where("ingredients").elemMatch(Criteria.where("$regex").is(regex)));
                    }
                } else {
                    Criteria[] ingredientCriteria = ingredients.stream()
                            .map(ing -> Criteria.where("ingredients").elemMatch(
                                    Criteria.where("$regex").is(Pattern.compile(ing, Pattern.CASE_INSENSITIVE))))
                            .toArray(Criteria[]::new);

                    criteriaList.add(new Criteria().orOperator(ingredientCriteria));
                }
            }


            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }

            Query countQuery = Query.of(query).limit(-1).skip(-1); // or just new Query(query) if you're not using Query.of
            long total = mongoTemplate.count(countQuery, Recipe.class);

            query.with(pageable);
            List<Recipe> recipes = mongoTemplate.find(query, Recipe.class);

            // Convert entities to DTOs to hide IDs
            List<RecipeResponseDTO> recipeResponseDTOs = recipes.stream()
                    .map(RecipeResponseDTO::fromEntity)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("recipes", recipeResponseDTOs);
            response.put("currentPage", page);
            response.put("totalItems", total);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving recipes: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/getRecipes/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable String id) {
        try {
            Optional<Recipe> recipeOptional = recipeRepository.findById(id);
            if (!recipeOptional.isPresent()) {
                return new ResponseEntity<>("Recipe not found with id: " + id, HttpStatus.NOT_FOUND);
            }

            Recipe recipe = recipeOptional.get();
            // Convert entity to DTO to hide ID
            RecipeResponseDTO responseDTO = RecipeResponseDTO.fromEntity(recipe);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving recipe: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addRecipe")
    public ResponseEntity<?> addRecipe(@Valid @RequestBody RecipeDTO recipeDTO, BindingResult bindingResult) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        try {
            // Convert DTO to entity
            Recipe recipe = recipeDTO.toEntity();

            // Apply title capitalization
            recipe.setTitle(capitalizeTitle(recipe.getTitle()));

            // Save entity and return as ResponseDTO (without ID)
            Recipe savedRecipe = recipeRepository.save(recipe);
            RecipeResponseDTO responseDTO = RecipeResponseDTO.fromEntity(savedRecipe);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating recipe: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteRecipe/{id}")
    public ResponseEntity<?> deleteRecipeById(@PathVariable String id) {
        try {
            // Check if recipe exists
            Optional<Recipe> recipeOptional = recipeRepository.findById(id);
            if (!recipeOptional.isPresent()) {
                return new ResponseEntity<>("Recipe not found with id: " + id, HttpStatus.NOT_FOUND);
            }

            // Delete the recipe
            recipeRepository.deleteById(id);

            return new ResponseEntity<>("Recipe deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting recipe: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateRecipe/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable String id, @RequestBody RecipeUpdateDTO updateDTO) {
        try {
            // Check if recipe exists
            Optional<Recipe> existingRecipeOptional = recipeRepository.findById(id);
            if (!existingRecipeOptional.isPresent()) {
                return new ResponseEntity<>("Recipe not found with id: " + id, HttpStatus.NOT_FOUND);
            }

            Recipe existingRecipe = existingRecipeOptional.get();

            // Apply updates from DTO - only provided fields will be updated
            updateDTO.updateRecipe(existingRecipe);

            // No validation needed for fields that weren't updated
            // Only validate cookingTime if it was provided in the update
            if (updateDTO.getCookingTime() != null && existingRecipe.getCookingTime() <= 0) {
                return new ResponseEntity<>("Cooking time should be greater than 0", HttpStatus.BAD_REQUEST);
            }

            // Save the updated recipe and convert to ResponseDTO
            Recipe savedRecipe = recipeRepository.save(existingRecipe);
            RecipeResponseDTO responseDTO = RecipeResponseDTO.fromEntity(savedRecipe);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating recipe: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String capitalizeTitle(String title) {
        if (title == null || title.isBlank()) return title;
        return title.substring(0, 1).toUpperCase() + title.substring(1);
    }
}