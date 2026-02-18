package com.ithing.mobile.domain.usecase

import androidx.compose.runtime.Composable
import com.ithing.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        authRepository.forgotPassword(email)
    }
}
