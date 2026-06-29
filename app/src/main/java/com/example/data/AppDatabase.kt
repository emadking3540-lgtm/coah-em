package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        WorkoutPlan::class,
        NutritionPlan::class,
        ChatMessage::class,
        ProgressMetric::class,
        SupplementPlan::class,
        VitaminPlan::class,
        GlobalExercise::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun chatDao(): ChatDao
    abstract fun progressDao(): ProgressDao
    abstract fun supplementDao(): SupplementDao
    abstract fun vitaminDao(): VitaminDao
    abstract fun globalExerciseDao(): GlobalExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymzone_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
