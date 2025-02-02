package pt.ipt.dam.sabordigital.data.remote.models

// Data class for authentication requests containing:
// - User credentials (email and password)
// Used when making login requests to obtain authentication tokens

data class TokenRequest(
    val email: String,     // User's email address for authentication
    val password: String   // User's password for authentication
)
