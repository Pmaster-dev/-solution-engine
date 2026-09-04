package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.ProblemDomain
import com.example.data.model.UrgencyLevel
import com.example.ui.SolutionsUiState
import com.example.ui.SolutionsViewModel
import com.example.ui.components.GoogleAuthDialog
import com.example.ui.components.SubscriptionPaywallDialog
import com.example.ui.components.SubscriptionTierBadge
import com.example.ui.screens.AccountSubscriptionScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CaseDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FrameworksHubScreen
import com.example.ui.screens.HighThinkingScreen
import com.example.ui.screens.HttpServerScreen
import com.example.ui.screens.NewProblemScreen
import com.example.ui.screens.SeoStudioScreen
import com.example.ui.screens.StrategyOutlineScreen
import com.example.ui.theme.SolutionsEngineTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SolutionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SolutionsEngineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SolutionsEngineApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolutionsEngineApp(viewModel: SolutionsViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    // Google Auth Dialog
    if (uiState.showGoogleAuthDialog) {
        GoogleAuthDialog(
            userProfile = uiState.userProfile,
            onDismiss = { viewModel.setShowGoogleAuth(false) },
            onSignIn = { name, email ->
                viewModel.signInGoogle(name, email, null)
            },
            onSignOut = {
                viewModel.signOutGoogle()
            }
        )
    }

    // Subscription / Paywall Dialog
    if (uiState.showPaywallDialog) {
        SubscriptionPaywallDialog(
            currentTier = uiState.userProfile.tier,
            onDismiss = { viewModel.setShowPaywall(false) },
            onUpgrade = { tier, cycle ->
                viewModel.upgradeSubscription(tier, cycle)
            }
        )
    }

    val bottomNavRoutes = listOf("dashboard", "strategy_studio", "frameworks", "seo_studio", "high_thinking", "account")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentRoute in bottomNavRoutes && currentRoute != "strategy_studio") {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentRoute) {
                                    "frameworks" -> "Frameworks & Models"
                                    "seo_studio" -> "SEO Engine"
                                    "high_thinking" -> "High Thinking"
                                    "account" -> "Account & Billing"
                                    else -> "Solutions Engine"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    actions = {
                        SubscriptionTierBadge(
                            tier = uiState.userProfile.tier,
                            onClick = { viewModel.setShowPaywall(true) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Google User Avatar
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.setShowGoogleAuth(true) }
                                .testTag("google_profile_avatar_topbar")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = uiState.userProfile.displayName.take(1).uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Cases") },
                        label = { Text("Cases", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_cases")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "strategy_studio",
                        onClick = {
                            navController.navigate("strategy_studio") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Strategy") },
                        label = { Text("Strategy", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_strategy")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "frameworks",
                        onClick = {
                            navController.navigate("frameworks") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.MilitaryTech, contentDescription = "Frameworks") },
                        label = { Text("Models", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_frameworks")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "seo_studio",
                        onClick = {
                            navController.navigate("seo_studio") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.TravelExplore, contentDescription = "SEO") },
                        label = { Text("SEO", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_seo")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "high_thinking",
                        onClick = {
                            navController.navigate("high_thinking") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "High Thinking") },
                        label = { Text("Thinking", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_thinking")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "account",
                        onClick = {
                            navController.navigate("account") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Account") },
                        label = { Text("Account", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_account")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    uiState = uiState,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onDomainFilterChange = { viewModel.setSelectedDomain(it) },
                    onStatusFilterChange = { viewModel.setSelectedStatus(it) },
                    onSelectCase = { caseId ->
                        viewModel.setActiveCaseDetail(caseId)
                        navController.navigate("case_detail/$caseId")
                    },
                    onNavigateNewProblem = { navController.navigate("new_problem") },
                    onQuickTemplate = { title, domain, urgency, desc ->
                        viewModel.analyzeProblem(title, domain, urgency, desc, "Quick starter template case.", true) { newCaseId ->
                            navController.navigate("case_detail/$newCaseId")
                        }
                    },
                    onNavigateAnalytics = { navController.navigate("analytics") },
                    onNavigateStrategyStudio = { navController.navigate("strategy_studio") }
                )
            }

            composable("strategy_studio") {
                StrategyOutlineScreen(
                    strategyOutlines = uiState.strategyOutlines,
                    selectedOutline = uiState.selectedStrategyOutline,
                    isGenerating = uiState.isGeneratingStrategy,
                    errorMessage = uiState.strategyError,
                    onBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("dashboard")
                        }
                    },
                    onSelectOutline = { outline ->
                        viewModel.selectStrategyOutline(outline)
                    },
                    onGenerate = { title, desc, domain, urgency, fw, constraints, timeline, useHighThinking ->
                        viewModel.generateStrategyOutline(
                            title = title,
                            description = desc,
                            domain = domain,
                            urgency = urgency,
                            framework = fw,
                            constraints = constraints,
                            timelineWeeks = timeline,
                            useHighThinking = useHighThinking
                        )
                    },
                    onConvertToCase = { outline ->
                        viewModel.convertStrategyOutlineToCase(outline) { newCaseId ->
                            navController.navigate("case_detail/$newCaseId")
                        }
                    },
                    onToggleMilestone = { outlineId, phaseNum, milestoneId, isDone ->
                        viewModel.toggleStrategyMilestone(outlineId, phaseNum, milestoneId, isDone)
                    },
                    onDeleteOutline = { outlineId ->
                        viewModel.deleteStrategyOutline(outlineId)
                    }
                )
            }

            composable("frameworks") {
                FrameworksHubScreen(
                    onSelectFrameworkForCase = { title, domain, urgency, desc ->
                        viewModel.analyzeProblem(title, domain, urgency, desc, "Framework application case.", true) { newCaseId ->
                            navController.navigate("case_detail/$newCaseId")
                        }
                    }
                )
            }

            composable("seo_studio") {
                SeoStudioScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onNavigateToCase = { caseId ->
                        viewModel.setActiveCaseDetail(caseId)
                        navController.navigate("case_detail/$caseId")
                    }
                )
            }

            composable("high_thinking") {
                HighThinkingScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            composable("account") {
                AccountSubscriptionScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onOpenPaywall = { viewModel.setShowPaywall(true) },
                    onOpenGoogleAuth = { viewModel.setShowGoogleAuth(true) },
                    onNavigateHttpServer = { navController.navigate("http_server") }
                )
            }

            composable("http_server") {
                HttpServerScreen(
                    serverState = uiState.httpServerState,
                    onStartServer = { port -> viewModel.startHttpServer(port) },
                    onStopServer = { viewModel.stopHttpServer() }
                )
            }

            composable("new_problem") {
                NewProblemScreen(
                    isAnalyzing = uiState.isAnalyzing,
                    errorMessage = uiState.analysisError,
                    onBack = { navController.popBackStack() },
                    onSubmit = { title, domain, urgency, description, context, useHighThinking ->
                        viewModel.analyzeProblem(title, domain, urgency, description, context, useHighThinking) { newCaseId ->
                            navController.popBackStack()
                            navController.navigate("case_detail/$newCaseId")
                        }
                    },
                    onNavigateStrategyStudio = { title, desc, domain, urgency ->
                        navController.popBackStack()
                        navController.navigate("strategy_studio")
                    }
                )
            }

            composable(
                route = "case_detail/{caseId}",
                arguments = listOf(navArgument("caseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
                val selectedCase = uiState.cases.find { it.id == caseId }

                if (selectedCase != null) {
                    CaseDetailScreen(
                        case = selectedCase,
                        onBack = { navController.popBackStack() },
                        onToggleTask = { cId, sId, tId, isCompleted ->
                            viewModel.toggleTask(cId, sId, tId, isCompleted)
                        },
                        onUpdateStatus = { cId, newStatus ->
                            viewModel.updateCaseStatus(cId, newStatus)
                        },
                        onDeleteCase = { cId ->
                            viewModel.deleteCase(cId)
                            navController.popBackStack()
                        }
                    )
                } else {
                    navController.popBackStack()
                }
            }

            composable("analytics") {
                AnalyticsScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
