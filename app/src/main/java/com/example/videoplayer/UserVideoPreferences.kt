package com.example.videoplayer

import android.content.Context

class UserVideoPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHistoryVideoIds(): List<Long> {
        val raw = preferences.getString(KEY_HISTORY_IDS, null).orEmpty()
        return raw
            .split(',')
            .mapNotNull { it.trim().toLongOrNull() }
            .distinct()
    }

    fun addToHistory(videoId: Long) {
        val updated = buildList {
            add(videoId)
            addAll(getHistoryVideoIds().filterNot { it == videoId })
        }
        preferences.edit().putString(KEY_HISTORY_IDS, updated.joinToString(",")).apply()
    }

    fun removeFromHistory(videoId: Long) {
        val updated = getHistoryVideoIds().filterNot { it == videoId }
        preferences.edit().putString(KEY_HISTORY_IDS, updated.joinToString(",")).apply()
    }

    fun getCustomTitle(videoId: Long): String? = preferences.getString(titleKey(videoId), null)

    fun setCustomTitle(videoId: Long, title: String) {
        val normalized = title.trim()
        if (normalized.isEmpty()) {
            preferences.edit().remove(titleKey(videoId)).apply()
        } else {
            preferences.edit().putString(titleKey(videoId), normalized).apply()
        }
    }

    fun clearCustomTitle(videoId: Long) {
        preferences.edit().remove(titleKey(videoId)).apply()
    }

    private fun titleKey(videoId: Long): String = "$KEY_TITLE_PREFIX$videoId"

    private companion object {
        const val PREFS_NAME = "video_player_user_data"
        const val KEY_HISTORY_IDS = "history_video_ids"
        const val KEY_TITLE_PREFIX = "title_"
    }
}
