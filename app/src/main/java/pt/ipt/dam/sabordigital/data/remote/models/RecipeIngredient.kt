package pt.ipt.dam.sabordigital.data.remote.models


// Data class representing an ingredient within a recipe context, containing:
// - Recipe reference (optional for new recipes)
// - Ingredient reference (optional for new ingredients)
// - Quantity information (amount and unit)
// - Associated ingredient details
// Used for both creating new recipe ingredients and displaying existing ones

data class RecipeIngredient(
    var recipe_id: Int? = null,      // Optional reference to recipe, null when creating new recipe
    var ingredient_id: Int? = null,   // Optional reference to ingredient, null for new ingredients
    var amount: Float?,               // Quantity of ingredient needed, nullable
    var unit: String?,               // Unit of measurement (e.g., grams, cups), nullable
    var ingredient: Ingredient        // Associated ingredient object with full ingredient details
)
