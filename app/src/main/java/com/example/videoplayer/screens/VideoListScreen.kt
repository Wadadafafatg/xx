package com.example.videoplayer.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.videoplayer.PermissionHandler
import com.example.videoplayer.Video
import com.example.videoplayer.VideoRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val CardColor = Color(0xFF1A1A1A)

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
        Scaffold(
            modifier = Modifier.background(Color.Black),
            containerColor = Color.Black,
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

                videos.isEmpty() -> EmptyVideosView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(innerPadding),
                    onRefresh = ::loadVideos,
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        HistorySection(videos = videos, onVideoSelected = onVideoSelected)
                    }
                    item {
                        LibrarySection(videos = videos, onVideoSelected = onVideoSelected)
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
private fun HistorySection(videos: List<Video>, onVideoSelected: (Video) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "History",
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(videos.take(10), key = { it.id }) { video ->
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .height(110.dp)
                        .clickable { onVideoSelected(video) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        Text(
                            text = formatDuration(video.durationMs),
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySection(videos: List<Video>, onVideoSelected: (Video) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Library",
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            videos.forEach { video ->
                LibraryCard(video = video, onClick = { onVideoSelected(video) })
            }
        }
    }
}

@Composable
private fun LibraryCard(video: Video, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "⋮",
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                style = MaterialTheme.typography.titleMedium,
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = video.title,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = formatDate(video.dateAddedSeconds),
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
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
                text = "Grant access so the app can scan and play your local videos.",
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
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
            Text(
                text = "No local videos found",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
            )
            Text(
                text = "Add videos to your device storage, then refresh.",
                modifier = Modifier.padding(8.dp),
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
            )
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

private fun formatDate(dateAddedSeconds: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(dateAddedSeconds * 1_000))
}
