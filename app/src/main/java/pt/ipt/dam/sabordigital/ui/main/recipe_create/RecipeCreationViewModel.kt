package pt.ipt.dam.sabordigital.ui.main.recipe_create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam.sabordigital.data.remote.models.Category
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.data.remote.models.RecipeCreate
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeCreationViewModel : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _ingredients = MutableLiveData<List<Ingredient>>()
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _selectedImage = MutableLiveData<Uri?>()
    val selectedImage: LiveData<Uri?> = _selectedImage


    private fun getAuthToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("jwt_token", null)
    }

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

    fun createRecipe(context: Context, recipe: RecipeCreate) {
        _loading.value = true
        val token = getAuthToken(context)
        if (token == null) {
            // Handle unauthorized state
            return
        }
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().createRecipe(
                    "$token",
                    recipe
                )
                    .enqueue(object : Callback<RecipeCreate> {
                        override fun onResponse(
                            call: Call<RecipeCreate>,
                            response: Response<RecipeCreate>
                        ) {
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<RecipeCreate>, t: Throwable) {
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    fun setSelectedImage(uri: Uri?) {
        _selectedImage.value = uri
    }
}
