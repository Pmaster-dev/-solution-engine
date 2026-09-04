package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProblemDomain
import com.example.data.model.RiskFactor
import com.example.data.model.StrategyMilestone
import com.example.data.model.StrategyOutlineResult
import com.example.data.model.StrategyPhase
import com.example.data.model.StrategyPromptPreset
import com.example.data.model.SuccessMetric
import com.example.data.model.UrgencyLevel
import com.example.ui.components.HighThinkingBadge

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StrategyOutlineScreen(
    strategyOutlines: List<StrategyOutlineResult>,
    selectedOutline: StrategyOutlineResult?,
    isGenerating: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSelectOutline: (StrategyOutlineResult) -> Unit,
    onGenerate: (
        title: String,
        description: String,
        domain: ProblemDomain,
        urgency: UrgencyLevel,
        framework: String,
        constraints: String,
        timelineWeeks: Int,
        useHighThinking: Boolean
    ) -> Unit,
    onConvertToCase: (StrategyOutlineResult) -> Unit,
    onToggleMilestone: (outlineId: String, phaseNumber: Int, milestoneId: String, isCompleted: Boolean) -> Unit,
    onDeleteOutline: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(if (selectedOutline != null) 1 else 0) }

    val presets = remember {
        listOf(
            StrategyPromptPreset(
                id = "preset_monolith",
                title = "Zero-Downtime Microservice Database Decomposition",
                domain = ProblemDomain.TECHNICAL,
                urgency = UrgencyLevel.HIGH,
                framework = "First Principles & Clean Architecture",
                description = "Monolithic PostgreSQL database suffers lock contention and deadlocks at peak traffic. We need an incremental, zero-downtime event-driven decomposition strategy to decouple order processing from catalog and inventory.",
                constraints = "Zero customer downtime SLA; 6-week delivery target; backward-compatible REST API endpoints",
                timelineWeeks = 6
            ),
            StrategyPromptPreset(
                id = "preset_churn",
                title = "Enterprise SaaS Churn Mitigation & Value Realization",
                domain = ProblemDomain.BUSINESS,
                urgency = UrgencyLevel.CRITICAL,
                framework = "MECE Issue Tree Decomposition",
                description = "Q3 enterprise customer churn surged by 18% following contract renewal cycle and new platform UI migration. Customers report feeling lost and missing legacy workflow efficiencies.",
                constraints = "Limited customer success headcount (4 managers for 120 accounts); Q4 renewal retention target > 92%",
                timelineWeeks = 4
            ),
            StrategyPromptPreset(
                id = "preset_seo_serp",
                title = "Core Web Vitals & SERP Rich Snippet Recovery",
                domain = ProblemDomain.SEO,
                urgency = UrgencyLevel.HIGH,
                framework = "5-Whys SERP Diagnostics & Schema Engineering",
                description = "High-intent transactional keyword rankings dropped from Position 2 to Position 8 following Google Core Update. Mobile LCP is 2.9s and structured FAQ Schema was corrupted.",
                constraints = "Frontend engineering sprint allocation: 2 engineers; 3-week sprint cycle",
                timelineWeeks = 3
            ),
            StrategyPromptPreset(
                id = "preset_velocity",
                title = "Cross-Functional Sprint Velocity & PR Cycle Remediation",
                domain = ProblemDomain.OPERATIONAL,
                urgency = UrgencyLevel.MEDIUM,
                framework = "DMAIC / Six Sigma Control Loop",
                description = "Average Pull Request review cycle ballooned to 4.8 days, resulting in missed release deadlines and engineer burnout. Flaky CI tests and unclear code ownership create bottlenecks.",
                constraints = "4 autonomous product squads (28 total engineers); existing GitHub Actions CI pipeline",
                timelineWeeks = 4
            )
        )
    }

    var inputTitle by remember { mutableStateOf("") }
    var inputDescription by remember { mutableStateOf("") }
    var inputDomain by remember { mutableStateOf(ProblemDomain.TECHNICAL) }
    var inputUrgency by remember { mutableStateOf(UrgencyLevel.HIGH) }
    var inputFramework by remember { mutableStateOf("Auto-Select") }
    var inputConstraints by remember { mutableStateOf("") }
    var inputTimelineWeeks by remember { mutableIntStateOf(4) }
    var useHighThinking by remember { mutableStateOf(true) }

    val frameworks = listOf(
        "Auto-Select",
        "MECE Issue Tree",
        "5-Whys Root Cause",
        "First Principles & Clean Architecture",
        "Cynefin Sensemaking",
        "DMAIC Six Sigma",
        "Kepner-Tregoe Matrix"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Strategy & Outline Studio",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        HighThinkingBadge(isHighThinking = true, compact = true)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Synthesize Strategy", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = if (strategyOutlines.isNotEmpty()) "Blueprints (${strategyOutlines.size})" else "Blueprint View",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                // Synthesis Input Form
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Gemini 3.1 Pro Strategic Engine",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Synthesizes multi-phase roadmaps, milestone checkpoints, risk mitigation matrices, and KPI telemetry from any problem description.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Preset Templates
                    item {
                        Text(
                            text = "💡 Quick Scenario Presets",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(presets) { preset ->
                                Surface(
                                    color = if (inputTitle == preset.title) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (inputTitle == preset.title) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier
                                        .clickable {
                                            inputTitle = preset.title
                                            inputDescription = preset.description
                                            inputDomain = preset.domain
                                            inputUrgency = preset.urgency
                                            inputFramework = preset.framework
                                            inputConstraints = preset.constraints
                                            inputTimelineWeeks = preset.timelineWeeks
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = preset.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Problem Title
                    item {
                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            label = { Text("Problem Title or Strategic Challenge") },
                            placeholder = { Text("e.g. Monolith Database Contention Under 5x Peak Traffic") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("strategy_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Problem Description
                    item {
                        OutlinedTextField(
                            value = inputDescription,
                            onValueChange = { inputDescription = it },
                            label = { Text("Deep Problem Description & Observed Symptoms *") },
                            placeholder = { Text("Explain what is failing, the operational impact, error logs, user complaints, and current bottlenecks...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .testTag("strategy_desc_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 6
                        )
                    }

                    // Domain & Urgency
                    item {
                        Text(
                            text = "Domain & Urgency Level",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProblemDomain.entries.forEach { dom ->
                                FilterChip(
                                    selected = inputDomain == dom,
                                    onClick = { inputDomain = dom },
                                    label = { Text(dom.displayName, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UrgencyLevel.entries.forEach { urg ->
                                FilterChip(
                                    selected = inputUrgency == urg,
                                    onClick = { inputUrgency = urg },
                                    label = { Text(urg.displayName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(urg.colorHex).copy(alpha = 0.2f),
                                        selectedLabelColor = Color(urg.colorHex)
                                    )
                                )
                            }
                        }
                    }

                    // Preferred Problem-Solving Framework
                    item {
                        Text(
                            text = "Problem-Solving Framework Preference",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            frameworks.forEach { fw ->
                                FilterChip(
                                    selected = inputFramework == fw,
                                    onClick = { inputFramework = fw },
                                    label = { Text(fw, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Constraints & Budget
                    item {
                        OutlinedTextField(
                            value = inputConstraints,
                            onValueChange = { inputConstraints = it },
                            label = { Text("Constraints, Budget, Team & SLA Boundaries") },
                            placeholder = { Text("e.g. Zero downtime allowance; 2 frontend devs + 1 architect; Q4 budget ceiling") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = false,
                            maxLines = 2
                        )
                    }

                    // Target Timeline Horizon Slider
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target Execution Horizon:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$inputTimelineWeeks Weeks (${inputTimelineWeeks * 5} Business Days)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = inputTimelineWeeks.toFloat(),
                                    onValueChange = { inputTimelineWeeks = it.toInt() },
                                    valueRange = 1f..12f,
                                    steps = 10
                                )
                            }
                        }
                    }

                    // High Thinking Mode Toggle
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6366F1).copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "High Thinking",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Gemini 3.1 Pro High Thinking",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = "Deep multi-step reasoning with expanded token budget",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = useHighThinking,
                                onCheckedChange = { useHighThinking = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF6366F1),
                                    checkedTrackColor = Color(0xFF6366F1).copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Generate Action Button
                    item {
                        Button(
                            onClick = {
                                onGenerate(
                                    inputTitle,
                                    inputDescription,
                                    inputDomain,
                                    inputUrgency,
                                    inputFramework,
                                    inputConstraints,
                                    inputTimelineWeeks,
                                    useHighThinking
                                )
                                selectedTab = 1
                            },
                            enabled = !isGenerating && inputDescription.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_strategy_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Synthesizing Strategy Blueprint with Gemini...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Structured Strategy Blueprint", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else {
                // Strategy Outline Viewer Tab
                if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Gemini 3.1 Pro is deconstructing the problem...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Applying ${inputFramework.ifBlank { "MECE & First Principles" }} and computing milestone roadmaps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (selectedOutline == null && strategyOutlines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Strategy Blueprints Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Describe a challenge in the Synthesize Strategy tab to generate a structured roadmap.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { selectedTab = 0 }) {
                                Text("Go to Strategy Studio")
                            }
                        }
                    }
                } else {
                    val activeOutline = selectedOutline ?: strategyOutlines.first()

                    StrategyOutlineViewerContent(
                        outline = activeOutline,
                        allOutlines = strategyOutlines,
                        onSelectOutline = onSelectOutline,
                        onConvertToCase = { onConvertToCase(activeOutline) },
                        onToggleMilestone = { phaseNum, milestoneId, completed ->
                            onToggleMilestone(activeOutline.id, phaseNum, milestoneId, completed)
                        },
                        onDeleteOutline = {
                            onDeleteOutline(activeOutline.id)
                        },
                        onCopyMarkdown = {
                            copyStrategyToClipboard(context, activeOutline)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StrategyOutlineViewerContent(
    outline: StrategyOutlineResult,
    allOutlines: List<StrategyOutlineResult>,
    onSelectOutline: (StrategyOutlineResult) -> Unit,
    onConvertToCase: () -> Unit,
    onToggleMilestone: (phaseNumber: Int, milestoneId: String, isCompleted: Boolean) -> Unit,
    onDeleteOutline: () -> Unit,
    onCopyMarkdown: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Outline Switcher (if multiple)
        if (allOutlines.size > 1) {
            item {
                Text(
                    text = "Saved Blueprints (${allOutlines.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allOutlines) { itemOutline ->
                        val isSelected = itemOutline.id == outline.id
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.clickable { onSelectOutline(itemOutline) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = itemOutline.problemTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Blueprint Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            color = Color(outline.urgency.colorHex).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = outline.urgency.displayName,
                                color = Color(outline.urgency.colorHex),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HighThinkingBadge(isHighThinking = outline.isHighThinkingUsed, compact = true)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDeleteOutline,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = outline.problemTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Framework: ${outline.frameworkUsed}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = outline.executiveSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Root Cause Callout
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Root Cause Bottleneck:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = outline.rootCauseHypothesis,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (outline.coreConstraints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Core Constraints & Boundaries:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            outline.coreConstraints.forEach { constraint ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "• $constraint",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Strategic Roadmap Phases Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗺️ Multi-Phase Strategic Execution Roadmap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${outline.strategicRoadmapPhases.size} Phases",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        items(outline.strategicRoadmapPhases) { phase ->
            PhaseRoadmapCard(
                phase = phase,
                onToggleMilestone = { milestoneId, isCompleted ->
                    onToggleMilestone(phase.phaseNumber, milestoneId, isCompleted)
                }
            )
        }

        // Risk Mitigation Matrix
        if (outline.riskMatrix.isNotEmpty()) {
            item {
                Text(
                    text = "🛡️ Risk Factors & Contingency Fallbacks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(outline.riskMatrix) { risk ->
                RiskFactorCard(risk = risk)
            }
        }

        // Success Metrics & Telemetry
        if (outline.successMetrics.isNotEmpty()) {
            item {
                Text(
                    text = "📊 Success Metrics & KPI Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    outline.successMetrics.forEach { metric ->
                        SuccessMetricCard(metric = metric, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Quick Win Recommendations
        if (outline.quickWinRecommendations.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🚀 Immediate Quick Wins (< 48h)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        outline.quickWinRecommendations.forEach { qwin ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("⚡", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = qwin,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Row
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyMarkdown,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Markdown", fontSize = 13.sp)
                }

                Button(
                    onClick = onConvertToCase,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = "Convert", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track as Active Case", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhaseRoadmapCard(
    phase: StrategyPhase,
    onToggleMilestone: (milestoneId: String, isCompleted: Boolean) -> Unit
) {
    val completedCount = phase.milestones.count { it.isCompleted }
    val totalCount = phase.milestones.size

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (completedCount == totalCount && totalCount > 0) Color(0xFF10B981).copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phase.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = phase.timelineWeeks,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = phase.objective,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Milestones Interactive Checklist
            Text(
                text = "Milestones & Checkpoints ($completedCount/$totalCount completed):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            phase.milestones.forEach { milestone ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleMilestone(milestone.id, !milestone.isCompleted) }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (milestone.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (milestone.isCompleted) "Completed" else "Incomplete",
                        tint = if (milestone.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = milestone.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (milestone.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (milestone.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                        if (milestone.description.isNotBlank()) {
                            Text(
                                text = milestone.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = milestone.timeline,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (phase.deliverables.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Key Deliverables: " + phase.deliverables.joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RiskFactorCard(risk: RiskFactor) {
    val impactColor = when (risk.impact.lowercase()) {
        "critical" -> Color(0xFFEF4444)
        "high" -> Color(0xFFF59E0B)
        else -> Color(0xFF3B82F6)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = impactColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Impact: ${risk.impact} | Prob: ${risk.probability}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = impactColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = risk.riskDescription,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mitigation Fallback: ${risk.mitigationPlan}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SuccessMetricCard(
    metric: SuccessMetric,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = metric.metricName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Baseline", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                    Text(metric.baseline, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF10B981))
                    Text(metric.target, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Horizon: ${metric.timeframe}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun copyStrategyToClipboard(context: Context, outline: StrategyOutlineResult) {
    val markdown = buildString {
        appendLine("# Strategic Blueprint: ${outline.problemTitle}")
        appendLine("**Framework:** ${outline.frameworkUsed} | **Domain:** ${outline.domain.displayName} | **Urgency:** ${outline.urgency.displayName}")
        appendLine()
        appendLine("## Executive Summary")
        appendLine(outline.executiveSummary)
        appendLine()
        appendLine("**Root Cause Bottleneck:** ${outline.rootCauseHypothesis}")
        appendLine()
        if (outline.coreConstraints.isNotEmpty()) {
            appendLine("## Constraints & Boundaries")
            outline.coreConstraints.forEach { appendLine("- $it") }
            appendLine()
        }
        appendLine("## Multi-Phase Strategic Execution Roadmap")
        outline.strategicRoadmapPhases.forEach { phase ->
            appendLine("### ${phase.name} (${phase.timelineWeeks})")
            appendLine(phase.objective)
            appendLine()
            appendLine("**Milestones:**")
            phase.milestones.forEach { m ->
                val check = if (m.isCompleted) "[x]" else "[ ]"
                appendLine("- $check **${m.title}** (${m.timeline}): ${m.deliverable}")
            }
            if (phase.deliverables.isNotEmpty()) {
                appendLine("**Deliverables:** ${phase.deliverables.joinToString(", ")}")
            }
            appendLine()
        }
        if (outline.riskMatrix.isNotEmpty()) {
            appendLine("## Risk Mitigation & Contingency Matrix")
            outline.riskMatrix.forEach { r ->
                appendLine("- **Risk:** ${r.riskDescription} (Impact: ${r.impact}, Prob: ${r.probability})")
                appendLine("  - **Mitigation:** ${r.mitigationPlan}")
            }
            appendLine()
        }
        if (outline.successMetrics.isNotEmpty()) {
            appendLine("## Success Telemetry & KPIs")
            outline.successMetrics.forEach { m ->
                appendLine("- **${m.metricName}:** Baseline: ${m.baseline} -> Target: ${m.target} (${m.timeframe})")
            }
            appendLine()
        }
        if (outline.quickWinRecommendations.isNotEmpty()) {
            appendLine("## Immediate Quick Wins (< 48h)")
            outline.quickWinRecommendations.forEach { appendLine("- ⚡ $it") }
            appendLine()
        }
        appendLine("---")
        appendLine("*Generated by Solutions Engine AI powered by Gemini 3.1 Pro High Thinking*")
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Strategy Outline", markdown)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Strategic Outline copied to clipboard (Markdown)!", Toast.LENGTH_LONG).show()
}
