package com.example.lacakair.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lacakair.auth.AuthState
import com.example.lacakair.auth.AuthViewModel
import com.example.lacakair.camera.CameraScreen
import com.example.lacakair.ui.screens.CreatePostScreen
import com.example.lacakair.ui.screens.HomeScreen
import com.example.lacakair.ui.screens.LoginScreen
import com.example.lacakair.ui.screens.MapScreen
import com.example.lacakair.ui.screens.RegisterScreen
import com.example.lacakair.viewmodel.PostViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Home.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    authViewModel.resetAuthState()
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = false }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    authViewModel.resetAuthState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                postViewModel = postViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToCreatePost = {
                    capturedImageUri = null
                    navController.navigate(Screen.CreatePost.createRoute())
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { uri ->
                    capturedImageUri = uri
                    navController.navigate(Screen.CreatePost.createRoute(Uri.encode(uri.toString()))) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                },
                onError = { exception ->
                    exception.printStackTrace()
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.CreatePost.route,
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val imageUriString = backStackEntry.arguments?.getString("imageUri")
            val imageUri = imageUriString?.let { Uri.parse(Uri.decode(it)) } ?: capturedImageUri

            CreatePostScreen(
                initialImageUri = imageUri,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                onPostCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onCreatePost = { uri, caption, latitude, longitude, locationName ->
                    postViewModel.createPostWithImage(
                        context = context,
                        imageUri = uri,
                        caption = caption,
                        latitude = latitude,
                        longitude = longitude,
                        locationName = locationName,
                        onSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            // TODO: Show error message
                            println("Error creating post: $error")
                        }
                    )
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                posts = postViewModel.posts.collectAsState().value,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
