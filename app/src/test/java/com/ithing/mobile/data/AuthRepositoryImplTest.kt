package com.ithing.mobile.data

import com.ithing.mobile.data.local.datastore.AuthDataStore
import com.ithing.mobile.data.remote.api.AuthApiService
import com.ithing.mobile.data.remote.dto.login.LoginResponseDto
import com.ithing.mobile.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    private lateinit var authApiService: AuthApiService
    private lateinit var authDataStore: AuthDataStore
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        authApiService = mock()
        authDataStore = mock()
        repository = AuthRepositoryImpl(authApiService, authDataStore)
    }

    @Test
    fun `login should call API and save token`() = runTest {
        val username = "test"
        val password = "password"
        val fakeToken = "fake_jwt_token"

        val response = LoginResponseDto(
            token = fakeToken,
            user = mock(), // adjust if required
            oemLogo = ""
        )

        whenever(authApiService.loginAttempt(any()))
            .thenReturn(response)

        repository.login(username, password)

        verify(authApiService).loginAttempt(any())
        verify(authDataStore).saveAccessToken(fakeToken)
    }

    @Test(expected = RuntimeException::class)
    fun `login should throw exception if API fails`() = runTest {
        val username = "test"
        val password = "password"

        whenever(authApiService.loginAttempt(any()))
            .thenThrow(RuntimeException("API failed"))

        repository.login(username, password)

        verify(authDataStore, never()).saveAccessToken(any())
    }

    @Test
    fun `logout should clear datastore`() = runTest {
        repository.logout()

        verify(authDataStore).clear()
    }
}
