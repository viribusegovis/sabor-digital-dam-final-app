package pt.ipt.dam.sabordigital.data.remote.models

// Data class for creating a new recipe, containing:
// - Author information
// - Basic recipe details
// - Preparation specifications
// - Lists of ingredients, instructions, and categories
// Used specifically for POST requests when creating new recipes

data class RecipeCreate(
    val author_id: Int,            // ID of the user creating the recipe
    val title: String,             // Recipe title/name
    val description: String?,      // Optional recipe description
    val preparation_time: Int,     // Time needed to prepare in minutes
    val servings: Int,            // Number of servings the recipe yields
    val difficulty: String,        // Difficulty level of the recipe
    val image_url: String?,        // Optional URL for recipe image
    val ingredients: List<RecipeIngredient>,  // List of ingredients with quantities and units
    val instructions: List<String>,           // List of step-by-step instructions
    val categories: List<Category>            // List of categories for the recipe
)
