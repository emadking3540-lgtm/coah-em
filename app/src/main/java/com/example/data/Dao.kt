package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCoach = 0")
    fun getAllTrainees(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSuspend(id: Int): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE isCoach = 1 LIMIT 1")
    suspend fun getCoach(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_plans WHERE traineeId = :traineeId ORDER BY id ASC")
    fun getWorkoutsForTrainee(traineeId: Int): Flow<List<WorkoutPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutPlan): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutPlan)

    @Query("DELETE FROM workout_plans WHERE id = :id")
    suspend fun deleteWorkoutById(id: Int)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutPlan)

    @Query("DELETE FROM workout_plans WHERE traineeId = :traineeId")
    suspend fun clearWorkoutsForTrainee(traineeId: Int)
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_plans WHERE traineeId = :traineeId ORDER BY id ASC")
    fun getMealsForTrainee(traineeId: Int): Flow<List<NutritionPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: NutritionPlan): Long

    @Update
    suspend fun updateMeal(meal: NutritionPlan)

    @Query("DELETE FROM nutrition_plans WHERE id = :id")
    suspend fun deleteMealById(id: Int)

    @Delete
    suspend fun deleteMeal(meal: NutritionPlan)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE traineeId = :traineeId ORDER BY timestamp ASC")
    fun getMessagesForTrainee(traineeId: Int): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE traineeId = :traineeId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessageForTrainee(traineeId: Int): Flow<ChatMessage?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress_metrics WHERE traineeId = :traineeId ORDER BY date ASC")
    fun getProgressForTrainee(traineeId: Int): Flow<List<ProgressMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(metric: ProgressMetric): Long

    @Delete
    suspend fun deleteProgress(metric: ProgressMetric)
}

@Dao
interface SupplementDao {
    @Query("SELECT * FROM supplement_plans WHERE traineeId = :traineeId ORDER BY id ASC")
    fun getSupplementsForTrainee(traineeId: Int): Flow<List<SupplementPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: SupplementPlan): Long

    @Update
    suspend fun updateSupplement(supplement: SupplementPlan)

    @Query("DELETE FROM supplement_plans WHERE id = :id")
    suspend fun deleteSupplementById(id: Int)

    @Delete
    suspend fun deleteSupplement(supplement: SupplementPlan)

    @Query("DELETE FROM supplement_plans WHERE traineeId = :traineeId")
    suspend fun clearSupplementsForTrainee(traineeId: Int)
}

@Dao
interface VitaminDao {
    @Query("SELECT * FROM vitamin_plans WHERE traineeId = :traineeId ORDER BY id ASC")
    fun getVitaminsForTrainee(traineeId: Int): Flow<List<VitaminPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitamin(vitamin: VitaminPlan): Long

    @Update
    suspend fun updateVitamin(vitamin: VitaminPlan)

    @Query("DELETE FROM vitamin_plans WHERE id = :id")
    suspend fun deleteVitaminById(id: Int)

    @Delete
    suspend fun deleteVitamin(vitamin: VitaminPlan)

    @Query("DELETE FROM vitamin_plans WHERE traineeId = :traineeId")
    suspend fun clearVitaminsForTrainee(traineeId: Int)
}

@Dao
interface GlobalExerciseDao {
    @Query("SELECT * FROM global_exercises ORDER BY id DESC")
    fun getAllGlobalExercises(): Flow<List<GlobalExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlobalExercise(exercise: GlobalExercise): Long

    @Delete
    suspend fun deleteGlobalExercise(exercise: GlobalExercise)
}
