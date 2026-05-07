package com.ithing.mobile.shared.domain.usecase

import com.ithing.mobile.shared.domain.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
