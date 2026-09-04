package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.Converters
import com.example.data.local.SolutionDao
import com.example.data.local.SolutionsDatabase
import com.example.data.model.CompetitiveSolution
import com.example.data.model.ProblemPlatform
import com.example.data.model.SolutionDifficulty
import com.example.data.model.SolutionVerdict
import com.example.data.repository.CompetitiveSolutionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CompetitiveSolutionDaoTest {

    private lateinit var database: SolutionsDatabase
    private lateinit var solutionDao: SolutionDao
    private lateinit var repository: CompetitiveSolutionRepository
    private val converters = Converters()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SolutionsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        solutionDao = database.solutionDao()
        repository = CompetitiveSolutionRepository(solutionDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test inserting and retrieving competitive programming solution`() = runBlocking {
        val solution = CompetitiveSolution(
            id = "sol_101",
            problemTitle = "Binary Tree Maximum Path Sum",
            platform = ProblemPlatform.LEETCODE,
            problemNumberOrId = "124",
            problemUrl = "https://leetcode.com/problems/binary-tree-maximum-path-sum/",
            difficulty = SolutionDifficulty.HARD,
            tags = listOf("Tree", "Depth-First Search", "Dynamic Programming", "Binary Tree"),
            language = "Kotlin",
            codeSnippet = "fun maxPathSum(root: TreeNode?): Int { ... }",
            timeComplexity = "O(N)",
            spaceComplexity = "O(H)",
            approachExplanation = "Post-order traversal calculating maximum gain from left and right subtrees.",
            verdict = SolutionVerdict.ACCEPTED,
            runtimeMs = 195,
            memoryMb = 38.5,
            notes = "Handle negative node values by taking max with 0.",
            isFavorite = true
        )

        repository.saveSolution(solution)

        val retrieved = repository.getSolutionById("sol_101").first()
        assertNotNull(retrieved)
        assertEquals("Binary Tree Maximum Path Sum", retrieved?.problemTitle)
        assertEquals(SolutionDifficulty.HARD, retrieved?.difficulty)
        assertEquals(ProblemPlatform.LEETCODE, retrieved?.platform)
        assertEquals(4, retrieved?.tags?.size)
        assertTrue(retrieved?.isFavorite == true)
    }

    @Test
    fun `test filtering solutions by difficulty and platform`() = runBlocking {
        repository.seedInitialCompetitiveSolutionsIfEmpty()

        val all = repository.allSolutions.first()
        assertTrue(all.isNotEmpty())

        val easySolutions = repository.getSolutionsByDifficulty(SolutionDifficulty.EASY).first()
        assertTrue(easySolutions.any { it.problemTitle == "Two Sum" })

        val leetCodeSolutions = repository.getSolutionsByPlatform(ProblemPlatform.LEETCODE).first()
        assertEquals(all.size, leetCodeSolutions.size)

        val searchResults = repository.searchSolutions("Graph").first()
        assertTrue(searchResults.any { it.problemTitle.contains("Course Schedule") })
    }

    @Test
    fun `test toggle favorite and delete`() = runBlocking {
        val solution = CompetitiveSolution(
            id = "sol_102",
            problemTitle = "LRU Cache",
            platform = ProblemPlatform.LEETCODE,
            difficulty = SolutionDifficulty.MEDIUM,
            isFavorite = false
        )

        repository.saveSolution(solution)
        var fetched = repository.getSolutionById("sol_102").first()
        assertFalse(fetched?.isFavorite ?: true)

        repository.toggleFavorite("sol_102", true)
        fetched = repository.getSolutionById("sol_102").first()
        assertTrue(fetched?.isFavorite == true)

        repository.deleteSolution("sol_102")
        fetched = repository.getSolutionById("sol_102").first()
        assertEquals(null, fetched)
    }
}
