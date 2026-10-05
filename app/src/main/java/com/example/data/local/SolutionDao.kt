package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for competitive programming solutions.
 * Provides reactive Flow queries, search, filtering, and CRUD suspend operations.
 */
@Dao
interface SolutionDao {

    @Query("SELECT * FROM competitive_solutions ORDER BY updatedAt DESC")
    fun getAllSolutions(): Flow<List<SolutionEntity>>

    @Query("SELECT * FROM competitive_solutions ORDER BY updatedAt DESC")
    suspend fun getAllSolutionsList(): List<SolutionEntity>

    @Query("SELECT * FROM competitive_solutions WHERE id = :id")
    fun getSolutionById(id: String): Flow<SolutionEntity?>

    @Query("SELECT * FROM competitive_solutions WHERE id = :id")
    suspend fun getSolutionByIdDirect(id: String): SolutionEntity?

    @Query("SELECT * FROM competitive_solutions WHERE platform = :platform ORDER BY updatedAt DESC")
    fun getSolutionsByPlatform(platform: String): Flow<List<SolutionEntity>>

    @Query("SELECT * FROM competitive_solutions WHERE difficulty = :difficulty ORDER BY updatedAt DESC")
    fun getSolutionsByDifficulty(difficulty: String): Flow<List<SolutionEntity>>

    @Query("SELECT * FROM competitive_solutions WHERE tagsJson LIKE '%' || :tag || '%' ORDER BY updatedAt DESC")
    fun getSolutionsByTag(tag: String): Flow<List<SolutionEntity>>

    @Query("SELECT * FROM competitive_solutions WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteSolutions(): Flow<List<SolutionEntity>>

    @Query("""
        SELECT * FROM competitive_solutions 
        WHERE problemTitle LIKE '%' || :query || '%' 
           OR tagsJson LIKE '%' || :query || '%' 
           OR approachExplanation LIKE '%' || :query || '%'
           OR language LIKE '%' || :query || '%'
           OR problemNumberOrId LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchSolutions(query: String): Flow<List<SolutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolution(solution: SolutionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSolutions(solutions: List<SolutionEntity>)

    @Update
    suspend fun updateSolution(solution: SolutionEntity)

    @Delete
    suspend fun deleteSolution(solution: SolutionEntity)

    @Query("UPDATE competitive_solutions SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM competitive_solutions WHERE id = :id")
    suspend fun deleteSolutionById(id: String)

    @Query("DELETE FROM competitive_solutions")
    suspend fun deleteAllSolutions()
}

typealias CompetitiveSolutionDao = SolutionDao
typealias CompetitiveProgrammingSolutionDao = SolutionDao

