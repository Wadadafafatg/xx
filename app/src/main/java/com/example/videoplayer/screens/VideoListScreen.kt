package com.example.videoplayer.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.videoplayer.PermissionHandler
import com.example.videoplayer.Video
import com.example.videoplayer.VideoRepository
import kotlinx.coroutines.launch

@Composable
fun VideoListScreen(
    onVideoSelected: (Video, String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { VideoRepository(context) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    fun handlePickedUri(uri: Uri?) {
        if (uri == null) return
        scope.launch {
            isLoading = true
            val video = repository.buildVideoFromUri(uri)
            onVideoSelected(video, video.title)
            isLoading = false
        }
    }

    PermissionHandler(onPermissionGranted = {}) { hasPermission, requestPermission ->
        val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent(), ::handlePickedUri)

        Scaffold(
            modifier = Modifier.background(Color.Black),
            containerColor = Color.Black,
            floatingActionButton = {
                if (hasPermission) {
                    FloatingActionButton(
                        onClick = { picker.launch("video/*") },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Pick video")
                    }
                }
            },
        ) { innerPadding ->
            when {
                !hasPermission -> PermissionView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(innerPadding),
                    requestPermission = requestPermission,
                )

                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White)
                }

                else -> HomePickerView(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
private fun HomePickerView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = "اختر فيديو للعرض",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
            )
            Text(
                text = "اضغط الزر الدائري بالأسفل لفتح الاستديو أو المعرض واختيار الفيديو.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.SansSerif,
            )
        }
    }
}

@Composable
private fun PermissionView(modifier: Modifier, requestPermission: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Storage permission required",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
            )
            Text(
                text = "Grant access so the app can open a video from your device.",
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
            )
            androidx.compose.material3.Button(onClick = requestPermission) {
                Text(text = "Grant permission")
            }
        }
    }
}
