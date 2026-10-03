package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.Solution
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseTest {

    @Test
    fun testAppDatabaseSingletonProvider() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db1 = AppDatabase.getInstance(context)
        val db2 = AppDatabase.getDatabase(context)

        assertNotNull("AppDatabase instance should not be null", db1)
        assertSame("AppDatabase must be a singleton instance", db1, db2)
        assertNotNull("SolutionDao must be accessible from AppDatabase", db1.solutionDao())
    }

    @Test
    fun testSolutionDaoOperationsViaAppDatabase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val dao = db.solutionDao()

        val solution = Solution(
            id = "test-sol-1",
            problemTitle = "Two Sum",
            platform = "LEETCODE",
            problemNumberOrId = "1",
            problemUrl = "https://leetcode.com/problems/two-sum/",
            difficulty = "EASY",
            tagsJson = "[\"Array\",\"Hash Table\"]",
            language = "Kotlin",
            codeSnippet = "fun twoSum(nums: IntArray, target: Int): IntArray = intArrayOf()",
            timeComplexity = "O(n)",
            spaceComplexity = "O(n)",
            approachExplanation = "One-pass hash table map lookup",
            verdict = "ACCEPTED",
            runtimeMs = 15,
            memoryMb = 38.5,
            notes = "Classic problem",
            isFavorite = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        dao.insertSolution(solution)
        val retrieved = dao.getSolutionById("test-sol-1").first()

        assertNotNull(retrieved)
        assertEquals("Two Sum", retrieved?.problemTitle)
        assertEquals("LEETCODE", retrieved?.platform)
        assertTrue(retrieved?.isFavorite == true)

        dao.deleteSolutionById("test-sol-1")
    }
}
