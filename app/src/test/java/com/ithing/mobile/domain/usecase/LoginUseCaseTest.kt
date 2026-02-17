package com.ithing.mobile.domain.usecase

import com.ithing.mobile.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        authRepository = mock()
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `invoke should call repository login successfully`() = runTest {
        val username = "test"
        val password = "password"

        whenever(authRepository.login(username, password))
            .thenReturn(Unit)

        loginUseCase(username, password)

        verify(authRepository).login(username, password)
    }

    @Test(expected = RuntimeException::class)
    fun `invoke should throw exception when repository fails`() = runTest {
        val username = "test"
        val password = "password"

        whenever(authRepository.login(username, password))
            .thenThrow(RuntimeException("Login failed"))

        loginUseCase(username, password)
    }
}
