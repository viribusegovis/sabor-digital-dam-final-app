package pt.ipt.dam.sabordigital.data.remote.models


data class RecipeIngredient(
    var recipe_id: Int? = null, // null for creation
    var ingredient_id: Int? = null, // null if new ingredient
    var amount: Float,
    var unit: String,
    var ingredient: Ingredient
)