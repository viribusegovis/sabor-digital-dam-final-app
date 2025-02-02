package pt.ipt.dam.sabordigital.data.remote.models

// Sealed class representing different types of ingredient items in a list
// Used for handling both regular ingredients and recipe-specific ingredients
// Provides type safety when dealing with different ingredient representations

sealed class IngredientListItem {
    // Represents a basic ingredient without quantity/unit information
    data class IngredientOnly(
        val ingredient: Ingredient
    ) : IngredientListItem()

    // Represents an ingredient within a recipe context
    // Includes additional information like quantity and units
    data class RecipeIngredient(
        val recipeIngredient: pt.ipt.dam.sabordigital.data.remote.models.RecipeIngredient
    ) : IngredientListItem()
}
