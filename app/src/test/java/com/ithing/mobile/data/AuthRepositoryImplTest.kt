package com.ithing.mobile.data

import com.ithing.mobile.core.session.SessionManager
import com.ithing.mobile.data.local.datastore.AuthDataStore
import com.ithing.mobile.data.remote.api.AuthApiService
import com.ithing.mobile.data.remote.dto.login.LoginDataDto
import com.ithing.mobile.data.remote.dto.login.LoginResponseDto
import com.ithing.mobile.data.remote.dto.login.UserDto
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
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        authApiService = mock()
        authDataStore = mock()
        sessionManager = mock()

        repository = AuthRepositoryImpl(
            authApiService,
            authDataStore,
            sessionManager
        )
    }

    @Test
    fun `login should call API and save token`() = runTest {
        val fakeToken = "fake_jwt_token"

        val fakeUser = UserDto(
            id = "test",
            name = "Test",
            address = "Address",
            phone = "123",
            organization = "Org",
            context = "User",
            admin = false,
            superadmin = true,
            oemadmin = false,
            customeradmin = false,
            createdAt = 0L,
            updatedAt = 0L,
            lastPasswordChange = 0L,
            password = ""
        )

        val response = LoginResponseDto(
            data = LoginDataDto(
                token = fakeToken,
                user = fakeUser,
                oemLogo = ""
            )
        )

        whenever(authApiService.loginAttempt(any()))
            .thenReturn(response)

        repository.login("test", "password")

        verify(authApiService).loginAttempt(any())
        verify(sessionManager).saveToken(fakeToken)
        verify(sessionManager).saveUserRole(any())
    }

    @Test
    fun `logout should clear session`() = runTest {
        repository.logout()

        verify(sessionManager).clearSession()
    }
}

