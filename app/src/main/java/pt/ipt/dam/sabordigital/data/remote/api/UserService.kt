package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.User
import pt.ipt.dam.sabordigital.data.remote.models.UserCreate
import pt.ipt.dam.sabordigital.data.remote.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


// Service interface managing user-related operations including:
// 1. User registration and account management
// 2. Profile information retrieval
// 3. Password management
// 4. Account deletion
// All operations are asynchronous using Retrofit's Call wrapper

data class PasswordChange(
    val password: String    // Data class for password change requests
)

interface UserService {
    @POST("users/register")  // POST endpoint for new user registration
    fun register(
        @Body request: UserCreate  // Takes user creation data in request body
    ): Call<UserResponse>         // Returns user response data

    @GET("users/me")             // GET endpoint for fetching user profile
    fun getUser(
        @Header("Authorization") token: String,  // Auth token for user identification
    ): Call<User>                // Returns user profile data

    @POST("users/password")      // POST endpoint for password changes
    fun changePassword(
        @Header("Authorization") token: String,  // Auth token required
        @Body password: PasswordChange          // New password data
    ): Call<Void>               // Returns nothing (void response)

    @DELETE("users/deletion")    // DELETE endpoint for account removal
    fun deleteAccount(
        @Header("Authorization") token: String,  // Auth token required
    ): Call<Void>               // Returns nothing (void response)
}
