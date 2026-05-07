package com.ithing.mobile.presentation.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ithing.mobile.R
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import com.ithing.mobile.presentation.components.IThingCard
import com.ithing.mobile.presentation.components.IThingScreenContainer
import com.ithing.mobile.presentation.theme.DarkText
import com.ithing.mobile.presentation.theme.NavyBlue
import com.ithing.mobile.presentation.theme.Slate600
import com.ithing.mobile.presentation.theme.Theme1Blue
import com.ithing.mobile.presentation.theme.Theme2Navy

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IThingCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 6,
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Welcome to iThing \u2014 where\nmachines speak and decisions get smarter.",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = NavyBlue
                    )

                    Text(
                        text = "\u201cYou can\u2019t manage, what you don\u2019t measure - We Measure Everything\u201d",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 21.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = DarkText
                    )

                    Text(
                        text = "Whether you're building machines or running them, iThing empowers you with real-time monitoring, intelligent analytics, and dashboards \u2014 all from a single platform.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 30.sp
                        ),
                        color = Slate600
                    )

                    Image(
                        painter = painterResource(id = R.drawable.home_machine),
                        contentDescription = "Machine illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(top = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IThingLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ithing_logo),
        contentDescription = "iTHING",
        modifier = modifier.width(224.dp),
        contentScale = ContentScale.Fit
    )
}
