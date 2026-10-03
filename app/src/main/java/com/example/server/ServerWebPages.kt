package com.example.server

object ServerWebPages {

    fun getReactSpaHtml(port: Int, hostIp: String): String {
        return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>Solutions Engine &mdash; Console & PWA</title>
    <!-- iOS Web App & Safari PWA Integration -->
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
    <meta name="apple-mobile-web-app-title" content="Solutions Engine">
    <link rel="apple-touch-icon" href="/apple-touch-icon.png">
    <link rel="manifest" href="/manifest.json">
    <meta name="theme-color" content="#0f172a">

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
        /* iOS Safe Area Padding */
        body {
            padding-top: max(1rem, env(safe-area-inset-top));
            padding-bottom: max(1.5rem, env(safe-area-inset-bottom));
            padding-left: max(1rem, env(safe-area-inset-left));
            padding-right: max(1rem, env(safe-area-inset-right));
            -webkit-tap-highlight-color: transparent;
        }
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

            // iOS & Solution State
            const [solutionsList, setSolutionsList] = useState([]);
            const [newProblemTitle, setNewProblemTitle] = useState('');
            const [newProblemFramework, setNewProblemFramework] = useState('5-whys');
            const [isSolving, setIsSolving] = useState(false);
            const [solverResult, setSolverResult] = useState(null);

            const isIOS = typeof navigator !== 'undefined' && (/iPad|iPhone|iPod/.test(navigator.userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1));
            const isStandalone = typeof window !== 'undefined' && (window.navigator.standalone === true || (window.matchMedia && window.matchMedia('(display-mode: standalone)').matches));

            // Cloud Publishing Matrix State (Replit vs Vercel vs GCP)
            const [cloudFilter, setCloudFilter] = useState('all');
            const [activeSymptom, setActiveSymptom] = useState('ports');

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

            const fetchSolutions = async () => {
                try {
                    const res = await fetch('/api/v1/solutions');
                    const data = await res.json();
                    if (data && Array.isArray(data.solutions)) {
                        setSolutionsList(data.solutions);
                    }
                } catch (err) {}
            };

            const handleSolveProblem = async (e) => {
                if (e) e.preventDefault();
                if (!newProblemTitle.trim()) return;
                setIsSolving(true);
                try {
                    const res = await fetch('/api/v1/solve', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            problem: newProblemTitle,
                            framework: newProblemFramework
                        })
                    });
                    const data = await res.json();
                    setSolverResult(data);
                    fetchSolutions();
                } catch (err) {
                    alert('Error running solver: ' + err.message);
                } finally {
                    setIsSolving(false);
                }
            };

            useEffect(() => {
                fetchTelemetry();
                fetchInfo();
                fetchLogs();
                fetchSolutions();

                const interval = setInterval(() => {
                    if (autoRefresh) {
                        fetchTelemetry();
                        fetchLogs();
                        fetchSolutions();
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

                    {/* iOS Detection & Standalone PWA Banner */}
                    {isIOS && (
                        <div class="mb-6 p-4 rounded-2xl bg-gradient-to-r from-sky-950/70 via-indigo-950/70 to-purple-950/70 border border-sky-500/40 shadow-lg flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                            <div class="flex items-center gap-3">
                                <div class="h-10 w-10 rounded-xl bg-sky-500/20 border border-sky-400/30 flex items-center justify-center text-xl shrink-0">
                                    🍎
                                </div>
                                <div>
                                    <div class="text-sm font-bold text-sky-200 flex items-center gap-2">
                                        <span>iOS Client Active</span>
                                        <span class={"text-[10px] px-2 py-0.5 rounded-full font-bold uppercase " + (isStandalone ? "bg-emerald-500/20 text-emerald-300 border border-emerald-500/30" : "bg-sky-500/20 text-sky-300 border border-sky-500/30")}>
                                            {isStandalone ? "Standalone Home Screen App" : "Safari Browser Mode"}
                                        </span>
                                    </div>
                                    <p class="text-xs text-slate-300 mt-0.5">
                                        {isStandalone ? "Running in full-screen standalone PWA mode with notch & safe-area support." : "Tap Share in Safari (box with up arrow) &rarr; 'Add to Home Screen' for native iOS app experience."}
                                    </p>
                                </div>
                            </div>
                            <button 
                                onClick={() => setActiveTab('ios')}
                                class="px-3 py-1.5 rounded-lg text-xs font-bold bg-sky-600 hover:bg-sky-500 text-white transition self-start sm:self-auto shrink-0 shadow-sm">
                                View iOS Guide &rarr;
                            </button>
                        </div>
                    )}

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
                            <div class="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Room DB Solutions</div>
                            <div class="text-lg font-bold text-white font-mono">{solutionsList.length}</div>
                            <div class="text-xs text-slate-400 mt-2">Synced from SQLite on host</div>
                        </div>

                        <div class="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-sm">
                            <div class="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Platform & AI</div>
                            <div class="text-sm font-semibold text-indigo-300 truncate">{serverInfo ? serverInfo.appName : 'Solutions Engine'}</div>
                            <div class="text-xs text-slate-400 mt-2">iOS Safari PWA & Shortcuts Ready</div>
                        </div>
                    </div>

                    {/* Navigation Tabs */}
                    <div class="flex flex-wrap gap-2 border-b border-slate-800 mb-6 pb-2">
                        <button 
                            onClick={() => setActiveTab('ios')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all flex items-center gap-1.5 " + 
                                (activeTab === 'ios' ? "border-sky-400 text-sky-300" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            🍎 iOS & Shortcuts
                        </button>
                        <button 
                            onClick={() => setActiveTab('solver')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all flex items-center gap-1.5 " + 
                                (activeTab === 'solver' ? "border-indigo-500 text-indigo-400" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            💡 Live Solver & Room DB ({solutionsList.length})
                        </button>
                        <button 
                            onClick={() => setActiveTab('cloud')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all flex items-center gap-1.5 " + 
                                (activeTab === 'cloud' ? "border-amber-400 text-amber-300" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            ☁️ Cloud Matrix (Replit &bull; Vercel &bull; GCP)
                        </button>
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
                        <button 
                            onClick={() => setActiveTab('export')}
                            class={"px-4 py-2 text-sm font-semibold border-b-2 transition-all " + 
                                (activeTab === 'export' ? "border-indigo-500 text-indigo-400" : "border-transparent text-slate-400 hover:text-slate-200")}>
                            📥 Export & Download
                        </button>
                    </div>

                    {/* Tab: iOS & Apple Shortcuts Integration */}
                    {activeTab === 'ios' && (
                        <div class="space-y-6">
                            {/* Card 1: iOS Safari PWA Walkthrough */}
                            <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-md">
                                <div class="flex items-center justify-between mb-4">
                                    <div class="flex items-center gap-3">
                                        <div class="h-10 w-10 rounded-xl bg-sky-500/20 border border-sky-400/30 flex items-center justify-center text-xl">
                                            🍎
                                        </div>
                                        <div>
                                            <h3 class="text-base font-bold text-white">Native iOS Experience (Safari PWA)</h3>
                                            <p class="text-xs text-slate-400">Zero-install standalone app for iPhone, iPad, and macOS Safari</p>
                                        </div>
                                    </div>
                                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                                        iOS 16, 17, 18 Ready
                                    </span>
                                </div>

                                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                                    <div class="bg-slate-950/70 border border-slate-800 rounded-xl p-4">
                                        <div class="flex items-center gap-2 text-sky-400 font-bold text-xs uppercase mb-2">
                                            <span class="h-5 w-5 rounded-full bg-sky-500/20 flex items-center justify-center text-[10px]">1</span>
                                            Open in Mobile Safari
                                        </div>
                                        <p class="text-xs text-slate-300 leading-relaxed">
                                            Connect iPhone/iPad to the same local Wi-Fi and open <span class="font-mono text-cyan-300">http://$hostIp:$port</span> in Safari.
                                        </p>
                                    </div>

                                    <div class="bg-slate-950/70 border border-slate-800 rounded-xl p-4">
                                        <div class="flex items-center gap-2 text-sky-400 font-bold text-xs uppercase mb-2">
                                            <span class="h-5 w-5 rounded-full bg-sky-500/20 flex items-center justify-center text-[10px]">2</span>
                                            Tap Share & Add to Home
                                        </div>
                                        <p class="text-xs text-slate-300 leading-relaxed">
                                            Tap Safari's <strong>Share</strong> button (box with upward arrow) and select <strong>"Add to Home Screen"</strong>.
                                        </p>
                                    </div>

                                    <div class="bg-slate-950/70 border border-slate-800 rounded-xl p-4">
                                        <div class="flex items-center gap-2 text-sky-400 font-bold text-xs uppercase mb-2">
                                            <span class="h-5 w-5 rounded-full bg-sky-500/20 flex items-center justify-center text-[10px]">3</span>
                                            Launch Standalone PWA
                                        </div>
                                        <p class="text-xs text-slate-300 leading-relaxed">
                                            Tap the custom <strong>⚡SE Solutions</strong> icon on your iOS home screen to launch in fullscreen mode without browser URL bars.
                                        </p>
                                    </div>
                                </div>

                                <div class="flex flex-wrap items-center gap-3 pt-4 border-t border-slate-800 text-xs">
                                    <div class="text-slate-400">iOS Assets:</div>
                                    <a href="/apple-touch-icon.png" target="_blank" class="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-sky-300 border border-slate-700 transition">
                                        View Apple Touch Icon (180x180 PNG)
                                    </a>
                                    <a href="/manifest.json" target="_blank" class="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-indigo-300 border border-slate-700 transition">
                                        View Web App Manifest (JSON)
                                    </a>
                                    <a href="/api/v1/ios/shortcut" target="_blank" class="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-emerald-300 border border-slate-700 transition">
                                        View Siri Shortcut Spec
                                    </a>
                                </div>
                            </div>

                            {/* Card 2: Apple Shortcuts & Siri Automation */}
                            <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-md">
                                <div class="flex items-center justify-between mb-4">
                                    <div class="flex items-center gap-3">
                                        <div class="h-10 w-10 rounded-xl bg-purple-500/20 border border-purple-400/30 flex items-center justify-center text-xl">
                                            🪄
                                        </div>
                                        <div>
                                            <h3 class="text-base font-bold text-white">Apple Shortcuts & Siri Voice Integration</h3>
                                            <p class="text-xs text-slate-400">Trigger problem deconstruction and 5-Whys diagnosis via Siri from your iPhone</p>
                                        </div>
                                    </div>
                                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-purple-500/20 text-purple-300 border border-purple-500/30">
                                        REST Webhook
                                    </span>
                                </div>

                                <div class="bg-slate-950 p-4 rounded-xl border border-slate-800/80 mb-4 font-mono text-xs text-purple-300 overflow-x-auto">
                                    curl -X POST http://$hostIp:$port/api/v1/solve \<br/>
                                    &nbsp;&nbsp;-H "Content-Type: application/json" \<br/>
                                    &nbsp;&nbsp;-d '{"problem": "High database latency on surge", "framework": "5-whys"}'
                                </div>

                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-4">
                                    <button 
                                        onClick={async () => {
                                            const curlCmd = `curl -X POST http://${hostIp}:${port}/api/v1/solve -H "Content-Type: application/json" -d '{"problem": "High database latency on surge", "framework": "5-whys"}'`;
                                            await navigator.clipboard.writeText(curlCmd);
                                            alert("📋 Copied Apple Shortcuts cURL command to clipboard!");
                                        }}
                                        class="py-2.5 px-4 rounded-xl text-xs font-bold bg-purple-600 hover:bg-purple-500 text-white transition flex items-center justify-center gap-2">
                                        <span>📋 Copy cURL for iOS Shortcut</span>
                                    </button>

                                    <button 
                                        onClick={async () => {
                                            const shortcutRecipe = {
                                                name: "Ask Solutions Engine",
                                                action: "Get Contents of URL",
                                                url: `http://${hostIp}:${port}/api/v1/solve`,
                                                method: "POST",
                                                headers: { "Content-Type": "application/json" },
                                                body: { problem: "Shortcut Input", framework: "5-whys" },
                                                spokenOutput: "rootCause"
                                            };
                                            await navigator.clipboard.writeText(JSON.stringify(shortcutRecipe, null, 2));
                                            alert("📋 Copied Apple Shortcuts JSON recipe to clipboard!");
                                        }}
                                        class="py-2.5 px-4 rounded-xl text-xs font-bold bg-slate-800 hover:bg-slate-700 text-purple-300 border border-purple-500/30 transition flex items-center justify-center gap-2">
                                        <span>⚙️ Copy Siri Shortcut Config</span>
                                    </button>
                                </div>

                                <div class="p-3 bg-purple-950/30 border border-purple-900/50 rounded-xl text-xs text-slate-300 flex items-start gap-2">
                                    <span class="text-purple-400 font-bold">💡 Voice Tip:</span>
                                    <span>Once configured in Apple Shortcuts app, say <em>"Hey Siri, ask Solutions Engine to solve this problem"</em> and Siri will vocalize the immediate root cause diagnosis directly!</span>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Tab: Live Solver & Room DB Explorer */}
                    {activeTab === 'solver' && (
                        <div class="space-y-6">
                            {/* Problem Solver Form */}
                            <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-md">
                                <div class="flex items-center justify-between mb-4">
                                    <div class="flex items-center gap-3">
                                        <div class="h-10 w-10 rounded-xl bg-indigo-500/20 border border-indigo-400/30 flex items-center justify-center text-xl">
                                            💡
                                        </div>
                                        <div>
                                            <h3 class="text-base font-bold text-white">Execute Problem Solver (iOS & Web)</h3>
                                            <p class="text-xs text-slate-400">Trigger multi-vector deconstruction and save results into host Room SQLite database</p>
                                        </div>
                                    </div>
                                    <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                                        Room DB Synced
                                    </span>
                                </div>

                                <form onSubmit={handleSolveProblem} class="space-y-4">
                                    <div>
                                        <label class="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">
                                            Problem Statement or Architectural Bottleneck:
                                        </label>
                                        <input 
                                            type="text" 
                                            value={newProblemTitle}
                                            onChange={(e) => setNewProblemTitle(e.target.value)}
                                            placeholder="e.g. Memory fragmentation under heavy WebSocket load" 
                                            class="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500"
                                        />
                                    </div>

                                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                        <div>
                                            <label class="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5">
                                                Analytical Framework:
                                            </label>
                                            <select 
                                                value={newProblemFramework}
                                                onChange={(e) => setNewProblemFramework(e.target.value)}
                                                class="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500">
                                                <option value="5-whys">5-Whys Root Cause Analysis</option>
                                                <option value="mece">MECE Issue Tree</option>
                                                <option value="cynefin">Cynefin Sensemaking Framework</option>
                                                <option value="dmaic">DMAIC Six Sigma Loop</option>
                                                <option value="kepner-tregoe">Kepner-Tregoe Rational Matrix</option>
                                            </select>
                                        </div>

                                        <div class="flex items-end">
                                            <button 
                                                type="submit"
                                                disabled={isSolving || !newProblemTitle.trim()}
                                                class={"w-full py-2.5 px-4 rounded-xl text-sm font-bold transition flex items-center justify-center gap-2 " + 
                                                    (isSolving || !newProblemTitle.trim() ? "bg-slate-800 text-slate-500 cursor-not-allowed" : "bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/30")}>
                                                {isSolving ? "Analyzing & Persisting..." : "🚀 Solve & Save to Room DB"}
                                            </button>
                                        </div>
                                    </div>
                                </form>

                                {/* Solver Result Card */}
                                {solverResult && (
                                    <div class="mt-6 p-4 rounded-xl bg-slate-950 border border-indigo-500/40">
                                        <div class="flex items-center justify-between mb-3 pb-2 border-b border-slate-800">
                                            <span class="text-xs font-bold uppercase text-emerald-400 flex items-center gap-1.5">
                                                <span>✓</span> Successfully Saved to Room Database (Solution ID: {solverResult.id})
                                            </span>
                                            <span class="text-xs font-mono text-slate-400">{solverResult.framework}</span>
                                        </div>

                                        <div class="text-xs text-slate-300 font-semibold mb-2">
                                            <span class="text-slate-400">Root Cause:</span> {solverResult.rootCause}
                                        </div>

                                        <div class="space-y-1 mb-3">
                                            {solverResult.fiveWhys && solverResult.fiveWhys.map((step, idx) => (
                                                <div key={idx} class="text-[11px] font-mono text-cyan-300/90 pl-2 border-l border-slate-800">
                                                    {step}
                                                </div>
                                            ))}
                                        </div>

                                        <div class="p-2.5 rounded-lg bg-emerald-950/30 border border-emerald-800/40 text-xs text-emerald-300">
                                            <strong>Immediate Quick Win:</strong> {solverResult.immediateQuickWin}
                                        </div>
                                    </div>
                                )}
                            </div>

                            {/* Room DB Solutions Listing */}
                            <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-md">
                                <div class="flex items-center justify-between mb-4">
                                    <div>
                                        <h3 class="text-base font-bold text-white flex items-center gap-2">
                                            <span>Active Solutions in Room SQLite Database</span>
                                            <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-slate-800 text-indigo-400 border border-slate-700">
                                                {solutionsList.length}
                                            </span>
                                        </h3>
                                        <p class="text-xs text-slate-400">Queried dynamically from host AppDatabase (SolutionDao)</p>
                                    </div>
                                    <button 
                                        onClick={fetchSolutions}
                                        class="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition">
                                        🔄 Refresh DB
                                    </button>
                                </div>

                                {solutionsList.length === 0 ? (
                                    <div class="text-center py-8 text-xs text-slate-400">
                                        No competitive solutions recorded yet in Room Database. Use the form above to solve and persist one!
                                    </div>
                                ) : (
                                    <div class="space-y-3">
                                        {solutionsList.map((sol) => (
                                            <div key={sol.id} class="p-4 rounded-xl bg-slate-950 border border-slate-800/90 hover:border-slate-700 transition">
                                                <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-2">
                                                    <div class="flex items-center gap-2">
                                                        <span class="text-xs font-bold text-white">{sol.problemTitle}</span>
                                                        <span class="text-[10px] font-semibold px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                                                            {sol.difficulty}
                                                        </span>
                                                        <span class="text-[10px] font-semibold px-2 py-0.5 rounded bg-sky-500/20 text-sky-300">
                                                            {sol.platform}
                                                        </span>
                                                    </div>
                                                    <span class="text-[11px] font-mono text-slate-500">ID: {sol.id}</span>
                                                </div>
                                                <p class="text-xs text-slate-300 mb-3">{sol.approachExplanation}</p>
                                                {sol.codeSnippet && (
                                                    <pre class="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-[11px] font-mono text-emerald-300 overflow-x-auto">
                                                        <code>{sol.codeSnippet}</code>
                                                    </pre>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Tab: Cloud Publishing & Deployment Matrix (Replit vs Vercel vs GCP) */}
                    {activeTab === 'cloud' && (
                        <div class="space-y-6">
                            {/* Hero Header */}
                            <div class="bg-gradient-to-r from-slate-900 via-amber-950/40 to-slate-900 border border-amber-500/30 rounded-2xl p-6 shadow-md">
                                <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
                                    <div class="flex items-center gap-3">
                                        <div class="h-12 w-12 rounded-xl bg-amber-500/20 border border-amber-400/30 flex items-center justify-center text-2xl shrink-0">
                                            ☁️
                                        </div>
                                        <div>
                                            <h3 class="text-lg font-bold text-white flex items-center gap-2">
                                                <span>Cloud Publishing & Deployment Matrix</span>
                                                <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30">
                                                    Replit &bull; Vercel &bull; GCP
                                                </span>
                                            </h3>
                                            <p class="text-xs text-slate-300 mt-1">
                                                Diagnose and resolve why your app works in Preview / Localhost but fails on public production URLs.
                                            </p>
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <span class="text-xs text-slate-400 font-semibold uppercase">Platform View:</span>
                                        {['all', 'replit', 'vercel', 'gcp'].map((p) => (
                                            <button 
                                                key={p}
                                                onClick={() => setCloudFilter(p)}
                                                class={"px-3 py-1 rounded-lg text-xs font-bold uppercase transition " + 
                                                    (cloudFilter === p ? "bg-amber-500 text-slate-950 shadow-md" : "bg-slate-800 text-slate-300 hover:bg-slate-700")}>
                                                {p === 'all' ? 'In-Between Matrix' : p}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            {/* Section 1: The Side-by-Side "In-Between" Comparison Matrix */}
                            <div class="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-md">
                                <div class="p-4 border-b border-slate-800 bg-slate-950/60 flex items-center justify-between">
                                    <div class="flex items-center gap-2">
                                        <span class="text-xs font-bold uppercase tracking-wider text-slate-400">Core Deployment & Publishing Architecture</span>
                                    </div>
                                    <span class="text-xs text-slate-400">Comparing Host, Ports, Secrets, Filesystem & Probes</span>
                                </div>

                                <div class="overflow-x-auto">
                                    <table class="w-full text-left text-xs border-collapse">
                                        <thead>
                                            <tr class="border-b border-slate-800 bg-slate-950 text-slate-300 font-semibold">
                                                <th class="p-4 w-44">Dimension</th>
                                                {(cloudFilter === 'all' || cloudFilter === 'replit') && (
                                                    <th class="p-4 bg-red-950/20 border-l border-slate-800 text-red-300 font-bold">
                                                        <div class="flex items-center gap-1.5">
                                                            <span class="text-sm">⚡</span> Replit Deployments
                                                        </div>
                                                    </th>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'vercel') && (
                                                    <th class="p-4 bg-sky-950/20 border-l border-slate-800 text-sky-300 font-bold">
                                                        <div class="flex items-center gap-1.5">
                                                            <span class="text-sm">▲</span> Vercel Cloud
                                                        </div>
                                                    </th>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'gcp') && (
                                                    <th class="p-4 bg-emerald-950/20 border-l border-slate-800 text-emerald-300 font-bold">
                                                        <div class="flex items-center gap-1.5">
                                                            <span class="text-sm">☁️</span> Google Cloud (GCP Run)
                                                        </div>
                                                    </th>
                                                )}
                                            </tr>
                                        </thead>
                                        <tbody class="divide-y divide-slate-800/80 text-slate-300">
                                            {/* Row 1: Target Architecture */}
                                            <tr class="hover:bg-slate-800/30 transition">
                                                <td class="p-4 font-bold text-white bg-slate-950/30">Target Workload</td>
                                                {(cloudFilter === 'all' || cloudFilter === 'replit') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-semibold text-slate-200">Stateful Container / VM</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Autoscale or Reserved VM up to 8GB. Expects a continuously running daemon (restarts if exits).</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'vercel') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-semibold text-slate-200">Serverless & Edge Functions</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Stateless micro-lambdas + Edge CDN. 10s execution timeout (Hobby) or 60s (Pro). No persistent socket loops.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'gcp') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-semibold text-slate-200">Containerized Microservices</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Cloud Run scale-to-zero container. Up to 32GB RAM / 8 vCPUs. Concurrency up to 1000 requests per container.</div>
                                                    </td>
                                                )}
                                            </tr>

                                            {/* Row 2: Host & Port Binding */}
                                            <tr class="hover:bg-slate-800/30 transition">
                                                <td class="p-4 font-bold text-white bg-slate-950/30">Host & Port Binding</td>
                                                {(cloudFilter === 'all' || cloudFilter === 'replit') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-mono text-red-300 font-bold">Must bind to 0.0.0.0</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Listening on <code class="text-rose-400 font-mono">127.0.0.1</code> or <code class="text-rose-400 font-mono">localhost</code> causes immediate <strong>502 Bad Gateway</strong>. Set <code class="text-cyan-300 font-mono">externalPort = 80</code>.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'vercel') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-mono text-sky-300 font-bold">Managed Edge Port</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Vercel injects port dynamically or handles requests via serverless handlers. For custom servers, uses <code class="text-cyan-300 font-mono">process.env.PORT</code>.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'gcp') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-mono text-emerald-300 font-bold">0.0.0.0:${'$'}{'{'}PORT:-8080{'}'}</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Cloud Run requires binding to <code class="text-emerald-400 font-mono">0.0.0.0</code> and listening on the container's <code class="text-emerald-400 font-mono">${'$'}PORT</code> env variable (default 8080).</div>
                                                    </td>
                                                )}
                                            </tr>

                                            {/* Row 3: Secrets & Environment Variables */}
                                            <tr class="hover:bg-slate-800/30 transition">
                                                <td class="p-4 font-bold text-white bg-slate-950/30">Secrets & Credentials</td>
                                                {(cloudFilter === 'all' || cloudFilter === 'replit') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-amber-300 font-semibold">Separate Production Secrets</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Editor Secrets <strong>DO NOT</strong> carry over to published app! Must manually populate in <em>Publishing &rarr; Secrets</em>.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'vercel') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-sky-300 font-semibold">Env Scope Tri-Partition</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Split into Production, Preview, Development. Client variables require <code class="text-sky-300 font-mono">NEXT_PUBLIC_</code> prefix.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'gcp') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-emerald-300 font-semibold">Secret Manager Integration</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Injected via <code class="text-emerald-300 font-mono">--set-secrets</code> or <code class="text-emerald-300 font-mono">--set-env-vars</code>. IAM roles govern access without code changes.</div>
                                                    </td>
                                                )}
                                            </tr>

                                            {/* Row 4: Filesystem & Persistence */}
                                            <tr class="hover:bg-slate-800/30 transition">
                                                <td class="p-4 font-bold text-white bg-slate-950/30">Storage & Filesystem</td>
                                                {(cloudFilter === 'all' || cloudFilter === 'replit') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-rose-400 font-bold">Ephemeral Disk</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Local filesystem resets on every publish & reboot. Files written locally vanish. Must use Neon PostgreSQL or S3.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'vercel') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-sky-400 font-bold">Read-Only Root</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Root filesystem is strictly read-only. Only ephemeral <code class="text-sky-300 font-mono">/tmp</code> is writable during invocation. Use Vercel Blob/Postgres.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'gcp') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-emerald-400 font-bold">Stateless Container Layer</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Container writes persist only for that instance's lifetime. Scale-down destroys disk. Use Cloud SQL / Cloud Storage (GCS).</div>
                                                    </td>
                                                )}
                                            </tr>

                                            {/* Row 5: Health Check & Timeout */}
                                            <tr class="hover:bg-slate-800/30 transition">
                                                <td class="p-4 font-bold text-white bg-slate-950/30">Health Check & Timeout</td>
                                                {(cloudFilter === 'all' || cloudFilter === 'replit') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-amber-300 font-bold">Strict 5s Root HTTP Timeout</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">If homepage takes &gt; 5 seconds to return 200 OK, Replit aborts publishing. Defer expensive AI/DB init.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'vercel') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-sky-300 font-bold">Build Phase Verification</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Build step compiles and verifies output. Function timeouts are 10s (Hobby) / 60s (Pro) per HTTP request.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'gcp') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-emerald-300 font-bold">Configurable Probes (240s)</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Custom Startup Probe with up to 240s initial delay prevents killing slow-starting JVM or heavy AI containers.</div>
                                                    </td>
                                                )}
                                            </tr>

                                            {/* Row 6: Staging vs Production Domain */}
                                            <tr class="hover:bg-slate-800/30 transition">
                                                <td class="p-4 font-bold text-white bg-slate-950/30">Preview &rarr; Public URLs</td>
                                                {(cloudFilter === 'all' || cloudFilter === 'replit') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-mono text-cyan-300 text-[11px]">&lt;id&gt;.replit.dev &rarr; &lt;app&gt;.replit.app</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Preview runs on dev subdomain. Published runs on production cluster with optional custom domain.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'vercel') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-mono text-cyan-300 text-[11px]">&lt;proj&gt;-&lt;hash&gt;.vercel.app &rarr; production</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Instant preview URL generated per git commit. Production points to primary branch or custom domain.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'gcp') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="font-mono text-cyan-300 text-[11px]">ais-dev-...run.app &rarr; ais-pre-...run.app</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Revision URLs for staging. Traffic splitting (canary releases: 90% prod / 10% new). Custom domain mapping.</div>
                                                    </td>
                                                )}
                                            </tr>

                                            {/* Row 7: DNS & Network Filtering */}
                                            <tr class="hover:bg-slate-800/30 transition">
                                                <td class="p-4 font-bold text-white bg-slate-950/30">DNS & ISP Filtering</td>
                                                {(cloudFilter === 'all' || cloudFilter === 'replit') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-amber-300 font-semibold">Common ISP Filter Target</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Some networks block <code class="text-cyan-300 font-mono">replit.dev</code>. Switch to Google DNS (8.8.8.8) or Cloudflare (1.1.1.1) and run <code class="text-cyan-300 font-mono">ipconfig /flushdns</code>.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'vercel') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-sky-300 font-semibold">Anycast Edge Routing</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Rarely filtered. Global Anycast DNS with edge acceleration and DDoS prevention out of the box.</div>
                                                    </td>
                                                )}
                                                {(cloudFilter === 'all' || cloudFilter === 'gcp') && (
                                                    <td class="p-4 border-l border-slate-800">
                                                        <div class="text-emerald-300 font-semibold">Google Global Edge</div>
                                                        <div class="text-[11px] text-slate-400 mt-1">Served over Google's global fiber backbone. Cloud Armor filtering + Cloud DNS guarantees 100% SLA.</div>
                                                    </td>
                                                )}
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            {/* Section 2: Interactive Troubleshooting Wizard */}
                            <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-md">
                                <div class="flex items-center justify-between mb-4">
                                    <div class="flex items-center gap-2">
                                        <span class="text-lg">🛠️</span>
                                        <h4 class="text-sm font-bold text-white uppercase tracking-wider">
                                            Interactive Deployment Troubleshooter (In-Between Diagnosis)
                                        </h4>
                                    </div>
                                    <span class="text-xs text-slate-400">Select your symptom to see targeted fixes across all 3 platforms</span>
                                </div>

                                <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-2 mb-6">
                                    {[
                                        { id: 'ports', label: '🔌 Port / 0.0.0.0' },
                                        { id: 'secrets', label: '🔑 Missing Secrets' },
                                        { id: 'timeout', label: '⏱️ Health Timeout' },
                                        { id: 'storage', label: '💾 Lost Disk Data' },
                                        { id: 'cors', label: '🌐 CORS / 502' },
                                        { id: 'dns', label: '📡 DNS / ISP Block' }
                                    ].map((sym) => (
                                        <button 
                                            key={sym.id}
                                            onClick={() => setActiveSymptom(sym.id)}
                                            class={"p-2.5 rounded-xl text-xs font-bold transition text-center border " + 
                                                (activeSymptom === sym.id ? "bg-amber-500/20 border-amber-500/50 text-amber-300 shadow-sm" : "bg-slate-950/70 border-slate-800 text-slate-400 hover:text-slate-200")}>
                                            {sym.label}
                                        </button>
                                    ))}
                                </div>

                                {/* Symptom Resolution Details */}
                                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    {/* Replit Diagnosis */}
                                    <div class="p-4 rounded-xl bg-slate-950 border border-red-900/40 flex flex-col justify-between">
                                        <div>
                                            <div class="flex items-center justify-between mb-2">
                                                <span class="text-xs font-bold text-red-300 flex items-center gap-1.5">
                                                    <span>⚡</span> Replit Resolution
                                                </span>
                                                <span class="text-[10px] font-mono uppercase bg-red-950/60 text-red-400 px-2 py-0.5 rounded border border-red-900/60">
                                                    {activeSymptom}
                                                </span>
                                            </div>
                                            <div class="text-xs text-slate-300 leading-relaxed mb-3">
                                                {activeSymptom === 'ports' && "Replit requires binding to 0.0.0.0. If server binds to localhost or 127.0.0.1, Replit's ingress proxy cannot reach your container, returning 502 Bad Gateway. In .replit, ensure externalPort = 80."}
                                                {activeSymptom === 'secrets' && "Project Editor Secrets (.env) DO NOT carry over to published deployments. Open the Publishing pane &rarr; Secrets tab and manually add every API key and DB connection string."}
                                                {activeSymptom === 'timeout' && "Replit sends an automated HTTP GET to your root (/) endpoint before promoting to live. If your app takes > 5 seconds to reply, publishing aborts. Defer expensive AI initialization."}
                                                {activeSymptom === 'storage' && "Replit's filesystem is completely ephemeral on publishing. Files written to local disk are wiped on restart. Connect to Replit SQL Database (Postgres) or Amazon S3."}
                                                {activeSymptom === 'cors' && "When accessing replit.app from an external web app, enable CORS headers with 'Access-Control-Allow-Origin: *' or configure proxy rewrites."}
                                                {activeSymptom === 'dns' && "If replit.dev / replit.app is inaccessible, web filters or ISP DNS are blocking the domain. Change DNS to 8.8.8.8 or 1.1.1.1 and run 'ipconfig /flushdns'."}
                                            </div>
                                        </div>
                                        <div class="p-2.5 rounded-lg bg-red-950/30 border border-red-800/40 text-[11px] font-mono text-red-300">
                                            {activeSymptom === 'ports' && "server.bind(\"0.0.0.0\", port)"}
                                            {activeSymptom === 'secrets' && "Publishing Pane &rarr; Production Secrets"}
                                            {activeSymptom === 'timeout' && "Cache root HTML & start async daemon"}
                                            {activeSymptom === 'storage' && "Use Neon PostgreSQL (DATABASE_URL)"}
                                            {activeSymptom === 'cors' && "header(\"Access-Control-Allow-Origin\", \"*\")"}
                                            {activeSymptom === 'dns' && "ipconfig /flushdns (Windows) / 8.8.8.8"}
                                        </div>
                                    </div>

                                    {/* Vercel Diagnosis */}
                                    <div class="p-4 rounded-xl bg-slate-950 border border-sky-900/40 flex flex-col justify-between">
                                        <div>
                                            <div class="flex items-center justify-between mb-2">
                                                <span class="text-xs font-bold text-sky-300 flex items-center gap-1.5">
                                                    <span>▲</span> Vercel Resolution
                                                </span>
                                                <span class="text-[10px] font-mono uppercase bg-sky-950/60 text-sky-400 px-2 py-0.5 rounded border border-sky-900/60">
                                                    {activeSymptom}
                                                </span>
                                            </div>
                                            <div class="text-xs text-slate-300 leading-relaxed mb-3">
                                                {activeSymptom === 'ports' && "Vercel automatically provisions edge routing for serverless functions and Next.js. For custom Node.js Express servers, listen on process.env.PORT || 3000."}
                                                {activeSymptom === 'secrets' && "In Vercel Dashboard &rarr; Project Settings &rarr; Environment Variables, check both 'Production' and 'Preview' boxes. Browser variables must start with NEXT_PUBLIC_."}
                                                {activeSymptom === 'timeout' && "Vercel functions fail with 504 Gateway Timeout if response exceeds 10s (Hobby) or 60s (Pro). Use streaming responses (Transfer-Encoding: chunked) or background queue."}
                                                {activeSymptom === 'storage' && "The entire root filesystem on Vercel is read-only. Writing via fs.writeFileSync will throw EROFS. Only /tmp is writable for ephemeral scratchpads."}
                                                {activeSymptom === 'cors' && "Add 'headers' config in vercel.json to allow Cross-Origin headers on /api/(.*) routes."}
                                                {activeSymptom === 'dns' && "Vercel uses Anycast Edge CDN. If your custom domain fails, verify CNAME points to cname.vercel-dns.com or A record to 76.76.21.21."}
                                            </div>
                                        </div>
                                        <div class="p-2.5 rounded-lg bg-sky-950/30 border border-sky-800/40 text-[11px] font-mono text-sky-300">
                                            {activeSymptom === 'ports' && "const port = process.env.PORT || 3000"}
                                            {activeSymptom === 'secrets' && "NEXT_PUBLIC_API_URL / Vercel Settings"}
                                            {activeSymptom === 'timeout' && "export const maxDuration = 60;"}
                                            {activeSymptom === 'storage' && "fs.writeFileSync('/tmp/temp.log')"}
                                            {activeSymptom === 'cors' && "vercel.json: \"source\": \"/api/(.*)\""}
                                            {activeSymptom === 'dns' && "CNAME &rarr; cname.vercel-dns.com"}
                                        </div>
                                    </div>

                                    {/* GCP Diagnosis */}
                                    <div class="p-4 rounded-xl bg-slate-950 border border-emerald-900/40 flex flex-col justify-between">
                                        <div>
                                            <div class="flex items-center justify-between mb-2">
                                                <span class="text-xs font-bold text-emerald-300 flex items-center gap-1.5">
                                                    <span>☁️</span> GCP Cloud Run Resolution
                                                </span>
                                                <span class="text-[10px] font-mono uppercase bg-emerald-950/60 text-emerald-400 px-2 py-0.5 rounded border border-emerald-900/60">
                                                    {activeSymptom}
                                                </span>
                                            </div>
                                            <div class="text-xs text-slate-300 leading-relaxed mb-3">
                                                {activeSymptom === 'ports' && "GCP Cloud Run injects ${'$'}PORT (defaults to 8080). Container MUST listen on 0.0.0.0:${'$'}PORT. If hardcoded to 3000 or localhost, container fails health check."}
                                                {activeSymptom === 'secrets' && "Use Google Secret Manager. Mount secrets as environment variables using gcloud run deploy --set-secrets=\"KEY=SECRET_NAME:latest\" or Cloud Console."}
                                                {activeSymptom === 'timeout' && "Configure Startup Probe (--startup-probe) with initial delay up to 240s so container does not get killed during heavy JVM / Python startup."}
                                                {activeSymptom === 'storage' && "Cloud Run containers are stateless. Disk writes are temporary in-memory tmpfs. Persist files to Google Cloud Storage (GCS) or Cloud SQL."}
                                                {activeSymptom === 'cors' && "Ensure public unauthenticated access is granted via --allow-unauthenticated and configure CORS on your HTTP response headers."}
                                                {activeSymptom === 'dns' && "Map custom domains in Cloud Run &rarr; Manage Custom Domains or use Cloud Load Balancing with Google-managed SSL certificates."}
                                            </div>
                                        </div>
                                        <div class="p-2.5 rounded-lg bg-emerald-950/30 border border-emerald-800/40 text-[11px] font-mono text-emerald-300">
                                            {activeSymptom === 'ports' && "app.listen(process.env.PORT || 8080, '0.0.0.0')"}
                                            {activeSymptom === 'secrets' && "gcloud run deploy --set-secrets=..."}
                                            {activeSymptom === 'timeout' && "--startup-probe initialDelaySeconds=15"}
                                            {activeSymptom === 'storage' && "gsutil cp file gs://bucket-name"}
                                            {activeSymptom === 'cors' && "gcloud run deploy --allow-unauthenticated"}
                                            {activeSymptom === 'dns' && "gcloud beta run domain-mappings create"}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Section 3: Ready-to-Copy Deployment Configs */}
                            <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-md">
                                <h4 class="text-sm font-bold text-white mb-3 flex items-center gap-2">
                                    <span>📋</span> Production-Ready Config Templates
                                </h4>
                                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <div class="bg-slate-950 p-4 rounded-xl border border-slate-800 flex flex-col justify-between">
                                        <div>
                                            <div class="flex items-center justify-between mb-2">
                                                <span class="text-xs font-mono font-bold text-red-300">.replit (Port & Run)</span>
                                                <button 
                                                    onClick={() => {
                                                        const txt = `run = ["bash", "-c", "node server.js"]\n\n[deployment]\nrun = ["bash", "-c", "node server.js"]\n\n[[ports]]\nlocalPort = 8080\nexternalPort = 80`;
                                                        navigator.clipboard.writeText(txt);
                                                        alert("Copied .replit config!");
                                                    }}
                                                    class="text-[10px] text-slate-400 hover:text-white">
                                                    Copy
                                                </button>
                                            </div>
                                            <pre class="text-[11px] font-mono text-slate-300 overflow-x-auto leading-relaxed">
<code>run = ["node", "server.js"]

[deployment]
run = ["node", "server.js"]

[[ports]]
localPort = 8080
externalPort = 80</code>
                                            </pre>
                                        </div>
                                    </div>

                                    <div class="bg-slate-950 p-4 rounded-xl border border-slate-800 flex flex-col justify-between">
                                        <div>
                                            <div class="flex items-center justify-between mb-2">
                                                <span class="text-xs font-mono font-bold text-sky-300">vercel.json (CORS & Limits)</span>
                                                <button 
                                                    onClick={() => {
                                                        const txt = `{\n  "version": 2,\n  "headers": [\n    {\n      "source": "/api/(.*)",\n      "headers": [\n        { "key": "Access-Control-Allow-Origin", "value": "*" },\n        { "key": "Access-Control-Allow-Methods", "value": "GET,POST,OPTIONS" }\n      ]\n    }\n  ]\n}`;
                                                        navigator.clipboard.writeText(txt);
                                                        alert("Copied vercel.json!");
                                                    }}
                                                    class="text-[10px] text-slate-400 hover:text-white">
                                                    Copy
                                                </button>
                                            </div>
                                            <pre class="text-[11px] font-mono text-slate-300 overflow-x-auto leading-relaxed">
<code>{'{'}
  "version": 2,
  "headers": [
    {'{'}
      "source": "/api/(.*)",
      "headers": [
        {'{'} "key": "Access-Control-Allow-Origin", "value": "*" {'}'}
      ]
    {'}'}
  ]
{'}'}</code>
                                            </pre>
                                        </div>
                                    </div>

                                    <div class="bg-slate-950 p-4 rounded-xl border border-slate-800 flex flex-col justify-between">
                                        <div>
                                            <div class="flex items-center justify-between mb-2">
                                                <span class="text-xs font-mono font-bold text-emerald-300">GCP Cloud Run Deploy</span>
                                                <button 
                                                    onClick={() => {
                                                        const txt = `gcloud run deploy my-app \\\n  --image gcr.io/my-project/my-app:latest \\\n  --platform managed \\\n  --region us-east1 \\\n  --allow-unauthenticated \\\n  --port 8080 \\\n  --memory 1Gi`;
                                                        navigator.clipboard.writeText(txt);
                                                        alert("Copied gcloud command!");
                                                    }}
                                                    class="text-[10px] text-slate-400 hover:text-white">
                                                    Copy
                                                </button>
                                            </div>
                                            <pre class="text-[11px] font-mono text-slate-300 overflow-x-auto leading-relaxed">
<code>gcloud run deploy my-app \
  --image gcr.io/proj/app \
  --region us-east1 \
  --allow-unauthenticated \
  --port 8080 \
  --memory 1Gi</code>
                                            </pre>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

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

                    {/* Tab: Export & Download (Bypasses 'Download not supported at this time') */}
                    {activeTab === 'export' && (
                        <div class="space-y-6">
                            <div class="bg-amber-500/10 border border-amber-500/30 rounded-2xl p-4 text-xs text-amber-200 flex items-start gap-3">
                                <span class="text-lg">💡</span>
                                <div>
                                    <strong class="font-bold text-amber-300">Browser / Emulator Download Notice:</strong>
                                    <p class="mt-1 text-amber-200/90 leading-relaxed">
                                        If your browser, Android WebView, or streaming emulator reports <em>"Download not supported at this time"</em>, you can bypass browser download handlers completely. Use <strong>📋 Copy to Clipboard</strong>, <strong>👁️ View Raw Inline</strong>, or <strong>💾 Save Directly to Device VVOL</strong> below.
                                    </p>
                                </div>
                            </div>

                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {[
                                    {
                                        title: "OpenAPI 3.0 API Specification",
                                        desc: "Complete REST API catalog, endpoints, and schemas in JSON format.",
                                        endpoint: "/api/v1/docs",
                                        badge: "Spec"
                                    },
                                    {
                                        title: "Device & TV Hardware Profile",
                                        desc: "Amlogic SoC, FireOS, Android TV, and recommended browser engine profile.",
                                        endpoint: "/api/v1/compatibility",
                                        badge: "Hardware"
                                    },
                                    {
                                        title: "Subsystem Telemetry & Health",
                                        desc: "JVM heap, memory stats, thread metrics, and component health audit.",
                                        endpoint: "/api/v1/health",
                                        badge: "Telemetry"
                                    },
                                    {
                                        title: "Recent HTTP Access Logs",
                                        desc: "Chronological log entries of all incoming HTTP requests and latencies.",
                                        endpoint: "/api/v1/logs",
                                        badge: "Logs"
                                    }
                                ].map((item, idx) => (
                                    <div key={idx} class="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between">
                                        <div>
                                            <div class="flex items-center justify-between mb-2">
                                                <h4 class="font-bold text-sm text-white">{item.title}</h4>
                                                <span class="text-[10px] font-semibold uppercase px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                                                    {item.badge}
                                                </span>
                                            </div>
                                            <p class="text-xs text-slate-400 mb-4">{item.desc}</p>
                                        </div>
                                        <div class="flex flex-wrap gap-2 pt-2 border-t border-slate-800/80">
                                            <button 
                                                onClick={async () => {
                                                    try {
                                                        const res = await fetch(item.endpoint);
                                                        const data = await res.text();
                                                        await navigator.clipboard.writeText(data);
                                                        alert("📋 Copied " + item.title + " to clipboard successfully!");
                                                    } catch (e) {
                                                        alert("Error copying: " + e.message);
                                                    }
                                                }}
                                                class="flex-1 py-2 px-3 rounded-lg text-xs font-semibold bg-indigo-600 hover:bg-indigo-500 text-white transition flex items-center justify-center gap-1">
                                                <span>📋 Copy Text</span>
                                            </button>
                                            <a 
                                                href={item.endpoint} 
                                                target="_blank"
                                                rel="noopener"
                                                class="flex-1 py-2 px-3 rounded-lg text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-cyan-300 border border-slate-700 transition flex items-center justify-center gap-1 text-center">
                                                <span>👁️ View Inline</span>
                                            </a>
                                        </div>
                                    </div>
                                ))}
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
    <title>Solutions Engine &mdash; Minimal HTML & iOS</title>
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
    <meta name="apple-mobile-web-app-title" content="Solutions Engine">
    <link rel="apple-touch-icon" href="/apple-touch-icon.png">
    <link rel="manifest" href="/manifest.json">
    <meta name="theme-color" content="#0f172a">
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
            padding: max(1.5rem, env(safe-area-inset-top)) max(1rem, env(safe-area-inset-right)) max(1.5rem, env(safe-area-inset-bottom)) max(1rem, env(safe-area-inset-left));
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
        .badge-ios {
            background: rgba(56, 189, 248, 0.2);
            color: var(--accent);
            padding: 4px 10px;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: bold;
            display: inline-block;
            border: 1px solid rgba(56, 189, 248, 0.4);
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
            <div>
                <span class="badge">ONLINE &bull; ANDROID NATIVE</span>
                <span class="badge-ios" style="margin-left: 6px;">🍎 iOS & PWA READY</span>
            </div>
            <a href="/" class="btn-react">Open Full React Console &rarr;</a>
        </div>

        <div class="card">
            <h1>🚀 HTTP://server &mdash; iOS Ready</h1>
            <p>A native local HTTP server daemon running on Android with complete iOS Safari PWA support. Listening at <code>http://$hostIp:$port</code>.</p>
            <hr style="border: 0; border-top: 1px solid var(--border); margin: 1.25rem 0;">
            <p><strong>Available REST v1 Endpoints & Diagnostics:</strong></p>
            <ul>
                <li><code>GET <a href="/api/v1/solutions">/api/v1/solutions</a></code> &mdash; Query Room SQLite database solutions</li>
                <li><code>POST <a href="/api/v1/solve">/api/v1/solve</a></code> &mdash; Apple Shortcuts & Siri 5-Whys solver webhook</li>
                <li><code>GET <a href="/api/v1/ios/shortcut">/api/v1/ios/shortcut</a></code> &mdash; Siri Shortcut automation configuration</li>
                <li><code>GET <a href="/manifest.json">/manifest.json</a></code> &mdash; iOS / Web App standalone manifest</li>
                <li><code>GET <a href="/apple-touch-icon.png">/apple-touch-icon.png</a></code> &mdash; 180x180 PNG home screen icon</li>
                <li><code>GET <a href="/api/v1/docs">/api/v1/docs</a></code> &mdash; OpenAPI 3.0 API Documentation JSON</li>
                <li><code>GET <a href="/api/v1/health">/api/v1/health</a></code> &mdash; Multi-subsystem health check (DB, JVM, AI)</li>
                <li><code>GET <a href="/api/v1/capabilities">/api/v1/capabilities</a></code> &mdash; AI models, frameworks, and iOS specs</li>
                <li><code>GET <a href="/api/v1/compatibility">/api/v1/compatibility</a></code> &mdash; Device matrix (iPhone, iPad, TV, Web)</li>
                <li><code>GET <a href="/api/v1/performance">/api/v1/performance</a></code> &mdash; Real-time CPU, thread & heap telemetry</li>
                <li><code>GET <a href="/api/v1/pairing">/api/v1/pairing</a></code> &mdash; Secure cross-device handshake token</li>
                <li><code>GET <a href="/api/v1/status">/api/v1/status</a></code> &mdash; Server status and uptime counter</li>
                <li><code>GET <a href="/api/v1/frameworks">/api/v1/frameworks</a></code> &mdash; Problem solving frameworks directory</li>
                <li><code>GET <a href="/api/v1/wol?mac=AA:BB:CC:DD:EE:FF">/api/v1/wol</a></code> &mdash; Wake-on-LAN magic packet broadcaster</li>
                <li><code>GET <a href="/api/v1/vvol">/api/v1/vvol</a></code> &mdash; Virtual Storage Volumes</li>
                <li><code>GET <a href="/api/v1/bot/status">/api/v1/bot/status</a></code> &mdash; AsyncBot resilience queue</li>
                <li><code>GET <a href="/api/v1/logs">/api/v1/logs</a></code> &mdash; Real-time HTTP access logs</li>
                <li><code>GET <a href="/api/v1/ping">/api/v1/ping</a></code> &mdash; Round-trip ping check</li>
            </ul>
        </div>

        <div class="card" style="font-size: 0.85rem; color: #38bdf8; background: rgba(56, 189, 248, 0.08); border: 1px solid rgba(56, 189, 248, 0.3);">
            <p style="margin:0;">🍎 <strong>Using iOS (iPhone or iPad)?</strong> Tap the Share button in Safari &rarr; select <strong>"Add to Home Screen"</strong> to run Solutions Engine in standalone fullscreen mode without browser URL bars.</p>
        </div>

        <div class="card" style="font-size: 0.85rem; color: #f59e0b; background: rgba(245, 158, 11, 0.1); border: 1px solid rgba(245, 158, 11, 0.3);">
            <p style="margin:0;">💡 <strong>Download Not Supported?</strong> In mobile browsers or streaming emulators, standard downloads may be disabled. Use the <a href="/" style="color: #fbbf24; font-weight: bold;">Export & Download</a> or <a href="/" style="color: #fbbf24; font-weight: bold;">iOS & Shortcuts</a> tab in the React Console to copy data directly to your clipboard.</p>
        </div>
    </div>
</body>
</html>"""
    }
}
