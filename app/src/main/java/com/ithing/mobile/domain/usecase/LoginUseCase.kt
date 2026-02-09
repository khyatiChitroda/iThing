package com.ithing.mobile.domain.usecase


import com.ithing.mobile.domain.model.User
import com.ithing.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        username: String,
        password: String
    ): User {
        return authRepository.login(
            username = username,
            password = password
        )
    }
}