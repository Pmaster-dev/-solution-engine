package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity to store competitive programming solutions.
 * Includes fields for problem name, platform, solution code, tags, and execution metadata.
 */
@Entity(tableName = "competitive_solutions")
data class SolutionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "problemTitle")
    val problemTitle: String,

    @ColumnInfo(name = "platform")
    val platform: String,

    @ColumnInfo(name = "problemNumberOrId")
    val problemNumberOrId: String = "",

    @ColumnInfo(name = "problemUrl")
    val problemUrl: String = "",

    @ColumnInfo(name = "difficulty")
    val difficulty: String = "Medium",

    @ColumnInfo(name = "tagsJson")
    val tagsJson: String = "[]",

    @ColumnInfo(name = "language")
    val language: String = "Kotlin",

    @ColumnInfo(name = "codeSnippet")
    val codeSnippet: String = "",

    @ColumnInfo(name = "timeComplexity")
    val timeComplexity: String = "O(N)",

    @ColumnInfo(name = "spaceComplexity")
    val spaceComplexity: String = "O(1)",

    @ColumnInfo(name = "approachExplanation")
    val approachExplanation: String = "",

    @ColumnInfo(name = "verdict")
    val verdict: String = "Accepted",

    @ColumnInfo(name = "runtimeMs")
    val runtimeMs: Int? = null,

    @ColumnInfo(name = "memoryMb")
    val memoryMb: Double? = null,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Problem name alias matching user prompt specifications.
     */
    val problemName: String
        get() = problemTitle

    /**
     * Solution code alias matching user prompt specifications.
     */
    val solutionCode: String
        get() = codeSnippet

    /**
     * Parsed list of tags for competitive programming solutions.
     */
    val tags: List<String>
        get() = try {
            if (tagsJson.startsWith("[")) {
                tagsJson.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                    .filter { it.isNotEmpty() }
            } else {
                tagsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        } catch (_: Exception) {
            emptyList()
        }

    /**
     * Secondary constructor allowing direct initialization with
     * problemName, platform, solutionCode, and tags list.
     */
    @Ignore
    constructor(
        problemName: String,
        platform: String,
        solutionCode: String,
        tags: List<String> = emptyList(),
        id: String = UUID.randomUUID().toString(),
        difficulty: String = "Medium",
        language: String = "Kotlin",
        notes: String = ""
    ) : this(
        id = id,
        problemTitle = problemName,
        platform = platform,
        problemNumberOrId = "",
        problemUrl = "",
        difficulty = difficulty,
        tagsJson = "[" + tags.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" } + "]",
        language = language,
        codeSnippet = solutionCode,
        timeComplexity = "O(N)",
        spaceComplexity = "O(1)",
        approachExplanation = "",
        verdict = "Accepted",
        runtimeMs = null,
        memoryMb = null,
        notes = notes,
        isFavorite = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

typealias Solution = SolutionEntity
typealias CompetitiveSolutionEntity = SolutionEntity
typealias CompetitiveProgrammingSolution = SolutionEntity

