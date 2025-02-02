package pt.ipt.dam.sabordigital.data.remote.models

import com.google.gson.annotations.SerializedName

// Data class representing the server's response to a successful authentication
// Contains access token details and user information
// Uses Gson annotations for JSON field mapping

data class TokenResponse(
    @SerializedName("access_token")   // Maps JSON "access_token" to accessToken
    val accessToken: String,          // JWT or similar token for authentication
    @SerializedName("token_type")     // Maps JSON "token_type" to tokenType
    val tokenType: String,            // Type of token (e.g., "Bearer")
    @SerializedName("user")           // Maps JSON "user" to user object
    val user: User                    // User details associated with the token
)

// Data class representing a user's profile information
// Contains basic user details and login tracking
// Uses Gson annotations for JSON field mapping

data class User(
    @SerializedName("user_id")        // Maps JSON "user_id" to id
    val id: Int,                      // Unique user identifier
    @SerializedName("email")          // Maps JSON "email" to email
    val email: String,                // User's email address
    @SerializedName("name")           // Maps JSON "name" to name
    val name: String,                 // User's display name
    @SerializedName("last_login")     // Maps JSON "last_login" to lastLogin
    val lastLogin: String             // Timestamp of user's last login
)
