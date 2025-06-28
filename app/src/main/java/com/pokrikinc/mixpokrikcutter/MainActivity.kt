package com.pokrikinc.mixpokrikcutter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import com.pokrikinc.mixpokrikcutter.ui.screens.PokrikApp
import com.pokrikinc.mixpokrikcutter.ui.theme.PokrikTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.init(this)
        RetrofitProvider.init()


        setContent {
            PokrikTheme {
                PokrikApp()
            }
        }
    }
}

