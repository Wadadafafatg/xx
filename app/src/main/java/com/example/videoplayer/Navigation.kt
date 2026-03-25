package com.example.videoplayer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.videoplayer.screens.VideoListScreen
import com.example.videoplayer.screens.VideoPlayerScreen

private object Destinations {
    const val VideoList = "video_list"
    const val VideoPlayer = "video_player"
    const val VideoUriArgument = "videoUri"
    const val VideoTitleArgument = "videoTitle"
    const val VideoIdArgument = "videoId"

    fun videoPlayerRoute(video: Video, resolvedTitle: String): String =
        "$VideoPlayer/${Uri.encode(video.uri.toString())}/${Uri.encode(resolvedTitle)}/${video.id}"
}

@Composable
fun VideoPlayerNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { UserVideoPreferences(context) }

    NavHost(
        navController = navController,
        startDestination = Destinations.VideoList,
    ) {
        composable(Destinations.VideoList) {
            VideoListScreen(
                onVideoSelected = { video, resolvedTitle ->
                    navController.navigate(Destinations.videoPlayerRoute(video, resolvedTitle))
                }
            )
        }
        composable(
            route = "${Destinations.VideoPlayer}/{${Destinations.VideoUriArgument}}/{${Destinations.VideoTitleArgument}}/{${Destinations.VideoIdArgument}}",
            arguments = listOf(
                navArgument(Destinations.VideoUriArgument) { type = NavType.StringType },
                navArgument(Destinations.VideoTitleArgument) { type = NavType.StringType },
                navArgument(Destinations.VideoIdArgument) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong(Destinations.VideoIdArgument) ?: -1L
            VideoPlayerScreen(
                videoId = videoId,
                videoUri = Uri.parse(
                    Uri.decode(backStackEntry.arguments?.getString(Destinations.VideoUriArgument).orEmpty())
                ),
                title = Uri.decode(backStackEntry.arguments?.getString(Destinations.VideoTitleArgument).orEmpty()),
                onBack = { navController.popBackStack() },
                onVideoDeleted = { navController.popBackStack() },
                onPlaybackStarted = {
                    if (videoId >= 0) {
                        preferences.addToHistory(videoId)
                    }
                },
            )
        }
    }
}
