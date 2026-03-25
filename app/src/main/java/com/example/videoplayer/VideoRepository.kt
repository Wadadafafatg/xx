package com.example.videoplayer

import android.content.ContentUris
import android.content.Context
import android.content.ContentValues
import android.provider.MediaStore
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

    suspend fun deleteVideo(video: Video): Boolean = withContext(Dispatchers.IO) {
        context.contentResolver.delete(video.uri, null, null) > 0
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

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, normalizedName)
        }
        context.contentResolver.update(video.uri, values, null, null) > 0
    }
}
