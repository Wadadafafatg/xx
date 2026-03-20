package com.example.videoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val colors = if (isSystemInDarkTheme()) {
                darkColorScheme(
                    primary = androidx.compose.ui.graphics.Color.White,
                    onPrimary = androidx.compose.ui.graphics.Color.Black,
                    background = androidx.compose.ui.graphics.Color.Black,
                    onBackground = androidx.compose.ui.graphics.Color.White,
                    surface = androidx.compose.ui.graphics.Color.Black,
                    onSurface = androidx.compose.ui.graphics.Color.White,
                )
            } else {
                lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color.Black,
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    background = androidx.compose.ui.graphics.Color.White,
                    onBackground = androidx.compose.ui.graphics.Color.Black,
                    surface = androidx.compose.ui.graphics.Color.White,
                    onSurface = androidx.compose.ui.graphics.Color.Black,
                )
            }

            MaterialTheme(colorScheme = colors) {
                VideoPlayerNavigation()
            }
        }
    }
}
