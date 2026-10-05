package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CompetitiveProgrammingSolution
import com.example.data.local.CompetitiveSolutionDao
import com.example.data.local.SolutionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit and Robolectric tests validating the Room database entity and DAO
 * for competitive programming solutions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CompetitiveProgrammingSolutionTest {

    private lateinit var context: Context
    private lateinit var inMemoryDb: AppDatabase
    private lateinit var dao: CompetitiveSolutionDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = inMemoryDb.solutionDao()
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
    }

    @Test
    fun testEntityFieldAccessorsAndConvenienceConstructor() {
        // Constructing solution using problem name, platform, solution code, and tags
        val tagsList = listOf("Dynamic Programming", "Bit Manipulation", "Arrays")
        val solution = CompetitiveProgrammingSolution(
            problemName = "Trapping Rain Water",
            platform = "LeetCode",
            solutionCode = "fun trap(height: IntArray): Int { return 0 }",
            tags = tagsList,
            difficulty = "Hard",
            language = "Kotlin"
        )

        // Verify field mapping
        assertEquals("Trapping Rain Water", solution.problemName)
        assertEquals("Trapping Rain Water", solution.problemTitle)
        assertEquals("LeetCode", solution.platform)
        assertEquals("fun trap(height: IntArray): Int { return 0 }", solution.solutionCode)
        assertEquals("fun trap(height: IntArray): Int { return 0 }", solution.codeSnippet)
        assertEquals(3, solution.tags.size)
        assertTrue(solution.tags.contains("Dynamic Programming"))
        assertTrue(solution.tags.contains("Bit Manipulation"))
        assertTrue(solution.tags.contains("Arrays"))
    }

    @Test
    fun testDaoInsertAndRetrieveCompetitiveSolutions() = runBlocking {
        val sol1 = SolutionEntity(
            problemName = "Two Sum",
            platform = "LeetCode",
            solutionCode = "class Solution { fun twoSum() {} }",
            tags = listOf("Array", "Hash Table"),
            difficulty = "Easy"
        )
        val sol2 = SolutionEntity(
            problemName = "Watermelon",
            platform = "Codeforces",
            solutionCode = "fun main() { val w = readLine()!!.toInt(); println(if (w > 2 && w % 2 == 0) \"YES\" else \"NO\") }",
            tags = listOf("Math", "Brute Force"),
            difficulty = "800"
        )

        dao.insertSolution(sol1)
        dao.insertSolution(sol2)

        val allSolutions = dao.getAllSolutions().first()
        assertEquals(2, allSolutions.size)

        // Query by platform
        val leetCodeSolutions = dao.getSolutionsByPlatform("LeetCode").first()
        assertEquals(1, leetCodeSolutions.size)
        assertEquals("Two Sum", leetCodeSolutions[0].problemName)
        assertEquals("LeetCode", leetCodeSolutions[0].platform)

        val codeforcesSolutions = dao.getSolutionsByPlatform("Codeforces").first()
        assertEquals(1, codeforcesSolutions.size)
        assertEquals("Watermelon", codeforcesSolutions[0].problemName)

        // Query by Tag
        val hashSolutions = dao.getSolutionsByTag("Hash Table").first()
        assertEquals(1, hashSolutions.size)
        assertEquals("Two Sum", hashSolutions[0].problemName)

        // Direct get by ID
        val retrieved = dao.getSolutionByIdDirect(sol1.id)
        assertNotNull(retrieved)
        assertEquals(sol1.id, retrieved?.id)
        assertEquals(sol1.solutionCode, retrieved?.solutionCode)

        // Delete test
        dao.deleteSolution(sol1)
        val remaining = dao.getAllSolutionsList()
        assertEquals(1, remaining.size)
        assertEquals("Watermelon", remaining[0].problemName)

        val deleted = dao.getSolutionByIdDirect(sol1.id)
        assertNull(deleted)
    }

    @Test
    fun testAppDatabaseSingletonProviderRegistersSolutionDao() {
        val db = AppDatabase.getInstance(context)
        assertNotNull("AppDatabase instance must be non-null", db)
        assertNotNull("SolutionDao must be provided by AppDatabase", db.solutionDao())
    }
}
