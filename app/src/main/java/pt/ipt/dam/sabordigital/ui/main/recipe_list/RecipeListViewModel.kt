package pt.ipt.dam.sabordigital.ui.main.recipe_list.ui.recipelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.ipt.dam.sabordigital.data.remote.models.Recipe

class RecipeListViewModel : ViewModel() {
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun searchRecipes(query: String) {
        _loading.value = true
        // Implement search logic
    }

    fun filterByCategory(category: String) {
        _loading.value = true
        // Implement category filter
    }

    fun filterByIngredient(ingredient: String) {
        _loading.value = true
        // Implement ingredient filter
    }

    fun clearFilters() {
        _loading.value = true
        // Reset to all recipes
    }
}
