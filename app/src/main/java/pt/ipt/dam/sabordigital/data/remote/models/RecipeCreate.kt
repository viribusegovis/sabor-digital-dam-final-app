package pt.ipt.dam.sabordigital.data.remote.models

data class RecipeCreate(
    val title: String,
    val description: String,
    val preparation_time: Int,
    val servings: Int,
    val difficulty: String,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<String>,
    val categories: List<Category>
)