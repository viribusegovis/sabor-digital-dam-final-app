package pt.ipt.dam.sabordigital
data class Recipe(
    val id: Long,
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val preparationTime: Int, // in minutes
    val servings: Int,
    val difficulty: DifficultyLevel,
    val imageUrl: String?,
    val authorId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String
)

enum class DifficultyLevel {
    FACIL,
    MEDIO,
    DIFICIL
}
