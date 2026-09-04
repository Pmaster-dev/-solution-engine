package com.example.data.repository

import com.example.data.local.Converters
import com.example.data.local.SeoDao
import com.example.data.local.SolutionsDao
import com.example.data.local.StrategyOutlineDao
import com.example.data.local.SubscriptionDao
import com.example.data.local.UserDao
import com.example.data.model.ActionTask
import com.example.data.model.CaseStatus
import com.example.data.model.DecisionMatrix
import com.example.data.model.ProblemCase
import com.example.data.model.ProblemDomain
import com.example.data.model.SeoAuditResult
import com.example.data.model.SolutionStrategy
import com.example.data.model.StrategyOutlineResult
import com.example.data.model.SubscriptionDetails
import com.example.data.model.SubscriptionTier
import com.example.data.model.UrgencyLevel
import com.example.data.model.UserProfile
import com.example.data.model.WhyStep
import com.example.data.remote.GeminiSolutionsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class SolutionsRepository(
    private val solutionsDao: SolutionsDao,
    private val userDao: UserDao,
    private val subscriptionDao: SubscriptionDao,
    private val seoDao: SeoDao,
    private val strategyDao: StrategyOutlineDao,
    private val geminiService: GeminiSolutionsService = GeminiSolutionsService()
) {
    private val converters = Converters()

    val allCases: Flow<List<ProblemCase>> = solutionsDao.getAllCaseEntities().map { entities ->
        entities.map { converters.toProblemCase(it) }
    }

    val userProfile: Flow<UserProfile> = userDao.getUserProfile().map { entity ->
        if (entity != null) converters.toUserProfile(entity) else UserProfile()
    }

    val activeSubscription: Flow<SubscriptionDetails> = subscriptionDao.getActiveSubscription().map { entity ->
        if (entity != null) converters.toSubscription(entity) else SubscriptionDetails()
    }

    val allSeoAudits: Flow<List<SeoAuditResult>> = seoDao.getAllSeoAudits().map { entities ->
        entities.map { converters.toSeoAudit(it) }
    }

    val allStrategyOutlines: Flow<List<StrategyOutlineResult>> = strategyDao.getAllStrategyOutlines().map { entities ->
        entities.map { converters.toStrategyOutline(it) }
    }

    fun getCaseById(id: String): Flow<ProblemCase?> = solutionsDao.getCaseEntityById(id).map { entity ->
        entity?.let { converters.toProblemCase(it) }
    }

    fun getStrategyOutlineById(id: String): Flow<StrategyOutlineResult?> = strategyDao.getStrategyOutlineById(id).map { entity ->
        entity?.let { converters.toStrategyOutline(it) }
    }

    suspend fun generateStrategyOutline(
        problemTitle: String,
        problemDescription: String,
        domain: ProblemDomain,
        urgency: UrgencyLevel,
        frameworkPreference: String,
        constraints: String,
        targetTimelineWeeks: Int,
        useHighThinking: Boolean = true
    ): StrategyOutlineResult = withContext(Dispatchers.IO) {
        val result = geminiService.analyzeAndGenerateStrategyOutline(
            problemTitle = problemTitle,
            problemDescription = problemDescription,
            domain = domain,
            urgency = urgency,
            frameworkPreference = frameworkPreference,
            constraints = constraints,
            targetTimelineWeeks = targetTimelineWeeks,
            useHighThinking = useHighThinking
        )

        strategyDao.insertStrategyOutline(converters.fromStrategyOutline(result))

        // Update token usage
        val currentUser = userDao.getUserProfile().first()
        if (currentUser != null) {
            val updated = currentUser.copy(tokensUsed = currentUser.tokensUsed + (if (useHighThinking) 520 else 220))
            userDao.insertUserProfile(updated)
        }

        return@withContext result
    }

    suspend fun convertStrategyOutlineToCase(outline: StrategyOutlineResult): ProblemCase = withContext(Dispatchers.IO) {
        val whySteps = listOf(
            WhyStep(1, "Why is '${outline.problemTitle}' critical?", outline.executiveSummary),
            WhyStep(2, "What is the primary technical/operational bottleneck?", outline.rootCauseHypothesis),
            WhyStep(3, "What constraints restrict direct resolution?", outline.coreConstraints.joinToString("; ").ifBlank { "Resource and time constraints" }),
            WhyStep(4, "What framework provides optimal remediation?", "${outline.frameworkUsed} methodology"),
            WhyStep(5, "Strategic Resolution Vector:", "Execute ${outline.strategicRoadmapPhases.size}-phase roadmap and mitigations.")
        )

        val solutionStrategies = outline.strategicRoadmapPhases.mapIndexed { index, phase ->
            val tasks = phase.milestones.mapIndexed { mIdx, m ->
                ActionTask(
                    taskDescription = "[${phase.name.take(15)}] ${m.title}: ${m.deliverable}",
                    isCompleted = m.isCompleted,
                    assigneeRole = if (mIdx == 0) "Lead Architect" else "Engineering Lead",
                    estimatedDays = 2
                )
            }

            SolutionStrategy(
                title = phase.name,
                approachType = "Roadmap Phase ${phase.phaseNumber}",
                summary = phase.objective,
                pros = phase.deliverables.ifEmpty { listOf("Provides concrete milestone delivery", "Reduces risk exposure") },
                cons = listOf("Requires cross-team synchronization during ${phase.timelineWeeks}"),
                riskScore = (index + 1) * 2,
                impactScore = 9,
                costEffort = if (index == 0) "Low" else "Medium",
                actionTasks = tasks,
                requiredResources = phase.deliverables
            )
        }

        val criteriaList = listOf("Execution Speed", "Risk Mitigation", "System Stability", "Cost Efficiency")
        val scoresMap = mutableMapOf<String, Map<String, Int>>()
        solutionStrategies.forEachIndexed { idx, sol ->
            scoresMap[sol.id] = mapOf(
                "Execution Speed" to (9 - idx),
                "Risk Mitigation" to (8 + idx % 3),
                "System Stability" to (9 - (idx % 2)),
                "Cost Efficiency" to (8 - idx)
            )
        }

        val problemCase = ProblemCase(
            title = outline.problemTitle,
            domain = outline.domain,
            urgency = outline.urgency,
            description = outline.problemDescription,
            context = "Strategy Outline Blueprint: ${outline.frameworkUsed}. ${outline.executiveSummary}",
            status = CaseStatus.ACTIVE,
            timestamp = System.currentTimeMillis(),
            fiveWhys = whySteps,
            solutions = solutionStrategies,
            decisionMatrix = DecisionMatrix(
                criteriaList = criteriaList,
                scores = scoresMap,
                winnerSolutionId = solutionStrategies.firstOrNull()?.id
            ),
            isHighThinkingUsed = outline.isHighThinkingUsed
        )

        solutionsDao.insertCaseEntity(converters.fromProblemCase(problemCase))
        return@withContext problemCase
    }

    suspend fun toggleStrategyMilestone(
        outlineId: String,
        phaseNumber: Int,
        milestoneId: String,
        isCompleted: Boolean
    ) = withContext(Dispatchers.IO) {
        val entity = strategyDao.getStrategyOutlineById(outlineId).first() ?: return@withContext
        val outline = converters.toStrategyOutline(entity)

        val updatedPhases = outline.strategicRoadmapPhases.map { phase ->
            if (phase.phaseNumber == phaseNumber) {
                val updatedMilestones = phase.milestones.map { m ->
                    if (m.id == milestoneId) m.copy(isCompleted = isCompleted) else m
                }
                phase.copy(milestones = updatedMilestones)
            } else phase
        }

        val updatedOutline = outline.copy(strategicRoadmapPhases = updatedPhases)
        strategyDao.insertStrategyOutline(converters.fromStrategyOutline(updatedOutline))
    }

    suspend fun deleteStrategyOutline(outlineId: String) = withContext(Dispatchers.IO) {
        strategyDao.deleteStrategyOutlineById(outlineId)
    }

    suspend fun analyzeNewProblem(
        title: String,
        domain: ProblemDomain,
        urgency: UrgencyLevel,
        description: String,
        context: String,
        useHighThinking: Boolean = true
    ): ProblemCase = withContext(Dispatchers.IO) {
        val newCase = geminiService.analyzeAndGenerateSolutions(title, domain, urgency, description, context, useHighThinking)
        solutionsDao.insertCaseEntity(converters.fromProblemCase(newCase))

        // Update token usage
        val currentUser = userDao.getUserProfile().first()
        if (currentUser != null) {
            val updated = currentUser.copy(tokensUsed = currentUser.tokensUsed + (if (useHighThinking) 480 else 180))
            userDao.insertUserProfile(updated)
        }

        return@withContext newCase
    }

    suspend fun runSeoAudit(
        url: String,
        focusKeywords: String,
        issueCategory: String,
        competitorUrl: String,
        useHighThinking: Boolean = true
    ): SeoAuditResult = withContext(Dispatchers.IO) {
        val auditResult = geminiService.analyzeSeoAudit(url, focusKeywords, issueCategory, competitorUrl, useHighThinking)
        seoDao.insertSeoAudit(converters.fromSeoAudit(auditResult))

        // Also create an integrated problem case in the database under domain = SEO
        val seoCase = ProblemCase(
            title = "SEO Audit & Strategy: $url",
            domain = ProblemDomain.SEO,
            urgency = UrgencyLevel.HIGH,
            description = "SEO diagnostic analysis for $url targeting keywords: $focusKeywords. Issue: $issueCategory",
            context = "Health Score: ${auditResult.healthScore}/100, DA: ${auditResult.domainAuthority}/100, LCP: ${auditResult.lcpMs}ms",
            status = CaseStatus.ACTIVE,
            timestamp = System.currentTimeMillis(),
            fiveWhys = auditResult.fiveWhysAnalysis,
            solutions = listOf(
                SolutionStrategy(
                    title = "Search Ranking & Technical Optimization",
                    approachType = "SEO Strategy",
                    summary = "Implement Core Web Vitals fixes, Schema.org JSON-LD structured data, and metadata optimization.",
                    pros = listOf("Recovers lost organic traffic", "Enhances rich snippet CTR", "Permanent indexation improvements"),
                    cons = listOf("Search engine re-indexing requires 7-21 days"),
                    riskScore = 2,
                    impactScore = 9,
                    costEffort = "Medium",
                    actionTasks = auditResult.actionTasks,
                    requiredResources = listOf("SEO Specialist", "Frontend Developer", "Google Search Console")
                )
            ),
            decisionMatrix = null,
            isHighThinkingUsed = useHighThinking
        )
        solutionsDao.insertCaseEntity(converters.fromProblemCase(seoCase))

        return@withContext auditResult
    }

    suspend fun updateTaskCompletion(
        caseId: String,
        solutionId: String,
        taskId: String,
        isCompleted: Boolean
    ) = withContext(Dispatchers.IO) {
        val caseEntity = solutionsDao.getCaseEntityById(caseId).first() ?: return@withContext
        val currentCase = converters.toProblemCase(caseEntity)

        val updatedSolutions = currentCase.solutions.map { sol ->
            if (sol.id == solutionId) {
                val updatedTasks = sol.actionTasks.map { task ->
                    if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
                }
                sol.copy(actionTasks = updatedTasks)
            } else sol
        }

        val anyAllTasksCompleted = updatedSolutions.any { sol ->
            sol.actionTasks.isNotEmpty() && sol.actionTasks.all { it.isCompleted }
        }
        val newStatus = if (anyAllTasksCompleted) CaseStatus.SOLVED else currentCase.status

        val updatedCase = currentCase.copy(solutions = updatedSolutions, status = newStatus)
        solutionsDao.insertCaseEntity(converters.fromProblemCase(updatedCase))
    }

    suspend fun updateCaseStatus(caseId: String, newStatus: CaseStatus) = withContext(Dispatchers.IO) {
        val caseEntity = solutionsDao.getCaseEntityById(caseId).first() ?: return@withContext
        val currentCase = converters.toProblemCase(caseEntity)
        val updatedCase = currentCase.copy(status = newStatus)
        solutionsDao.insertCaseEntity(converters.fromProblemCase(updatedCase))
    }

    suspend fun deleteCase(caseId: String) = withContext(Dispatchers.IO) {
        solutionsDao.deleteCaseById(caseId)
    }

    suspend fun deleteSeoAudit(auditId: String) = withContext(Dispatchers.IO) {
        seoDao.deleteSeoAuditById(auditId)
    }

    // Google Authentication & User Profile Management
    suspend fun signInWithGoogle(displayName: String, email: String, avatarUrl: String?) = withContext(Dispatchers.IO) {
        val existing = userDao.getUserProfile().first()
        val user = (existing?.let { converters.toUserProfile(it) } ?: UserProfile()).copy(
            id = "google_" + UUID.randomUUID().toString().take(8),
            displayName = displayName,
            email = email,
            avatarUrl = avatarUrl,
            authProvider = "Google Account"
        )
        userDao.insertUserProfile(converters.fromUserProfile(user))
    }

    suspend fun signOutGoogle() = withContext(Dispatchers.IO) {
        val guestUser = UserProfile(
            id = "guest_user",
            displayName = "Guest User",
            email = "guest@solutionsengine.local",
            avatarUrl = null,
            authProvider = "Local Guest",
            tier = SubscriptionTier.FREE,
            isHighThinkingEnabled = false
        )
        userDao.insertUserProfile(converters.fromUserProfile(guestUser))
    }

    suspend fun toggleHighThinking(enabled: Boolean) = withContext(Dispatchers.IO) {
        val current = userDao.getUserProfile().first()?.let { converters.toUserProfile(it) } ?: UserProfile()
        val updated = current.copy(isHighThinkingEnabled = enabled)
        userDao.insertUserProfile(converters.fromUserProfile(updated))
    }

    // Subscription & Paywall Management
    suspend fun updateSubscription(tier: SubscriptionTier, billingCycle: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val duration = if (billingCycle.lowercase().contains("year")) 86400000L * 365 else 86400000L * 30
        val newSub = SubscriptionDetails(
            tier = tier,
            billingCycle = billingCycle,
            isAutoRenew = true,
            startDate = now,
            expiryDate = now + duration,
            transactionId = "GPA." + (1000..9999).random() + "-" + (1000..9999).random() + "-" + (1000..9999).random()
        )
        subscriptionDao.insertSubscription(converters.fromSubscription(newSub))

        // Sync user profile tier
        val currentUser = userDao.getUserProfile().first()?.let { converters.toUserProfile(it) } ?: UserProfile()
        val updatedUser = currentUser.copy(
            tier = tier,
            isHighThinkingEnabled = (tier != SubscriptionTier.FREE)
        )
        userDao.insertUserProfile(converters.fromUserProfile(updatedUser))
    }

    suspend fun cancelSubscriptionAutoRenew() = withContext(Dispatchers.IO) {
        val current = subscriptionDao.getActiveSubscription().first()?.let { converters.toSubscription(it) } ?: return@withContext
        val updated = current.copy(isAutoRenew = false)
        subscriptionDao.insertSubscription(converters.fromSubscription(updated))
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Initialize User Profile & Subscription if empty
        val userEntity = userDao.getUserProfile().first()
        if (userEntity == null) {
            val initialUser = UserProfile(
                displayName = "Alex Rivera",
                email = "alex.rivera@gmail.com",
                authProvider = "Google Account",
                tier = SubscriptionTier.PRO,
                isHighThinkingEnabled = true
            )
            userDao.insertUserProfile(converters.fromUserProfile(initialUser))
        }

        val subEntity = subscriptionDao.getActiveSubscription().first()
        if (subEntity == null) {
            val initialSub = SubscriptionDetails(
                tier = SubscriptionTier.PRO,
                billingCycle = "Monthly",
                isAutoRenew = true
            )
            subscriptionDao.insertSubscription(converters.fromSubscription(initialSub))
        }

        // Initialize SEO Audits if empty
        val existingSeo = seoDao.getAllSeoAudits().first()
        if (existingSeo.isEmpty()) {
            val sampleAudit = geminiService.generateFallbackSeoAudit(
                urlStr = "https://myshop-solutions.com",
                keywords = "ai problem solving, root cause analysis tool, decision matrix software",
                issueCategory = "SERP Rank Drop",
                competitorUrl = "https://competitor-solutions.com"
            )
            seoDao.insertSeoAudit(converters.fromSeoAudit(sampleAudit))
        }

        // Initialize Problem Cases if empty
        val existing = solutionsDao.getAllCaseEntities().first()
        if (existing.isEmpty()) {
            val sample1 = geminiService.generateFallbackSolutions(
                title = "High Latency & Connection Spikes in Backend API",
                domain = ProblemDomain.TECHNICAL,
                urgency = UrgencyLevel.CRITICAL,
                description = "P99 latency spiked from 120ms to 2.8s during peak hours, causing 504 gateway timeouts.",
                contextInfo = "Microservice architecture running on Kubernetes with Cloud PostgreSQL DB.",
                useHighThinking = true
            )

            val sample2 = geminiService.generateFallbackSolutions(
                title = "Organic Search Ranking Drop After Domain Migration",
                domain = ProblemDomain.SEO,
                urgency = UrgencyLevel.HIGH,
                description = "Top 3 commercial keyword rankings dropped to page 2 following site redesign and URL re-structuring.",
                contextInfo = "Ecommerce store with 120,000 monthly organic visitors before migration.",
                useHighThinking = true
            )

            val sample3 = geminiService.generateFallbackSolutions(
                title = "Customer Churn After Subscription Price Tiering",
                domain = ProblemDomain.BUSINESS,
                urgency = UrgencyLevel.HIGH,
                description = "15% drop in enterprise tier retention following Q3 pricing update without feature add-ons.",
                contextInfo = "SaaS B2B platform with 450 active enterprise clients.",
                useHighThinking = true
            )

            solutionsDao.insertCaseEntity(converters.fromProblemCase(sample1))
            solutionsDao.insertCaseEntity(converters.fromProblemCase(sample2))
            solutionsDao.insertCaseEntity(converters.fromProblemCase(sample3))
        }

        // Initialize Strategy Outlines if empty
        val existingStrategies = strategyDao.getAllStrategyOutlines().first()
        if (existingStrategies.isEmpty()) {
            val sampleStrategy = geminiService.generateFallbackStrategyOutline(
                title = "Zero-Downtime Microservice Database Decoupling & Event Mesh Migration",
                description = "Monolithic PostgreSQL database encounters deadlocks under peak transactional volume. Team needs an incremental, zero-downtime event-driven decomposition strategy.",
                domain = ProblemDomain.TECHNICAL,
                urgency = UrgencyLevel.HIGH,
                framework = "First Principles & Clean Architecture",
                constraints = "Zero downtime tolerance; Max 6 weeks timeline; Maintain backwards compatibility for mobile clients",
                timelineWeeks = 6,
                useHighThinking = true
            )
            strategyDao.insertStrategyOutline(converters.fromStrategyOutline(sampleStrategy))
        }
    }
}
