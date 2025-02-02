package pt.ipt.dam.sabordigital.ui.main.recipe_create

import android.content.Context
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

/**
 * ViewModel for handling recipe creation logic.
 *
 * Handles loading categories, image selection, and creation of a new recipe.
 * Uses Retrofit to communicate with the backend API and LiveData to provide state updates.
 */
class RecipeCreationViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()

    /**
     * LiveData representing the loading state.
     */
    val loading: LiveData<Boolean> = _loading

    private val _categories = MutableLiveData<List<Category>>()

    /**
     * LiveData containing the list of available categories.
     */
    val categories: LiveData<List<Category>> = _categories

    private val _ingredients = MutableLiveData<List<Ingredient>>()

    /**
     * LiveData containing the list of available ingredients.
     */
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _selectedImage = MutableLiveData<String?>()

    /**
     * LiveData representing the Base64 encoded string of the selected image.
     */
    val selectedImage: LiveData<String?> = _selectedImage

    /**
     * Retrieves the authentication token from SharedPreferences.
     *
     * @param context The context used to access SharedPreferences.
     * @return The JWT token as a String if available, or null otherwise.
     */
    private fun getAuthToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("jwt_token", null)
    }

    /**
     * Loads the available recipe categories from the API.
     *
     * Sets [_loading] to true before calling the API. Once the response is received,
     * updates the [_categories] LiveData with the retrieved list or handles failures appropriately.
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
                            if (response.isSuccessful) {
                                // Update LiveData with category list from response.
                                _categories.value = response.body()
                            }
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                            // On failure, simply stop the loading indicator.
                            _loading.value = false
                        }
                    })
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    /**
     * Creates a new recipe using the provided RecipeCreate object.
     *
     * Retrieves the auth token from SharedPreferences and sends a POST request to create the recipe.
     * Updates the loading state based on the API call result.
     *
     * @param context The context used to access SharedPreferences.
     * @param recipe The RecipeCreate object containing details of the new recipe.
     */
    fun createRecipe(context: Context, recipe: RecipeCreate) {
        _loading.value = true
        val token = getAuthToken(context)
        if (token == null) {
            // Handle unauthorized state if token is not available.
            _loading.value = false
            return
        }
        viewModelScope.launch {
            try {
                RetrofitInitializer().recipeService().createRecipe(
                    "$token", recipe
                ).enqueue(object : Callback<RecipeCreate> {
                    override fun onResponse(
                        call: Call<RecipeCreate>,
                        response: Response<RecipeCreate>
                    ) {
                        // Once a response is received, stop the loading indicator.
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

    /**
     * Sets the selected image's Base64 string after image selection.
     *
     * @param imageBase64 A Base64 encoded string representing the image, or null if no image.
     */
    fun setSelectedImage(imageBase64: String?) {
        _selectedImage.value = imageBase64
    }
}