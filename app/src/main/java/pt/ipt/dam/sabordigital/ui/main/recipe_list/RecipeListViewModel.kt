package pt.ipt.dam.sabordigital.ui.main.recipe_list.ui.recipelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam.sabordigital.data.remote.models.Category
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel responsible for managing the state and operations of the recipe list.
 *
 * It handles loading recipes, searching, and filtering by categories or ingredients.
 * Additionally, it loads the available categories and top ingredients for filter options.
 */
class RecipeListViewModel : ViewModel() {

    // LiveData holding the list of recipes to be displayed.
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    // LiveData holding the list of available categories.
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // LiveData holding the list of available ingredients.
    private val _ingredients = MutableLiveData<List<Ingredient>>()
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    // LiveData representing the loading state of data operations.
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // Variables used for maintaining current filter and search state.
    private var currentCategoryId: Int? = null
    private var currentIngredientId: Int? = null
    private var currentSearchQuery: String? = null

    /**
     * Returns the currently selected category ID used for filtering recipes.
     */
    fun getCurrentCategoryId() = currentCategoryId

    /**
     * Returns the currently selected ingredient ID used for filtering recipes.
     */
    fun getCurrentIngredientId() = currentIngredientId

    /**
     * Refreshes the recipe list based on the active filters.
     *
     * Checks current filters in order:
     * - If a category is selected, filters by that category.
     * - Else if an ingredient is selected, filters by that ingredient.
     * - Else if a search query exists, performs the search.
     * - Otherwise, loads all recipes.
     */
    fun refreshWithCurrentFilters() {
        when {
            currentCategoryId != null -> filterByCategory(currentCategoryId!!)
            currentIngredientId != null -> filterByIngredient(currentIngredientId!!)
            currentSearchQuery != null -> searchRecipes(currentSearchQuery!!)
            else -> loadRecipes()
        }
    }

    /**
     * Checks whether any filters (category, ingredient, or search query) are currently active.
     *
     * @return True if at least one filter is active, false otherwise.
     */
    fun hasFilters(): Boolean {
        return currentCategoryId != null || currentIngredientId != null || currentSearchQuery != null
    }

    /**
     * Loads all recipes from the API.
     *
     * Sets the loading indicator to true, and on response updates recipe list.
     * Resets the loading state once the call completes.
     */
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
                            // If API call is successful, update the recipe list.
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

    /**
     * Searches for recipes matching the given query.
     *
     * Clears any category or ingredient filters.
     * Sets the search query variable and triggers an API search request.
     *
     * @param query The search query input from the user.
     */
    fun searchRecipes(query: String) {
        currentSearchQuery = query
        currentCategoryId = null
        currentIngredientId = null
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

    /**
     * Filters recipes by a specific category.
     *
     * Sets the current category filter, clears any other filters, and makes an API call to get recipes.
     *
     * @param categoryId The ID of the selected category.
     */
    fun filterByCategory(categoryId: Int) {
        currentCategoryId = categoryId
        currentIngredientId = null
        currentSearchQuery = null
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

    /**
     * Filters recipes by a specific ingredient.
     *
     * Sets the current ingredient filter, clears any other filters, and makes an API call to get recipes.
     *
     * @param ingredientId The ID of the selected ingredient.
     */
    fun filterByIngredient(ingredientId: Int) {
        currentIngredientId = ingredientId
        currentCategoryId = null
        currentSearchQuery = null
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

    /**
     * Loads all available categories from the API.
     *
     * Updates the [_categories] LiveData with the list of categories once loaded.
     */
    fun loadCategories() {
        _loading.value = true
        viewModelScope.launch {
            try {
                RetrofitInitializer().categoryService().getCategories()
                    .enqueue(object : Callback<List<Category>> {
                        override fun onResponse(
                            call: Call<List<Category>>,
                            response: Response<List<Category>>
                        ) {
                            // Update the LiveData list if request is successful.
                            if (response.isSuccessful) {
                                _categories.value = response.body()
                            }
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    /**
     * Loads top ingredients from the API, limited to a maximum of 30.
     *
     * Updates the [_ingredients] LiveData with the retrieved list.
     */
    fun loadIngredients() {
        _loading.value = true
        viewModelScope.launch {
            try {
                RetrofitInitializer().ingredientService().getTopIngredients(30)
                    .enqueue(object : Callback<List<Ingredient>> {
                        override fun onResponse(
                            call: Call<List<Ingredient>>,
                            response: Response<List<Ingredient>>
                        ) {
                            if (response.isSuccessful) {
                                _ingredients.value = response.body()
                            }
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<List<Ingredient>>, t: Throwable) {
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    /**
     * Clears any active filters and loads all recipes.
     *
     * Resets the current filter parameters and triggers the full list refresh.
     */
    fun clearFilters() {
        currentCategoryId = null
        currentIngredientId = null
        currentSearchQuery = null
        loadRecipes()
    }
}
