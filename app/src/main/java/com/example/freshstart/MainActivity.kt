package com.example.freshstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.freshstart.data.repository.SampleRepositoryImpl
import com.example.freshstart.presentation.SampleScreen
import com.example.freshstart.presentation.SampleViewModel
import com.example.freshstart.ui.theme.FreshStartTheme

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        SampleViewModel(
            repository = SampleRepositoryImpl()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreshStartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SampleScreen(viewModel = viewModel)
                }
            }
        }
    }
}
