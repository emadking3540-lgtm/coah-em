package com.example.data

import kotlinx.coroutines.flow.Flow

class GymRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val workoutDao = db.workoutDao()
    private val nutritionDao = db.nutritionDao()
    private val chatDao = db.chatDao()
    private val progressDao = db.progressDao()
    private val supplementDao = db.supplementDao()
    private val vitaminDao = db.vitaminDao()
    private val globalExerciseDao = db.globalExerciseDao()

    // Users
    val allTrainees: Flow<List<User>> = userDao.getAllTrainees()

    fun getUserById(id: Int): Flow<User?> = userDao.getUserById(id)

    suspend fun getUserByIdSuspend(id: Int): User? = userDao.getUserByIdSuspend(id)

    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    suspend fun getCoach(): User? = userDao.getCoach()

    suspend fun insertUser(user: User): Long = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    // Workout Plans
    fun getWorkoutsForTrainee(traineeId: Int): Flow<List<WorkoutPlan>> = 
        workoutDao.getWorkoutsForTrainee(traineeId)

    suspend fun insertWorkout(workout: WorkoutPlan): Long = workoutDao.insertWorkout(workout)

    suspend fun updateWorkout(workout: WorkoutPlan) = workoutDao.updateWorkout(workout)

    suspend fun deleteWorkout(workout: WorkoutPlan) = workoutDao.deleteWorkout(workout)

    suspend fun deleteWorkoutById(id: Int) = workoutDao.deleteWorkoutById(id)

    suspend fun clearWorkoutsForTrainee(traineeId: Int) = workoutDao.clearWorkoutsForTrainee(traineeId)

    // Nutrition Plans
    fun getMealsForTrainee(traineeId: Int): Flow<List<NutritionPlan>> = 
        nutritionDao.getMealsForTrainee(traineeId)

    suspend fun insertMeal(meal: NutritionPlan): Long = nutritionDao.insertMeal(meal)

    suspend fun updateMeal(meal: NutritionPlan) = nutritionDao.updateMeal(meal)

    suspend fun deleteMeal(meal: NutritionPlan) = nutritionDao.deleteMeal(meal)

    suspend fun deleteMealById(id: Int) = nutritionDao.deleteMealById(id)

    // Chat
    fun getMessagesForTrainee(traineeId: Int): Flow<List<ChatMessage>> = 
        chatDao.getMessagesForTrainee(traineeId)

    fun getLatestMessageForTrainee(traineeId: Int): Flow<ChatMessage?> = 
        chatDao.getLatestMessageForTrainee(traineeId)

    suspend fun insertMessage(message: ChatMessage): Long = chatDao.insertMessage(message)

    // Progress Metrics
    fun getProgressForTrainee(traineeId: Int): Flow<List<ProgressMetric>> = 
        progressDao.getProgressForTrainee(traineeId)

    suspend fun insertProgress(metric: ProgressMetric): Long = progressDao.insertProgress(metric)

    suspend fun deleteProgress(metric: ProgressMetric) = progressDao.deleteProgress(metric)

    // Supplements
    fun getSupplementsForTrainee(traineeId: Int): Flow<List<SupplementPlan>> = 
        supplementDao.getSupplementsForTrainee(traineeId)

    suspend fun insertSupplement(supplement: SupplementPlan): Long = supplementDao.insertSupplement(supplement)

    suspend fun updateSupplement(supplement: SupplementPlan) = supplementDao.updateSupplement(supplement)

    suspend fun deleteSupplement(supplement: SupplementPlan) = supplementDao.deleteSupplement(supplement)

    suspend fun deleteSupplementById(id: Int) = supplementDao.deleteSupplementById(id)

    suspend fun clearSupplementsForTrainee(traineeId: Int) = supplementDao.clearSupplementsForTrainee(traineeId)

    // Vitamins
    fun getVitaminsForTrainee(traineeId: Int): Flow<List<VitaminPlan>> = 
        vitaminDao.getVitaminsForTrainee(traineeId)

    suspend fun insertVitamin(vitamin: VitaminPlan): Long = vitaminDao.insertVitamin(vitamin)

    suspend fun updateVitamin(vitamin: VitaminPlan) = vitaminDao.updateVitamin(vitamin)

    suspend fun deleteVitamin(vitamin: VitaminPlan) = vitaminDao.deleteVitamin(vitamin)

    suspend fun deleteVitaminById(id: Int) = vitaminDao.deleteVitaminById(id)

    suspend fun clearVitaminsForTrainee(traineeId: Int) = vitaminDao.clearVitaminsForTrainee(traineeId)

    // Global Exercise Library
    fun getAllGlobalExercises(): Flow<List<GlobalExercise>> = 
        globalExerciseDao.getAllGlobalExercises()

    suspend fun insertGlobalExercise(exercise: GlobalExercise): Long = globalExerciseDao.insertGlobalExercise(exercise)

    suspend fun deleteGlobalExercise(exercise: GlobalExercise) = globalExerciseDao.deleteGlobalExercise(exercise)
}
