package com.codewithfk.chatter

import WelcomeScreen
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.codewithfk.chatter.feature.auth.signin.SignInScreen
import com.codewithfk.chatter.feature.auth.signup.SignUpScreen
import com.codewithfk.chatter.feature.chat.ChatScreen
import com.codewithfk.chatter.feature.home.HomeScreen
import com.codewithfk.chatter.screens.OnboardingScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember

private val Context.dataStore by preferencesDataStore(name = "settings")
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

@Composable
fun MainApp() {
    val context = LocalContext.current
    val onboardingCompleted = remember {
        runBlocking {
            context.dataStore.data.first()[ONBOARDING_COMPLETED] ?: false
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val start = when {
            !onboardingCompleted -> "onboarding"
            currentUser != null -> "home"
            else -> "login"
        }
        
        NavHost(navController = navController, startDestination = start) {
            composable(
                "welcome/{username}",
                arguments = listOf(navArgument("username") { type = NavType.StringType })
            ) {
                val username = it.arguments?.getString("username") ?: "User"
                WelcomeScreen(
                    username = username,
                    onStartClick = {
                        navController.navigate("home") {
                            popUpTo("welcome/{username}") { inclusive = true }
                        }
                    }
                )
            }

            composable("onboarding") {
                OnboardingScreen {
                    runBlocking {
                        context.dataStore.edit { settings ->
                            settings[ONBOARDING_COMPLETED] = true
                        }
                    }
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            }

            composable("login") {
                SignInScreen(navController)
            }
            composable("signup") {
                SignUpScreen(navController)
            }
            composable("home") {
                HomeScreen(navController)
            }
            composable("chat/{channelId}&{channelName}", arguments = listOf(
                navArgument("channelId") {
                    type = NavType.StringType
                },
                navArgument("channelName") {
                    type = NavType.StringType
                }
            )) {
                val channelId = it.arguments?.getString("channelId") ?: ""
                val channelName = it.arguments?.getString("channelName") ?: ""
                ChatScreen(navController, channelId,channelName)
            }
        }
    }
}