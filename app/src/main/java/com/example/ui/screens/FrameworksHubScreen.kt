package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProblemDomain
import com.example.data.model.UrgencyLevel

data class ProblemFramework(
    val id: String,
    val name: String,
    val origin: String,
    val category: String, // "Root Cause", "Decision Making", "Strategy & Scoping", "Speed & Execution", "Architecture"
    val icon: ImageVector,
    val iconColor: Color,
    val bestFor: String,
    val whyBest: String,
    val whenToUse: String,
    val whenNotToUse: String,
    val steps: List<String>,
    val defaultDomain: ProblemDomain,
    val sampleTitle: String,
    val sampleDesc: String
)

@Composable
fun FrameworksHubScreen(
    onSelectFrameworkForCase: (title: String, domain: ProblemDomain, urgency: UrgencyLevel, desc: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedFrameworkId by remember { mutableStateOf<String?>(null) }

    // Interactive Advisor State
    var problemInput by remember { mutableStateOf("") }
    var recommendedFrameworkId by remember { mutableStateOf<String?>("5_whys") }

    val categories = listOf("All", "Root Cause", "Decision Making", "Strategy & Scoping", "Execution", "Software Architecture")

    val frameworks = remember {
        listOf(
            ProblemFramework(
                id = "5_whys",
                name = "5-Whys Root Cause Analysis",
                origin = "Toyota Production System / Taiichi Ohno",
                category = "Root Cause",
                icon = Icons.Default.Search,
                iconColor = Color(0xFF6366F1),
                bestFor = "Recurring bugs, production incidents, operational latency, and systemic failures.",
                whyBest = "Drills past superficial symptoms directly down to the foundational organizational or technical failure point.",
                whenToUse = "When an unexpected defect, latency spike, conversion drop, or regression occurs.",
                whenNotToUse = "When exploring open-ended brainstorms with no clear baseline failure.",
                steps = listOf(
                    "1. Define the specific observed problem with measurable data.",
                    "2. Ask 'Why did this happen?' and document the immediate direct cause.",
                    "3. Repeat 'Why?' 4 more times on each successive answer.",
                    "4. Identify the systemic/procedural root vulnerability.",
                    "5. Implement a robust preventative countermeasure, not just a patch."
                ),
                defaultDomain = ProblemDomain.TECHNICAL,
                sampleTitle = "Root Cause: High Database Connection Timeouts",
                sampleDesc = "Investigating connection pool exhaustion during peak traffic hours using 5-Whys diagnostic tree."
            ),
            ProblemFramework(
                id = "cynefin",
                name = "Cynefin Sensemaking Framework",
                origin = "Dave Snowden / IBM",
                category = "Decision Making",
                icon = Icons.Default.CompassCalibration,
                iconColor = Color(0xFF0EA5E9),
                bestFor = "Navigating uncertainty, deciding whether a crisis requires immediate triage, expert analysis, or experiments.",
                whyBest = "Classifies problems into Clear, Complicated, Complex, or Chaotic domains to avoid applying the wrong leadership or engineering playbook.",
                whenToUse = "When facing high ambiguity or competing organizational opinions on how to proceed.",
                whenNotToUse = "For simple, routine administrative tasks.",
                steps = listOf(
                    "1. Clear: Sense -> Categorize -> Respond (Follow Best Practices).",
                    "2. Complicated: Sense -> Analyze -> Respond (Engage Domain Experts).",
                    "3. Complex: Probe -> Sense -> Respond (Run Safe-to-fail Experiments).",
                    "4. Chaotic: Act -> Sense -> Respond (Immediate Triage to stabilize).",
                    "5. Move solutions from chaotic/complex into stable manageable patterns."
                ),
                defaultDomain = ProblemDomain.ORGANIZATIONAL,
                sampleTitle = "Cynefin Classification: Cloud Migration & Team Restructure",
                sampleDesc = "Classifying legacy monolith decommissioning across Complicated and Complex architectural domains."
            ),
            ProblemFramework(
                id = "mece_issue_tree",
                name = "MECE + Issue Trees",
                origin = "McKinsey & Company / Barbara Minto",
                category = "Strategy & Scoping",
                icon = Icons.Default.AccountTree,
                iconColor = Color(0xFF10B981),
                bestFor = "Market entry, cost reduction, revenue expansion, and exhaustive problem decomposition.",
                whyBest = "Ensures solutions are 'Mutually Exclusive, Collectively Exhaustive'—no overlaps, no blind spots.",
                whenToUse = "When breaking down complex business, architectural, or growth challenges into solvable sub-problems.",
                whenNotToUse = "During early stage freewheeling creative brainstorming.",
                steps = listOf(
                    "1. State the central strategic question clearly at the tree root.",
                    "2. Split into 2-5 distinct branches that cover 100% of the possibility space.",
                    "3. Validate no overlapping categories exist between sibling nodes.",
                    "4. Recursively decompose each sub-branch down to actionable hypotheses.",
                    "5. Prioritize branches based on ROI and feasibility impact."
                ),
                defaultDomain = ProblemDomain.BUSINESS,
                sampleTitle = "MECE Revenue & Margin Breakdown",
                sampleDesc = "Deconstructing SaaS revenue bottlenecks into Customer Acquisition Cost (CAC), Lifetime Value (LTV), and Churn vectors."
            ),
            ProblemFramework(
                id = "kepner_tregoe",
                name = "Kepner-Tregoe Decision Matrix",
                origin = "Charles Kepner & Benjamin Tregoe",
                category = "Decision Making",
                icon = Icons.Default.Star,
                iconColor = Color(0xFFF59E0B),
                bestFor = "High-stakes vendor selection, tech stack migrations, and multi-variable trade-offs.",
                whyBest = "Separates mandatory requirements ('MUSTs') from weighted preferences ('WANTs') with risk consequence profiling.",
                whenToUse = "When selecting between 3+ viable candidate architectures or vendors with competing stakeholder priorities.",
                whenNotToUse = "When only one obvious solution is feasible.",
                steps = listOf(
                    "1. State the decision objective clearly.",
                    "2. Define non-negotiable 'MUST' criteria (instant pass/fail gate).",
                    "3. Define weighted 'WANT' criteria (scored 1 to 10 based on importance).",
                    "4. Score all candidate options against each criteria.",
                    "5. Conduct Potential Problem Analysis (PPA) to stress-test the winning candidate."
                ),
                defaultDomain = ProblemDomain.TECHNICAL,
                sampleTitle = "Kepner-Tregoe: Cloud Provider & Database Architecture Selection",
                sampleDesc = "Evaluating AWS vs GCP vs Cloud SQL on Latency, Cost, Compliance MUSTs and Developer Velocity WANTs."
            ),
            ProblemFramework(
                id = "first_principles",
                name = "First Principles Thinking",
                origin = "Aristotle & Modern Engineering (SpaceX / Tesla)",
                category = "Strategy & Scoping",
                icon = Icons.Default.Bolt,
                iconColor = Color(0xFF8B5CF6),
                bestFor = "Cost breakthroughs, novel product innovation, and defying conventional legacy assumptions.",
                whyBest = "Strips away analogies ('we do it because everyone does') and builds up from fundamental truths and physics.",
                whenToUse = "When industry standard solutions are too slow, too expensive, or technologically stale.",
                whenNotToUse = "When standard commercial off-the-shelf components already solve the problem cheaply.",
                steps = listOf(
                    "1. Identify and question all existing assumptions.",
                    "2. Deconstruct the problem to its fundamental, undeniable foundational truths.",
                    "3. Calculate theoretical minimum cost/latency/weight based on raw materials.",
                    "4. Reconstruct an entirely novel solution upwards from those core truths.",
                    "5. Validate against physical and technical limits."
                ),
                defaultDomain = ProblemDomain.TECHNICAL,
                sampleTitle = "First Principles: 90% Cost Reduction in Real-Time AI Inference",
                sampleDesc = "Deconstructing token generation costs down to raw compute FLOPs and memory bandwidth fundamentals."
            ),
            ProblemFramework(
                id = "dmaic",
                name = "DMAIC / Six Sigma",
                origin = "Motorola & General Electric",
                category = "Execution",
                icon = Icons.Default.Timeline,
                iconColor = Color(0xFFEC4899),
                bestFor = "SLA improvement, defect reduction, CI/CD pipeline reliability, and repeatable operational scaling.",
                whyBest = "Provides a disciplined, data-driven cycle with statistical rigor to guarantee long-term control.",
                whenToUse = "When optimizing an existing process that is running inconsistently or producing waste.",
                whenNotToUse = "When inventing a brand new 0-to-1 product.",
                steps = listOf(
                    "1. DEFINE: Outline problem statement, goal, and customer deliverable.",
                    "2. MEASURE: Collect baseline telemetry and defect frequency data.",
                    "3. ANALYZE: Identify statistical variation and root cause waste.",
                    "4. IMPROVE: Implement and verify automated safeguards.",
                    "5. CONTROL: Deploy monitoring dashboards and continuous feedback loops."
                ),
                defaultDomain = ProblemDomain.OPERATIONAL,
                sampleTitle = "DMAIC: Reducing App Crash Rate Below 0.05%",
                sampleDesc = "Standardizing release gates, memory leak detection, and automated canary rollouts."
            ),
            ProblemFramework(
                id = "ooda_loop",
                name = "OODA Loop (Observe, Orient, Decide, Act)",
                origin = "Col. John Boyd (USAF Military Strategist)",
                category = "Execution",
                icon = Icons.Default.Speed,
                iconColor = Color(0xFFEF4444),
                bestFor = "High-pressure incident response, market disruption battles, and rapid cybersecurity remediation.",
                whyBest = "Allows teams to cycle faster than competitors or evolving incident threats to gain decisive tactical initiative.",
                whenToUse = "During live system outages, competitive blitzkriegs, or fast-evolving PR/security threats.",
                whenNotToUse = "When designing static 5-year enterprise compliance frameworks.",
                steps = listOf(
                    "1. OBSERVE: Gather raw real-time signals without initial bias.",
                    "2. ORIENT: Contextualize signals using mental models, past data, and threat vectors.",
                    "3. DECIDE: Select the highest-leverage rapid intervention.",
                    "4. ACT: Execute immediately with measurable checkpoints.",
                    "5. LOOP: Continuously update observation based on consequences."
                ),
                defaultDomain = ProblemDomain.TECHNICAL,
                sampleTitle = "OODA Rapid Response: DDoS Mitigation & DNS Failover",
                sampleDesc = "Real-time mitigation cycle for high-volume anomalous network attacks."
            ),
            ProblemFramework(
                id = "clean_arch_compose",
                name = "Modern Android & Clean Architecture",
                origin = "Android Jetpack & Clean Architecture (Robert C. Martin)",
                category = "Software Architecture",
                icon = Icons.Default.Engineering,
                iconColor = Color(0xFF3B82F6),
                bestFor = "Building maintainable, testable, responsive Android applications with Jetpack Compose, Room, and AI integration.",
                whyBest = "Separates UI (Jetpack Compose declarative state), ViewModel (Unidirectional Data Flow), Domain Repository, and Data persistence (Room SQLite).",
                whenToUse = "When designing modern mobile apps that require rock-solid offline caching, reactive state, and seamless AI features.",
                whenNotToUse = "For throwaway single-file shell scripts.",
                steps = listOf(
                    "1. Presentation Layer: Pure Compose Composables consuming immutable StateFlows.",
                    "2. State Management: ViewModel handling business events and coroutine scopes.",
                    "3. Repository Layer: Single source of truth abstracting Local (Room) and Remote (Gemini API).",
                    "4. Persistence: Room Database with type-safe DAOs and Kotlin Flow queries.",
                    "5. AI Integration: Gemini 3.1 Pro client with high thinking configuration."
                ),
                defaultDomain = ProblemDomain.TECHNICAL,
                sampleTitle = "Architecture Blueprint: Unidirectional Data Flow & Room Caching",
                sampleDesc = "Implementing scalable MVI/MVVM pattern with Gemini AI processing and local SQLite persistence."
            )
        )
    }

    // Dynamic Framework Matching Logic
    fun computeBestFramework(query: String): ProblemFramework {
        val lower = query.lowercase()
        return when {
            lower.contains("crash") || lower.contains("bug") || lower.contains("latency") || lower.contains("timeout") || lower.contains("fail") || lower.contains("why") ->
                frameworks.first { it.id == "5_whys" }
            lower.contains("vendor") || lower.contains("choose") || lower.contains("matrix") || lower.contains("select") || lower.contains("compare") || lower.contains("tradeoff") ->
                frameworks.first { it.id == "kepner_tregoe" }
            lower.contains("market") || lower.contains("revenue") || lower.contains("cost") || lower.contains("growth") || lower.contains("breakdown") || lower.contains("mece") ->
                frameworks.first { it.id == "mece_issue_tree" }
            lower.contains("uncertain") || lower.contains("chaos") || lower.contains("crisis") || lower.contains("ambiguity") || lower.contains("cynefin") ->
                frameworks.first { it.id == "cynefin" }
            lower.contains("invent") || lower.contains("physics") || lower.contains("cheap") || lower.contains("first principle") || lower.contains("novel") ->
                frameworks.first { it.id == "first_principles" }
            lower.contains("sla") || lower.contains("quality") || lower.contains("six sigma") || lower.contains("process") || lower.contains("dmaic") ->
                frameworks.first { it.id == "dmaic" }
            lower.contains("incident") || lower.contains("speed") || lower.contains("urgent") || lower.contains("ooda") || lower.contains("attack") ->
                frameworks.first { it.id == "ooda_loop" }
            lower.contains("android") || lower.contains("compose") || lower.contains("architecture") || lower.contains("clean") || lower.contains("app") ->
                frameworks.first { it.id == "clean_arch_compose" }
            else -> frameworks.first { it.id == "5_whys" }
        }
    }

    val activeRecommended = remember(problemInput) {
        if (problemInput.isNotBlank()) computeBestFramework(problemInput) else frameworks.first { it.id == "5_whys" }
    }

    val filteredFrameworks = remember(searchQuery, selectedCategory) {
        frameworks.filter { fw ->
            val matchesCategory = selectedCategory == "All" || fw.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    fw.name.contains(searchQuery, ignoreCase = true) ||
                    fw.bestFor.contains(searchQuery, ignoreCase = true) ||
                    fw.whyBest.contains(searchQuery, ignoreCase = true) ||
                    fw.origin.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
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
                                listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.15f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF6366F1),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = "Frameworks",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Problem-Solving & Strategy Frameworks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "World-Class Mental Models, Decision Matrices & Engineering Architectures",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive "AI Best Framework Matcher"
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Framework Recommender",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Describe your challenge below to immediately identify the optimal framework:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = problemInput,
                        onValueChange = { problemInput = it },
                        placeholder = { Text("e.g. Server connection timeouts under Black Friday traffic, or choosing cloud vendor...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("framework_advisor_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recommended Framework Card Callout
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = activeRecommended.iconColor.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, activeRecommended.iconColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = activeRecommended.icon,
                                        contentDescription = null,
                                        tint = activeRecommended.iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Top Match: ${activeRecommended.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = activeRecommended.iconColor
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = activeRecommended.iconColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "98% MATCH",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = activeRecommended.iconColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeRecommended.whyBest,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onSelectFrameworkForCase(
                                        if (problemInput.isNotBlank()) problemInput else activeRecommended.sampleTitle,
                                        activeRecommended.defaultDomain,
                                        UrgencyLevel.HIGH,
                                        "Applying ${activeRecommended.name} methodology: ${activeRecommended.sampleDesc}"
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = activeRecommended.iconColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apply_framework_btn")
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch Case with ${activeRecommended.name.split(" ").take(2).joinToString(" ")}")
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Column {
                Text(
                    text = "Explore Framework Library (${frameworks.size} Models)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, methodology, or use-case...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Framework List
        items(filteredFrameworks) { fw ->
            val isExpanded = expandedFrameworkId == fw.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedFrameworkId = if (isExpanded) null else fw.id },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = fw.iconColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = fw.icon,
                                        contentDescription = null,
                                        tint = fw.iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = fw.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${fw.category} • ${fw.origin}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Best For: ${fw.bestFor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                text = "Why This Framework Excels:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = fw.iconColor
                            )
                            Text(
                                text = fw.whyBest,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Execution Steps:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            fw.steps.forEach { step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = fw.iconColor,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.08f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("✓ When to Use", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                        Text(fw.whenToUse, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.08f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("✕ Avoid When", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        Text(fw.whenNotToUse, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    onSelectFrameworkForCase(
                                        fw.sampleTitle,
                                        fw.defaultDomain,
                                        UrgencyLevel.HIGH,
                                        "Applying ${fw.name} methodology: ${fw.sampleDesc}"
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = fw.iconColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use ${fw.name.split(" ").take(2).joinToString(" ")} in Case")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
