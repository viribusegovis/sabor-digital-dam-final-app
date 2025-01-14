package pt.ipt.dam.sabordigital.ui.details

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeDetailsViewModel : ViewModel() {
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> = _recipe

    fun fetchRecipeDetails(context: Context, recipeId: Int) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("jwt_token", null)

        token?.let {
            RetrofitInitializer().recipeService().getRecipeDetails(recipeId)
                .enqueue(object : Callback<Recipe> {
                    override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                        if (response.isSuccessful) {
                            _recipe.value = response.body()
                        }
                    }

                    override fun onFailure(call: Call<Recipe>, t: Throwable) {
                        // Handle Error
                    }
                })
        }
    }

    fun getDifficultyText(difficulty: Int): String {
        return when (difficulty) {
            1 -> "Fácil"
            2 -> "Médio"
            3 -> "Difícil"
            else -> "Desconhecido"
        }
    }

    fun shouldShowFilledStar(starIndex: Int, difficulty: Int): Boolean {
        return starIndex < difficulty
    }

}