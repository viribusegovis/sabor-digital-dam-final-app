package pt.ipt.dam.sabordigital.data.remote.models

data class Ingredient(
    val ingredient_id: Int? = null, // null if new ingredient
    var name: String,
    val imageUrl: String? = null,
    val recipeCount: Int = 0
)