package com.ithing.mobile

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ithing.mobile.presentation.theme.IThingMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.rememberNavController
import com.ithing.mobile.presentation.navigation.AppNavGraph
import kotlinx.coroutines.delay


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Volatile
    private var isUiReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        window.decorView.importantForAutofill =
            View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        splashScreen.setKeepOnScreenCondition { !isUiReady }
        enableEdgeToEdge()
        setContent {
            IThingMobileTheme(darkTheme = false) {
                var showAppContent by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(APP_ICON_DURATION_MS)
                    showAppContent = true
                }
                if (showAppContent) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                    SideEffect { isUiReady = true }
                }
            }

        }
    }

    private companion object {
        const val APP_ICON_DURATION_MS = 1_000L
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IThingMobileTheme {
        Greeting("Android")
    }
}
