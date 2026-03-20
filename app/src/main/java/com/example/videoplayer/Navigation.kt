package com.example.videoplayer

import android.net.Uri
import androidx.compose.runtime.Composable
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

    fun videoPlayerRoute(videoUri: String, title: String): String =
        "$VideoPlayer/$videoUri/${Uri.encode(title)}"
}

@Composable
fun VideoPlayerNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.VideoList,
    ) {
        composable(Destinations.VideoList) {
            VideoListScreen(
                onVideoSelected = { video ->
                    navController.navigate(
                        Destinations.videoPlayerRoute(
                            videoUri = Uri.encode(video.uri.toString()),
                            title = video.title,
                        )
                    )
                }
            )
        }
        composable(
            route = "${Destinations.VideoPlayer}/{${Destinations.VideoUriArgument}}/{${Destinations.VideoTitleArgument}}",
            arguments = listOf(
                navArgument(Destinations.VideoUriArgument) { type = NavType.StringType },
                navArgument(Destinations.VideoTitleArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            VideoPlayerScreen(
                videoUri = Uri.parse(
                    Uri.decode(backStackEntry.arguments?.getString(Destinations.VideoUriArgument).orEmpty())
                ),
                title = Uri.decode(backStackEntry.arguments?.getString(Destinations.VideoTitleArgument).orEmpty()),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
