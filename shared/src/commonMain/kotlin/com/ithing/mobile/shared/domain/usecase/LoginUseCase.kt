package com.ithing.mobile.shared.domain.usecase

import com.ithing.mobile.shared.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ) = authRepository.login(
        username = username,
        password = password
    )
}
