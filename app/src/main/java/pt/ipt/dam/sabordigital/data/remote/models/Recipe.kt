package pt.ipt.dam.sabordigital.data.remote.models

data class Recipe(
    val id: Int,
    val title: String,
    val description: String?,
    val preparation_time: Int,
    val servings: Int,
    val difficulty: String,
    val imageUrl: String?,
    val categories: List<Category>,
    val ingredients: List<Ingredient>

)
