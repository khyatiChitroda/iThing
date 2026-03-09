package com.ithing.mobile.presentation.components

import androidx.lifecycle.ViewModel
import com.ithing.mobile.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppShellViewModel @Inject constructor(
    val sessionManager: SessionManager
) : ViewModel()