package com.example.data.model

import java.util.UUID

enum class ProblemDomain(val displayName: String, val iconName: String) {
    TECHNICAL("Technical & Systems", "Memory"),
    BUSINESS("Business & Strategy", "TrendingUp"),
    SEO("SEO & Search Growth", "TravelExplore"),
    PERSONAL("Personal & Career", "Person"),
    OPERATIONAL("Operational & Process", "Settings"),
    ORGANIZATIONAL("Team & Culture", "Groups")
}

enum class UrgencyLevel(val displayName: String, val colorHex: Long) {
    CRITICAL("Critical Emergency", 0xFFEF4444),
    HIGH("High Urgency", 0xFFF59E0B),
    MEDIUM("Medium Priority", 0xFF3B82F6),
    LOW("Low Priority", 0xFF10B981)
}

enum class CaseStatus(val displayName: String) {
    ANALYZING("AI Analyzing"),
    ACTIVE("Execution In Progress"),
    SOLVED("Resolved"),
    ARCHIVED("Archived")
}

enum class SubscriptionTier(
    val title: String,
    val priceMonthly: Double,
    val priceYearly: Double,
    val maxCasesPerMonth: Int,
    val badgeLabel: String,
    val features: List<String>
) {
    FREE(
        title = "Free Starter",
        priceMonthly = 0.0,
        priceYearly = 0.0,
        maxCasesPerMonth = 3,
        badgeLabel = "FREE",
        features = listOf(
            "Standard 5-Whys Diagnosis",
            "Basic Solution Generator",
            "3 Cases / month limit",
            "Standard Speed"
        )
    ),
    PRO(
        title = "Pro Strategist",
        priceMonthly = 14.99,
        priceYearly = 149.99,
        maxCasesPerMonth = 9999,
        badgeLabel = "PRO ⚡",
        features = listOf(
            "Gemini 3.1 Pro High Thinking Mode",
            "Unlimited 5-Whys Problem Cases",
            "Complete SEO Engine & SERP Audits",
            "Schema.org JSON-LD Generator & Exporter",
            "Full Decision Matrix & Action Plan Tracker",
            "Priority AI Reasoning Pipeline"
        )
    ),
    ENTERPRISE(
        title = "Enterprise Architect",
        priceMonthly = 49.99,
        priceYearly = 499.99,
        maxCasesPerMonth = 99999,
        badgeLabel = "ENTERPRISE ★",
        features = listOf(
            "All Pro Strategist Features Included",
            "Multi-Seat Team Collaboration",
            "Custom Decision Criteria Weights",
            "Export to Webhook, CSV & PDF",
            "Unlimited Thinking Token Budgets",
            "Dedicated Support & Custom Integrations"
        )
    )
}

data class UserProfile(
    val id: String = "user_google_default",
    val displayName: String = "Alex Rivera",
    val email: String = "alex.rivera@gmail.com",
    val avatarUrl: String? = null,
    val authProvider: String = "Google Account",
    val tier: SubscriptionTier = SubscriptionTier.PRO,
    val isHighThinkingEnabled: Boolean = true,
    val tokensUsed: Int = 1420,
    val memberSince: Long = System.currentTimeMillis() - 86400000L * 45
)

data class SubscriptionDetails(
    val tier: SubscriptionTier = SubscriptionTier.PRO,
    val billingCycle: String = "Monthly", // "Monthly" or "Yearly"
    val isAutoRenew: Boolean = true,
    val startDate: Long = System.currentTimeMillis() - 86400000L * 15,
    val expiryDate: Long = System.currentTimeMillis() + 86400000L * 15,
    val transactionId: String = "GPA.3391-7294-8192-0941"
)

data class WhyStep(
    val level: Int,
    val question: String,
    val answer: String
)

data class ActionTask(
    val id: String = UUID.randomUUID().toString(),
    val taskDescription: String,
    val isCompleted: Boolean = false,
    val assigneeRole: String = "Owner",
    val estimatedDays: Int = 1
)

