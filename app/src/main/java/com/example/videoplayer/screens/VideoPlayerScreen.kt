package com.example.videoplayer.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.videoplayer.UserVideoPreferences
import com.example.videoplayer.Video
import com.example.videoplayer.VideoRepository
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerScreen(
    videoId: Long,
    videoUri: Uri,
    title: String,
    onBack: () -> Unit,
    onVideoDeleted: () -> Unit,
    onPlaybackStarted: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { VideoRepository(context) }
    val preferences = remember { UserVideoPreferences(context) }
    val scope = rememberCoroutineScope()
    val snackbars = remember { SnackbarHostState() }

    var menuExpanded by remember { mutableStateOf(false) }
    var renameDialogOpen by remember { mutableStateOf(false) }
    var deleteDialogOpen by remember { mutableStateOf(false) }
    var editedTitle by remember(title) { mutableStateOf(title) }
    var displayTitle by remember(title) { mutableStateOf(title) }

    val replayMessageVisible = remember { mutableStateOf(false) }
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
        }
    }

    BackHandler(onBack = onBack)

    LaunchedEffect(videoUri, exoPlayer) {
        replayMessageVisible.value = false
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, it.decorView) }

        if (window != null && controller != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    onPlaybackStarted()
                } else if (playbackState == Player.STATE_ENDED) {
                    replayMessageVisible.value = true
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            if (window != null && controller != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(context).apply {
                    useController = true
                    player = exoPlayer
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { playerView ->
                if (playerView.player !== exoPlayer) {
                    playerView.player = exoPlayer
                }
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = displayTitle,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Video options", tint = Color.White)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            renameDialogOpen = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            menuExpanded = false
                            deleteDialogOpen = true
                        },
                    )
                }
            }
        }

        if (replayMessageVisible.value) {
            Text(
                text = "انتهى تشغيل الفيديو: اضغط للعودة",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        SnackbarHost(
            hostState = snackbars,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (renameDialogOpen) {
        AlertDialog(
            onDismissRequest = { renameDialogOpen = false },
            title = { Text("Rename video") },
            text = {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newTitle = editedTitle.trim()
                    if (newTitle.isNotEmpty()) {
                        scope.launch {
                            if (videoId >= 0) {
                                preferences.setCustomTitle(videoId, newTitle)
                                repository.renameVideo(
                                    Video(
                                        id = videoId,
                                        title = displayTitle,
                                        durationMs = 0,
                                        dateAddedSeconds = 0,
                                        uri = videoUri,
                                    ),
                                    newTitle,
                                )
                            }
                            displayTitle = newTitle
                            renameDialogOpen = false
                            snackbars.showSnackbar("Video name saved")
                        }
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogOpen = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (deleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { deleteDialogOpen = false },
            title = { Text("Delete video") },
            text = { Text("Are you sure you want to delete this video?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val deleted = repository.deleteVideo(
                            Video(
                                id = videoId,
                                title = displayTitle,
                                durationMs = 0,
                                dateAddedSeconds = 0,
                                uri = videoUri,
                            )
                        )
                        deleteDialogOpen = false
                        if (deleted) {
                            if (videoId >= 0) {
                                preferences.removeFromHistory(videoId)
                                preferences.clearCustomTitle(videoId)
                            }
                            onVideoDeleted()
                        } else {
                            snackbars.showSnackbar("Unable to delete video")
                        }
                    }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogOpen = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
