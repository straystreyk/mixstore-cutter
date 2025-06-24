package com.pokrikinc.mixpokrikcutter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pokrikinc.mixpokrikcutter.ui.screens.PokrikApp
import com.pokrikinc.mixpokrikcutter.ui.theme.PokrikTheme


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

