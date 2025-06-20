package com.example.testandroidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.testandroidapp.ui.screens.PokrikApp
import com.example.testandroidapp.ui.theme.PokrikTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PokrikTheme {
                PokrikApp()
            }
        }
    }
}

