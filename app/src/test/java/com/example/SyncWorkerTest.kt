package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.data.repository.SyncWorker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = ConnectKarApplication::class)
class SyncWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSyncWorker_instantiationAndBasicExecutionWithNoFirestore() = runBlocking {
        // Instantiate using the test worker builder
        val worker = TestListenableWorkerBuilder<SyncWorker>(context).build()
        
        // Since FirebaseManager.firestore is null in JVM test environment,
        // doWork() should return retry()
        val result = worker.doWork()
        
        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
