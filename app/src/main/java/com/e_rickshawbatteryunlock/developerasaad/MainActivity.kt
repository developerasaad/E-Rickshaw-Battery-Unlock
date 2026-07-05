package com.e_rickshawbatteryunlock.developerasaad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.e_rickshawbatteryunlock.developerasaad.ui.navigation.AppNavHost
import com.e_rickshawbatteryunlock.developerasaad.ui.theme.BatteryUnlockTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire application.
 *
 * Jetpack Compose Navigation handles all screen transitions within this activity.
 * Edge-to-edge display is enabled for a full-bleed immersive experience.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryUnlockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
