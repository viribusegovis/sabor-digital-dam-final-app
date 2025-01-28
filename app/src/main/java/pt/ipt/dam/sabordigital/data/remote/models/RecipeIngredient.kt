package pt.ipt.dam.sabordigital.data.remote.models


data class RecipeIngredient(
    val recipe_id: Int,
    val ingredient_id: Int,
    val amount: Float,
    val unit: String,
    val ingredient: Ingredient
)