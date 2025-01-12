package pt.ipt.dam.sabordigital

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadRecipes()
    }

    fun searchRecipes(query: String) = viewModelScope.launch {
        _isLoading.value = true
        try {
            // API call to search recipes
            // recipeRepository.searchRecipes(query)
            _isLoading.value = false
        } catch (e: Exception) {
            _error.value = "Erro ao pesquisar receitas: ${e.message}"
            _isLoading.value = false
        }
    }

    fun filterRecipes(filter: String) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val filteredRecipes = when (filter) {
                "recent" -> emptyList<Recipe>()
                "favorites" -> emptyList<Recipe>()
                "popular" -> emptyList<Recipe>()
                else -> emptyList<Recipe>()
            }
            _recipes.value = filteredRecipes
            _isLoading.value = false
        } catch (e: Exception) {
            _error.value = "Erro ao filtrar receitas: ${e.message}"
            _isLoading.value = false
        }
    }

    private fun loadRecipes() = viewModelScope.launch {
        _isLoading.value = true
        try {
            // Initial API call to load recipes
            // _recipes.value = recipeRepository.getRecipes()
            _isLoading.value = false
        } catch (e: Exception) {
            _error.value = "Erro ao carregar receitas: ${e.message}"
            _isLoading.value = false
        }
    }
}
