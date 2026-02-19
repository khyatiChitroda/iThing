package com.ithing.mobile.presentation.feature.splash

import com.ithing.mobile.core.session.UserRole


sealed class SplashDestination {
    data class Authenticated(val role: UserRole) : SplashDestination()
    object Unauthenticated : SplashDestination()
}

