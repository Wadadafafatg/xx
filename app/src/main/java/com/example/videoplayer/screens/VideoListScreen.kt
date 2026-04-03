package com.example.videoplayer.screens

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.example.videoplayer.PermissionHandler
import com.example.videoplayer.UserVideoPreferences
import com.example.videoplayer.Video
import com.example.videoplayer.VideoRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CardColor = Color(0xFF1A1A1A)

@Composable
fun VideoListScreen(
    onVideoSelected: (Video, String) -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { VideoRepository(context) }
    val preferences = remember { UserVideoPreferences(context) }
    var videos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var deletingVideo by remember { mutableStateOf<Video?>(null) }
    val scope = rememberCoroutineScope()

    fun resolvedTitle(video: Video): String = preferences.getCustomTitle(video.id) ?: video.title

    fun loadVideos() {
        scope.launch {
            isLoading = true
            videos = repository.loadVideos()
            isLoading = false
        }
    }

    PermissionHandler(onPermissionGranted = ::loadVideos) { hasPermission, requestPermission ->
        val byId = videos.associateBy { it.id }
        val historyVideos = preferences.getHistoryVideoIds().mapNotNull { byId[it] }

        Scaffold(
            modifier = Modifier.background(Color.Black),
            containerColor = Color.Black,
            bottomBar = {
                TestBannerAd()
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
                    if (historyVideos.isNotEmpty()) {
                        item {
                            HistorySection(
                                videos = historyVideos,
                                resolveTitle = ::resolvedTitle,
                                onVideoSelected = { video -> onVideoSelected(video, resolvedTitle(video)) },
                            )
                        }
                    }
                    item {
                        LibrarySection(
                            videos = videos,
                            resolveTitle = ::resolvedTitle,
                            onVideoSelected = { video -> onVideoSelected(video, resolvedTitle(video)) },
                            onRename = { video, newTitle ->
                                scope.launch {
                                    val renamed = repository.renameVideo(video, newTitle)
                                    if (renamed) {
                                        preferences.clearCustomTitle(video.id)
                                        loadVideos()
                                    } else {
                                        preferences.setCustomTitle(video.id, newTitle)
                                        videos = videos.toList()
                                    }
                                }
                            },
                            onDelete = { video -> deletingVideo = video },
                            onClearTitle = { video ->
                                preferences.clearCustomTitle(video.id)
                                videos = videos.toList()
                            },
                        )
                    }
                }
            }
        }

        if (deletingVideo != null) {
            val video = deletingVideo ?: return@PermissionHandler
            AlertDialog(
                onDismissRequest = { deletingVideo = null },
                title = { Text("Delete video") },
                text = { Text("Are you sure you want to delete ${resolvedTitle(video)} from your device?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                repository.deleteVideo(video)
                                preferences.removeFromHistory(video.id)
                                preferences.clearCustomTitle(video.id)
                                deletingVideo = null
                                loadVideos()
                            }
                        }
                    ) { Text("Delete", color = Color.Red) }
                },
                dismissButton = { TextButton(onClick = { deletingVideo = null }) { Text("Cancel") } },
                containerColor = Color(0xFF121212),
                titleContentColor = Color.White,
                textContentColor = Color.White,
            )
        }
    }

    LaunchedEffect(Unit) {
        if (videos.isEmpty()) {
            isLoading = false
        }
    }
}

@Composable
private fun HistorySection(
    videos: List<Video>,
    resolveTitle: (Video) -> String,
    onVideoSelected: (Video) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "History",
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(videos.take(20), key = { it.id }) { video ->
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .height(130.dp)
                        .clickable { onVideoSelected(video) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                ) {
                    VideoThumbnail(video = video)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                    ) {
                        Text(
                            text = resolveTitle(video),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.align(Alignment.BottomStart),
                        )
                        Text(
                            text = formatDuration(video.durationMs),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.BottomEnd),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySection(
    videos: List<Video>,
    resolveTitle: (Video) -> String,
    onVideoSelected: (Video) -> Unit,
    onRename: (Video, String) -> Unit,
    onDelete: (Video) -> Unit,
    onClearTitle: (Video) -> Unit,
) {
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
                LibraryCard(
                    video = video,
                    resolvedTitle = resolveTitle(video),
                    onClick = { onVideoSelected(video) },
                    onRename = { onRename(video, it) },
                    onDelete = { onDelete(video) },
                    onClearTitle = { onClearTitle(video) },
                )
            }
        }
    }
}

@Composable
private fun LibraryCard(
    video: Video,
    resolvedTitle: String,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onClearTitle: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var renameDialogOpen by remember { mutableStateOf(false) }
    var infoDialogOpen by remember { mutableStateOf(false) }
    var editedTitle by remember(video.id, resolvedTitle) { mutableStateOf(resolvedTitle) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
        ) {
            VideoThumbnail(video = video)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Video options",
                        tint = Color.White,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = Color(0xFF1E1E1E),
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename", color = Color.White) },
                        onClick = {
                            menuExpanded = false
                            renameDialogOpen = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Video info", color = Color.White) },
                        onClick = {
                            menuExpanded = false
                            infoDialogOpen = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        },
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = resolvedTitle,
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

    if (renameDialogOpen) {
        AlertDialog(
            onDismissRequest = { renameDialogOpen = false },
            title = { Text("Rename video", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose a new name for this video.", color = Color.White.copy(alpha = 0.8f))
                    OutlinedTextField(value = editedTitle, onValueChange = { editedTitle = it }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editedTitle.isNotBlank()) {
                            onRename(editedTitle)
                            renameDialogOpen = false
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onClearTitle()
                        renameDialogOpen = false
                    }) { Text("Reset") }
                    TextButton(onClick = { renameDialogOpen = false }) { Text("Cancel") }
                }
            },
            containerColor = Color(0xFF121212),
        )
    }

    if (infoDialogOpen) {
        AlertDialog(
            onDismissRequest = { infoDialogOpen = false },
            title = { Text("Video details", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(label = "Name", value = resolvedTitle)
                    InfoRow(label = "Duration", value = formatDuration(video.durationMs))
                    InfoRow(label = "Added", value = formatDate(video.dateAddedSeconds))
                    InfoRow(label = "URI", value = video.uri.toString())
                }
            },
            confirmButton = {
                TextButton(onClick = { infoDialogOpen = false }) { Text("Close") }
            },
            containerColor = Color(0xFF121212),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium)
        Text(text = value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun VideoThumbnail(video: Video) {
    val context = LocalContext.current
    val thumbnail by produceState<Bitmap?>(initialValue = null, video.uri) {
        value = loadVideoThumbnail(context = context, video = video)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2A2A2A)))
        }
    }
}

private suspend fun loadVideoThumbnail(context: android.content.Context, video: Video): Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(video.uri, Size(640, 360), null)
            } else {
                MediaMetadataRetriever().use { retriever ->
                    retriever.setDataSource(context, video.uri)
                    retriever.getFrameAtTime(-1) ?: retriever.embeddedPicture?.let { bytes ->
                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                }
            }
        }.getOrNull()
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

@Composable
private fun TestBannerAd() {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = "ca-app-pub-3940256099942544/6300978111"
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("Ads", "Banner loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w("Ads", "Banner failed to load: ${loadAdError.message}")
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { adView }
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
