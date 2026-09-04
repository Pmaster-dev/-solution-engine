package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SolutionsDatabase
import com.example.data.model.CaseStatus
import com.example.data.model.ProblemCase
import com.example.data.model.ProblemDomain
import com.example.data.model.RiskFactor
import com.example.data.model.SeoAuditResult
import com.example.data.model.StrategyOutlineResult
import com.example.data.model.SubscriptionDetails
import com.example.data.model.SubscriptionTier
import com.example.data.model.UrgencyLevel
import com.example.data.model.UserProfile
import com.example.data.repository.SolutionsRepository
import com.example.server.AndroidLocalHttpServer
import com.example.server.HttpServerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SolutionsUiState(
    val cases: List<ProblemCase> = emptyList(),
    val filteredCases: List<ProblemCase> = emptyList(),
    val selectedDomain: ProblemDomain? = null,
    val selectedStatus: CaseStatus? = null,
    val searchQuery: String = "",
    val isAnalyzing: Boolean = false,
    val analysisError: String? = null,
    val activeCaseDetailId: String? = null,
    val userProfile: UserProfile = UserProfile(),
    val subscription: SubscriptionDetails = SubscriptionDetails(),
    val seoAudits: List<SeoAuditResult> = emptyList(),
    val strategyOutlines: List<StrategyOutlineResult> = emptyList(),
    val selectedStrategyOutline: StrategyOutlineResult? = null,
    val isGeneratingStrategy: Boolean = false,
    val strategyError: String? = null,
    val isSeoAnalyzing: Boolean = false,
    val isHighThinkingThinking: Boolean = false,
    val highThinkingResult: String? = null,
    val showPaywallDialog: Boolean = false,
    val showGoogleAuthDialog: Boolean = false,
    val userMessage: String? = null,
    val httpServerState: HttpServerState = HttpServerState()
)

class SolutionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SolutionsRepository
    private val httpServer = AndroidLocalHttpServer(application)

    private val _selectedDomain = MutableStateFlow<ProblemDomain?>(null)
    private val _selectedStatus = MutableStateFlow<CaseStatus?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isAnalyzing = MutableStateFlow(false)
    private val _analysisError = MutableStateFlow<String?>(null)
    private val _activeCaseDetailId = MutableStateFlow<String?>(null)

    private val _selectedStrategyOutline = MutableStateFlow<StrategyOutlineResult?>(null)
    private val _isGeneratingStrategy = MutableStateFlow(false)
    private val _strategyError = MutableStateFlow<String?>(null)

    private val _isSeoAnalyzing = MutableStateFlow(false)
    private val _isHighThinkingThinking = MutableStateFlow(false)
    private val _highThinkingResult = MutableStateFlow<String?>(null)
    private val _showPaywallDialog = MutableStateFlow(false)
    private val _showGoogleAuthDialog = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    init {
        val database = SolutionsDatabase.getDatabase(application)
        repository = SolutionsRepository(
            solutionsDao = database.solutionsDao(),
            userDao = database.userDao(),
            subscriptionDao = database.subscriptionDao(),
            seoDao = database.seoDao(),
            strategyDao = database.strategyOutlineDao()
        )
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    private data class FilterState(
        val domain: ProblemDomain?,
        val status: CaseStatus?,
        val query: String
    )

    private data class AccountState(
        val user: UserProfile,
        val sub: SubscriptionDetails,
        val seoAudits: List<SeoAuditResult>,
        val strategyOutlines: List<StrategyOutlineResult>
    )

    private data class ExtraState(
        val isAnalyzing: Boolean,
        val error: String?,
        val detailId: String?,
        val isSeoAnalyzing: Boolean,
        val isHighThinkingThinking: Boolean,
        val highThinkingResult: String?,
        val showPaywall: Boolean,
        val showGoogleAuth: Boolean,
        val userMessage: String?,
        val selectedStrategy: StrategyOutlineResult?,
        val isGeneratingStrategy: Boolean,
        val strategyError: String?
    )

    private val filterState: Flow<FilterState> = combine(
        _selectedDomain,
        _selectedStatus,
        _searchQuery
    ) { domain, status, query ->
        FilterState(domain, status, query)
    }

    private val accountState: Flow<AccountState> = combine(
        repository.userProfile,
        repository.activeSubscription,
        repository.allSeoAudits,
        repository.allStrategyOutlines
    ) { user, sub, seoAudits, strategyOutlines ->
        AccountState(user, sub, seoAudits, strategyOutlines)
    }

    private val extraStateA = combine(
        _isAnalyzing,
        _analysisError,
        _activeCaseDetailId,
        _isSeoAnalyzing,
        _selectedStrategyOutline
    ) { isAnalyzing, error, detailId, seoAnalyzing, selectedStrategy ->
        listOf(isAnalyzing, error, detailId, seoAnalyzing, selectedStrategy)
    }

    private val extraStateB = combine(
        _isHighThinkingThinking,
        _highThinkingResult,
        _showPaywallDialog,
        _showGoogleAuthDialog,
        _userMessage
    ) { highThinking, result, paywall, auth, msg ->
        listOf(highThinking, result, paywall, auth, msg)
    }

    private val extraStateC = combine(
        _isGeneratingStrategy,
        _strategyError
    ) { isGen, err ->
        listOf(isGen, err)
    }

    private val extraState: Flow<ExtraState> = combine(
        extraStateA,
        extraStateB,
        extraStateC
    ) { partA, partB, partC ->
        ExtraState(
            isAnalyzing = partA[0] as Boolean,
            error = partA[1] as String?,
            detailId = partA[2] as String?,
            isSeoAnalyzing = partA[3] as Boolean,
            selectedStrategy = partA[4] as StrategyOutlineResult?,
            isHighThinkingThinking = partB[0] as Boolean,
            highThinkingResult = partB[1] as String?,
            showPaywall = partB[2] as Boolean,
            showGoogleAuth = partB[3] as Boolean,
            userMessage = partB[4] as String?,
            isGeneratingStrategy = partC[0] as Boolean,
            strategyError = partC[1] as String?
        )
    }

    val uiState: StateFlow<SolutionsUiState> = combine(
        repository.allCases,
        accountState,
        filterState,
        extraState,
        httpServer.serverState
    ) { cases, account, filter, extra, serverState ->
        val filtered = cases.filter { case ->
            val matchesDomain = filter.domain == null || case.domain == filter.domain
            val matchesStatus = filter.status == null || case.status == filter.status
            val matchesQuery = filter.query.isBlank() ||
                    case.title.contains(filter.query, ignoreCase = true) ||
                    case.description.contains(filter.query, ignoreCase = true) ||
                    case.domain.displayName.contains(filter.query, ignoreCase = true)
            matchesDomain && matchesStatus && matchesQuery
        }

        val activeStrategy = extra.selectedStrategy ?: account.strategyOutlines.firstOrNull()

        SolutionsUiState(
            cases = cases,
            filteredCases = filtered,
            selectedDomain = filter.domain,
            selectedStatus = filter.status,
            searchQuery = filter.query,
            isAnalyzing = extra.isAnalyzing,
            analysisError = extra.error,
            activeCaseDetailId = extra.detailId,
            userProfile = account.user,
            subscription = account.sub,
            seoAudits = account.seoAudits,
            strategyOutlines = account.strategyOutlines,
            selectedStrategyOutline = activeStrategy,
            isGeneratingStrategy = extra.isGeneratingStrategy,
            strategyError = extra.strategyError,
            isSeoAnalyzing = extra.isSeoAnalyzing,
            isHighThinkingThinking = extra.isHighThinkingThinking,
            highThinkingResult = extra.highThinkingResult,
            showPaywallDialog = extra.showPaywall,
            showGoogleAuthDialog = extra.showGoogleAuth,
            userMessage = extra.userMessage,
            httpServerState = serverState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SolutionsUiState()
    )

    fun startHttpServer(port: Int = 8080) {
        viewModelScope.launch {
            httpServer.startServer(port)
            _userMessage.value = "HTTP server started on port $port"
        }
    }

    fun stopHttpServer() {
        viewModelScope.launch {
            httpServer.stopServer()
            _userMessage.value = "HTTP server stopped"
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedDomain(domain: ProblemDomain?) {
        _selectedDomain.value = domain
    }

    fun setSelectedStatus(status: CaseStatus?) {
        _selectedStatus.value = status
    }

    fun setActiveCaseDetail(caseId: String?) {
        _activeCaseDetailId.value = caseId
    }

    fun setShowPaywall(show: Boolean) {
        _showPaywallDialog.value = show
    }

    fun setShowGoogleAuth(show: Boolean) {
        _showGoogleAuthDialog.value = show
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun selectStrategyOutline(outline: StrategyOutlineResult?) {
        _selectedStrategyOutline.value = outline
    }

    fun generateStrategyOutline(
        title: String,
        description: String,
        domain: ProblemDomain = ProblemDomain.TECHNICAL,
        urgency: UrgencyLevel = UrgencyLevel.HIGH,
        framework: String = "Auto-Select",
        constraints: String = "",
        timelineWeeks: Int = 4,
        useHighThinking: Boolean = true,
        onSuccess: ((StrategyOutlineResult) -> Unit)? = null
    ) {
        if (description.isBlank()) {
            _strategyError.value = "Please provide a problem description to generate strategy"
            return
        }

        _isGeneratingStrategy.value = true
        _strategyError.value = null

        viewModelScope.launch {
            try {
                val outline = repository.generateStrategyOutline(
                    problemTitle = title.ifBlank { "Systemic Strategy Blueprint" },
                    problemDescription = description,
                    domain = domain,
                    urgency = urgency,
                    frameworkPreference = framework,
                    constraints = constraints,
                    targetTimelineWeeks = timelineWeeks,
                    useHighThinking = useHighThinking
                )
                _isGeneratingStrategy.value = false
                _selectedStrategyOutline.value = outline
                _userMessage.value = "Strategic roadmap & outline synthesized with Gemini 3.1 Pro!"
                onSuccess?.invoke(outline)
            } catch (e: Exception) {
                _isGeneratingStrategy.value = false
                _strategyError.value = "Strategy synthesis failed: ${e.localizedMessage}"
            }
        }
    }

    fun convertStrategyOutlineToCase(outline: StrategyOutlineResult, onCaseCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val newCase = repository.convertStrategyOutlineToCase(outline)
                _activeCaseDetailId.value = newCase.id
                _userMessage.value = "Strategy blueprint converted to active tracked case!"
                onCaseCreated(newCase.id)
            } catch (e: Exception) {
                _userMessage.value = "Could not convert strategy: ${e.localizedMessage}"
            }
        }
    }

    fun toggleStrategyMilestone(outlineId: String, phaseNumber: Int, milestoneId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleStrategyMilestone(outlineId, phaseNumber, milestoneId, isCompleted)
        }
    }

    fun deleteStrategyOutline(outlineId: String) {
        viewModelScope.launch {
            repository.deleteStrategyOutline(outlineId)
            if (_selectedStrategyOutline.value?.id == outlineId) {
                _selectedStrategyOutline.value = null
            }
            _userMessage.value = "Strategy outline removed"
        }
    }

    fun analyzeProblem(
        title: String,
        domain: ProblemDomain,
        urgency: UrgencyLevel,
        description: String,
        context: String,
        useHighThinking: Boolean = true,
        onSuccess: (String) -> Unit
    ) {
        if (title.isBlank() || description.isBlank()) {
            _analysisError.value = "Please fill in problem title and description"
            return
        }

        val currentTier = uiState.value.userProfile.tier
        if (currentTier == SubscriptionTier.FREE && uiState.value.cases.size >= currentTier.maxCasesPerMonth) {
            _showPaywallDialog.value = true
            _userMessage.value = "Free plan limit reached (3 cases). Upgrade to Pro for unlimited cases!"
            return
        }

        _isAnalyzing.value = true
        _analysisError.value = null

        viewModelScope.launch {
            try {
                val newCase = repository.analyzeNewProblem(title, domain, urgency, description, context, useHighThinking)
                _isAnalyzing.value = false
                _activeCaseDetailId.value = newCase.id
                _userMessage.value = "Problem analyzed with ${if (useHighThinking) "Gemini 3.1 Pro (High Thinking)" else "Standard AI"}"
                onSuccess(newCase.id)
            } catch (e: Exception) {
                _isAnalyzing.value = false
                _analysisError.value = "Analysis failed: ${e.localizedMessage}"
            }
        }
    }

    fun runSeoAudit(
        url: String,
        keywords: String,
        category: String,
        competitorUrl: String,
        useHighThinking: Boolean = true,
        onSuccess: (String) -> Unit
    ) {
        if (url.isBlank()) {
            _userMessage.value = "Please enter a valid website URL for SEO analysis"
            return
        }

        _isSeoAnalyzing.value = true
        viewModelScope.launch {
            try {
                val result = repository.runSeoAudit(url, keywords, category, competitorUrl, useHighThinking)
                _isSeoAnalyzing.value = false
                _userMessage.value = "SEO Audit complete for $url! Health Score: ${result.healthScore}/100"
                onSuccess(result.id)
            } catch (e: Exception) {
                _isSeoAnalyzing.value = false
                _userMessage.value = "SEO audit error: ${e.localizedMessage}"
            }
        }
    }

    fun executeDeepHighThinking(problemQuery: String, domainContext: String) {
        if (problemQuery.isBlank()) {
            _userMessage.value = "Please enter a complex problem scenario for High Thinking mode."
            return
        }

        _isHighThinkingThinking.value = true
        _highThinkingResult.value = null

        viewModelScope.launch {
            try {
                val newCase = repository.analyzeNewProblem(
                    title = "Deep Thinking: ${problemQuery.take(40)}...",
                    domain = ProblemDomain.TECHNICAL,
                    urgency = UrgencyLevel.HIGH,
                    description = problemQuery,
                    context = domainContext,
                    useHighThinking = true
                )
                _isHighThinkingThinking.value = false
                _highThinkingResult.value = """
                    ⭐ 5-Whys Deep Root Cause:
                    ${newCase.fiveWhys.lastOrNull()?.answer ?: "Systemic operational constraint identified."}
                    
                    💡 Top Recommended Strategy:
                    ${newCase.solutions.firstOrNull()?.title}: ${newCase.solutions.firstOrNull()?.summary}
                    
                    📋 Key Execution Tasks:
                    ${newCase.solutions.firstOrNull()?.actionTasks?.joinToString("\n") { "• ${it.taskDescription} (${it.estimatedDays}d)" } ?: "Tasks generated."}
                """.trimIndent()
                _userMessage.value = "Gemini 3.1 Pro High Thinking synthesis complete!"
            } catch (e: Exception) {
                _isHighThinkingThinking.value = false
                _userMessage.value = "Thinking mode error: ${e.localizedMessage}"
            }
        }
    }

    fun toggleTask(caseId: String, solutionId: String, taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskCompletion(caseId, solutionId, taskId, isCompleted)
        }
    }

    fun updateCaseStatus(caseId: String, status: CaseStatus) {
        viewModelScope.launch {
            repository.updateCaseStatus(caseId, status)
        }
    }

    fun deleteCase(caseId: String) {
        viewModelScope.launch {
            repository.deleteCase(caseId)
            if (_activeCaseDetailId.value == caseId) {
                _activeCaseDetailId.value = null
            }
            _userMessage.value = "Case deleted"
        }
    }

    fun deleteSeoAudit(auditId: String) {
        viewModelScope.launch {
            repository.deleteSeoAudit(auditId)
            _userMessage.value = "SEO Audit report removed"
        }
    }

    // Google Auth Methods
    fun signInGoogle(displayName: String, email: String, avatarUrl: String?) {
        viewModelScope.launch {
            repository.signInWithGoogle(displayName, email, avatarUrl)
            _showGoogleAuthDialog.value = false
            _userMessage.value = "Signed in as $displayName ($email)"
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            repository.signOutGoogle()
            _showGoogleAuthDialog.value = false
            _userMessage.value = "Signed out of Google account."
        }
    }

    fun toggleHighThinking(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleHighThinking(enabled)
            _userMessage.value = if (enabled) "⚡ Gemini 3.1 Pro High Thinking Enabled" else "Standard AI mode enabled"
        }
    }

    // Subscription Billing Methods
    fun upgradeSubscription(tier: SubscriptionTier, billingCycle: String) {
        viewModelScope.launch {
            repository.updateSubscription(tier, billingCycle)
            _showPaywallDialog.value = false
            _userMessage.value = "🎉 Successfully upgraded to ${tier.title} (${billingCycle}) via Google Play!"
        }
    }

    fun cancelSubscriptionAutoRenew() {
        viewModelScope.launch {
            repository.cancelSubscriptionAutoRenew()
            _userMessage.value = "Auto-renew disabled. Plan active until end of billing cycle."
        }
    }
}
