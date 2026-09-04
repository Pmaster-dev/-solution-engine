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
    val errorMessage: String? = null
)

class AndroidLocalHttpServer(
    private val context: Context
) {
    private var serverSocket: ServerSocket? = null
    private var isListening = false
    private var serverThread: Thread? = null

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
                errorMessage = null
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
                var line: String? = reader.readLine()
                while (!line.isNullOrEmpty()) {
                    line = reader.readLine()
                }

                val responseBody = when {
                    path == "/" -> {
                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="utf-8">
                            <title>Solutions Engine Local Server</title>
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <style>
                                body { font-family: system-ui, -apple-system, sans-serif; background: #0f172a; color: #f8fafc; padding: 2rem; }
                                .card { background: #1e293b; border-radius: 12px; padding: 1.5rem; max-width: 600px; margin: auto; border: 1px solid #334155; }
                                h1 { color: #6366f1; font-size: 1.5rem; margin-top: 0; }
                                .badge { background: #10b981; color: #000; padding: 4px 8px; border-radius: 6px; font-size: 0.8rem; font-weight: bold; }
                                code { background: #090d16; padding: 2px 6px; border-radius: 4px; color: #38bdf8; }
                                a { color: #818cf8; }
                            </style>
                        </head>
                        <body>
                            <div class="card">
                                <h1>🚀 Solutions Engine Local HTTP Server</h1>
                                <p><span class="badge">ONLINE</span> Running natively on Android OS.</p>
                                <hr style="border: 0; border-top: 1px solid #334155; margin: 1rem 0;">
                                <p><strong>Endpoints:</strong></p>
                                <ul>
                                    <li><code>GET /api/status</code> &mdash; <a href="/api/status">Server Telemetry JSON</a></li>
                                    <li><code>GET /api/info</code> &mdash; <a href="/api/info">Application Engine Details</a></li>
                                </ul>
                            </div>
                        </body>
                        </html>
                        """.trimIndent()
                    }
                    path == "/api/status" -> {
                        """{"status":"online","engine":"SolutionsEngine-Android","port":${_serverState.value.port},"timestamp":${System.currentTimeMillis()}}"""
                    }
                    path == "/api/info" -> {
                        """{"appName":"Solutions Engine","aiModels":["gemini-2.5-pro","gemini-2.5-flash"],"platform":"Android Jetpack Compose"}"""
                    }
                    else -> {
                        """{"error":"Not Found","path":"$path"}"""
                    }
                }

                val statusCode = if (path == "/" || path.startsWith("/api/")) 200 else 404
                val contentType = if (path == "/") "text/html; charset=UTF-8" else "application/json; charset=UTF-8"

                out.print("HTTP/1.1 $statusCode OK\r\n")
                out.print("Content-Type: $contentType\r\n")
                out.print("Content-Length: ${responseBody.toByteArray().size}\r\n")
                out.print("Access-Control-Allow-Origin: *\r\n")
                out.print("Connection: close\r\n\r\n")
                out.print(responseBody)
                out.flush()

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
