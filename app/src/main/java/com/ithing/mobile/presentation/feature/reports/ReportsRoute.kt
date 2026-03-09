package com.ithing.mobile.presentation.feature.reports

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun ReportsRoute(
    viewModel: ReportsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    Text("Reports Screen")
}