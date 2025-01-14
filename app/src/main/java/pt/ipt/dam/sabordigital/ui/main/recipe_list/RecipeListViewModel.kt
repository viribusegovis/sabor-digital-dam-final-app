package pt.ipt.dam.sabordigital.ui.main.recipe_list.ui.recipelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeListViewModel : ViewModel() {
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadRecipes() {
        _loading.value = true
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().getAllRecipes()
                    .enqueue(object : Callback<List<Recipe>> {
                        override fun onResponse(
                            call: Call<List<Recipe>>,
                            response: Response<List<Recipe>>
                        ) {
                            if (response.isSuccessful) {
                                _recipes.value = response.body()
                            }
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    fun searchRecipes(query: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().searchRecipes(query)
                    .enqueue(object : Callback<List<Recipe>> {
                        override fun onResponse(
                            call: Call<List<Recipe>>,
                            response: Response<List<Recipe>>
                        ) {
                            if (response.isSuccessful) {
                                _recipes.value = response.body()
                            }
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    fun filterByCategory(categoryId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().getRecipesByCategory(categoryId)
                    .enqueue(object : Callback<List<Recipe>> {
                        override fun onResponse(
                            call: Call<List<Recipe>>,
                            response: Response<List<Recipe>>
                        ) {
                            if (response.isSuccessful) {
                                _recipes.value = response.body()
                            }
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    fun filterByIngredient(ingredientId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().getRecipesByIngredient(ingredientId)
                    .enqueue(object : Callback<List<Recipe>> {
                        override fun onResponse(
                            call: Call<List<Recipe>>,
                            response: Response<List<Recipe>>
                        ) {
                            if (response.isSuccessful) {
                                _recipes.value = response.body()
                            }
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    fun clearFilters() {
        loadRecipes()
    }
}
