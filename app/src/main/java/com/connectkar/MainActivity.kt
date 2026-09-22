package com.connectkar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.connectkar.auth.AuthViewModel
import com.connectkar.ui.FirebaseAuthScreen
import com.connectkar.ui.theme.ConnectKarTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectKarTheme {
                FirebaseAuthScreen(viewModel = authViewModel)
            }
        }
    }
}
