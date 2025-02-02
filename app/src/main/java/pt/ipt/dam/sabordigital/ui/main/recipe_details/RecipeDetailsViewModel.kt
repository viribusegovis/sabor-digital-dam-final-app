package pt.ipt.dam.sabordigital.ui.main.recipe_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam.sabordigital.data.remote.models.Instruction
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.data.remote.models.RecipeIngredient
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel for the Recipe Details screen.
 *
 * Handles fetching and exposing recipe details, its ingredients and instructions.
 * Communicates with the Retrofit API and provides LiveData objects to the UI.
 */
class RecipeDetailsViewModel : ViewModel() {

    private val _recipe = MutableLiveData<Recipe>()

    /**
     * LiveData containing the details of the recipe.
     */
    val recipe: LiveData<Recipe> = _recipe

    private val _ingredients = MutableLiveData<List<RecipeIngredient>>()

    /**
     * LiveData containing the list of recipe ingredients.
     */
    val ingredients: LiveData<List<RecipeIngredient>> = _ingredients

    private val _instructions = MutableLiveData<List<Instruction>>()

    /**
     * LiveData containing the list of instructions for the recipe.
     */
    val instructions: LiveData<List<Instruction>> = _instructions

    private val _loading = MutableLiveData<Boolean>()

    /**
     * LiveData representing the loading state.
     */
    val loading: LiveData<Boolean> = _loading

    /**
     * Loads the detailed recipe information from the backend.
     *
     * Initiates an API call to retrieve recipe details by cookbook ID.
     * Updates the [_recipe] and loading state accordingly.
     *
     * @param recipeId The ID of the recipe to load.
     */
    fun loadRecipeDetails(recipeId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().getRecipeDetails(recipeId)
                    .enqueue(object : Callback<Recipe> {
                        override fun onResponse(
                            call: Call<Recipe>,
                            response: Response<Recipe>
                        ) {
                            // If response is successful, update recipe LiveData.
                            if (response.isSuccessful) {
                                _recipe.value = response.body()
                            }
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<Recipe>, t: Throwable) {
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    /**
     * Loads the recipe ingredients from the backend.
     *
     * Initiates an API call to retrieve ingredients belonging to the given recipe.
     * Updates the [_ingredients] LiveData upon successful response.
     *
     * @param recipeId The ID of the recipe for which to load ingredients.
     */
    fun loadRecipeIngredients(recipeId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().getRecipeIngredients(recipeId)
                    .enqueue(object : Callback<List<RecipeIngredient>> {
                        override fun onResponse(
                            call: Call<List<RecipeIngredient>>,
                            response: Response<List<RecipeIngredient>>
                        ) {
                            // Validate and update ingredients list.
                            if (response.isSuccessful) {
                                _ingredients.value = response.body()
                            }
                        }

                        override fun onFailure(call: Call<List<RecipeIngredient>>, t: Throwable) {
                            // Handle error case if needed.
                        }
                    })
            } catch (e: Exception) {
                // Optionally handle exception.
            }
        }
    }

    /**
     * Loads the recipe instructions from the backend.
     *
     * Initiates an API call to retrieve instructions for the specified recipe.
     * On success, updates the [_instructions] LiveData.
     *
     * @param recipeId The ID of the recipe for which to load instructions.
     */
    fun loadRecipeInstructions(recipeId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().getRecipeInstructions(recipeId)
                    .enqueue(object : Callback<List<Instruction>> {
                        override fun onResponse(
                            call: Call<List<Instruction>>,
                            response: Response<List<Instruction>>
                        ) {
                            // If response is successful, update instructions list.
                            if (response.isSuccessful) {
                                _instructions.value = response.body()
                            }
                        }

                        override fun onFailure(call: Call<List<Instruction>>, t: Throwable) {
                            // Handle error case if needed.
                        }
                    })
            } catch (e: Exception) {
                // Optionally handle exception.
            }
        }
    }
}
