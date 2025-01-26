package pt.ipt.dam.sabordigital.data.remote.models

data class Ingredient(
    val ingredient_id: Int,
    val name: String,
    val imageUrl: String?,
    val recipeCount: Int = 0
)