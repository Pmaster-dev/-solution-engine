package com.example.server

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.UUID

// ==========================================
// 1. Hardware & Platform Profiler (TV / Amlogic / FireOS / Pie)
// ==========================================

enum class DeviceFamily {
    FIRE_TV,
    ANDROID_TV,
    STANDARD_ANDROID,
    EMULATOR_OR_WEB
}

data class HardwareProfile(
    val family: DeviceFamily,
    val brand: String,
    val model: String,
    val hardware: String,
    val isAmlogic: Boolean,
    val androidApiLevel: Int,
    val isPieOrOlder: Boolean,
    val defaultBrowserEngine: String,
    val supports10FtUi: Boolean,
    val hasUsbHost: Boolean
)

object TvPlatformDetector {
    fun detect(context: Context): HardwareProfile {
        val brand = Build.BRAND.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        val board = Build.BOARD.lowercase()

        val isAmazon = brand.contains("amazon") || manufacturer.contains("amazon")
        val isAmlogic = hardware.contains("amlogic") || hardware.contains("meson") || board.contains("amlogic")

        val pm = context.packageManager
        val hasLeanback = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
                pm.hasSystemFeature("amazon.hardware.fire_tv")
        val hasUsb = pm.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

        val family = when {
            isAmazon -> DeviceFamily.FIRE_TV
            hasLeanback -> DeviceFamily.ANDROID_TV
            Build.FINGERPRINT.contains("generic") -> DeviceFamily.EMULATOR_OR_WEB
            else -> DeviceFamily.STANDARD_ANDROID
        }

        val browserEngine = when {
            isAmazon -> "Amazon Silk (Blink-based)"
            Build.VERSION.SDK_INT <= 28 -> "GeckoView (Bundled modern Firefox engine recommended for Android 9 Pie)"
            else -> "Android System WebView / GeckoView"
        }

        return HardwareProfile(
            family = family,
            brand = Build.BRAND,
            model = Build.MODEL,
            hardware = Build.HARDWARE,
            isAmlogic = isAmlogic,
            androidApiLevel = Build.VERSION.SDK_INT,
            isPieOrOlder = Build.VERSION.SDK_INT <= 28,
            defaultBrowserEngine = browserEngine,
            supports10FtUi = hasLeanback || isAmazon,
            hasUsbHost = hasUsb
        )
    }
}

// ==========================================
// 2. Wake-on-LAN (WoL) Engine (Windows + WSL + Linux Server)
// ==========================================

data class WolTarget(
    val name: String,
    val macAddress: String,
    val broadcastIp: String = "255.255.255.255",
    val port: Int = 9
)

object WakeOnLanService {
    suspend fun wake(target: WolTarget): Result<String> = withContext(Dispatchers.IO) {
        try {
            val macBytes = parseMacAddress(target.macAddress)
            val bytes = ByteArray(6 + 16 * macBytes.size)
            for (i in 0 until 6) {
                bytes[i] = 0xFF.toByte()
            }
            for (i in 6 until bytes.size step macBytes.size) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
            }

            val address = InetAddress.getByName(target.broadcastIp)
            val packet = DatagramPacket(bytes, bytes.size, address, target.port)

            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.send(packet)
            }
            Result.success("WoL magic packet successfully transmitted to ${target.name} (${target.macAddress})")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseMacAddress(macStr: String): ByteArray {
        val cleanMac = macStr.replace(":", "").replace("-", "").trim()
        require(cleanMac.length == 12) { "Invalid MAC address: $macStr (expected 12 hexadecimal characters)" }
        val bytes = ByteArray(6)
        for (i in 0 until 6) {
            bytes[i] = cleanMac.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return bytes
    }
}

// ==========================================
// 3. Virtual Volume (VVOL) Manager
// ==========================================

data class VirtualVolume(
    val id: String,
    val displayName: String,
    val rootDir: File,
    val isReadOnly: Boolean,
    val description: String
)

data class FileEntry(
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val canRead: Boolean,
    val canWrite: Boolean
)

class VirtualVolumeManager(private val context: Context) {
    fun getMountedVolumes(): List<VirtualVolume> {
        val list = mutableListOf(
            VirtualVolume(
                id = "app-files",
                displayName = "Application Files",
                rootDir = context.filesDir,
                isReadOnly = false,
                description = "Internal sandbox files and persistent application data"
            ),
            VirtualVolume(
                id = "app-cache",
                displayName = "Application Cache",
                rootDir = context.cacheDir,
                isReadOnly = false,
                description = "Temporary scratchpad cache"
            ),
            VirtualVolume(
                id = "app-native-libs",
                displayName = "Native Shared Libraries (.so)",
                rootDir = File(context.applicationInfo.nativeLibraryDir),
                isReadOnly = true,
                description = "Packaged JNI native binaries (.so)"
            )
        )

        // External files / USB mounts
        val extFiles = context.getExternalFilesDir(null)
        if (extFiles != null) {
            list.add(
                VirtualVolume(
                    id = "ext-storage",
                    displayName = "External App Storage",
                    rootDir = extFiles,
                    isReadOnly = false,
                    description = "Primary external/removable storage directory"
                )
            )
        }

        // Secondary external storage (e.g. PNY USB drives or SD cards)
        val extDirs = context.getExternalFilesDirs(null)
        if (extDirs != null && extDirs.size > 1) {
            extDirs.filterNotNull().drop(1).forEachIndexed { index, file ->
                list.add(
                    VirtualVolume(
                        id = "pny-usb-$index",
                        displayName = "Removable USB / OTG ($index)",
                        rootDir = file,
                        isReadOnly = !file.canWrite(),
                        description = "Connected PNY external USB / SD storage mount"
                    )
                )
            }
        }

        return list
    }

