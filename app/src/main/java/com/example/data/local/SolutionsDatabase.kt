package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.ActionTask
import com.example.data.model.CaseStatus
import com.example.data.model.DecisionMatrix
import com.example.data.model.CompetitiveSolution
import com.example.data.model.ProblemCase
import com.example.data.model.ProblemDomain
import com.example.data.model.ProblemPlatform
import com.example.data.model.RiskFactor
import com.example.data.model.SeoAuditResult
import com.example.data.model.SolutionDifficulty
import com.example.data.model.SolutionStrategy
import com.example.data.model.SolutionVerdict
import com.example.data.model.StrategyMilestone
import com.example.data.model.StrategyOutlineResult
import com.example.data.model.StrategyPhase
import com.example.data.model.SubscriptionDetails
import com.example.data.model.SubscriptionTier
import com.example.data.model.SuccessMetric
import com.example.data.model.UrgencyLevel
import com.example.data.model.UserProfile
import com.example.data.model.WhyStep
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "problem_cases")
data class ProblemCaseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val domain: String,
    val urgency: String,
    val description: String,
    val context: String,
    val status: String,
    val timestamp: Long,
    val fiveWhysJson: String,
    val solutionsJson: String,
    val decisionMatrixJson: String,
    val isHighThinkingUsed: Boolean = true
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val authProvider: String,
    val tier: String,
    val isHighThinkingEnabled: Boolean,
    val tokensUsed: Int,
    val memberSince: Long
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String = "active_subscription",
    val tier: String,
    val billingCycle: String,
    val isAutoRenew: Boolean,
    val startDate: Long,
    val expiryDate: Long,
    val transactionId: String
)

@Entity(tableName = "seo_audits")
data class SeoAuditEntity(
    @PrimaryKey val id: String,
    val targetUrl: String,
    val focusKeywordsJson: String,
    val issueCategory: String,
    val healthScore: Int,
    val domainAuthority: Int,
    val lcpMs: Int,
    val clsScore: Double,
    val inpMs: Int,
    val isMobileFriendly: Boolean,
    val criticalIssuesJson: String,
    val fiveWhysJson: String,
    val recommendedMetaTitle: String,
    val recommendedMetaDesc: String,
    val schemaJsonLd: String,
    val actionTasksJson: String,
    val timestamp: Long
)

