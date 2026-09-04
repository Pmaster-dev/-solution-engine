# Solutions Engine — AI Agent Architecture & System Documentation (`AGENTS.md`)

This document outlines the autonomous AI agent workflows, reasoning engines, domain frameworks, and integration architecture powering the **Solutions Engine** Android application.

---

## 1. Overview & Core Mission

**Solutions Engine** is an enterprise-grade AI problem-solving, strategic outline, and root-cause deconstruction platform powered by Google Gemini (Gemini 2.5 Pro / Gemini 3.1 Pro High Thinking). It transforms ambiguous, unstructured technical and business challenges into deterministic execution roadmaps, 5-Whys diagnostic trees, decision matrices, and risk-mitigated action plans.

---

## 2. Multi-Agent Systems & AI Engine Directory

### 🧠 A. Strategic Outline & Roadmap Agent (`StrategyOutlineAgent`)
- **Primary Service Class:** `GeminiSolutionsService.analyzeAndGenerateStrategyOutline(...)`
- **Model Engine:** `gemini-2.5-pro` (configured with High Thinking extended reasoning budget: 8,192 tokens)
- **Role & Responsibilities:**
  - Deconstructs ambiguous engineering, product, and business challenges.
  - Applies structured analytical frameworks (MECE, First Principles, Cynefin, DMAIC, Kepner-Tregoe).
  - Generates phased roadmap milestones with execution timelines (1–12 weeks), deliverables, and dependencies.
  - Computes probability/impact matrices for failure modes and generates concrete contingency fallbacks.
  - Formats immediate (<48h) quick-win recommendations and KPI telemetry trackers.
- **Output Artifacts:** `StrategyOutlineResult` Room Entity, interactive markdown export, 1-tap conversion to active cases.

---

### 🔍 B. 5-Whys Root Cause Diagnostics Agent (`RootCauseDiagnosticsAgent`)
- **Primary Service Class:** `GeminiSolutionsService.analyzeNewProblem(...)`
- **Model Engine:** `gemini-2.5-flash` / `gemini-2.5-pro`
- **Role & Responsibilities:**
  - Performs iterative causal chain deconstruction (Toyota Production System 5-Whys methodology).
  - Eliminates superficial symptom fixing in favor of core architectural/operational remediation.
  - Generates 3 multi-vector solution strategies (e.g., Short-term Hotfix vs. Long-term Refactor vs. Process Guardrail).
  - Formulates weighted Multi-Criteria Decision Matrices with automated scoring across speed, cost, risk, and stability.
- **Output Artifacts:** `ProblemCase` Room Entity with dynamic progress tracking.

---

### 🌐 C. Technical SEO & SERP Diagnostics Agent (`SeoDiagnosticsAgent`)
- **Primary Service Class:** `GeminiSolutionsService.analyzeSeoAudit(...)`
- **Model Engine:** `gemini-2.5-flash`
- **Role & Responsibilities:**
  - Audits site architecture, Core Web Vitals (LCP, INP, CLS), crawl budget, and indexability issues.
  - Diagnoses SERP rank drops, intent shifts, and schema validation failures.
  - Produces valid JSON-LD structured data snippets ready for deployment.
- **Output Artifacts:** `SeoAuditResult` Room Entity with prioritized high/medium/low action items.

---

### ⚡ D. High-Thinking Deep Reasoning Sandbox (`HighThinkingAgent`)
- **Primary Service Class:** `GeminiSolutionsService.queryHighThinkingReasoning(...)`
- **Model Engine:** `gemini-2.5-pro` (Extended reasoning mode enabled with system instructions)
- **Role & Responsibilities:**
  - Facilitates complex trade-off analysis (e.g., CAP Theorem compromises, monolithic vs. microservices migration, build vs. buy decisions).
  - Employs step-by-step chain-of-thought verification before formulating final recommendations.

---

## 3. Supported Problem-Solving Frameworks

| Framework | Domain Suitability | Core Mechanism |
| :--- | :--- | :--- |
| **MECE Issue Tree** | Business, Strategy, Product | Mutually Exclusive, Collectively Exhaustive problem branch decomposition. |
| **5-Whys Root Cause** | Technical, Operational, SRE | Recursive 5-layer causal chain extraction targeting root bottlenecks. |
| **First Principles & Clean Architecture** | Software Engineering, System Design | Strips assumptions down to fundamental truths and reconstructs clean boundaries. |
| **Cynefin Sensemaking** | Incident Response, Crisis Management | Classifies problems into Clear, Complicated, Complex, or Chaotic domains. |
| **DMAIC Six Sigma** | Operations, Quality Assurance | Define, Measure, Analyze, Improve, and Control continuous improvement loops. |
| **Kepner-Tregoe Matrix** | High-Stakes Decision Making | Structured appraisal of Musts vs. Wants with rational weighted scoring. |

---

## 4. Architectural Stack & Technical Specifications

```
┌─────────────────────────────────────────────────────────────┐
│                 Jetpack Compose UI Layer                    │
│  (Dashboard, Strategy Studio, Cases, Frameworks, SEO, VM)   │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Events
┌──────────────────────────────▼──────────────────────────────┐
│                    SolutionsViewModel                       │
│  (State aggregation, UI state coordination, token counters)  │
└──────────────────────────────┬──────────────────────────────┘
                               │ Coroutines / Flow
┌──────────────────────────────▼──────────────────────────────┐
│                   SolutionsRepository                       │
│       (Data orchestration, fallback handling, caching)      │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
┌──────────────▼──────────────┐ ┌──────────────▼──────────────┐
│    Room Database (Local)    │ │   Gemini AI Remote Service  │
│  - ProblemCaseDao           │ │  - Gemini 2.5 Pro (Thinking)│
│  - StrategyOutlineDao       │ │  - Gemini 2.5 Flash         │
│  - SeoDao, UserDao, SubDao  │ │  - Structured JSON Schemas  │
└─────────────────────────────┘ └─────────────────────────────┘
```

- **Runtime:** Android 14+ (API 34/35), Kotlin 2.0.0
- **UI Toolkit:** Jetpack Compose with Material 3 (Dynamic color palettes & edge-to-edge support)
- **Local Persistence:** Room Database v3 with custom JSON type converters (`Converters.kt`)
- **Networking & AI:** Direct Gemini REST API integration using structured JSON schemas and fallbacks
- **Testing:** Local JVM testing via Robolectric & Roborazzi screenshot verification

---

## 5. Security & Privacy Protocols

1. **API Key Isolation:** Gemini API keys are injected at build time via `BuildConfig.GEMINI_API_KEY` through AI Studio secrets (`.env`).
2. **On-Device Data Sovereignty:** All problem briefs, 5-Whys analyses, and strategic blueprints persist locally in the encrypted SQLite Room database.
3. **No Unsolicited Background Telemetry:** AI queries execute strictly on user initiation.

---

## 6. Verification & Automated Testing

To run the automated suite for all agent converters, data models, and UI flows:

```bash
# Run unit & Robolectric tests
gradle :app:testDebugUnitTest

# Compile & verify applet integrity
gradle :app:assembleDebug
```

---
*Maintained by the Solutions Engine Autonomous Architecture Team.*
