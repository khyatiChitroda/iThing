package com.ithing.mobile.presentation.feature.splash


sealed class SplashDestination {
    object Authenticated : SplashDestination()
    object Unauthenticated : SplashDestination()
}