@Entity(tableName = "strategy_outlines")
data class StrategyOutlineEntity(
    @PrimaryKey val id: String,
    val problemTitle: String,
    val problemDescription: String,
    val domain: String,
    val urgency: String,
    val frameworkUsed: String,
    val executiveSummary: String,
    val rootCauseHypothesis: String,
    val coreConstraintsJson: String,
    val strategicRoadmapPhasesJson: String,
    val riskMatrixJson: String,
    val successMetricsJson: String,
    val quickWinRecommendationsJson: String,
    val isHighThinkingUsed: Boolean,
    val timestamp: Long
)

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val whyStepListType = Types.newParameterizedType(List::class.java, WhyStep::class.java)
    private val solutionListType = Types.newParameterizedType(List::class.java, SolutionStrategy::class.java)
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val taskListType = Types.newParameterizedType(List::class.java, ActionTask::class.java)
    private val phaseListType = Types.newParameterizedType(List::class.java, StrategyPhase::class.java)
    private val riskListType = Types.newParameterizedType(List::class.java, RiskFactor::class.java)
    private val metricListType = Types.newParameterizedType(List::class.java, SuccessMetric::class.java)

    @TypeConverter
    fun fromProblemCase(case: ProblemCase): ProblemCaseEntity {
        val whyAdapter = moshi.adapter<List<WhyStep>>(whyStepListType)
        val solAdapter = moshi.adapter<List<SolutionStrategy>>(solutionListType)
        val matrixAdapter = moshi.adapter(DecisionMatrix::class.java)

        return ProblemCaseEntity(
            id = case.id,
            title = case.title,
            domain = case.domain.name,
            urgency = case.urgency.name,
            description = case.description,
            context = case.context,
            status = case.status.name,
            timestamp = case.timestamp,
            fiveWhysJson = whyAdapter.toJson(case.fiveWhys),
            solutionsJson = solAdapter.toJson(case.solutions),
            decisionMatrixJson = case.decisionMatrix?.let { matrixAdapter.toJson(it) } ?: "",
            isHighThinkingUsed = case.isHighThinkingUsed
        )
    }

    fun toProblemCase(entity: ProblemCaseEntity): ProblemCase {
        val whyAdapter = moshi.adapter<List<WhyStep>>(whyStepListType)
        val solAdapter = moshi.adapter<List<SolutionStrategy>>(solutionListType)
        val matrixAdapter = moshi.adapter(DecisionMatrix::class.java)

        val whyList = try { whyAdapter.fromJson(entity.fiveWhysJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val solList = try { solAdapter.fromJson(entity.solutionsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val matrix = if (entity.decisionMatrixJson.isNotBlank()) {
            try { matrixAdapter.fromJson(entity.decisionMatrixJson) } catch (e: Exception) { null }
        } else null

        val domain = try { ProblemDomain.valueOf(entity.domain) } catch (e: Exception) { ProblemDomain.TECHNICAL }
        val urgency = try { UrgencyLevel.valueOf(entity.urgency) } catch (e: Exception) { UrgencyLevel.MEDIUM }
        val status = try { CaseStatus.valueOf(entity.status) } catch (e: Exception) { CaseStatus.ACTIVE }

        return ProblemCase(
            id = entity.id,
            title = entity.title,
            domain = domain,
            urgency = urgency,
            description = entity.description,
            context = entity.context,
            status = status,
            timestamp = entity.timestamp,
            fiveWhys = whyList,
            solutions = solList,
            decisionMatrix = matrix,
            isHighThinkingUsed = entity.isHighThinkingUsed
        )
    }

    fun fromUserProfile(user: UserProfile): UserProfileEntity {
        return UserProfileEntity(
            id = user.id,
            displayName = user.displayName,
            email = user.email,
            avatarUrl = user.avatarUrl,
            authProvider = user.authProvider,
            tier = user.tier.name,
            isHighThinkingEnabled = user.isHighThinkingEnabled,
            tokensUsed = user.tokensUsed,
            memberSince = user.memberSince
        )
    }

    fun toUserProfile(entity: UserProfileEntity): UserProfile {
        val tier = try { SubscriptionTier.valueOf(entity.tier) } catch (e: Exception) { SubscriptionTier.PRO }
        return UserProfile(
            id = entity.id,
            displayName = entity.displayName,
            email = entity.email,
            avatarUrl = entity.avatarUrl,
            authProvider = entity.authProvider,
            tier = tier,
            isHighThinkingEnabled = entity.isHighThinkingEnabled,
            tokensUsed = entity.tokensUsed,
            memberSince = entity.memberSince
        )
    }

    fun fromSubscription(sub: SubscriptionDetails): SubscriptionEntity {
        return SubscriptionEntity(
            id = "active_subscription",
            tier = sub.tier.name,
            billingCycle = sub.billingCycle,
            isAutoRenew = sub.isAutoRenew,
            startDate = sub.startDate,
            expiryDate = sub.expiryDate,
            transactionId = sub.transactionId
        )
    }

    fun toSubscription(entity: SubscriptionEntity): SubscriptionDetails {
        val tier = try { SubscriptionTier.valueOf(entity.tier) } catch (e: Exception) { SubscriptionTier.PRO }
        return SubscriptionDetails(
            tier = tier,
            billingCycle = entity.billingCycle,
            isAutoRenew = entity.isAutoRenew,
            startDate = entity.startDate,
            expiryDate = entity.expiryDate,
            transactionId = entity.transactionId
        )
    }

    fun fromSeoAudit(audit: SeoAuditResult): SeoAuditEntity {
        val strAdapter = moshi.adapter<List<String>>(stringListType)
        val whyAdapter = moshi.adapter<List<WhyStep>>(whyStepListType)
        val taskAdapter = moshi.adapter<List<ActionTask>>(taskListType)

        return SeoAuditEntity(
            id = audit.id,
            targetUrl = audit.targetUrl,
            focusKeywordsJson = strAdapter.toJson(audit.focusKeywords),
            issueCategory = audit.issueCategory,
            healthScore = audit.healthScore,
            domainAuthority = audit.domainAuthority,
            lcpMs = audit.lcpMs,
            clsScore = audit.clsScore,
            inpMs = audit.inpMs,
            isMobileFriendly = audit.isMobileFriendly,
            criticalIssuesJson = strAdapter.toJson(audit.criticalIssues),
            fiveWhysJson = whyAdapter.toJson(audit.fiveWhysAnalysis),
            recommendedMetaTitle = audit.recommendedMetaTitle,
            recommendedMetaDesc = audit.recommendedMetaDesc,
            schemaJsonLd = audit.schemaJsonLd,
            actionTasksJson = taskAdapter.toJson(audit.actionTasks),
            timestamp = audit.timestamp
        )
    }

    fun toSeoAudit(entity: SeoAuditEntity): SeoAuditResult {
        val strAdapter = moshi.adapter<List<String>>(stringListType)
        val whyAdapter = moshi.adapter<List<WhyStep>>(whyStepListType)
        val taskAdapter = moshi.adapter<List<ActionTask>>(taskListType)

        val keywords = try { strAdapter.fromJson(entity.focusKeywordsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val issues = try { strAdapter.fromJson(entity.criticalIssuesJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val whyList = try { whyAdapter.fromJson(entity.fiveWhysJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val tasks = try { taskAdapter.fromJson(entity.actionTasksJson) ?: emptyList() } catch (e: Exception) { emptyList() }

        return SeoAuditResult(
            id = entity.id,
            targetUrl = entity.targetUrl,
            focusKeywords = keywords,
            issueCategory = entity.issueCategory,
            healthScore = entity.healthScore,
            domainAuthority = entity.domainAuthority,
            lcpMs = entity.lcpMs,
            clsScore = entity.clsScore,
            inpMs = entity.inpMs,
            isMobileFriendly = entity.isMobileFriendly,
            criticalIssues = issues,
            fiveWhysAnalysis = whyList,
            recommendedMetaTitle = entity.recommendedMetaTitle,
            recommendedMetaDesc = entity.recommendedMetaDesc,
            schemaJsonLd = entity.schemaJsonLd,
            actionTasks = tasks,
            timestamp = entity.timestamp
        )
    }

    fun fromStrategyOutline(outline: StrategyOutlineResult): StrategyOutlineEntity {
        val strAdapter = moshi.adapter<List<String>>(stringListType)
        val phaseAdapter = moshi.adapter<List<StrategyPhase>>(phaseListType)
        val riskAdapter = moshi.adapter<List<RiskFactor>>(riskListType)
        val metricAdapter = moshi.adapter<List<SuccessMetric>>(metricListType)

        return StrategyOutlineEntity(
            id = outline.id,
            problemTitle = outline.problemTitle,
            problemDescription = outline.problemDescription,
            domain = outline.domain.name,
            urgency = outline.urgency.name,
            frameworkUsed = outline.frameworkUsed,
            executiveSummary = outline.executiveSummary,
            rootCauseHypothesis = outline.rootCauseHypothesis,
            coreConstraintsJson = strAdapter.toJson(outline.coreConstraints),
            strategicRoadmapPhasesJson = phaseAdapter.toJson(outline.strategicRoadmapPhases),
            riskMatrixJson = riskAdapter.toJson(outline.riskMatrix),
            successMetricsJson = metricAdapter.toJson(outline.successMetrics),
            quickWinRecommendationsJson = strAdapter.toJson(outline.quickWinRecommendations),
            isHighThinkingUsed = outline.isHighThinkingUsed,
            timestamp = outline.timestamp
        )
    }

    fun toStrategyOutline(entity: StrategyOutlineEntity): StrategyOutlineResult {
        val strAdapter = moshi.adapter<List<String>>(stringListType)
        val phaseAdapter = moshi.adapter<List<StrategyPhase>>(phaseListType)
        val riskAdapter = moshi.adapter<List<RiskFactor>>(riskListType)
        val metricAdapter = moshi.adapter<List<SuccessMetric>>(metricListType)

        val constraints = try { strAdapter.fromJson(entity.coreConstraintsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val phases = try { phaseAdapter.fromJson(entity.strategicRoadmapPhasesJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val risks = try { riskAdapter.fromJson(entity.riskMatrixJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val metrics = try { metricAdapter.fromJson(entity.successMetricsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val quickWins = try { strAdapter.fromJson(entity.quickWinRecommendationsJson) ?: emptyList() } catch (e: Exception) { emptyList() }

        val domain = try { ProblemDomain.valueOf(entity.domain) } catch (e: Exception) { ProblemDomain.TECHNICAL }
        val urgency = try { UrgencyLevel.valueOf(entity.urgency) } catch (e: Exception) { UrgencyLevel.HIGH }

        return StrategyOutlineResult(
            id = entity.id,
            problemTitle = entity.problemTitle,
            problemDescription = entity.problemDescription,
            domain = domain,
            urgency = urgency,
            frameworkUsed = entity.frameworkUsed,
            executiveSummary = entity.executiveSummary,
            rootCauseHypothesis = entity.rootCauseHypothesis,
            coreConstraints = constraints,
            strategicRoadmapPhases = phases,
            riskMatrix = risks,
            successMetrics = metrics,
            quickWinRecommendations = quickWins,
            isHighThinkingUsed = entity.isHighThinkingUsed,
            timestamp = entity.timestamp
        )
    }

    fun fromCompetitiveSolution(sol: CompetitiveSolution): SolutionEntity {
        val strAdapter = moshi.adapter<List<String>>(stringListType)
        return SolutionEntity(
            id = sol.id,
            problemTitle = sol.problemTitle,
            platform = sol.platform.name,
            problemNumberOrId = sol.problemNumberOrId,
            problemUrl = sol.problemUrl,
            difficulty = sol.difficulty.name,
            tagsJson = strAdapter.toJson(sol.tags),
            language = sol.language,
            codeSnippet = sol.codeSnippet,
            timeComplexity = sol.timeComplexity,
            spaceComplexity = sol.spaceComplexity,
            approachExplanation = sol.approachExplanation,
            verdict = sol.verdict.name,
            runtimeMs = sol.runtimeMs,
            memoryMb = sol.memoryMb,
            notes = sol.notes,
            isFavorite = sol.isFavorite,
            createdAt = sol.createdAt,
            updatedAt = sol.updatedAt
        )
    }

    fun toCompetitiveSolution(entity: SolutionEntity): CompetitiveSolution {
        val strAdapter = moshi.adapter<List<String>>(stringListType)
        val tags = try { strAdapter.fromJson(entity.tagsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val platform = try { ProblemPlatform.valueOf(entity.platform) } catch (e: Exception) { ProblemPlatform.LEETCODE }
        val difficulty = try { SolutionDifficulty.valueOf(entity.difficulty) } catch (e: Exception) { SolutionDifficulty.MEDIUM }
        val verdict = try { SolutionVerdict.valueOf(entity.verdict) } catch (e: Exception) { SolutionVerdict.ACCEPTED }

        return CompetitiveSolution(
            id = entity.id,
            problemTitle = entity.problemTitle,
            platform = platform,
            problemNumberOrId = entity.problemNumberOrId,
            problemUrl = entity.problemUrl,
            difficulty = difficulty,
            tags = tags,
            language = entity.language,
            codeSnippet = entity.codeSnippet,
            timeComplexity = entity.timeComplexity,
            spaceComplexity = entity.spaceComplexity,
            approachExplanation = entity.approachExplanation,
            verdict = verdict,
            runtimeMs = entity.runtimeMs,
            memoryMb = entity.memoryMb,
            notes = entity.notes,
            isFavorite = entity.isFavorite,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

@Dao
interface SolutionsDao {
    @Query("SELECT * FROM problem_cases ORDER BY timestamp DESC")
    fun getAllCaseEntities(): Flow<List<ProblemCaseEntity>>

    @Query("SELECT * FROM problem_cases WHERE id = :id")
    fun getCaseEntityById(id: String): Flow<ProblemCaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaseEntity(entity: ProblemCaseEntity)

    @Query("DELETE FROM problem_cases WHERE id = :id")
    suspend fun deleteCaseById(id: String)

    @Query("DELETE FROM problem_cases")
    suspend fun deleteAll()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(user: UserProfileEntity)

    @Query("DELETE FROM user_profiles")
    suspend fun clearUserProfile()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE id = 'active_subscription' LIMIT 1")
    fun getActiveSubscription(): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: SubscriptionEntity)

    @Query("DELETE FROM subscriptions")
    suspend fun clearSubscription()
}

@Dao
interface SeoDao {
    @Query("SELECT * FROM seo_audits ORDER BY timestamp DESC")
    fun getAllSeoAudits(): Flow<List<SeoAuditEntity>>

    @Query("SELECT * FROM seo_audits WHERE id = :id")
    fun getSeoAuditById(id: String): Flow<SeoAuditEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeoAudit(audit: SeoAuditEntity)

    @Query("DELETE FROM seo_audits WHERE id = :id")
    suspend fun deleteSeoAuditById(id: String)
}

@Dao
interface StrategyOutlineDao {
    @Query("SELECT * FROM strategy_outlines ORDER BY timestamp DESC")
    fun getAllStrategyOutlines(): Flow<List<StrategyOutlineEntity>>

    @Query("SELECT * FROM strategy_outlines WHERE id = :id")
    fun getStrategyOutlineById(id: String): Flow<StrategyOutlineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategyOutline(outline: StrategyOutlineEntity)

    @Query("DELETE FROM strategy_outlines WHERE id = :id")
    suspend fun deleteStrategyOutlineById(id: String)

    @Query("DELETE FROM strategy_outlines")
    suspend fun deleteAll()
}

@Database(
    entities = [
        ProblemCaseEntity::class,
        UserProfileEntity::class,
        SubscriptionEntity::class,
        SeoAuditEntity::class,
        StrategyOutlineEntity::class,
        SolutionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SolutionsDatabase : RoomDatabase() {
    abstract fun solutionsDao(): SolutionsDao
    abstract fun userDao(): UserDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun seoDao(): SeoDao
    abstract fun strategyOutlineDao(): StrategyOutlineDao
    abstract fun solutionDao(): SolutionDao

    companion object {
        @Volatile
        private var INSTANCE: SolutionsDatabase? = null

        fun getDatabase(context: Context): SolutionsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SolutionsDatabase::class.java,
                    "solutions_engine_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
