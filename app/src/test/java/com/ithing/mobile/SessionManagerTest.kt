package com.ithing.mobile.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var sessionManager: SessionManager
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        testScope = TestScope(testDispatcher)

        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File.createTempFile("test", ".preferences_pb") }
        )

        sessionManager = SessionManager(dataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveToken and getToken should return same value`() = runTest {
        val token = "test_token"

        sessionManager.saveToken(token)
        testScheduler.advanceUntilIdle()

        val storedToken = sessionManager.getToken()

        assertEquals(token, storedToken)
    }

    @Test
    fun `clearSession should remove token`() = runTest {
        val token = "test_token"

        sessionManager.saveToken(token)
        testScheduler.advanceUntilIdle()

        sessionManager.clearSession()
        testScheduler.advanceUntilIdle()

        val storedToken = sessionManager.getToken()

        assertNull(storedToken)
    }
}