data class SolutionStrategy(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val approachType: String, // e.g. "Quick Win", "Long-Term Fix", "Strategic Pivot"
    val summary: String,
    val pros: List<String>,
    val cons: List<String>,
    val riskScore: Int, // 1 (low) to 10 (high)
    val impactScore: Int, // 1 (low) to 10 (high)
    val costEffort: String, // e.g. "Low", "Medium", "High"
    val actionTasks: List<ActionTask>,
    val requiredResources: List<String>
)

data class DecisionMatrix(
    val criteriaList: List<String>,
    // Map<SolutionId, Map<Criterion, Score(1-10)>>
    val scores: Map<String, Map<String, Int>>,
    val winnerSolutionId: String? = null
)

data class ProblemCase(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val domain: ProblemDomain,
    val urgency: UrgencyLevel,
    val description: String,
    val context: String = "",
    val status: CaseStatus = CaseStatus.ANALYZING,
    val timestamp: Long = System.currentTimeMillis(),
    val fiveWhys: List<WhyStep> = emptyList(),
    val solutions: List<SolutionStrategy> = emptyList(),
    val decisionMatrix: DecisionMatrix? = null,
    val isHighThinkingUsed: Boolean = true
)

data class SeoAuditResult(
    val id: String = UUID.randomUUID().toString(),
    val targetUrl: String,
    val focusKeywords: List<String>,
    val issueCategory: String, // e.g. "SERP Rank Drop", "Technical Health", "Core Web Vitals", "Content Cannibalization"
    val healthScore: Int, // 0-100
    val domainAuthority: Int, // 0-100
    val lcpMs: Int, // e.g. 1850ms
    val clsScore: Double, // e.g. 0.04
    val inpMs: Int, // e.g. 120ms
    val isMobileFriendly: Boolean = true,
    val criticalIssues: List<String> = emptyList(),
    val fiveWhysAnalysis: List<WhyStep> = emptyList(),
    val recommendedMetaTitle: String = "",
    val recommendedMetaDesc: String = "",
    val schemaJsonLd: String = "",
    val actionTasks: List<ActionTask> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class StrategyMilestone(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val timeline: String,
    val deliverable: String,
    val isCompleted: Boolean = false
)

data class StrategyPhase(
    val phaseNumber: Int,
    val name: String, // e.g. "Phase 1: Immediate Triage & Symptom Containment"
    val objective: String,
    val timelineWeeks: String, // e.g. "Week 1 - 2"
    val milestones: List<StrategyMilestone>,
    val deliverables: List<String>
)

data class RiskFactor(
    val riskDescription: String,
    val probability: String, // "Low", "Medium", "High"
    val impact: String, // "Low", "Medium", "Critical"
    val mitigationPlan: String
)

data class SuccessMetric(
    val metricName: String,
    val baseline: String,
    val target: String,
    val timeframe: String
)

data class StrategyOutlineResult(
    val id: String = UUID.randomUUID().toString(),
    val problemTitle: String,
    val problemDescription: String,
    val domain: ProblemDomain = ProblemDomain.TECHNICAL,
    val urgency: UrgencyLevel = UrgencyLevel.HIGH,
    val frameworkUsed: String, // e.g. "MECE + First Principles", "5-Whys Root Cause", "Cynefin Sensemaking", "DMAIC Six Sigma", "Clean Architecture"
    val executiveSummary: String,
    val rootCauseHypothesis: String,
    val coreConstraints: List<String>,
    val strategicRoadmapPhases: List<StrategyPhase>,
    val riskMatrix: List<RiskFactor>,
    val successMetrics: List<SuccessMetric>,
    val quickWinRecommendations: List<String>,
    val isHighThinkingUsed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class StrategyPromptPreset(
    val id: String,
    val title: String,
    val domain: ProblemDomain,
    val urgency: UrgencyLevel,
    val framework: String,
    val description: String,
    val constraints: String,
    val timelineWeeks: Int
)
