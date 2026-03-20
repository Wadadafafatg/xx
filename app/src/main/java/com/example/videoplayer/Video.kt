package com.example.videoplayer

import android.net.Uri

data class Video(
    val id: Long,
    val title: String,
    val durationMs: Long,
    val uri: Uri,
)
