package com.ithing.mobile.domain.usecase

import com.ithing.mobile.domain.model.User
import com.ithing.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class GetLoggedInUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): User? {
        return authRepository.getLoggedInUser()
    }
}