    fun resolveFile(volumeId: String, relativePath: String): File? {
        val volume = getMountedVolumes().find { it.id == volumeId } ?: return null
        val baseDir = volume.rootDir
        val target = if (relativePath.isBlank() || relativePath == "/") baseDir else File(baseDir, relativePath).canonicalFile

        // Path traversal guard
        if (!target.path.startsWith(baseDir.canonicalPath)) {
            return null
        }
        return target
    }

    fun listDirectory(volumeId: String, relativeSubPath: String = ""): List<FileEntry>? {
        val targetDir = resolveFile(volumeId, relativeSubPath) ?: return null
        if (!targetDir.exists() || !targetDir.isDirectory) return null
        val volume = getMountedVolumes().find { it.id == volumeId } ?: return null
        val baseDirPath = volume.rootDir.canonicalPath

        return targetDir.listFiles()?.map { f ->
            val relPath = f.canonicalPath.removePrefix(baseDirPath).removePrefix(File.separator)
            FileEntry(
                name = f.name,
                relativePath = relPath,
                isDirectory = f.isDirectory,
                sizeBytes = if (f.isFile) f.length() else 0L,
                lastModified = f.lastModified(),
                canRead = f.canRead(),
                canWrite = f.canWrite()
            )
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
    }
}

// ==========================================
// 4. EdgeBot Engine: SyncBot & AsyncBot
// ==========================================

object SyncBot {
    sealed class Result {
        data class Success(val bytesWritten: Long, val path: String, val durationMs: Long) : Result()
        data class Failure(val error: String) : Result()
    }

    fun atomicCopy(source: File, destination: File): Result {
        if (!source.exists()) return Result.Failure("Source file does not exist: ${source.path}")
        val start = System.currentTimeMillis()
        return try {
            destination.parentFile?.mkdirs()
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var read: Int
                    var total = 0L
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        total += read
                    }
                    // Hardware sync to ensure NAND/USB persistence
                    output.fd.sync()
                    Result.Success(total, destination.absolutePath, System.currentTimeMillis() - start)
                }
            }
        } catch (e: Exception) {
            Result.Failure(e.localizedMessage ?: "I/O write exception")
        }
    }
}

data class BotSyncJob(
    val jobId: String = UUID.randomUUID().toString().take(8),
    val status: String,
    val sourcePath: String,
    val destinationPath: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AsyncBot(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val _events = MutableSharedFlow<BotSyncJob>(replay = 20)
    val events: SharedFlow<BotSyncJob> = _events.asSharedFlow()

    private val jobHistory = mutableListOf<BotSyncJob>()

    fun getHistory(): List<BotSyncJob> = synchronized(jobHistory) { jobHistory.toList() }

    fun scheduleBackgroundSync(
        source: File,
        destination: File,
        maxRetries: Int = 3
    ): String {
        val jobId = "job-" + UUID.randomUUID().toString().take(6)
        val initialJob = BotSyncJob(
            jobId = jobId,
            status = "QUEUED",
            sourcePath = source.path,
            destinationPath = destination.path,
            message = "Scheduled in AsyncBot queue (blackout resilient)"
        )
        recordJob(initialJob)

        scope.launch {
            var attempt = 0
            var completed = false
            while (attempt < maxRetries && !completed) {
                attempt++
                val copyResult = SyncBot.atomicCopy(source, destination)
                when (copyResult) {
                    is SyncBot.Result.Success -> {
                        completed = true
                        val done = BotSyncJob(
                            jobId = jobId,
                            status = "COMPLETED",
                            sourcePath = source.path,
                            destinationPath = destination.path,
                            message = "Transferred ${copyResult.bytesWritten} bytes in ${copyResult.durationMs}ms with hardware fd.sync()"
                        )
                        recordJob(done)
                    }
                    is SyncBot.Result.Failure -> {
                        if (attempt >= maxRetries) {
                            val failed = BotSyncJob(
                                jobId = jobId,
                                status = "FAILED",
                                sourcePath = source.path,
                                destinationPath = destination.path,
                                message = "Exhausted $maxRetries attempts: ${copyResult.error}"
                            )
                            recordJob(failed)
                        } else {
                            val retrying = BotSyncJob(
                                jobId = jobId,
                                status = "RETRYING",
                                sourcePath = source.path,
                                destinationPath = destination.path,
                                message = "Attempt $attempt failed: ${copyResult.error}. Retrying..."
                            )
                            recordJob(retrying)
                            delay(2000)
                        }
                    }
                }
            }
        }
        return jobId
    }

    private fun recordJob(job: BotSyncJob) {
        synchronized(jobHistory) {
            jobHistory.add(0, job)
            if (jobHistory.size > 50) jobHistory.removeLast()
        }
        scope.launch {
            _events.emit(job)
        }
    }
}
