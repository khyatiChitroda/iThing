package com.ithing.mobile.domain.usecase

import com.ithing.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newPassword: String) {
        authRepository.changePassword(newPassword)
    }
}
