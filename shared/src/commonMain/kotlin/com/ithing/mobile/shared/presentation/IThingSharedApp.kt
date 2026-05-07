package com.ithing.mobile.shared.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ithing.mobile.shared.core.Platform

@Composable
fun IThingSharedApp(platform: Platform = Platform()) {
    MaterialTheme {
        Text("iThing shared module ready on ${platform.name}")
    }
}
