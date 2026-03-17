package com.ithing.mobile.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveToken and getToken should return same value`() = runTest(testDispatcher) {
        dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File.createTempFile("test", ".preferences_pb") }
        )
        sessionManager = SessionManager(dataStore)

        val token = "test_token"

        sessionManager.saveToken(token)
        testScheduler.advanceUntilIdle()

        val storedToken = sessionManager.getToken()

        assertEquals(token, storedToken)
    }

    @Test
    fun `clearSession should remove token`() = runTest(testDispatcher) {
        dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File.createTempFile("test", ".preferences_pb") }
        )
        sessionManager = SessionManager(dataStore)

        val token = "test_token"

        sessionManager.saveToken(token)
        testScheduler.advanceUntilIdle()

        sessionManager.clearSession()
        testScheduler.advanceUntilIdle()

        val storedToken = sessionManager.getToken()

        assertNull(storedToken)
    }
}
