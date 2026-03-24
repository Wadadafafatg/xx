package com.example.videoplayer.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.videoplayer.PermissionHandler
import com.example.videoplayer.Video
import com.example.videoplayer.VideoRepository
import kotlinx.coroutines.launch

@Composable
fun VideoListScreen(
    onVideoSelected: (Video) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { VideoRepository(context) }
    var videos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadVideos() {
        scope.launch {
            isLoading = true
            videos = repository.loadVideos()
            isLoading = false
        }
    }

    PermissionHandler(onPermissionGranted = ::loadVideos) { hasPermission, requestPermission ->
        Scaffold { innerPadding ->
            when {
                !hasPermission -> PermissionView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    requestPermission = requestPermission,
                )

                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                videos.isEmpty() -> EmptyVideosView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onRefresh = ::loadVideos,
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(videos, key = { it.id }) { video ->
                        VideoRow(video = video, onClick = { onVideoSelected(video) })
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (videos.isEmpty()) {
            isLoading = false
        }
    }
}

@Composable
private fun VideoRow(video: Video, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = formatDuration(video.durationMs),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(text = "Play", style = MaterialTheme.typography.labelLarge)
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
            )
            Text(
                text = "Grant access so the app can scan and play your local videos.",
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
            Button(onClick = requestPermission) {
                Text(text = "Grant permission")
            }
        }
    }
}

@Composable
private fun EmptyVideosView(modifier: Modifier, onRefresh: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "No local videos found", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Add videos to your device storage, then refresh.", modifier = Modifier.padding(8.dp))
            Button(onClick = onRefresh) {
                Text(text = "Refresh")
            }
        }
    }
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
