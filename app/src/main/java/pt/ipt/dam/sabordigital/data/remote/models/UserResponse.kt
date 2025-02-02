package pt.ipt.dam.sabordigital.data.remote.models

// Data class representing the server's response after user operations
// Contains user information without sensitive data
// Used for registration and user update responses

data class UserResponse(
    val email: String,     // User's email address
    val name: String,      // User's display name
    val password: String   // Hashed/encrypted password (should typically not be returned in production)
)
