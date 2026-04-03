package com.example.videoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
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
                var showAdsNotice by rememberSaveable { mutableStateOf(true) }
                VideoPlayerNavigation()

                if (showAdsNotice) {
                    AlertDialog(
                        onDismissRequest = { showAdsNotice = false },
                        title = { Text("تنبيه مهم") },
                        text = {
                            Text(
                                "هذا التطبيق يعتمد على الإعلانات كمصدر الربح الأساسي لاستمراره. " +
                                    "مشاهدة الإعلانات تساعدنا على الاستمرار وتطوير التطبيق بشكل أفضل."
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { showAdsNotice = false }) {
                                Text("حسناً")
                            }
                        },
                    )
                }
            }
        }
    }
}
