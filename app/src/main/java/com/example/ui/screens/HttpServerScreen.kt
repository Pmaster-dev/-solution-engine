package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.server.HttpServerState
import com.example.server.ServerLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HttpServerScreen(
    serverState: HttpServerState,
    onStartServer: (Int) -> Unit,
    onStopServer: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var portInput by remember { mutableStateOf(serverState.port.toString()) }
    val serverUrl = "http://${serverState.hostIp}:${serverState.port}"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Hero Status Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = if (serverState.isRunning) {
                                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                } else {
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                }
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (serverState.isRunning) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Dns,
                                            contentDescription = null,
                                            tint = if (serverState.isRunning) Color(0xFF10B981) else Color.LightGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "HTTP://server",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = if (serverState.isRunning) "NATIVE ANDROID DAEMON (ONLINE)" else "DAEMON OFFLINE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (serverState.isRunning) Color(0xFF34D399) else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Status Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (serverState.isRunning) Color(0xFF065F46) else Color(0xFF334155),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = if (serverState.isRunning) "ACTIVE" else "STOPPED",
                                    color = if (serverState.isRunning) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // URL Display Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF090D16),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = serverUrl,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(serverUrl))
                                        },
                                        modifier = Modifier.size(28.dp).testTag("copy_server_url_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy URL",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF6366F1).copy(alpha = 0.25f),
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString("$serverUrl/"))
                                        }
                                    ) {
                                        Text(
                                            text = "⚛️ React SPA (/)",
                                            color = Color(0xFFA5B4FC),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.25f),
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString("$serverUrl/html"))
                                        }
                                    ) {
                                        Text(
                                            text = "📄 Plain HTML (/html)",
                                            color = Color(0xFF6EE7B7),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Control Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!serverState.isRunning) {
                                OutlinedTextField(
                                    value = portInput,
                                    onValueChange = { portInput = it.filter { ch -> ch.isDigit() }.take(5) },
                                    label = { Text("Port", color = Color.LightGray) },
                                    singleLine = true,
                                    modifier = Modifier.width(100.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )

                                Button(
                                    onClick = {
                                        val port = portInput.toIntOrNull() ?: 8080
                                        onStartServer(port)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .testTag("start_server_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start HTTP Server", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            } else {
                                Button(
                                    onClick = onStopServer,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("stop_server_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Stop HTTP Server", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        if (serverState.errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = serverState.errorMessage,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lan, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Host IP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = serverState.hostIp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Total Requests", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${serverState.requestCount} served",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // iOS & Apple Ecosystem Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🍎", fontSize = 18.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "iOS Client & Apple Ecosystem",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Zero-Install Safari PWA & Siri Automation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "iOS 16-18 Ready",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Solutions Engine runs on any iPhone or iPad without needing Xcode or the App Store:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text("1. ", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 12.sp)
                            Text(
                                text = "Open Safari on iPhone/iPad: $serverUrl",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            Text("2. ", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 12.sp)
                            Text(
                                text = "Tap the Share icon (box with up arrow) & select 'Add to Home Screen'.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            Text("3. ", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 12.sp)
                            Text(
                                text = "Launch standalone PWA with notch support and full Room DB sync.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(serverUrl))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("copy_ios_safari_url_button")
                        ) {
                            Text("Copy Safari Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val siriCurl = "curl -X POST $serverUrl/api/v1/solve -H \"Content-Type: application/json\" -d '{\"problem\": \"Database latency spike\", \"framework\": \"5-whys\"}'"
                                clipboardManager.setText(AnnotatedString(siriCurl))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("copy_ios_shortcuts_curl_button")
                        ) {
                            Text("Copy Siri cURL", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Available API Endpoints
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Exposed REST Endpoints",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    EndpointItem(
                        method = "GET",
                        path = "/ (or /react)",
                        description = "Full-featured React 18 SPA Console with Live Telemetry, Polling, iOS PWA & REST Tester."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/manifest.json & /apple-touch-icon.png",
                        description = "Web App Manifest and 180x180 PNG icon for iOS home screen installation."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET / POST",
                        path = "/api/v1/solutions",
                        description = "Query or insert solutions into the host Room SQLite database from iOS or web."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "POST",
                        path = "/api/v1/solve",
                        description = "Apple Shortcuts and Siri webhook for instantaneous 5-Whys problem deconstruction."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/ios/shortcut",
                        description = "Apple Shortcuts automation configuration and Siri trigger instructions."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/html (or /simple)",
                        description = "Ultra-lightweight zero-dependency HTML dashboard with iOS PWA support."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/health",
                        description = "Multi-subsystem health check (HTTP server, JVM memory, Room DB, Gemini AI)."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/capabilities",
                        description = "Engine capabilities, thinking token budgets, and client profiles."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/compatibility",
                        description = "Device matrix fitting across Phone, Tablet, Smart TV (D-Pad), and Web."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/performance",
                        description = "Real-time CPU cores, thread counts, and JVM heap utilization metrics."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/pairing",
                        description = "Secure device-to-device handshake & pairing token generation."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/docs",
                        description = "Machine-readable OpenAPI 3.0.3 specification JSON."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/status",
                        description = "Health status, uptime seconds & active port telemetry."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/frameworks",
                        description = "Supported problem frameworks (5-Whys, MECE, Cynefin, DMAIC, Kepner-Tregoe)."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/logs",
                        description = "JSON stream of recent HTTP access logs."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/wol?mac=AA:BB:CC:DD:EE:FF",
                        description = "Wake-on-LAN magic packet UDP broadcaster for Windows, WSL & Linux servers."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/vvol",
                        description = "Virtual Storage Volumes index (internal files, cache, .so libs, PNY USB/OTG)."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "GET",
                        path = "/api/v1/bot/status",
                        description = "AsyncBot resilience queue status and SyncBot atomic verification history."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EndpointItem(
                        method = "POST",
                        path = "/api/v1/echo",
                        description = "JSON payload echo test endpoint with CORS support."
                    )
                }
            }
        }

        // Live Server Access Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Access Logs (${serverState.logs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (serverState.logs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (serverState.isRunning) "Listening for incoming HTTP requests on port ${serverState.port}..." else "Start the server to view real-time HTTP traffic.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(serverState.logs) { log ->
                ServerLogCard(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun EndpointItem(method: String, path: String, description: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF10B981).copy(alpha = 0.2f)
            ) {
                Text(
                    text = method,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = path,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ServerLogCard(log: ServerLogEntry) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(log.timestamp))

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (log.statusCode == 200) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${log.statusCode}",
                        color = if (log.statusCode == 200) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${log.method} ${log.path}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "From ${log.clientIp}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = formattedTime,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
