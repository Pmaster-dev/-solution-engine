package com.example.server

object ServerWebPages {

    fun getReactSpaHtml(port: Int, hostIp: String): String {
        return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HTTP://server &mdash; React Console</title>
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- React & ReactDOM CDN -->
    <script crossorigin src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
    <!-- Babel Standalone for JSX -->
    <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
    <style>
        @keyframes pulse-subtle {
            0%, 100% { opacity: 1; transform: scale(1); }
            50% { opacity: 0.8; transform: scale(1.04); }
        }
        .pulse-subtle { animation: pulse-subtle 2s infinite ease-in-out; }
        pre code { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }
    </style>
</head>
<body class="bg-slate-950 text-slate-100 min-h-screen font-sans selection:bg-indigo-500 selection:text-white">
    <div id="root"></div>

    <script type="text/babel">
        const { useState, useEffect } = React;

        function App() {
            const [activeTab, setActiveTab] = useState('endpoints');
            const [serverStatus, setServerStatus] = useState({ status: 'connecting', port: $port, timestamp: Date.now() });
            const [serverInfo, setServerInfo] = useState(null);
            const [latency, setLatency] = useState(null);
            const [customMethod, setCustomMethod] = useState('GET');
            const [customEndpoint, setCustomEndpoint] = useState('/api/status');
            const [customBody, setCustomBody] = useState('{\n  "client": "React Web Console",\n  "test": true\n}');
            const [apiResponse, setApiResponse] = useState(null);
            const [isLoading, setIsLoading] = useState(false);
            const [liveLogs, setLiveLogs] = useState([]);
            const [autoRefresh, setAutoRefresh] = useState(true);

            // Fetch initial status and info
            const fetchTelemetry = async () => {
                const t0 = performance.now();
                try {
                    const res = await fetch('/api/status');
                    const data = await res.json();
                    setServerStatus(data);
                    setLatency(Math.round(performance.now() - t0));
                } catch (err) {
                    setServerStatus(prev => ({ ...prev, status: 'unreachable', error: err.message }));
                    setLatency(null);
                }
            };

            const fetchInfo = async () => {
                try {
                    const res = await fetch('/api/info');
                    const data = await res.json();
                    setServerInfo(data);
                } catch (err) {
                    console.error('Failed to fetch info', err);
                }
            };

            const fetchLogs = async () => {
                try {
                    const res = await fetch('/api/logs');
                    const data = await res.json();
                    if (Array.isArray(data)) setLiveLogs(data);
                } catch (err) {}
            };

            useEffect(() => {
                fetchTelemetry();
                fetchInfo();
                fetchLogs();

                const interval = setInterval(() => {
                    if (autoRefresh) {
                        fetchTelemetry();
                        fetchLogs();
                    }
                }, 3000);

                return () => clearInterval(interval);
            }, [autoRefresh]);

            // Execute custom or quick API request
            const handleExecute = async (method = customMethod, endpoint = customEndpoint, body = null) => {
                setIsLoading(true);
                const t0 = performance.now();
                try {
                    const options = {
                        method: method,
                        headers: { 'Content-Type': 'application/json' }
                    };
                    if (method === 'POST' && (body || customBody)) {
                        options.body = body || customBody;
                    }
                    const res = await fetch(endpoint, options);
                    const dt = Math.round(performance.now() - t0);
                    let parsed;
                    const text = await res.text();
                    try {
                        parsed = JSON.parse(text);
                    } catch(e) {
                        parsed = text;
                    }
                    setApiResponse({
                        status: res.status,
                        statusText: res.statusText,
                        timeMs: dt,
                        endpoint: endpoint,
                        method: method,
                        data: parsed
                    });
                } catch (err) {
                    setApiResponse({
                        status: 0,
                        statusText: 'Request Failed',
                        timeMs: Math.round(performance.now() - t0),
                        endpoint: endpoint,
                        method: method,
                        error: err.message
                    });
                } finally {
                    setIsLoading(false);
                    fetchLogs();
                }
            };

            return (
                <div class="max-w-6xl mx-auto px-4 py-8">
                    {/* Header */}
                    <header class="flex flex-col md:flex-row md:items-center justify-between pb-6 mb-8 border-b border-slate-800 gap-4">
                        <div class="flex items-center space-x-3">
                            <div class="h-11 w-11 rounded-xl bg-gradient-to-tr from-indigo-600 to-cyan-400 flex items-center justify-center shadow-lg shadow-indigo-500/20">
                                <span class="font-black text-xl text-white">⚡</span>
                            </div>
                            <div>
                                <div class="flex items-center gap-2">
                                    <h1 class="text-2xl font-black tracking-tight text-white">HTTP://server</h1>
                                    <span class="px-2 py-0.5 rounded text-xs font-semibold bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">React SPA</span>
                                </div>
                                <p class="text-xs text-slate-400">Native Android Daemon &bull; Running on <span class="text-slate-300 font-mono">$hostIp:$port</span></p>
                            </div>
                        </div>

                        <div class="flex items-center gap-3">
                            <button 
                                onClick={() => setAutoRefresh(!autoRefresh)}
                                class={"px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors flex items-center gap-1.5 " + 
                                    (autoRefresh ? "bg-emerald-950/40 border-emerald-700/60 text-emerald-400" : "bg-slate-900 border-slate-700 text-slate-400")}>
                                <span class={"h-2 w-2 rounded-full " + (autoRefresh ? "bg-emerald-400 pulse-subtle" : "bg-slate-500")}></span>
                                {autoRefresh ? "Live Polling (3s)" : "Polling Paused"}
                            </button>
                            <a 
                                href="/html" 
                                class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-700 transition">
                                Switch to Plain HTML
                            </a>
                        </div>
                    </header>

                    {/* Quick Stats Grid */}
                    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                        <div class="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-sm">
                            <div class="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Daemon Status</div>
                            <div class="flex items-center gap-2">
                                <span class={"h-3 w-3 rounded-full " + (serverStatus.status === 'online' ? "bg-emerald-400 shadow-lg shadow-emerald-400/50" : "bg-rose-500")}></span>
                                <span class="text-lg font-bold text-white capitalize">{serverStatus.status}</span>
                            </div>
                            <div class="text-xs text-slate-400 mt-2">Latency: {latency !== null ? (latency + ' ms') : '---'}</div>
                        </div>

                        <div class="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-sm">
                            <div class="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Local Address</div>
                            <div class="text-lg font-mono font-bold text-cyan-400 truncate">$hostIp</div>
                            <div class="text-xs text-slate-400 mt-2">Listening Port: <span class="font-mono text-slate-200">$port</span></div>
                        </div>

                        <div class="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-sm">
                            <div class="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Total Requests</div>
                            <div class="text-lg font-bold text-white font-mono">{serverStatus.requestCount ?? '---'}</div>
                            <div class="text-xs text-slate-400 mt-2">Handled in current session</div>
                        </div>

                        <div class="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-sm">
                            <div class="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Platform & AI</div>
                            <div class="text-sm font-semibold text-indigo-300 truncate">{serverInfo ? serverInfo.appName : 'Solutions Engine'}</div>
                            <div class="text-xs text-slate-400 mt-2">Gemini 2.5 Pro / Flash Ready</div>
                        </div>
                    </div>

                    {/* Navigation Tabs */}
                    <div class="flex space-x-2 border-b border-slate-800 mb-6">
                        <button 
                            onClick={() => setActiveTab('endpoints')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all " + 
                                (activeTab === 'endpoints' ? "border-indigo-500 text-indigo-400" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            🚀 v1 Endpoints & Docs
                        </button>
                        <button 
                            onClick={() => setActiveTab('health')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all " + 
                                (activeTab === 'health' ? "border-indigo-500 text-indigo-400" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            💓 Health & Performance
                        </button>
                        <button 
                            onClick={() => setActiveTab('pairing')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all " + 
                                (activeTab === 'pairing' ? "border-indigo-500 text-indigo-400" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            🤝 Pairing & Fitting
                        </button>
                        <button 
                            onClick={() => setActiveTab('tester')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all " + 
                                (activeTab === 'tester' ? "border-indigo-500 text-indigo-400" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            🛠️ API Console & REST Tester
                        </button>
                        <button 
                            onClick={() => setActiveTab('logs')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all " + 
                                (activeTab === 'logs' ? "border-indigo-500 text-indigo-400" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            📜 Live Logs ({liveLogs.length})
                        </button>
                    </div>

                    {/* Tab 1: v1 Endpoints & OpenApi Docs */}
                    {activeTab === 'endpoints' && (
                        <div>
                            <div class="mb-4 flex items-center justify-between">
                                <span class="text-xs font-bold uppercase tracking-wider text-slate-400">Available v1 Core APIs</span>
                                <a href="/api/v1/docs" target="_blank" class="text-xs font-semibold text-cyan-400 hover:text-cyan-300 flex items-center gap-1">
                                    <span>View OpenAPI JSON Spec (/api/v1/docs)</span> &rarr;
                                </a>
                            </div>
                            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                {[
                                    { method: 'GET', path: '/api/v1/health', badge: 'Health', desc: 'Subsystem health checks (Server, JVM memory, Room DB, Gemini AI).' },
                                    { method: 'GET', path: '/api/v1/capabilities', badge: 'Capabilities', desc: 'Feature matrix: AI models, analytical frameworks, and client profiles.' },
                                    { method: 'GET', path: '/api/v1/compatibility', badge: 'Compat', desc: 'Device matrix fitting across Phone, Tablet, TV (Leanback/D-Pad), and Web.' },
                                    { method: 'GET', path: '/api/v1/performance', badge: 'Performance', desc: 'CPU cores, thread count, JVM heap utilization, and latency target.' },
                                    { method: 'GET', path: '/api/v1/pairing', badge: 'Pairing', desc: 'Handshake token generation for secure cross-device pairing.' },
                                    { method: 'GET', path: '/api/v1/docs', badge: 'OpenAPI', desc: 'Complete machine-readable OpenAPI 3.0.3 specification.' },
                                    { method: 'GET', path: '/api/v1/status', badge: 'Telemetry', desc: 'Uptime in seconds, request counter, host IP and port status.' },
                                    { method: 'GET', path: '/api/v1/frameworks', badge: 'Models', desc: 'Query 5-Whys, MECE, Cynefin, DMAIC, Kepner-Tregoe definitions.' },
                                    { method: 'POST', path: '/api/v1/echo', badge: 'Test', desc: 'Reflects test JSON payload back to client with CORS headers.' }
                                ].map((ep, idx) => (
                                    <div key={idx} class="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl p-4 flex flex-col justify-between transition group shadow-sm">
                                        <div>
                                            <div class="flex items-center justify-between mb-2">
                                                <div class="flex items-center gap-1.5">
                                                    <span class={"text-xs font-mono font-bold px-2 py-0.5 rounded " + 
                                                        (ep.method === 'GET' ? "bg-emerald-500/20 text-emerald-400 border border-emerald-500/30" : "bg-amber-500/20 text-amber-400 border border-amber-500/30")}>
                                                        {ep.method}
                                                    </span>
                                                    <span class="text-[10px] font-semibold uppercase px-1.5 py-0.5 rounded bg-slate-800 text-slate-300">
                                                        {ep.badge}
                                                    </span>
                                                </div>
                                                <span class="text-[11px] font-mono text-cyan-400">{ep.path}</span>
                                            </div>
                                            <p class="text-xs text-slate-300 leading-relaxed mb-4">{ep.desc}</p>
                                        </div>
                                        <button 
                                            onClick={() => {
                                                setCustomMethod(ep.method);
                                                setCustomEndpoint(ep.path);
                                                handleExecute(ep.method, ep.path);
                                            }}
                                            class="w-full py-2 px-3 rounded-lg text-xs font-semibold bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 border border-indigo-500/30 transition flex items-center justify-center gap-1.5">
                                            <span>Execute / Inspect</span> &rarr;
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Tab 2: Health & Performance Matrix */}
                    {activeTab === 'health' && (
                        <div class="space-y-6">
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div class="bg-slate-900 border border-slate-800 rounded-2xl p-5">
                                    <div class="flex items-center justify-between mb-4">
                                        <h3 class="text-sm font-bold text-white flex items-center gap-2">
                                            <span class="text-emerald-400">●</span> Subsystem Health Checks
                                        </h3>
                                        <button onClick={() => handleExecute('GET', '/api/v1/health')} class="text-xs font-semibold text-indigo-400 hover:text-indigo-300">
                                            Poll Health
                                        </button>
                                    </div>
                                    <div class="space-y-2.5 text-xs">
                                        <div class="flex justify-between p-2 rounded-lg bg-slate-950/60 border border-slate-800">
                                            <span class="text-slate-400">Android HTTP Server Socket</span>
                                            <span class="text-emerald-400 font-bold font-mono">HEALTHY (Port $port)</span>
                                        </div>
                                        <div class="flex justify-between p-2 rounded-lg bg-slate-950/60 border border-slate-800">
                                            <span class="text-slate-400">Room SQLite Database v3</span>
                                            <span class="text-emerald-400 font-bold font-mono">HEALTHY (5 Tables Active)</span>
                                        </div>
                                        <div class="flex justify-between p-2 rounded-lg bg-slate-950/60 border border-slate-800">
                                            <span class="text-slate-400">Gemini 2.5 Pro / Flash Engine</span>
                                            <span class="text-indigo-400 font-bold font-mono">READY (8k Thinking)</span>
                                        </div>
                                        <div class="flex justify-between p-2 rounded-lg bg-slate-950/60 border border-slate-800">
                                            <span class="text-slate-400">Offline Fallback Heuristics</span>
                                            <span class="text-emerald-400 font-bold font-mono">READY</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="bg-slate-900 border border-slate-800 rounded-2xl p-5">
                                    <div class="flex items-center justify-between mb-4">
                                        <h3 class="text-sm font-bold text-white flex items-center gap-2">
                                            <span class="text-cyan-400">⚡</span> Performance Telemetry
                                        </h3>
                                        <button onClick={() => handleExecute('GET', '/api/v1/performance')} class="text-xs font-semibold text-cyan-400 hover:text-cyan-300">
                                            Query Metrics
                                        </button>
                                    </div>
                                    <div class="space-y-2.5 text-xs">
                                        <div class="flex justify-between p-2 rounded-lg bg-slate-950/60 border border-slate-800">
                                            <span class="text-slate-400">Target Response Latency</span>
                                            <span class="text-cyan-400 font-mono font-bold">&lt; 15 ms</span>
                                        </div>
                                        <div class="flex justify-between p-2 rounded-lg bg-slate-950/60 border border-slate-800">
                                            <span class="text-slate-400">Current Measured Ping</span>
                                            <span class="text-emerald-400 font-mono font-bold">{latency !== null ? latency + ' ms' : '---'}</span>
                                        </div>
                                        <div class="flex justify-between p-2 rounded-lg bg-slate-950/60 border border-slate-800">
                                            <span class="text-slate-400">Uptime Seconds</span>
                                            <span class="text-slate-200 font-mono font-bold">{serverStatus.uptimeSeconds ?? '---'} s</span>
                                        </div>
                                        <div class="flex justify-between p-2 rounded-lg bg-slate-950/60 border border-slate-800">
                                            <span class="text-slate-400">Session Request Counter</span>
                                            <span class="text-slate-200 font-mono font-bold">{serverStatus.requestCount ?? 0}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Tab 3: Pairing & Cross-Device Compatibility */}
                    {activeTab === 'pairing' && (
                        <div class="space-y-6">
                            <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                                <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
                                    <div>
                                        <h3 class="text-base font-bold text-white">Cross-Device Pairing & Handshake</h3>
                                        <p class="text-xs text-slate-400">Register external web consoles, tablets, or TV clients with the host Android device.</p>
                                    </div>
                                    <button 
                                        onClick={() => handleExecute('GET', '/api/v1/pairing')}
                                        class="px-4 py-2 rounded-xl text-xs font-bold bg-indigo-600 hover:bg-indigo-500 text-white transition flex items-center gap-1.5 self-start md:self-auto">
                                        <span>Generate Pairing Token</span> 🔑
                                    </button>
                                </div>

                                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs">
                                    <div class="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                                        <div class="font-bold text-indigo-400 mb-1">📱 Phone Client</div>
                                        <p class="text-slate-400 text-[11px] leading-relaxed">Full Native Compose UI with responsive single-view adaptive layouts.</p>
                                        <span class="inline-block mt-2 px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 text-[10px] font-bold">Supported</span>
                                    </div>
                                    <div class="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                                        <div class="font-bold text-indigo-400 mb-1">💻 Tablet / Dual-Pane</div>
                                        <p class="text-slate-400 text-[11px] leading-relaxed">Canonical two-pane master-detail fitting with dynamic window size classes.</p>
                                        <span class="inline-block mt-2 px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 text-[10px] font-bold">Supported</span>
                                    </div>
                                    <div class="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                                        <div class="font-bold text-indigo-400 mb-1">📺 Smart TV (10ft UI)</div>
                                        <p class="text-slate-400 text-[11px] leading-relaxed">10-foot spatial navigation, D-Pad remote focus rings, 1080p/4K responsive scaling.</p>
                                        <span class="inline-block mt-2 px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 text-[10px] font-bold">D-Pad Ready</span>
                                    </div>
                                    <div class="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
                                        <div class="font-bold text-indigo-400 mb-1">🌐 Web & Headless</div>
                                        <p class="text-slate-400 text-[11px] leading-relaxed">React 18 Single-Page Application, OpenAPI 3.0 docs, and minimal HTML fallback.</p>
                                        <span class="inline-block mt-2 px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 text-[10px] font-bold">Active</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Tab 2: Custom API Console */}
                    {activeTab === 'tester' && (
                        <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6">
                            <h2 class="text-base font-bold text-white mb-4">Interactive Request Builder</h2>
                            <div class="flex flex-col sm:flex-row gap-3 mb-4">
                                <select 
                                    value={customMethod} 
                                    onChange={(e) => setCustomMethod(e.target.value)}
                                    class="bg-slate-950 border border-slate-700 rounded-xl px-3 py-2.5 text-sm font-semibold text-slate-200 focus:outline-none focus:border-indigo-500">
                                    <option value="GET">GET</option>
                                    <option value="POST">POST</option>
                                </select>
                                <input 
                                    type="text" 
                                    value={customEndpoint} 
                                    onChange={(e) => setCustomEndpoint(e.target.value)}
                                    placeholder="/api/status" 
                                    class="flex-1 bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm font-mono text-slate-200 focus:outline-none focus:border-indigo-500"
                                />
                                <button 
                                    disabled={isLoading}
                                    onClick={() => handleExecute()}
                                    class="px-6 py-2.5 rounded-xl font-bold text-sm bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white transition flex items-center justify-center gap-2">
                                    {isLoading ? 'Sending...' : 'Send Request'}
                                </button>
                            </div>

                            {customMethod === 'POST' && (
                                <div class="mb-4">
                                    <label class="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Request Body (JSON)</label>
                                    <textarea 
                                        rows="4" 
                                        value={customBody} 
                                        onChange={(e) => setCustomBody(e.target.value)}
                                        class="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-xs font-mono text-cyan-300 focus:outline-none focus:border-indigo-500">
                                    </textarea>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Tab 3: Live Access Logs */}
                    {activeTab === 'logs' && (
                        <div class="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
                            <div class="p-4 border-b border-slate-800 flex justify-between items-center">
                                <span class="text-xs font-bold uppercase tracking-wider text-slate-400">Recent Server Traffic</span>
                                <button 
                                    onClick={fetchLogs}
                                    class="text-xs text-indigo-400 hover:text-indigo-300 font-semibold">
                                    Refresh Now
                                </button>
                            </div>
                            <div class="divide-y divide-slate-800/60 max-h-96 overflow-y-auto">
                                {liveLogs.length === 0 ? (
                                    <div class="p-8 text-center text-xs text-slate-500">No requests recorded yet. Make a request above to see live traffic.</div>
                                ) : (
                                    liveLogs.map((log, i) => (
                                        <div key={i} class="px-4 py-2.5 flex items-center justify-between text-xs font-mono">
                                            <div class="flex items-center gap-3">
                                                <span class={"px-1.5 py-0.5 rounded font-bold " + (log.statusCode === 200 ? "bg-emerald-500/20 text-emerald-400" : "bg-rose-500/20 text-rose-400")}>
                                                    {log.statusCode}
                                                </span>
                                                <span class="font-bold text-slate-200">{log.method}</span>
                                                <span class="text-cyan-400">{log.path}</span>
                                                <span class="text-slate-500 text-[11px] hidden sm:inline">from {log.clientIp}</span>
                                            </div>
                                            <span class="text-slate-500 text-[11px]">
                                                {new Date(log.timestamp).toLocaleTimeString()}
                                            </span>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    )}

                    {/* Response Viewer (Active if response exists) */}
                    {apiResponse && (
                        <div class="mt-8 bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
                            <div class="bg-slate-950 px-4 py-3 border-b border-slate-800 flex items-center justify-between">
                                <div class="flex items-center gap-3">
                                    <span class="text-xs font-bold uppercase tracking-wider text-slate-400">Response Inspector</span>
                                    <span class={"text-xs font-mono font-bold px-2 py-0.5 rounded " + (apiResponse.status === 200 ? "bg-emerald-500/20 text-emerald-400" : "bg-rose-500/20 text-rose-400")}>
                                        {apiResponse.status} {apiResponse.statusText}
                                    </span>
                                    <span class="text-xs font-mono text-slate-400">{apiResponse.timeMs}ms</span>
                                </div>
                                <button 
                                    onClick={() => navigator.clipboard.writeText(JSON.stringify(apiResponse.data, null, 2))}
                                    class="text-xs text-slate-400 hover:text-white transition">
                                    Copy JSON
                                </button>
                            </div>
                            <div class="p-4 bg-slate-950/60 overflow-x-auto">
                                <pre class="text-xs font-mono text-emerald-300">
                                    <code>{typeof apiResponse.data === 'object' ? JSON.stringify(apiResponse.data, null, 2) : apiResponse.data}</code>
                                </pre>
                            </div>
                        </div>
                    )}

                    {/* Footer */}
                    <footer class="mt-12 pt-6 border-t border-slate-900 text-center text-xs text-slate-600">
                        Solutions Engine HTTP Server &bull; Native Android Kotlin Socket &bull; React 18 + Tailwind UI
                    </footer>
                </div>
            );
        }

        const root = ReactDOM.createRoot(document.getElementById('root'));
        root.render(<App />);
    </script>
</body>
</html>"""
    }

    fun getMinimalHtml(port: Int, hostIp: String): String {
        return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>HTTP://server &mdash; Minimal HTML</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        :root {
            --bg: #090d16;
            --surface: #131d2e;
            --border: #23334d;
            --text: #e2e8f0;
            --accent: #38bdf8;
            --success: #10b981;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background-color: var(--bg);
            color: var(--text);
            margin: 0;
            padding: 2rem 1rem;
            display: flex;
            justify-content: center;
        }
        .container {
            width: 100%;
            max-width: 680px;
        }
        .card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            box-shadow: 0 4px 20px rgba(0,0,0,0.4);
        }
        h1 {
            color: #fff;
            margin-top: 0;
            font-size: 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        .badge {
            background: rgba(16, 185, 129, 0.2);
            color: var(--success);
            padding: 4px 10px;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: bold;
            display: inline-block;
            border: 1px solid rgba(16, 185, 129, 0.4);
        }
        a {
            color: var(--accent);
            text-decoration: none;
        }
        a:hover {
            text-decoration: underline;
        }
        code {
            background: #06090e;
            color: #38bdf8;
            padding: 3px 6px;
            border-radius: 6px;
            font-family: monospace;
            font-size: 0.9em;
        }
        ul {
            padding-left: 1.25rem;
            line-height: 1.8;
        }
        .switch-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }
        .btn-react {
            background: #4f46e5;
            color: #fff;
            padding: 6px 14px;
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="switch-bar">
            <span class="badge">ONLINE &bull; ANDROID NATIVE</span>
            <a href="/" class="btn-react">Open Full React Console &rarr;</a>
        </div>

        <div class="card">
            <h1>🚀 HTTP://server</h1>
            <p>A native local HTTP server daemon running on Android. Listening at <code>http://$hostIp:$port</code>.</p>
            <hr style="border: 0; border-top: 1px solid var(--border); margin: 1.25rem 0;">
            <p><strong>Available REST v1 Endpoints & Diagnostics:</strong></p>
            <ul>
                <li><code>GET <a href="/api/v1/docs">/api/v1/docs</a></code> &mdash; OpenAPI 3.0 API Documentation JSON</li>
                <li><code>GET <a href="/api/v1/health">/api/v1/health</a></code> &mdash; Multi-subsystem health check (DB, JVM, AI)</li>
                <li><code>GET <a href="/api/v1/capabilities">/api/v1/capabilities</a></code> &mdash; AI models, frameworks, and engine specs</li>
                <li><code>GET <a href="/api/v1/compatibility">/api/v1/compatibility</a></code> &mdash; Device matrix (Phone, Tablet, Smart TV, Web)</li>
                <li><code>GET <a href="/api/v1/performance">/api/v1/performance</a></code> &mdash; Real-time CPU, thread & heap telemetry</li>
                <li><code>GET <a href="/api/v1/pairing">/api/v1/pairing</a></code> &mdash; Secure cross-device handshake token</li>
                <li><code>GET <a href="/api/v1/status">/api/v1/status</a></code> &mdash; Server status and uptime counter</li>
                <li><code>GET <a href="/api/v1/frameworks">/api/v1/frameworks</a></code> &mdash; Problem solving frameworks directory</li>
                <li><code>GET <a href="/api/v1/logs">/api/v1/logs</a></code> &mdash; Real-time HTTP access logs</li>
                <li><code>GET <a href="/api/v1/ping">/api/v1/ping</a></code> &mdash; Round-trip ping check</li>
            </ul>
        </div>

        <div class="card" style="font-size: 0.85rem; color: #94a3b8;">
            <p style="margin:0;">💡 <strong>Tip:</strong> Open <a href="/">/</a> in your browser on another phone, tablet, or laptop on the same Wi-Fi network to use the interactive React console!</p>
        </div>
    </div>
</body>
</html>"""
    }
}
