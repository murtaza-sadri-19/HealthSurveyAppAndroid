package com.example.healthsurveyappandroid.data

import androidx.room.*

@Dao
interface SurveyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: SurveyEntity)

    @Query("SELECT * FROM surveys")
    suspend fun getAllSurveys(): List<SurveyEntity>

    @Delete
    suspend fun deleteSurvey(survey: SurveyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surveys: List<SurveyEntity>)

    @Query("SELECT COUNT(*) FROM surveys")
    suspend fun getCount(): Int

    @Query("DELETE FROM surveys")
    suspend fun deleteAll()
}