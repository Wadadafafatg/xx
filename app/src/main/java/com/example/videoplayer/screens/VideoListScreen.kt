package com.example.videoplayer.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.example.videoplayer.PermissionHandler
import com.example.videoplayer.Video
import com.example.videoplayer.VideoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

        val picker = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
            ::handlePickedUri
        )

        Scaffold(
            modifier = Modifier.background(Color.Black),
            containerColor = Color.Black,

            // ✅ الإعلان تحت
            bottomBar = {
                TestBannerAd()
            },

            // ✅ الزر موجود
            floatingActionButton = {
                if (hasPermission) {
                    FloatingActionButton(
                        onClick = { picker.launch("video/*") },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pick video"
                        )
                    }
                }
            }
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

                else -> HomePickerView(
                    modifier = Modifier.padding(innerPadding)
                )
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
private fun PermissionView(
    modifier: Modifier,
    requestPermission: () -> Unit
) {
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
            Button(onClick = requestPermission) {
                Text(text = "Grant permission")
            }
        }
    }
}

@Composable
private fun TestBannerAd() {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

private fun formatDate(dateAddedSeconds: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(dateAddedSeconds * 1_000))
}