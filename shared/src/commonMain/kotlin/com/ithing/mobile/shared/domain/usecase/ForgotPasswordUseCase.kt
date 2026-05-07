package com.ithing.mobile.shared.domain.usecase

import com.ithing.mobile.shared.domain.repository.AuthRepository

class ForgotPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        authRepository.forgotPassword(email)
    }
}
