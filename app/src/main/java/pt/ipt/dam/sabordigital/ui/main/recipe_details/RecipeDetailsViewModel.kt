package pt.ipt.dam.sabordigital.ui.main.recipe_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.data.remote.models.Instruction
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeDetailsViewModel : ViewModel() {
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> = _recipe

    private val _ingredients = MutableLiveData<List<Ingredient>>()
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _instructions = MutableLiveData<List<Instruction>>()
    val instructions: LiveData<List<Instruction>> = _instructions

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

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

    fun loadRecipeIngredients(recipeId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().getRecipeIngredients(recipeId)
                    .enqueue(object : Callback<List<Ingredient>> {
                        override fun onResponse(
                            call: Call<List<Ingredient>>,
                            response: Response<List<Ingredient>>
                        ) {
                            if (response.isSuccessful) {
                                _ingredients.value = response.body()
                            }
                        }

                        override fun onFailure(call: Call<List<Ingredient>>, t: Throwable) {
                            // Handle error
                        }
                    })
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadRecipeInstructions(recipeId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().getRecipeInstructions(recipeId)
                    .enqueue(object : Callback<List<Instruction>> {
                        override fun onResponse(
                            call: Call<List<Instruction>>,
                            response: Response<List<Instruction>>
                        ) {
                            if (response.isSuccessful) {
                                _instructions.value = response.body()
                            }
                        }

                        override fun onFailure(call: Call<List<Instruction>>, t: Throwable) {
                            // Handle error
                        }
                    })
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
