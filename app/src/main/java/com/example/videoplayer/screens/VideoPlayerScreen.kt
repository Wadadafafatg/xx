package com.example.videoplayer.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoPlayerScreen(
    videoUri: Uri,
    title: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val replayCount = remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                VideoView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    val controller = MediaController(context).also { mediaController ->
                        mediaController.setAnchorView(this)
                    }
                    setMediaController(controller)
                    tag = videoUri.toString()
                    setVideoURI(videoUri)
                    setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.isLooping = false
                        start()
                    }
                    setOnCompletionListener {
                        seekTo(0)
                        replayCount.intValue++
                    }
                }
            },
            update = { videoView ->
                if (videoView.tag != videoUri.toString()) {
                    videoView.tag = videoUri.toString()
                    videoView.setVideoURI(videoUri)
                    videoView.requestFocus()
                    videoView.start()
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(16.dp),
        ) {
            IconButton(onClick = onBack) {
                Text(text = "Back", color = Color.White)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            if (replayCount.intValue > 0) {
                Text(
                    text = "Playback finished — tap play to replay.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }
        }
    }

    LaunchedEffect(replayCount.intValue) {
        // Recompose top overlay when playback completes.
    }

    DisposableEffect(Unit) {
        onDispose {
            // VideoView is disposed with AndroidView lifecycle.
        }
    }
}
