package com.example.videoplayer

import android.content.ContentUris
import android.content.Context
import android.content.ContentValues
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.MediaStore
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepository(private val context: Context) {

    suspend fun loadVideos(): List<Video> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        buildList {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder,
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    add(
                        Video(
                            id = id,
                            title = cursor.getString(titleColumn) ?: "Untitled video",
                            durationMs = cursor.getLong(durationColumn),
                            dateAddedSeconds = cursor.getLong(dateAddedColumn),
                            uri = ContentUris.withAppendedId(collection, id),
                        )
                    )
                }
            }
        }
    }

    suspend fun buildVideoFromUri(uri: Uri): Video = withContext(Dispatchers.IO) {
        val displayName = resolveDisplayName(uri) ?: "Untitled video"
        val durationMs = resolveDurationMs(uri)
        val mediaId = resolveMediaId(uri) ?: stableIdFromUri(uri)

        Video(
            id = mediaId,
            title = displayName,
            durationMs = durationMs,
            dateAddedSeconds = System.currentTimeMillis() / 1_000,
            uri = uri,
        )
    }

    suspend fun deleteVideo(video: Video): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.delete(video.uri, null, null) > 0
        }.getOrDefault(false)
    }

    suspend fun renameVideo(video: Video, newTitle: String): Boolean = withContext(Dispatchers.IO) {
        val sanitizedTitle = newTitle.trim()
        if (sanitizedTitle.isEmpty()) return@withContext false

        val oldTitle = video.title
        val extension = oldTitle.substringAfterLast('.', "")
        val hasExtension = oldTitle.contains('.') && extension.isNotBlank()
        val normalizedName = buildString {
            append(sanitizedTitle)
            if (hasExtension && !sanitizedTitle.endsWith(".$extension", ignoreCase = true)) {
                append(".")
                append(extension)
            }
        }

        runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, normalizedName)
            }
            context.contentResolver.update(video.uri, values, null, null) > 0
        }.getOrDefault(false)
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex >= 0) cursor.getString(columnIndex) else null
            }
        }.getOrNull()
    }

    private fun resolveDurationMs(uri: Uri): Long {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Video.Media.DURATION),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use 0L
                val columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                if (columnIndex >= 0) cursor.getLong(columnIndex) else 0L
            } ?: 0L
        }.getOrDefault(0L)
    }

    private fun resolveMediaId(uri: Uri): Long? {
        val idString = uri.lastPathSegment?.substringAfterLast('/') ?: return null
        return idString.toLongOrNull()
    }

    private fun stableIdFromUri(uri: Uri): Long {
        return uri.toString().lowercase(Locale.getDefault()).hashCode().toLong()
    }
}
