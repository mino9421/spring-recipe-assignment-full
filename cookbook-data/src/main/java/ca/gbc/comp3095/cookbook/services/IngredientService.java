package ca.gbc.comp3095.cookbook.services;

import ca.gbc.comp3095.cookbook.model.Ingredient;

import java.util.Set;

public interface IngredientService extends CrudService<Ingredient, Long> {

    // Methods to be expanded Upon (Find Ingredients By Recipe)
    Set<Ingredient> findAllByRecipeId(Long recipeId);
}
