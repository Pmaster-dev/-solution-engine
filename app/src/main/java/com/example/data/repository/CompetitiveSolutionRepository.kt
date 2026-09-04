package com.example.data.repository

import com.example.data.local.Converters
import com.example.data.local.SolutionDao
import com.example.data.model.CompetitiveSolution
import com.example.data.model.ProblemPlatform
import com.example.data.model.SolutionDifficulty
import com.example.data.model.SolutionVerdict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CompetitiveSolutionRepository(
    private val solutionDao: SolutionDao
) {
    private val converters = Converters()

    val allSolutions: Flow<List<CompetitiveSolution>> = solutionDao.getAllSolutions().map { entities ->
        entities.map { converters.toCompetitiveSolution(it) }
    }

    val favoriteSolutions: Flow<List<CompetitiveSolution>> = solutionDao.getFavoriteSolutions().map { entities ->
        entities.map { converters.toCompetitiveSolution(it) }
    }

    fun getSolutionById(id: String): Flow<CompetitiveSolution?> = solutionDao.getSolutionById(id).map { entity ->
        entity?.let { converters.toCompetitiveSolution(it) }
    }

    fun getSolutionsByPlatform(platform: ProblemPlatform): Flow<List<CompetitiveSolution>> =
        solutionDao.getSolutionsByPlatform(platform.name).map { entities ->
            entities.map { converters.toCompetitiveSolution(it) }
        }

    fun getSolutionsByDifficulty(difficulty: SolutionDifficulty): Flow<List<CompetitiveSolution>> =
        solutionDao.getSolutionsByDifficulty(difficulty.name).map { entities ->
            entities.map { converters.toCompetitiveSolution(it) }
        }

    fun searchSolutions(query: String): Flow<List<CompetitiveSolution>> =
        solutionDao.searchSolutions(query).map { entities ->
            entities.map { converters.toCompetitiveSolution(it) }
        }

    suspend fun saveSolution(solution: CompetitiveSolution) = withContext(Dispatchers.IO) {
        val entity = converters.fromCompetitiveSolution(solution.copy(updatedAt = System.currentTimeMillis()))
        solutionDao.insertSolution(entity)
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        solutionDao.updateFavoriteStatus(id, isFavorite, System.currentTimeMillis())
    }

    suspend fun deleteSolution(id: String) = withContext(Dispatchers.IO) {
        solutionDao.deleteSolutionById(id)
    }

    suspend fun deleteAllSolutions() = withContext(Dispatchers.IO) {
        solutionDao.deleteAllSolutions()
    }

    suspend fun seedInitialCompetitiveSolutionsIfEmpty() = withContext(Dispatchers.IO) {
        val existing = solutionDao.getAllSolutions().first()
        if (existing.isEmpty()) {
            val sample1 = CompetitiveSolution(
                problemTitle = "Two Sum",
                platform = ProblemPlatform.LEETCODE,
                problemNumberOrId = "1",
                problemUrl = "https://leetcode.com/problems/two-sum/",
                difficulty = SolutionDifficulty.EASY,
                tags = listOf("Array", "Hash Table"),
                language = "Kotlin",
                codeSnippet = """
                    class Solution {
                        fun twoSum(nums: IntArray, target: Int): IntArray {
                            val map = HashMap<Int, Int>()
                            for (i in nums.indices) {
                                val complement = target - nums[i]
                                if (map.containsKey(complement)) {
                                    return intArrayOf(map[complement]!!, i)
                                }
                                map[nums[i]] = i
                            }
                            throw IllegalArgumentException("No two sum solution")
                        }
                    }
                """.trimIndent(),
                timeComplexity = "O(N)",
                spaceComplexity = "O(N)",
                approachExplanation = "Use a single-pass hash map to store each visited number with its index. For each number, check if the complement (target - num) exists in the map.",
                verdict = SolutionVerdict.ACCEPTED,
                runtimeMs = 180,
                memoryMb = 37.4,
                notes = "Optimal one-pass solution. Avoid nested loop O(N^2) brute force.",
                isFavorite = true
            )

            val sample2 = CompetitiveSolution(
                problemTitle = "Course Schedule (Cycle Detection in Directed Graph)",
                platform = ProblemPlatform.LEETCODE,
                problemNumberOrId = "207",
                problemUrl = "https://leetcode.com/problems/course-schedule/",
                difficulty = SolutionDifficulty.MEDIUM,
                tags = listOf("Graph", "Topological Sort", "BFS", "DFS"),
                language = "Kotlin",
                codeSnippet = """
                    class Solution {
                        fun canFinish(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
                            val inDegree = IntArray(numCourses)
                            val adj = Array(numCourses) { ArrayList<Int>() }
                            
                            for (pre in prerequisites) {
                                adj[pre[1]].add(pre[0])
                                inDegree[pre[0]]++
                            }
                            
                            val queue = ArrayDeque<Int>()
                            for (i in 0 until numCourses) {
                                if (inDegree[i] == 0) queue.add(i)
                            }
                            
                            var visitedCount = 0
                            while (queue.isNotEmpty()) {
                                val curr = queue.removeFirst()
                                visitedCount++
                                for (neighbor in adj[curr]) {
                                    inDegree[neighbor]--
                                    if (inDegree[neighbor] == 0) queue.add(neighbor)
                                }
                            }
                            
                            return visitedCount == numCourses
                        }
                    }
                """.trimIndent(),
                timeComplexity = "O(V + E)",
                spaceComplexity = "O(V + E)",
                approachExplanation = "Kahn's Algorithm (BFS Topological Sort): compute in-degrees for all vertices. Enqueue all vertices with 0 in-degree. Decrement neighbor in-degrees as vertices are processed. If processed count equals numCourses, graph is a DAG (no cycles).",
                verdict = SolutionVerdict.ACCEPTED,
                runtimeMs = 210,
                memoryMb = 41.2,
                notes = "Classic topological sort. Can also be solved using 3-color DFS cycle detection.",
                isFavorite = true
            )

            val sample3 = CompetitiveSolution(
                problemTitle = "Trapping Rain Water",
                platform = ProblemPlatform.LEETCODE,
                problemNumberOrId = "42",
                problemUrl = "https://leetcode.com/problems/trapping-rain-water/",
                difficulty = SolutionDifficulty.HARD,
                tags = listOf("Array", "Two Pointers", "Dynamic Programming", "Monotonic Stack"),
                language = "Kotlin",
                codeSnippet = """
                    class Solution {
                        fun trap(height: IntArray): Int {
                            if (height.isEmpty()) return 0
                            var left = 0
                            var right = height.size - 1
                            var leftMax = 0
                            var rightMax = 0
                            var totalWater = 0
                            
                            while (left < right) {
                                if (height[left] < height[right]) {
                                    if (height[left] >= leftMax) {
                                        leftMax = height[left]
                                    } else {
                                        totalWater += leftMax - height[left]
                                    }
                                    left++
                                } else {
                                    if (height[right] >= rightMax) {
                                        rightMax = height[right]
                                    } else {
                                        totalWater += rightMax - height[right]
                                    }
                                    right--
                                }
                            }
                            return totalWater
                        }
                    }
                """.trimIndent(),
                timeComplexity = "O(N)",
                spaceComplexity = "O(1)",
                approachExplanation = "Two-pointer approach maintaining leftMax and rightMax boundaries. Process the smaller height pointer inward to calculate trapped water at each step without auxiliary memory.",
                verdict = SolutionVerdict.ACCEPTED,
                runtimeMs = 165,
                memoryMb = 36.8,
                notes = "O(1) space two-pointer approach is superior to O(N) DP prefix/suffix max arrays.",
                isFavorite = false
            )

            val entities = listOf(sample1, sample2, sample3).map { converters.fromCompetitiveSolution(it) }
            solutionDao.insertAllSolutions(entities)
        }
    }
}
