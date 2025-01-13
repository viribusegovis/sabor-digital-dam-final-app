package pt.ipt.dam.sabordigital.data.remote.models

data class Recipe(
    val id: Int,
    val title: String,
    val description: String?,
    val preparationTime: Int,
    val servings: Int,
    val difficulty: String,
    val imageUrl: String?
)
