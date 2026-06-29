package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.GymZoneApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable Edge-to-Edge full content bleeding
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Instantiate the MainViewModel
                val viewModel: MainViewModel = viewModel()
                // Render the primary GymZone application flow
                GymZoneApp(viewModel = viewModel)
            }
        }
    }
}
