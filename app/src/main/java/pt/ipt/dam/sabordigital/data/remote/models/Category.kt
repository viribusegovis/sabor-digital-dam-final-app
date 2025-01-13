package pt.ipt.dam.sabordigital.data.remote.models

data class Category(
    val id: Int,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val recipeCount: Int = 0
)