package pt.ipt.dam.sabordigital.data.remote.models

// Data class for new user registration containing:
// - Essential user information needed to create an account
// Used when making registration requests to the API

data class UserCreate(
    val email: String,     // User's email address for account creation
    val password: String,  // User's chosen password
    val name: String       // User's display name
)
