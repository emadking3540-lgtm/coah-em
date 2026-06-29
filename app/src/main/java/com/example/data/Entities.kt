package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String = "123456", // default safe password
    val age: Int = 25,
    val height: Double = 175.0, // in cm
    val weight: Double = 75.0, // in kg
    val goal: String = "تضخيم", // تضخيم / تنشيف / لياقة
    val subscriptionStart: Long = System.currentTimeMillis(),
    val subscriptionEnd: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // 30 days default
    val healthHistory: String = "لا توجد إصابات",
    val privateNotes: String = "",
    val isCoach: Boolean = false,
    val bankCardNumber: String = "",
    val bankName: String = "",
    val bankCardHolder: String = ""
) : Serializable

@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val traineeId: Int,
    val dayOfWeek: String, // السبت, الأحد, الاثنين, الثلاثاء, الأربعاء, الخميس, الجمعة
    val muscleGroup: String, // الصدر, الظهر, الأرجل, الأكتاف, الأذرع, بطن / كارديو, راحة
    val exerciseName: String,
    val sets: Int = 4,
    val reps: String = "12-10-8-8",
    val videoRef: String = "", // Link to animation/video instruction
    val notes: String = "",
    val isDone: Boolean = false
) : Serializable

@Entity(tableName = "nutrition_plans")
data class NutritionPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val traineeId: Int,
    val mealName: String, // الوجبة الأولى, الوجبة الثانية, إلخ
    val contents: String, // التفاصيل (مثال: 100g شوفان + حليب)
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val alternatives: String = "", // بدائل الوجبة
    val supplements: String = "" // المكملات المصاحبة
) : Serializable

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val traineeId: Int, // Channel ID (trainee room)
    val senderId: Int, // The author (Coach id or Trainee id)
    val senderName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentType: String = "TEXT", // TEXT, IMAGE, AUDIO, PROGRESS
    val attachmentUrl: String? = null
) : Serializable

@Entity(tableName = "progress_metrics")
data class ProgressMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val traineeId: Int,
    val date: Long = System.currentTimeMillis(),
    val weight: Double, // in kg
    val chestCm: Double = 0.0,
    val armsCm: Double = 0.0,
    val waistCm: Double = 0.0,
    val photoUrl: String? = null // progress image path
) : Serializable

@Entity(tableName = "supplement_plans")
data class SupplementPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val traineeId: Int,
    val itemName: String,
    val dosage: String,
    val timing: String,
    val isDone: Boolean = false
) : Serializable

@Entity(tableName = "vitamin_plans")
data class VitaminPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val traineeId: Int,
    val itemName: String,
    val dosage: String,
    val timing: String,
    val isDone: Boolean = false
) : Serializable

@Entity(tableName = "global_exercises")
data class GlobalExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val defaultMuscleGroup: String,
    val defaultVideoUrl: String = ""
) : Serializable
