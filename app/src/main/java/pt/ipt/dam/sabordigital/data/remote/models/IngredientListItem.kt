package pt.ipt.dam.sabordigital.data.remote.models

sealed class IngredientListItem {
    data class IngredientOnly(val ingredient: Ingredient) : IngredientListItem()
    data class RecipeIngredient(
        val recipeIngredient: pt.ipt.dam.sabordigital.data.remote.models.RecipeIngredient
    ) : IngredientListItem()
}
