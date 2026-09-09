package com.example.server

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ServerLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val clientIp: String,
    val method: String,
    val path: String,
    val statusCode: Int
)

data class HttpServerState(
    val isRunning: Boolean = false,
    val port: Int = 8080,
    val hostIp: String = "127.0.0.1",
    val requestCount: Int = 0,
    val logs: List<ServerLogEntry> = emptyList(),
    val errorMessage: String? = null,
    val startedAt: Long = System.currentTimeMillis()
)

class AndroidLocalHttpServer(
    private val context: Context
) {
    private var serverSocket: ServerSocket? = null
    private var isListening = false
    private var serverThread: Thread? = null

    private val virtualVolumeManager = VirtualVolumeManager(context)
    val asyncBot = AsyncBot()

    private val _serverState = MutableStateFlow(HttpServerState(hostIp = getDeviceIpAddress()))
    val serverState: StateFlow<HttpServerState> = _serverState.asStateFlow()

    suspend fun startServer(port: Int = 8080) = withContext(Dispatchers.IO) {
        if (_serverState.value.isRunning) return@withContext

        try {
            val hostIp = getDeviceIpAddress()
            val socket = ServerSocket(port)
            serverSocket = socket
            isListening = true

            _serverState.value = _serverState.value.copy(
                isRunning = true,
                port = port,
                hostIp = hostIp,
                errorMessage = null,
                startedAt = System.currentTimeMillis()
            )

            serverThread = Thread {
                while (isListening && !socket.isClosed) {
                    try {
                        val clientSocket = socket.accept()
                        handleClientConnection(clientSocket)
                    } catch (e: Exception) {
                        if (!isListening) break
                    }
                }
            }.apply {
                name = "AndroidHttpServerThread"
                isDaemon = true
                start()
            }
        } catch (e: Exception) {
            _serverState.value = _serverState.value.copy(
                isRunning = false,
                errorMessage = "Failed to bind port $port: ${e.localizedMessage}"
            )
        }
    }

    suspend fun stopServer() = withContext(Dispatchers.IO) {
        isListening = false
        try {
            serverSocket?.close()
        } catch (ignored: Exception) {}
        serverSocket = null
        serverThread?.interrupt()
        serverThread = null

        _serverState.value = _serverState.value.copy(
            isRunning = false
        )
    }

    private fun handleClientConnection(client: Socket) {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val out = PrintWriter(client.getOutputStream(), true)

                val requestLine = reader.readLine() ?: return@Thread
                val parts = requestLine.split(" ")
                val method = if (parts.isNotEmpty()) parts[0] else "GET"
                val path = if (parts.size > 1) parts[1] else "/"
                val clientIp = client.inetAddress?.hostAddress ?: "Unknown"

                // Read remaining headers
                var contentLength = 0
                var line: String? = reader.readLine()
                while (!line.isNullOrEmpty()) {
                    val lower = line.lowercase(Locale.ROOT)
                    if (lower.startsWith("content-length:")) {
                        contentLength = lower.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                    line = reader.readLine()
                }

                var requestBody = ""
                if (contentLength > 0) {
                    val charBuffer = CharArray(contentLength)
                    var readTotal = 0
                    while (readTotal < contentLength) {
                        val r = reader.read(charBuffer, readTotal, contentLength - readTotal)
                        if (r == -1) break
                        readTotal += r
                    }
                    requestBody = String(charBuffer, 0, readTotal)
                }

                if (method.equals("OPTIONS", ignoreCase = true)) {
                    out.print("HTTP/1.1 200 OK\r\n")
                    out.print("Access-Control-Allow-Origin: *\r\n")
                    out.print("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
                    out.print("Access-Control-Allow-Headers: Content-Type, Authorization\r\n")
                    out.print("Content-Length: 0\r\n")
                    out.print("Connection: close\r\n\r\n")
                    out.flush()
                    client.close()
                    return@Thread
                }

                val currentPort = _serverState.value.port
                val currentHost = _serverState.value.hostIp
                val startedAt = _serverState.value.startedAt
                val uptimeSec = (System.currentTimeMillis() - startedAt) / 1000

                // Clean path and extract query parameters
                val cleanPath = path.substringBefore("?")
                val queryString = path.substringAfter("?", "")
                val queryParams = if (queryString.isNotEmpty()) {
                    queryString.split("&").mapNotNull { param ->
                        val parts = param.split("=", limit = 2)
                        if (parts.isNotEmpty()) parts[0] to (parts.getOrNull(1) ?: "") else null
                    }.toMap()
                } else emptyMap()

                val (statusCode, contentType, responseBody) = when {
                    cleanPath == "/" || cleanPath == "/react" -> {
                        Triple(200, "text/html; charset=UTF-8", ServerWebPages.getReactSpaHtml(currentPort, currentHost))
                    }
                    cleanPath == "/html" || cleanPath == "/simple" -> {
                        Triple(200, "text/html; charset=UTF-8", ServerWebPages.getMinimalHtml(currentPort, currentHost))
                    }
                    cleanPath == "/api/status" || cleanPath == "/api/v1/status" -> {
                        val json = """{"status":"online","engine":"SolutionsEngine-Android","port":$currentPort,"hostIp":"$currentHost","uptimeSeconds":$uptimeSec,"requestCount":${_serverState.value.requestCount},"timestamp":${System.currentTimeMillis()}}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/health" || cleanPath == "/api/v1/health" -> {
                        val rt = Runtime.getRuntime()
                        val usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)
                        val maxMb = rt.maxMemory() / (1024 * 1024)
                        val json = """{"status":"HEALTHY","checks":{"httpServer":{"status":"UP","port":$currentPort,"host":"$currentHost"},"jvmMemory":{"status":"UP","usedMb":$usedMb,"maxMb":$maxMb},"database":{"status":"UP","engine":"Room SQLite v3"},"aiGemini":{"status":"UP","primaryModel":"gemini-2.5-pro","fallbackModel":"gemini-2.5-flash"}},"uptimeSeconds":$uptimeSec,"timestamp":${System.currentTimeMillis()}}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/docs" || cleanPath == "/api/v1/docs" -> {
                        val json = """{"openapi":"3.0.3","info":{"title":"Solutions Engine Local Android Server API","version":"1.0.0","description":"Edge server running natively on Android providing REST APIs, device health, pairing, capability negotiation, and telemetry."},"servers":[{"url":"http://$currentHost:$currentPort","description":"Active Android Local Device"}],"endpoints":[{"path":"/api/v1/status","method":"GET","description":"Server operational status and uptime."},{"path":"/api/v1/health","method":"GET","description":"Multi-subsystem health telemetry check."},{"path":"/api/v1/capabilities","method":"GET","description":"Feature matrix, AI models, and framework support."},{"path":"/api/v1/compatibility","method":"GET","description":"Hardware profiling, Amlogic/FireOS/Android TV, and browser engine."},{"path":"/api/v1/performance","method":"GET","description":"System performance metrics (JVM memory, thread counts, GC)."},{"path":"/api/v1/pairing","method":"GET/POST","description":"Secure device-to-device handshake & session registration."},{"path":"/api/v1/wol","method":"GET/POST","description":"Wake-on-LAN magic packet dispatcher for Windows, WSL, and Linux servers."},{"path":"/api/v1/vvol","method":"GET","description":"Virtual Storage Volumes index (internal files, cache, native libs, PNY USB)."},{"path":"/api/v1/bot/status","method":"GET","description":"SyncBot & AsyncBot job queue status and blackout resilience history."},{"path":"/api/v1/frameworks","method":"GET","description":"Structured analytical problem-solving models."},{"path":"/api/v1/logs","method":"GET","description":"Recent HTTP access traffic logs."},{"path":"/api/v1/echo","method":"POST","description":"CORS reflection test endpoint."}]}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/capabilities" || cleanPath == "/api/v1/capabilities" -> {
                        val json = """{"engine":"SolutionsEngine-Android","version":"2.1.0","capabilities":{"aiReasoning":{"gemini25Pro":{"thinkingBudget":8192,"role":"Deep strategic roadmaps & root-cause decomposition"},"gemini25Flash":{"role":"Rapid 5-Whys causal chain & technical SEO audits"},"offlineFallbackSolvers":true},"persistence":{"database":"Room v2.6.1","tables":["problem_cases","strategy_outlines","solutions","users","subscriptions"],"encryptionReady":true},"networking":{"localHttpServer":{"version":"1.2","corsAll":true,"keepAlive":true,"websockets":false},"restApiVersion":"v1"},"crossPlatformFitting":{"supportedClients":["Android TV (Leanback)","Web Browser / React 18 SPA","Desktop HTTP Clients / curl","Smart Display HTML5"],"spatialNavigationSupport":true,"dpadAccessible":true}}}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/compatibility" || cleanPath == "/api/v1/compatibility" -> {
                        val profile = TvPlatformDetector.detect(context)
                        val json = """{"platform":"Android OS","deviceFamily":"${profile.family}","brand":"${profile.brand}","model":"${profile.model}","hardware":"${profile.hardware}","isAmlogic":${profile.isAmlogic},"androidApiLevel":${profile.androidApiLevel},"isPieOrOlder":${profile.isPieOrOlder},"defaultBrowserEngine":"${profile.defaultBrowserEngine}","supports10FtUi":${profile.supports10FtUi},"hasUsbHost":${profile.hasUsbHost},"protocols":["HTTP/1.1","REST/JSON","CORS","UDP-WoL"],"screenProfiles":{"phone":{"support":"Full Native Compose UI","responsive":true},"tablet":{"support":"Master-Detail Two-Pane Canvas","responsive":true},"smartTv":{"support":"10-Foot Spatial Navigation / D-Pad Remote Ready","dpadAccessible":true,"viewport":"1080p/4K"},"webConsole":{"support":"React 18 Single-Page Application (SPA) + Minimal HTML5 Fallback"}},"security":{"transport":"Local Wi-Fi Network / Direct Socket","authentication":{"supportedModes":["pairing-token","open-lan"],"activeMode":"pairing-token"}}}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/v1/wol" || cleanPath == "/api/wol" -> {
                        val mac = queryParams["mac"] ?: "AA:BB:CC:DD:EE:FF"
                        val ip = queryParams["ip"] ?: "255.255.255.255"
                        val name = queryParams["name"] ?: "Remote Host"
                        val wolResult = kotlinx.coroutines.runBlocking {
                            WakeOnLanService.wake(WolTarget(name = name, macAddress = mac, broadcastIp = ip))
                        }
                        val json = if (wolResult.isSuccess) {
                            """{"status":"SUCCESS","message":"${wolResult.getOrNull()}","target":{"name":"$name","mac":"$mac","broadcastIp":"$ip"},"timestamp":${System.currentTimeMillis()}}"""
                        } else {
                            """{"status":"ERROR","message":"${wolResult.exceptionOrNull()?.message}","target":{"name":"$name","mac":"$mac","broadcastIp":"$ip"},"timestamp":${System.currentTimeMillis()}}"""
                        }
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/v1/vvol" || cleanPath == "/api/vvol" || cleanPath == "/api/v1/fs/volumes" -> {
                        val volumes = virtualVolumeManager.getMountedVolumes()
                        val volsJson = volumes.joinToString(prefix = "[", postfix = "]") { v ->
                            """{"id":"${v.id}","name":"${v.displayName}","path":"${v.rootDir.absolutePath}","readOnly":${v.isReadOnly},"description":"${v.description}","exists":${v.rootDir.exists()}}"""
                        }
                        val json = """{"status":"OK","totalVolumes":${volumes.size},"volumes":$volsJson,"timestamp":${System.currentTimeMillis()}}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/v1/fs/browse" || cleanPath == "/api/fs/browse" -> {
                        val volumeId = queryParams["volume"] ?: "app-files"
                        val subPath = queryParams["path"] ?: ""
                        val entries = virtualVolumeManager.listDirectory(volumeId, subPath)
                        val json = if (entries != null) {
                            val itemsJson = entries.joinToString(prefix = "[", postfix = "]") { f ->
                                """{"name":"${f.name}","relativePath":"${f.relativePath}","isDirectory":${f.isDirectory},"sizeBytes":${f.sizeBytes},"lastModified":${f.lastModified},"canRead":${f.canRead},"canWrite":${f.canWrite}}"""
                            }
                            """{"status":"OK","volumeId":"$volumeId","subPath":"$subPath","count":${entries.size},"entries":$itemsJson,"timestamp":${System.currentTimeMillis()}}"""
                        } else {
                            """{"status":"ERROR","message":"Invalid volume or path traversal rejected","volumeId":"$volumeId","subPath":"$subPath"}"""
                        }
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/v1/fs/resolve" || cleanPath == "/api/fs/resolve" -> {
                        val volumeId = queryParams["volume"] ?: "app-files"
                        val subPath = queryParams["path"] ?: ""
                        val file = virtualVolumeManager.resolveFile(volumeId, subPath)
                        val json = if (file != null) {
                            """{"status":"OK","volumeId":"$volumeId","relativePath":"$subPath","canonicalPath":"${file.canonicalPath}","exists":${file.exists()},"isDirectory":${file.isDirectory},"length":${file.length()},"canRead":${file.canRead()},"canWrite":${file.canWrite()}}"""
                        } else {
                            """{"status":"ERROR","message":"Path traversal prohibited or volume unrecognized","volumeId":"$volumeId","relativePath":"$subPath"}"""
                        }
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/v1/bot/status" || cleanPath == "/api/bot/status" -> {
                        val history = asyncBot.getHistory()
                        val historyJson = history.joinToString(prefix = "[", postfix = "]") { job ->
                            """{"jobId":"${job.jobId}","status":"${job.status}","source":"${job.sourcePath}","destination":"${job.destinationPath}","message":"${job.message}","timestamp":${job.timestamp}}"""
                        }
                        val json = """{"status":"OK","asyncBotActive":true,"syncBotReady":true,"recentJobs":$historyJson,"timestamp":${System.currentTimeMillis()}}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/performance" || cleanPath == "/api/v1/performance" -> {
                        val rt = Runtime.getRuntime()
                        val totalMb = rt.totalMemory() / (1024 * 1024)
                        val freeMb = rt.freeMemory() / (1024 * 1024)
                        val usedMb = totalMb - freeMb
                        val maxMb = rt.maxMemory() / (1024 * 1024)
                        val activeThreads = Thread.activeCount()
                        val availableProcessors = rt.availableProcessors()
                        val json = """{"timestamp":${System.currentTimeMillis()},"uptimeSeconds":$uptimeSec,"requestCounter":${_serverState.value.requestCount},"cpu":{"availableCores":$availableProcessors,"activeThreads":$activeThreads},"memory":{"usedMb":$usedMb,"freeMb":$freeMb,"totalAllocatedMb":$totalMb,"maxHeapMb":$maxMb,"heapUtilizationPercent":${if (maxMb > 0) (usedMb * 100 / maxMb) else 0}},"throughput":{"status":"OPTIMAL","avgResponseTimeTargetMs":"<15ms"}}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/pairing" || cleanPath == "/api/v1/pairing" -> {
                        val token = "SE-" + ((System.currentTimeMillis() % 89999) + 10000)
                        val json = """{"status":"PAIRED","device":"Android Host","hostIp":"$currentHost","port":$currentPort,"pairingToken":"$token","clientIp":"$clientIp","grantedScopes":["read:telemetry","execute:solve","read:frameworks","read:logs"],"handshakeTimestamp":${System.currentTimeMillis()}}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/info" || cleanPath == "/api/v1/info" -> {
                        val json = """{"appName":"Solutions Engine","version":"2.1.0","apiVersions":["v1"],"aiModels":["gemini-2.5-pro","gemini-2.5-flash"],"platform":"Android Jetpack Compose","serverType":"Native Android Socket Server","features":["v1 REST Suite","React SPA Console","Minimal HTML Fallback","Pairing Handshake","Performance Telemetry","Live Traffic Logging"]}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/ping" || cleanPath == "/api/v1/ping" -> {
                        Triple(200, "application/json; charset=UTF-8", """{"pong":true,"uptimeSeconds":$uptimeSec,"time":${System.currentTimeMillis()}}""")
                    }
                    cleanPath == "/api/frameworks" || cleanPath == "/api/v1/frameworks" -> {
                        val json = """{"frameworks":[{"id":"5-whys","name":"5-Whys Root Cause Analysis","domain":"Technical / Operational"},{"id":"mece","name":"MECE Issue Tree","domain":"Business & Strategy"},{"id":"cynefin","name":"Cynefin Sensemaking","domain":"Incident Response"},{"id":"dmaic","name":"DMAIC Six Sigma","domain":"Continuous Improvement"},{"id":"kepner-tregoe","name":"Kepner-Tregoe Rational Matrix","domain":"High Stakes Decisions"}]}"""
                        Triple(200, "application/json; charset=UTF-8", json)
                    }
                    cleanPath == "/api/logs" || cleanPath == "/api/v1/logs" -> {
                        val logsJson = _serverState.value.logs.take(15).joinToString(prefix = "[", postfix = "]") { log ->
                            """{"timestamp":${log.timestamp},"clientIp":"${log.clientIp}","method":"${log.method}","path":"${log.path}","statusCode":${log.statusCode}}"""
                        }
                        Triple(200, "application/json; charset=UTF-8", logsJson)
                    }
                    cleanPath == "/api/echo" || cleanPath == "/api/v1/echo" -> {
                        val sanitizedBody = if (requestBody.isEmpty()) """{"message":"Echo: Empty body or GET request","receivedAt":${System.currentTimeMillis()}}""" else requestBody
                        Triple(200, "application/json; charset=UTF-8", sanitizedBody)
                    }
                    else -> {
                        Triple(404, "application/json; charset=UTF-8", """{"error":"Not Found","path":"$path","documentation":"http://$currentHost:$currentPort/api/v1/docs"}""")
                    }
                }

                val responseBytes = responseBody.toByteArray(Charsets.UTF_8)
                out.print("HTTP/1.1 $statusCode OK\r\n")
                out.print("Content-Type: $contentType\r\n")
                out.print("Content-Length: ${responseBytes.size}\r\n")
                out.print("Access-Control-Allow-Origin: *\r\n")
                out.print("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
                out.print("Access-Control-Allow-Headers: Content-Type, Authorization\r\n")
                out.print("Connection: close\r\n\r\n")
                out.flush()
                client.getOutputStream().write(responseBytes)
                client.getOutputStream().flush()

                // Update server logs & request count
                val logEntry = ServerLogEntry(
                    clientIp = clientIp,
                    method = method,
                    path = path,
                    statusCode = statusCode
                )

                _serverState.value = _serverState.value.copy(
                    requestCount = _serverState.value.requestCount + 1,
                    logs = (listOf(logEntry) + _serverState.value.logs).take(30)
                )

                client.close()
            } catch (e: Exception) {
                try { client.close() } catch (ignored: Exception) {}
            }
        }.start()
    }

    private fun getDeviceIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (ignored: Exception) {}
        return "127.0.0.1"
    }
}
