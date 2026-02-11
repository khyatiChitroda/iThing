package com.ithing.mobile.domain.usecase

import com.ithing.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke() {
        repository.logout()
    }
}
