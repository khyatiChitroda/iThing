package com.ithing.mobile.shared.domain.usecase

import com.ithing.mobile.shared.domain.repository.AuthRepository

class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newPassword: String) {
        authRepository.changePassword(newPassword)
    }
}
