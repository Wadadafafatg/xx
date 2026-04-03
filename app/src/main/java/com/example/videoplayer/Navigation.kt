package com.example.videoplayer

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.videoplayer.screens.VideoListScreen
import com.example.videoplayer.screens.VideoPlayerScreen
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay

private object Destinations {
    const val VideoList = "video_list"
    const val VideoPlayer = "video_player"
    const val VideoUriArgument = "videoUri"
    const val VideoTitleArgument = "videoTitle"
    const val VideoIdArgument = "videoId"

    fun videoPlayerRoute(video: Video, resolvedTitle: String): String =
        "$VideoPlayer/${Uri.encode(video.uri.toString())}/${Uri.encode(resolvedTitle)}/${video.id}"
}

private const val TestInterstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712"
private const val InterstitialRetryDelayMs = 10_000L

@Composable
fun VideoPlayerNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val preferences = remember { UserVideoPreferences(context) }
    val activity = context as? Activity
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }

    fun loadInterstitialAd() {
        InterstitialAd.load(
            context,
            TestInterstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loadedAd: InterstitialAd) {
                    interstitialAd = loadedAd
                    Log.d("Ads", "Interstitial loaded")
                }

                override fun onAdFailedToLoad(loadError: LoadAdError) {
                    interstitialAd = null
                    Log.w("Ads", "Interstitial failed to load: ${loadError.message}")
                }
            }
        )
    }

    LaunchedEffect(interstitialAd) {
        if (interstitialAd == null) {
            loadInterstitialAd()
            delay(InterstitialRetryDelayMs)
            if (interstitialAd == null) {
                loadInterstitialAd()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.VideoList,
    ) {
        composable(Destinations.VideoList) {
            VideoListScreen(
                onVideoSelected = { video, resolvedTitle ->
                    val route = Destinations.videoPlayerRoute(video, resolvedTitle)
                    val ad = interstitialAd
                    if (activity == null || ad == null) {
                        navController.navigate(route)
                        if (ad == null) loadInterstitialAd()
                    } else {
                        interstitialAd = null
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                loadInterstitialAd()
                                navController.navigate(route)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                Log.w("Ads", "Interstitial failed to show: ${adError.message}")
                                loadInterstitialAd()
                                navController.navigate(route)
                            }
                        }
                        ad.show(activity)
                    }
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
