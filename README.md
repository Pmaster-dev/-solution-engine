# Solutions Engine 🧠⚡

[![Android](https://img.shields.io/badge/Platform-Android%2014%2B-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Gemini](https://img.shields.io/badge/AI-Google%20Gemini%202.5%20%2F%203.1-4E7FFF.svg?style=flat&logo=google)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Solutions Engine** is an enterprise-grade AI problem-solving, strategic roadmap synthesis, and root-cause deconstruction platform for Android. Powered by Google Gemini (Gemini 2.5 Pro & Gemini 3.1 Pro with High Thinking extended reasoning), Solutions Engine transforms ambiguous, high-stakes technical, business, and operational dilemmas into structured execution blueprints, interactive 5-Whys diagnostic trees, decision matrices, and risk-mitigated action plans.

---

## 📑 Table of Contents

- [Project Overview](#-project-overview)
  - [Key Capabilities](#-key-capabilities)
  - [Core Problem-Solving Frameworks](#-core-problem-solving-frameworks)
  - [Architecture & Tech Stack](#-architecture--tech-stack)
- [Installation](#-installation)
  - [Prerequisites](#prerequisites)
  - [Configuration & API Keys](#configuration--api-keys)
  - [Building the Application](#building-the-application)
- [Usage](#-usage)
  - [1. Strategic Outline & Execution Studio](#1-strategic-outline--execution-studio)
  - [2. 5-Whys Root Cause Diagnostics](#2-5-whys-root-cause-diagnostics)
  - [3. Technical SEO & SERP Diagnostics](#3-technical-seo--serp-diagnostics)
  - [4. High-Thinking Deep Reasoning Sandbox](#4-high-thinking-deep-reasoning-sandbox)
  - [5. Frameworks & Mental Models Library](#5-frameworks--mental-models-library)
- [Contributing](#-contributing)
  - [Development Workflow](#development-workflow)
  - [Testing & Quality Assurance](#testing--quality-assurance)
- [License](#-license)

---

## 🌟 Project Overview

Modern systems and organizations face complex, multidimensional challenges that cannot be solved with generic advice. Solutions Engine applies structured analytical frameworks combined with Google Gemini's extended reasoning to break problems down to their fundamental drivers and deliver deterministic, actionable execution plans.

### 🚀 Key Capabilities

- **AI Strategic Outline Studio:** Deconstructs complex initiatives into multi-phase execution roadmaps (1–12 week horizons), milestone checklists, risk matrices, KPI telemetry, and immediate quick wins (<48h).
- **5-Whys Root Cause Engine:** Performs recursive causal chain analysis (Toyota Production System methodology) to isolate true systemic failure points from superficial symptoms.
- **Weighted Decision Matrices:** Automates multi-criteria decision appraisals across speed, cost, risk, and stability to recommend optimal remediation pathways.
- **Technical SEO & SERP Diagnostics:** Audits site architecture, Core Web Vitals (LCP, INP, CLS), crawl budget, and indexability issues with instant JSON-LD schema generation.
- **High-Thinking Deep Reasoning:** Features an extended token reasoning mode (8,192 token thinking budget) for exploring architectural trade-offs, monolith vs. microservices migration, and CAP theorem compromises.
- **Local Sovereignty & Offline Resilience:** Backed by a local encrypted Room Database (v3) with complete offline caching and instant Markdown export.

### 📐 Core Problem-Solving Frameworks

| Framework | Target Domain | Core Mechanism |
| :--- | :--- | :--- |
| **MECE Issue Tree** | Business, Strategy, Product | Mutually Exclusive, Collectively Exhaustive branch decomposition. |
| **5-Whys Root Cause** | Technical, Operational, SRE | Recursive 5-layer causal chain extraction targeting root bottlenecks. |
| **First Principles & Clean Architecture** | Software Engineering, System Design | Strips assumptions down to fundamental axioms and builds clean boundaries. |
| **Cynefin Sensemaking** | Incident Response, Crisis Management | Categorizes problems into Clear, Complicated, Complex, or Chaotic contexts. |
| **DMAIC Six Sigma** | Operations, Quality Assurance | Define, Measure, Analyze, Improve, and Control continuous quality loops. |
| **Kepner-Tregoe Matrix** | High-Stakes Decision Making | Structured appraisal of Musts vs. Wants with rational weighted scoring. |

### 🏗️ Architecture & Tech Stack

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

- **Language:** Kotlin 2.0.0
- **UI Framework:** Jetpack Compose with Material Design 3 (Dynamic Color, Edge-to-Edge window insets)
- **Architecture:** MVVM + Unidirectional Data Flow (UDF)
- **Local Persistence:** Room Database v3 with TypeConverters (`Converters.kt`)
- **AI Backend:** Google Gemini REST API (`gemini-2.5-pro`, `gemini-2.5-flash`)
- **Async Concurrency:** Kotlin Coroutines & Reactive `StateFlow`
- **Testing:** Local JVM Testing via Robolectric & Roborazzi Visual Regression

---

## 🛠️ Installation

### Prerequisites

- **Android Studio:** Ladybug (2024.2+) or newer
- **JDK:** OpenJDK 17 or higher
- **Android SDK:** API Level 34 / 35 (Android 14 / 15)
- **Build System:** Gradle (Kotlin DSL `.gradle.kts`)

### Configuration & API Keys

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/solutions-engine.git
   cd solutions-engine
   ```

2. **Configure Environment Variables:**
   Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

   Open `.env` and add your Google Gemini API key:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   > 💡 *You can obtain a free Gemini API key from [Google AI Studio](https://aistudio.google.com).*

### Building the Application

Compile and assemble the debug APK:
```bash
gradle :app:assembleDebug
```

Run local unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```

---

## 📱 Usage

### 1. Strategic Outline & Execution Studio
1. Open the **Strategy** tab from the bottom navigation.
2. Enter your problem statement or select a starter preset (e.g., *Microservice Decoupling*, *Churn Spike*, *Core Web Vitals Drop*).
3. Select your domain, preferred analytical framework (or leave on **Auto-Select**), constraints, and execution timeline horizon (1–12 weeks).
4. Tap **Generate Blueprint** to synthesize:
   - Executive Summary & Root Cause Hypothesis
   - Phased Roadmap with interactive milestone checkboxes
   - Risk & Mitigation Matrix with probability/impact ratings
   - Success KPI metrics tracker & immediate quick wins (<48h)
5. Export full blueprints to **Markdown** or tap **Track as Active Case** for continuous monitoring.

### 2. 5-Whys Root Cause Diagnostics
1. Tap the **+** FAB on the Dashboard or Cases screen.
2. Provide a problem brief and select urgency and domain.
3. The engine generates a 5-layer recursive causal chain deconstructing the root issue.
4. Compare 3 generated multi-vector solution strategies using the automated **Decision Matrix**.

### 3. Technical SEO & SERP Diagnostics
1. Navigate to the **SEO** tab.
2. Enter a target URL, core issue, and primary keywords.
3. Review audit scores across Crawlability, Core Web Vitals, and Content Quality.
4. Copy generated, schema-compliant **JSON-LD** structured data directly to your clipboard.

### 4. High-Thinking Deep Reasoning Sandbox
1. Open the **Thinking** tab.
2. Input complex trade-offs (e.g., *Eventual Consistency vs. Strong Consistency in Global Fintech*).
3. Gemini 2.5 Pro activates its deep reasoning chain-of-thought engine, returning comprehensive architectural evaluations.

### 5. Frameworks & Mental Models Library
1. Access the **Models** tab.
2. Browse detailed guides, step-by-step methodologies, and examples for MECE, 5-Whys, First Principles, Cynefin, DMAIC, and Kepner-Tregoe.

---

## 🤝 Contributing

We welcome contributions to **Solutions Engine**! To ensure high code quality and maintainability, please follow these guidelines:

### Development Workflow

1. **Fork the Repository** and create a descriptive feature branch:
   ```bash
   git checkout -b feature/new-analytical-framework
   ```
2. **Adhere to Code Guidelines:**
   - Write UI exclusively in **Jetpack Compose** using **Material Design 3** tokens.
   - Maintain proper `testTag` identifiers on all interactive components for testability.
   - Keep source files modular and under 500 lines where practical.
   - Ensure all network interactions with Gemini use structured JSON schemas with graceful fallbacks.
3. **Update Room Migrations:**
   - When modifying local entities, update the Room database schema and provide corresponding migration paths in `SolutionsDatabase.kt`.
4. **Submit a Pull Request:**
   - Include a clear description of the problem solved or feature implemented.
   - Reference any relevant issues.

### Testing & Quality Assurance

Before submitting your pull request, verify that all unit and screenshot tests pass:

```bash
# Run unit & Robolectric tests
gradle :app:testDebugUnitTest

# Compile debug build
gradle :app:assembleDebug
```

---

## 📄 License

```
Copyright 2026 Solutions Engine Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
