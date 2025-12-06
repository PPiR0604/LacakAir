package com.example.lacakair.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object CreatePost : Screen("create_post?imageUri={imageUri}") {
        fun createRoute(imageUri: String? = null): String {
            return if (imageUri != null) "create_post?imageUri=$imageUri" else "create_post"
        }
    }
}
