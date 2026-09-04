package com.example.data.model

import java.util.UUID

enum class SolutionDifficulty(val displayName: String, val colorHex: Long) {
    EASY("Easy", 0xFF10B981),
    MEDIUM("Medium", 0xFFF59E0B),
    HARD("Hard", 0xFFEF4444)
}

enum class ProblemPlatform(val displayName: String) {
    LEETCODE("LeetCode"),
    CODEFORCES("Codeforces"),
    HACKERRANK("HackerRank"),
    ATCODER("AtCoder"),
    CODECHEF("CodeChef"),
    KATTIS("Kattis"),
    OTHER("Other")
}

enum class SolutionVerdict(val displayName: String, val isPassing: Boolean) {
    ACCEPTED("Accepted", true),
    WRONG_ANSWER("Wrong Answer", false),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded (TLE)", false),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded (MLE)", false),
    COMPILATION_ERROR("Compilation Error", false),
    DRAFT("Draft / In Progress", false)
}

data class CompetitiveSolution(
    val id: String = UUID.randomUUID().toString(),
    val problemTitle: String,
    val platform: ProblemPlatform = ProblemPlatform.LEETCODE,
    val problemNumberOrId: String = "",
    val problemUrl: String = "",
    val difficulty: SolutionDifficulty = SolutionDifficulty.MEDIUM,
    val tags: List<String> = emptyList(),
    val language: String = "Kotlin", // Kotlin, C++, Java, Python, Rust, Go
    val codeSnippet: String = "",
    val timeComplexity: String = "O(N)",
    val spaceComplexity: String = "O(1)",
    val approachExplanation: String = "",
    val verdict: SolutionVerdict = SolutionVerdict.ACCEPTED,
    val runtimeMs: Int? = null,
    val memoryMb: Double? = null,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
