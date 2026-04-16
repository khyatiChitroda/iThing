package com.ithing.mobile.presentation.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ithing.mobile.R
import com.ithing.mobile.presentation.theme.DarkText
import com.ithing.mobile.presentation.theme.LightGrayBg
import com.ithing.mobile.presentation.theme.NavyBlue

private val HomeWebFont = FontFamily.SansSerif
private val FeatureCardBg = Color(0xFFEFF6FF)

private data class HomeFeature(
    val title: String,
    val imageRes: Int
)

@Composable
fun HomeScreen() {
    val features = remember {
        listOf(
            HomeFeature("Real Time Monitoring", R.drawable.home_realtime),
            HomeFeature("Predictive Maintenance", R.drawable.home_maintenance),
            HomeFeature("Visual Dashboard", R.drawable.home_visual_dashboard),
            HomeFeature("Secure & Scalable", R.drawable.home_secure)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Welcome to iThing — where\nmachines speak and decisions get smarter.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = HomeWebFont,
                        fontSize = 30.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                )

                Text(
                    text = "\"You can't manage what you don't measure - We Measure Everything\"",
                    modifier = Modifier.padding(top = 14.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = HomeWebFont,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        lineHeight = 28.sp
                    )
                )

                Text(
                    text = "Whether you're building machines or running them, iThing empowers you with real-time monitoring, intelligent analytics, and dashboards — all from a single platform. From reducing downtime to uncovering performance insights, our end-to-end IoT solution helps you turn raw machine data into clear, actionable value.",
                    modifier = Modifier.padding(top = 18.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = HomeWebFont,
                        color = DarkText,
                        lineHeight = 27.sp
                    )
                )

                Text(
                    text = "Let your Machines be smarter. Harness the data driven actionable intelligence with zero complexity and turn into decisions and value…!!",
                    modifier = Modifier.padding(top = 18.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = HomeWebFont,
                        color = DarkText,
                        lineHeight = 27.sp
                    )
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_machine),
                    contentDescription = "Machine illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Let Your Machines be Smarter",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = HomeWebFont,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue,
                        fontSize = 24.sp
                    )
                )

                Text(
                    text = "Harness the data driven actionable intelligence with zero complexity.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = HomeWebFont,
                        color = DarkText,
                        lineHeight = 26.sp
                    )
                )
            }
        }

        item {
            val rows = ((features.size + 1) / 2).coerceAtLeast(1)
            val gridHeight = rows * 210 + (rows - 1) * 16

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false
            ) {
                items(features) { feature ->
                    HomeFeatureCard(feature = feature)
                }
            }
        }
    }
}

@Composable
private fun HomeFeatureCard(feature: HomeFeature) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FeatureCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = feature.imageRes),
                    contentDescription = feature.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = feature.title,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = HomeWebFont,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyBlue,
                    fontSize = 18.sp
                )
            )
        }
    }
}