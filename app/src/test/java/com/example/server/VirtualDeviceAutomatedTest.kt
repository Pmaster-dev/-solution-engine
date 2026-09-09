package com.example.server

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Automated Virtual Device Debug & Test Suite
 * Validates TV profiling, WoL packet construction, VVOL sandboxing, and Bot sync.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VirtualDeviceAutomatedTest {

    private lateinit var context: Context
    private lateinit var vvolManager: VirtualVolumeManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        vvolManager = VirtualVolumeManager(context)
    }

    @Test
    fun testTvPlatformDetectionDefaults() {
        val profile = TvPlatformDetector.detect(context)
        assertNotNull(profile)
        assertNotNull(profile.family)
        assertNotNull(profile.defaultBrowserEngine)
        // Verify detection properties evaluate without crashing on virtual device
        assertTrue(profile.androidApiLevel > 0)
    }

    @Test
    fun testWakeOnLanPacketConstruction() = runBlocking {
        // Valid MAC address formats
        val targetColon = WolTarget(name = "Windows PC", macAddress = "00:11:22:33:44:55")
        val resultColon = WakeOnLanService.wake(targetColon)
        assertTrue("WoL should succeed on valid colon MAC", resultColon.isSuccess)

        val targetHyphen = WolTarget(name = "Linux Server", macAddress = "AA-BB-CC-DD-EE-FF")
        val resultHyphen = WakeOnLanService.wake(targetHyphen)
        assertTrue("WoL should succeed on valid hyphen MAC", resultHyphen.isSuccess)

        // Invalid MAC address should fail gracefully
        val invalidTarget = WolTarget(name = "Bad Device", macAddress = "12:34:56")
        val resultInvalid = WakeOnLanService.wake(invalidTarget)
        assertTrue("WoL should fail gracefully on truncated MAC", resultInvalid.isFailure)
    }

    @Test
    fun testVirtualVolumeMountsAndSandboxing() {
        val volumes = vvolManager.getMountedVolumes()
        assertTrue("At least 3 core virtual volumes should be mounted", volumes.size >= 3)
        assertTrue("app-files volume must be registered", volumes.any { it.id == "app-files" })
        assertTrue("app-cache volume must be registered", volumes.any { it.id == "app-cache" })
        assertTrue("app-native-libs volume must be registered", volumes.any { it.id == "app-native-libs" })

        // Path Traversal Attack Test: Attempting to break out of sandbox must return null
        val maliciousAttempt = vvolManager.resolveFile("app-files", "../../etc/passwd")
        assertNull("Path traversal attack must be blocked and resolve to null", maliciousAttempt)

        val maliciousAttempt2 = vvolManager.resolveFile("app-cache", "../../../data/system")
        assertNull("Path traversal attack must be blocked", maliciousAttempt2)

        // Valid safe relative resolution
        val safeFile = vvolManager.resolveFile("app-files", "telemetry/report.log")
        assertNotNull("Safe sub-path must resolve properly", safeFile)
        assertTrue(safeFile!!.path.startsWith(context.filesDir.canonicalPath))

        // Directory listing test
        val entries = vvolManager.listDirectory("app-files", "")
        assertNotNull("Root of app-files must be listable", entries)
    }

    @Test
    fun testSyncBotAtomicCopyAndVerification() {
        val sourceFile = File(context.cacheDir, "test_source.bin")
        sourceFile.writeBytes("SOLUTIONS_ENGINE_TELEMETRY_PAYLOAD_TEST".toByteArray())

        val destFile = File(context.filesDir, "test_backup/test_dest.bin")

        val result = SyncBot.atomicCopy(sourceFile, destFile)
        assertTrue("SyncBot atomic copy must succeed", result is SyncBot.Result.Success)

        val success = result as SyncBot.Result.Success
        assertEquals(sourceFile.length(), success.bytesWritten)
        assertTrue(destFile.exists())
        assertEquals("SOLUTIONS_ENGINE_TELEMETRY_PAYLOAD_TEST", destFile.readText())

        // Clean up
        sourceFile.delete()
        destFile.delete()
    }

    @Test
    fun testAsyncBotResilienceQueue() {
        val asyncBot = AsyncBot()
        val sourceFile = File(context.cacheDir, "async_test_source.txt")
        sourceFile.writeText("Async blackout resilient buffer")

        val destFile = File(context.filesDir, "async_backup.txt")

        val jobId = asyncBot.scheduleBackgroundSync(sourceFile, destFile, maxRetries = 2)
        assertNotNull(jobId)
        assertTrue(jobId.startsWith("job-"))

        val history = asyncBot.getHistory()
        assertTrue("AsyncBot history should record scheduled jobs", history.isNotEmpty())
        assertEquals(jobId, history.first().jobId)

        // Clean up
        sourceFile.delete()
    }
}
