package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "competitive_solutions")
data class SolutionEntity(
    @PrimaryKey val id: String,
    val problemTitle: String,
    val platform: String,
    val problemNumberOrId: String,
    val problemUrl: String,
    val difficulty: String,
    val tagsJson: String,
    val language: String,
    val codeSnippet: String,
    val timeComplexity: String,
    val spaceComplexity: String,
    val approachExplanation: String,
    val verdict: String,
    val runtimeMs: Int?,
    val memoryMb: Double?,
    val notes: String,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
