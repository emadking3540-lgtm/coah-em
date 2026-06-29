package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = GymRepository(db)

    // Auth State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _coachInfo = MutableStateFlow<User?>(null)
    val coachInfo: StateFlow<User?> = _coachInfo.asStateFlow()

    // Coach Dashboard Trainees
    val trainees: StateFlow<List<User>> = repository.allTrainees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Trainee (for detail, edit, chat views)
    private val _selectedTrainee = MutableStateFlow<User?>(null)
    val selectedTrainee: StateFlow<User?> = _selectedTrainee.asStateFlow()

    // Selected Custom Workouts Flow
    val selectedTraineeWorkouts: StateFlow<List<WorkoutPlan>> = _selectedTrainee
        .flatMapLatest { trainee ->
            if (trainee != null) repository.getWorkoutsForTrainee(trainee.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Custom Meals Flow
    val selectedTraineeMeals: StateFlow<List<NutritionPlan>> = _selectedTrainee
        .flatMapLatest { trainee ->
            if (trainee != null) repository.getMealsForTrainee(trainee.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Trainee Chat Flow
    val chatMessages: StateFlow<List<ChatMessage>> = _selectedTrainee
        .flatMapLatest { trainee ->
            if (trainee != null) repository.getMessagesForTrainee(trainee.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Trainee Progress Metrics Flow
    val progressMetrics: StateFlow<List<ProgressMetric>> = _selectedTrainee
        .flatMapLatest { trainee ->
            if (trainee != null) repository.getProgressForTrainee(trainee.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Trainee Supplements Flow
    val selectedTraineeSupplements: StateFlow<List<SupplementPlan>> = _selectedTrainee
        .flatMapLatest { trainee ->
            if (trainee != null) repository.getSupplementsForTrainee(trainee.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Trainee Vitamins Flow
    val selectedTraineeVitamins: StateFlow<List<VitaminPlan>> = _selectedTrainee
        .flatMapLatest { trainee ->
            if (trainee != null) repository.getVitaminsForTrainee(trainee.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global Reusable Exercises Library Flow
    val globalExercises: StateFlow<List<GlobalExercise>> = repository.getAllGlobalExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Auto seed DB to guarantee functional state & beautiful mockup on launch
        viewModelScope.launch {
            seedDatabaseIfNeeded()
            _coachInfo.value = repository.getCoach()
        }
    }

    // Actions
    fun setSelectedTrainee(trainee: User?) {
        _selectedTrainee.value = trainee
    }

    fun loginWithGoogle(
        email: String,
        onSuccess: (User) -> Unit,
        onNewUserNeeded: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loginError.value = null
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail == "kinge767@gmail.com") {
                var coach = repository.getUserByEmail("kinge767@gmail.com")
                if (coach == null) {
                    coach = User(
                        fullName = "الكابتن أحمد علي",
                        phone = "0111223344",
                        email = "kinge767@gmail.com",
                        password = "123",
                        isCoach = true,
                        bankCardNumber = "4321654309871122",
                        bankName = "مصرف الرافدين - بغداد",
                        bankCardHolder = "احمد علي حمزة"
                    )
                    repository.insertUser(coach)
                    coach = repository.getUserByEmail("kinge767@gmail.com")
                }
                if (coach != null) {
                    _currentUser.value = coach
                    _selectedTrainee.value = null
                    onSuccess(coach)
                }
            } else {
                val existing = repository.getUserByEmail(trimmedEmail)
                if (existing != null) {
                    if (existing.isCoach) {
                        _loginError.value = "غير مصرح لك بتسجيل الدخول كمدرب"
                    } else {
                        _currentUser.value = existing
                        _selectedTrainee.value = existing
                        onSuccess(existing)
                    }
                } else {
                    onNewUserNeeded(trimmedEmail)
                }
            }
        }
    }

    fun registerNewGoogleUser(
        email: String,
        fullName: String,
        age: Int,
        height: Double,
        goal: String,
        onSuccess: (User) -> Unit
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val trainee = User(
                fullName = fullName,
                phone = "0770000000",
                email = email.trim().lowercase(),
                password = "123",
                age = age,
                height = height,
                weight = 75.0,
                goal = goal,
                subscriptionStart = now,
                subscriptionEnd = now + (30L * 24 * 60 * 60 * 1000),
                isCoach = false
            )
            val nextId = repository.insertUser(trainee)
            val user = repository.getUserByIdSuspend(nextId.toInt())
            if (user != null) {
                _currentUser.value = user
                _selectedTrainee.value = user
                generateStarterPlan(user.id, goal)
                onSuccess(user)
            }
        }
    }

    fun loginWithEmail(email: String, pin: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.getUserByEmail(email.trim())
            if (user != null && user.password == pin.trim()) {
                _currentUser.value = user
                _loginError.value = null
                onSuccess(user)
                if (!user.isCoach) {
                    _selectedTrainee.value = user
                }
            } else {
                _loginError.value = "البريد الإلكتروني أو رمز الدخول غير صحيح"
            }
        }
    }

    fun loginAsDemoUser(isCoach: Boolean, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            if (isCoach) {
                val coach = repository.getCoach()
                if (coach != null) {
                    _currentUser.value = coach
                    _selectedTrainee.value = null
                    onSuccess(coach)
                }
            } else {
                val list = trainees.value
                if (list.isNotEmpty()) {
                    val trainee = list.first()
                    _currentUser.value = trainee
                    _selectedTrainee.value = trainee
                    onSuccess(trainee)
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedTrainee.value = null
    }

    // --- CRUD Trainees ---
    fun addTrainee(
        name: String,
        phone: String,
        email: String,
        age: Int,
        height: Double,
        weight: Double,
        goal: String,
        subMonths: Int,
        healthHistory: String,
        privateNotes: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val expiry = now + (subMonths * 30L * 24 * 60 * 60 * 1000)
            val trainee = User(
                fullName = name,
                phone = phone,
                email = email.trim().lowercase(),
                password = "123", // standard seed passcode
                age = age,
                height = height,
                weight = weight,
                goal = goal,
                subscriptionStart = now,
                subscriptionEnd = expiry,
                healthHistory = healthHistory,
                privateNotes = privateNotes,
                isCoach = false
            )
            val nextId = repository.insertUser(trainee)

            // Auto-generate some basic starter template workouts & meal programs for newly added trainee!
            generateStarterPlan(nextId.toInt(), goal)
        }
    }

    fun updateTrainee(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
            if (_selectedTrainee.value?.id == user.id) {
                _selectedTrainee.value = user
            }
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = user
            }
        }
    }

    fun updateCoach(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
            _coachInfo.value = user
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = user
            }
        }
    }

    fun deleteTrainee(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
            if (_selectedTrainee.value?.id == user.id) {
                _selectedTrainee.value = null
            }
        }
    }

    // --- Workout Actions ---
    fun addOrUpdateWorkout(workout: WorkoutPlan) {
        viewModelScope.launch {
            if (workout.id == 0) {
                repository.insertWorkout(workout)
            } else {
                repository.updateWorkout(workout)
            }
        }
    }

    fun deleteWorkout(workout: WorkoutPlan) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun toggleWorkoutDone(workout: WorkoutPlan) {
        viewModelScope.launch {
            repository.updateWorkout(workout.copy(isDone = !workout.isDone))
        }
    }

    fun clearWorkoutSchedule(traineeId: Int) {
        viewModelScope.launch {
            repository.clearWorkoutsForTrainee(traineeId)
        }
    }

    // --- Supplement Actions ---
    fun addOrUpdateSupplement(supplement: SupplementPlan) {
        viewModelScope.launch {
            if (supplement.id == 0) {
                repository.insertSupplement(supplement)
            } else {
                repository.updateSupplement(supplement)
            }
        }
    }

    fun deleteSupplement(supplement: SupplementPlan) {
        viewModelScope.launch {
            repository.deleteSupplement(supplement)
        }
    }

    fun toggleSupplementDone(supplement: SupplementPlan) {
        viewModelScope.launch {
            repository.updateSupplement(supplement.copy(isDone = !supplement.isDone))
        }
    }

    fun clearSupplementsForTrainee(traineeId: Int) {
        viewModelScope.launch {
            repository.clearSupplementsForTrainee(traineeId)
        }
    }

    // --- Vitamin Actions ---
    fun addOrUpdateVitamin(vitamin: VitaminPlan) {
        viewModelScope.launch {
            if (vitamin.id == 0) {
                repository.insertVitamin(vitamin)
            } else {
                repository.updateVitamin(vitamin)
            }
        }
    }

    fun deleteVitamin(vitamin: VitaminPlan) {
        viewModelScope.launch {
            repository.deleteVitamin(vitamin)
        }
    }

    fun toggleVitaminDone(vitamin: VitaminPlan) {
        viewModelScope.launch {
            repository.updateVitamin(vitamin.copy(isDone = !vitamin.isDone))
        }
    }

    fun clearVitaminsForTrainee(traineeId: Int) {
        viewModelScope.launch {
            repository.clearVitaminsForTrainee(traineeId)
        }
    }

    // --- Global Reusable Exercises Actions ---
    fun addGlobalExercise(name: String, group: String, video: String = "") {
        viewModelScope.launch {
            repository.insertGlobalExercise(
                GlobalExercise(name = name, defaultMuscleGroup = group, defaultVideoUrl = video)
            )
        }
    }

    fun deleteGlobalExercise(exercise: GlobalExercise) {
        viewModelScope.launch {
            repository.deleteGlobalExercise(exercise)
        }
    }

    // --- Nutrition Actions ---
    fun addOrUpdateMeal(meal: NutritionPlan) {
        viewModelScope.launch {
            if (meal.id == 0) {
                repository.insertMeal(meal)
            } else {
                repository.updateMeal(meal)
            }
        }
    }

    fun deleteMeal(meal: NutritionPlan) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
        }
    }

    // --- Chat Actions ---
    fun sendChatMessage(text: String, attachType: String = "TEXT", attachUrl: String? = null) {
        val trainee = _selectedTrainee.value ?: return
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            val msg = ChatMessage(
                traineeId = trainee.id,
                senderId = current.id,
                senderName = current.fullName,
                messageText = text,
                attachmentType = attachType,
                attachmentUrl = attachUrl
            )
            repository.insertMessage(msg)
        }
    }

    // --- Progress Metric Actions ---
    fun addProgressMetric(weight: Double, chest: Double, arms: Double, waist: Double, imageUrl: String? = null) {
        val trainee = _selectedTrainee.value ?: return
        viewModelScope.launch {
            val metric = ProgressMetric(
                traineeId = trainee.id,
                weight = weight,
                chestCm = chest,
                armsCm = arms,
                waistCm = waist,
                photoUrl = imageUrl
            )
            repository.insertProgress(metric)

            // Update user's current weight to match the latest progress metric!
            val updatedUser = trainee.copy(weight = weight)
            repository.updateUser(updatedUser)
            _selectedTrainee.value = updatedUser
            if (_currentUser.value?.id == trainee.id) {
                _currentUser.value = updatedUser
            }
        }
    }

    fun deleteProgressMetric(metric: ProgressMetric) {
        viewModelScope.launch {
            repository.deleteProgress(metric)
        }
    }

    // --- Seed Mechanisms ---
    private suspend fun seedDatabaseIfNeeded() {
        val traineesList = repository.getUserByEmail("ahmed@gymzone.com")
        if (traineesList != null) return // Already seeded

        // 1. Insert Coach
        val coach = User(
            fullName = "الكابتن أحمد علي",
            phone = "0111223344",
            email = "kinge767@gmail.com",
            password = "123",
            isCoach = true,
            bankCardNumber = "4321654309871122",
            bankName = "مصرف الرافدين - بغداد",
            bankCardHolder = "احمد علي حمزة"
        )
        val coachId = repository.insertUser(coach).toInt()

        // 2. Insert Ahmed (Bulking Trainee)
        val now = System.currentTimeMillis()
        val trainee1 = User(
            fullName = "أحمد حسن (تضخيم)",
            phone = "01012345678",
            email = "ahmed@gymzone.com",
            password = "123",
            age = 24,
            height = 180.0,
            weight = 72.5,
            goal = "تضخيم",
            subscriptionStart = now - (15L * 24 * 60 * 60 * 1000), // joined 15 days ago
            subscriptionEnd = now + (15L * 24 * 60 * 60 * 1000), // active, expires in 15 days
            healthHistory = "لا توجد إصابات سابقة - يعاني من حساسية طفيفة للشوفان لكن يمكنه تناوله مطبوخاً بشكل جيد.",
            privateNotes = "ملتزم ومتحمس للغاية. يهدف لزيادة الوزن العضلي الصافي بمعدل 4 كجم في هذا الطور.",
            isCoach = false
        )
        val t1Id = repository.insertUser(trainee1).toInt()

        // 3. Insert Mohamed (Cutting Trainee - near expiry)
        val trainee2 = User(
            fullName = "محمد سعيد (تنشيف)",
            phone = "01098765432",
            email = "mohamed@gymzone.com",
            password = "123",
            age = 29,
            height = 174.0,
            weight = 88.0,
            goal = "تنشيف",
            subscriptionStart = now - (28L * 24 * 60 * 60 * 1000), // joined 28 days ago
            subscriptionEnd = now + (2L * 24 * 60 * 60 * 1000), // expires in 2 days (shows warning!)
            healthHistory = "خشونة بسيطة في الركبة اليسرى - ينصح بعدم استخدام أوزان ثقيلة للغاية في جهاز السكوات الحر واستخدام المكبس كبديل آمن.",
            privateNotes = "يريد خفض نسبة الدهون من 22٪ إلى 14٪ للمشاركة في حدث هواة مطلع الصيف الحالي.",
            isCoach = false
        )
        val t2Id = repository.insertUser(trainee2).toInt()

        // 4. Seed Workouts for Ahmed (Bulking)
        val workoutsT1 = listOf(
            WorkoutPlan(traineeId = t1Id, dayOfWeek = "السبت", muscleGroup = "الصدر والترايسبس", exerciseName = "بار مستوي (Bench Press)", sets = 4, reps = "12-10-8-6", notes = "نزول بطيء ومحكوم وثانية ثبات في الأسفل"),
            WorkoutPlan(traineeId = t1Id, dayOfWeek = "السبت", muscleGroup = "الصدر والترايسبس", exerciseName = "تجميع دمبل مائل (Incline DB)", sets = 4, reps = "10-10-8-8", notes = "مد حركي كامل دون إمالة المرفقين"),
            WorkoutPlan(traineeId = t1Id, dayOfWeek = "السبت", muscleGroup = "الصدر والترايسبس", exerciseName = "تفتيح كابل سفلي لمسار عضلة الصدر", sets = 3, reps = "12-12-10", notes = "عصر قوي جداً لمدة ثانيتين في نهاية كل تكرار"),
            WorkoutPlan(traineeId = t1Id, dayOfWeek = "الأحد", muscleGroup = "الظهر والبايسبس", exerciseName = "سحب عالي قبضة واسعة (Lat Pulldown)", sets = 4, reps = "12-10-10-8", notes = "تجنب الأرجحة الخلفية بالظهر"),
            WorkoutPlan(traineeId = t1Id, dayOfWeek = "الأحد", muscleGroup = "الظهر والبايسبس", exerciseName = "سحب بار على مائل (T-Bar Row)", sets = 4, reps = "10-8-8-8", notes = "ابق ظهرك مسطحاً وصدرك للأعلى"),
            WorkoutPlan(traineeId = t1Id, dayOfWeek = "الثلاثاء", muscleGroup = "الأكتاف والبطن", exerciseName = "ضغط دمبل كتف (DB Shoulder Press)", sets = 4, reps = "12-10-8-8", notes = "التركيز على حماية مفصل الكتف"),
            WorkoutPlan(traineeId = t1Id, dayOfWeek = "الأربعاء", muscleGroup = "الأرجل", exerciseName = "سكوات حر بالبار (Squat)", sets = 4, reps = "12-10-10-8", notes = "عمق كامل تحت الموازي بأمان مع شد الجذع")
        )
        workoutsT1.forEach { repository.insertWorkout(it) }

        // 5. Seed Meals for Ahmed (Bulking)
        val mealsT1 = listOf(
            NutritionPlan(traineeId = t1Id, mealName = "الوجبة 1: الفطور", contents = "100 جرام شوفان مطبوخ بالماء + سكوب بروتين + موزة كاملة + 5 حبات لوز", calories = 620, protein = 38.0, carbs = 82.0, fats = 12.0, alternatives = "بديل: 4 بياض بيض + بيضتين كاملتين + رغيف خبز أسمر"),
            NutritionPlan(traineeId = t1Id, mealName = "الوجبة 2: الغداء (قبل التمرين)", contents = "150 جرام صدور دجاج مشوية + 200 جرام أرز بسمتي أبيض مسلوق + خضار مشكل", calories = 680, protein = 46.0, carbs = 95.0, fats = 8.0, alternatives = "بديل: 180 جرام لحم بقري مفروم قليل الدسم + 250 جرام بطاطس مشوية"),
            NutritionPlan(traineeId = t1Id, mealName = "الوجبة 3: العشاء (بعد التمرين)", contents = "150 جرام سمك فيليه مشوي + 150 جرام بطاطا حلوة مسلوقة + سلطة خضراء بملعقة زيت زيتون", calories = 520, protein = 35.0, carbs = 55.0, fats = 15.0, supplements = "أوميجا 3 (حبه واحدة)")
        )
        mealsT1.forEach { repository.insertMeal(it) }

        // 6. Seed Workouts for Mohamed (Cutting)
        val workoutsT2 = listOf(
            WorkoutPlan(traineeId = t2Id, dayOfWeek = "السبت", muscleGroup = "أرجل وبطن", exerciseName = "مكبس أرجل (Leg Press)", sets = 4, reps = "15-12-12-10", notes = "بديل آمن للسكوات الحر بسبب ركبتك. التحكم في النزول دقيق جداً"),
            WorkoutPlan(traineeId = t2Id, dayOfWeek = "السبت", muscleGroup = "أرجل وبطن", exerciseName = "رفرفة خلفية أرجل (Leg Curl)", sets = 4, reps = "15-12-12-12", notes = "قبضة قوية للجسم ثابتاً"),
            WorkoutPlan(traineeId = t2Id, dayOfWeek = "الأحد", muscleGroup = "دفع (Push - صدر وأكتاف وتراي)", exerciseName = "ضغط تجميع دمبل مستوي", sets = 4, reps = "12-10-10-10", notes = "تحكم كامل لعدم الضغط على الأوتار"),
            WorkoutPlan(traineeId = t2Id, dayOfWeek = "الاثنين", muscleGroup = "سحب (Pull - ظهر وباي)", exerciseName = "سحب أرضي كابل قبضة ضيقة", sets = 4, reps = "12-12-10-10", notes = "فرد كامل وتراجع للأكتاف للخلف")
        )
        workoutsT2.forEach { repository.insertWorkout(it) }

        // 7. Seed Meals for Mohamed (Cutting)
        val mealsT2 = listOf(
            NutritionPlan(traineeId = t2Id, mealName = "الوجبة 1: الفطور", contents = "5 بياض بيض مسلوق + بيضة واحدة كاملة + 50 جرام خبز سن أسود + خيار وجرجير", calories = 330, protein = 28.0, carbs = 20.0, fats = 10.0, supplements = "مالتي فيتامين (حبه)"),
            NutritionPlan(traineeId = t2Id, mealName = "الوجبة 2: الغداء", contents = "180 جرام صدور دجاج مشوية + 100 جرام أرز مسلوق + طبق كوسة مسلوقة دايت", calories = 480, protein = 48.0, carbs = 42.0, fats = 6.0, alternatives = "بديل: 180 جرام علبة تونة مصفاة من الزيت تماماً"),
            NutritionPlan(traineeId = t2Id, mealName = "الوجبة 3: قبل النوم", contents = "200 جرام جبنة قريش دايت + خيارتين", calories = 210, protein = 24.0, carbs = 6.0, fats = 4.0, supplements = "جلوتامين 5g + زنك")
        )
        mealsT2.forEach { repository.insertMeal(it) }

        // 8. Seed Chat Messages for Ahmed (Trainee 1)
        val msgListT1 = listOf(
            ChatMessage(traineeId = t1Id, senderId = coachId, senderName = "الكابتن أحمد علي", messageText = "أهلاً بك يا بطل في GymZone! لقد قمت بكتابة برنامج التمارين والتغذية المخصصة لك. ابدأ بالالتزام ودعنا نتتبع تقدمك أسبوعياً.", timestamp = now - (3L * 24 * 60 * 60 * 1000)),
            ChatMessage(traineeId = t1Id, senderId = t1Id, senderName = "أحمد حسن (تضخيم)", messageText = "أهلاً يا كوتش! شكراً جزيلاً البرامج واضحة جداً وتصميم التطبيق رائع وعصري. سألتزم بالتمارين والغذاء بدءاً من الغد.", timestamp = now - (2L * 24 * 60 * 60 * 1000)),
            ChatMessage(traineeId = t1Id, senderId = t1Id, senderName = "أحمد حسن (تضخيم)", messageText = "كابتن، قمت بقياس الوزن اليوم وهو 72.5 كجم بزيادة نصف كيلو عن الأسبوع الماضي!", timestamp = now - (1L * 24 * 60 * 60 * 1000)),
            ChatMessage(traineeId = t1Id, senderId = coachId, senderName = "الكابتن أحمد علي", messageText = "رائع وممتاز يا بطل! استبشر خيراً، الاستمرارية والالتزام الدقيق بالوجبات هي المفتاح الأساسي. استمر بنفس الحماس والقوة 💥💪", timestamp = now - (12 * 60 * 60 * 1000))
        )
        msgListT1.forEach { repository.insertMessage(it) }

        // 9. Seed Progress Metrics for Ahmed (Trainee 1)
        val metricsT1 = listOf(
            ProgressMetric(traineeId = t1Id, date = now - (21L * 24 * 60 * 60 * 1000), weight = 71.0, chestCm = 104.0, armsCm = 36.5, waistCm = 84.0),
            ProgressMetric(traineeId = t1Id, date = now - (14L * 24 * 60 * 60 * 1000), weight = 71.5, chestCm = 104.8, armsCm = 37.0, waistCm = 83.8),
            ProgressMetric(traineeId = t1Id, date = now - (7L * 24 * 60 * 60 * 1000), weight = 72.0, chestCm = 105.5, armsCm = 37.2, waistCm = 83.5),
            ProgressMetric(traineeId = t1Id, date = now, weight = 72.5, chestCm = 106.2, armsCm = 37.6, waistCm = 83.4)
        )
        metricsT1.forEach { repository.insertProgress(it) }

        // 10. Seed Progress Metrics for Mohamed (Trainee 2)
        val metricsT2 = listOf(
            ProgressMetric(traineeId = t2Id, date = now - (28L * 24 * 60 * 60 * 1000), weight = 92.4, chestCm = 114.0, armsCm = 43.5, waistCm = 98.0),
            ProgressMetric(traineeId = t2Id, date = now - (21L * 24 * 60 * 60 * 1000), weight = 91.0, chestCm = 113.8, armsCm = 43.1, waistCm = 96.0),
            ProgressMetric(traineeId = t2Id, date = now - (14L * 24 * 60 * 60 * 1000), weight = 90.1, chestCm = 113.2, armsCm = 42.8, waistCm = 94.5),
            ProgressMetric(traineeId = t2Id, date = now - (7L * 24 * 60 * 60 * 1000), weight = 88.9, chestCm = 112.5, armsCm = 42.5, waistCm = 92.5),
            ProgressMetric(traineeId = t2Id, date = now, weight = 88.0, chestCm = 112.0, armsCm = 42.1, waistCm = 90.5)
        )
        metricsT2.forEach { repository.insertProgress(it) }

        // Seed Message for Mohamed
        ChatMessage(traineeId = t2Id, senderId = coachId, senderName = "الكابتن أحمد علي", messageText = "يا بطل، قمت بمراجعة الوزن اليوم. الوزن انخفض لـ 88 كيلو وهذا رائع جداً! متبقي يومين فقط على نهاية اشتراكك، دعنا نجدد الاشتراك غداً للبدء في طور التقطيع الأقوى ونظام الكربوهيدرات المتقطع.", timestamp = now - (4 * 60 * 60 * 1000)).also {
            repository.insertMessage(it)
        }
    }

    private suspend fun generateStarterPlan(traineeId: Int, goal: String) {
        val workouts = if (goal == "تنشيف" || goal == "تنشيف") {
            listOf(
                WorkoutPlan(traineeId = traineeId, dayOfWeek = "السبت", muscleGroup = "دفع (صدر/كتف/تراي)", exerciseName = "Bench Press", sets = 4, reps = "15-12-10-10"),
                WorkoutPlan(traineeId = traineeId, dayOfWeek = "السبت", muscleGroup = "دفع (صدر/كتف/تراي)", exerciseName = "Lateral Raises", sets = 4, reps = "15-15-12-12"),
                WorkoutPlan(traineeId = traineeId, dayOfWeek = "الأحد", muscleGroup = "سحب (ظهر/باي)", exerciseName = "Lat Pulldown", sets = 4, reps = "12-12-10-10"),
                WorkoutPlan(traineeId = traineeId, dayOfWeek = "الاثنين", muscleGroup = "أرجل وبطن", exerciseName = "Leg Press", sets = 4, reps = "15-15-12-12")
            )
        } else {
            listOf(
                WorkoutPlan(traineeId = traineeId, dayOfWeek = "السبت", muscleGroup = "الصدر والذراع", exerciseName = "Incline Dumbbell Press", sets = 4, reps = "12-10-8-8"),
                WorkoutPlan(traineeId = traineeId, dayOfWeek = "الأحد", muscleGroup = "الظهر والأكتاف", exerciseName = "Barbell Rows", sets = 4, reps = "10-10-8-8"),
                WorkoutPlan(traineeId = traineeId, dayOfWeek = "الثلاثاء", muscleGroup = "أرجل كاملة", exerciseName = "Squats", sets = 4, reps = "12-10-8-8")
            )
        }
        workouts.forEach { repository.insertWorkout(it) }

        val meals = if (goal == "تنشيف" || goal == "تنشيف") {
            listOf(
                NutritionPlan(traineeId = traineeId, mealName = "الوجبة 1", contents = "5 بياض بيض + نصف رغيف خبز بلدي + خيار وجرجير", calories = 300, protein = 25.0, carbs = 30.0, fats = 5.0),
                NutritionPlan(traineeId = traineeId, mealName = "الوجبة 2", contents = "150g صدور دجاج مشوية + 100g أرز أبيض مسلوق + خضار سوتيه", calories = 450, protein = 45.0, carbs = 40.0, fats = 6.0)
            )
        } else {
            listOf(
                NutritionPlan(traineeId = traineeId, mealName = "الوجبة 1", contents = "3 بيضات كاملة + 80g شوفان بالحليب والموز عسل", calories = 600, protein = 32.0, carbs = 75.0, fats = 18.0),
                NutritionPlan(traineeId = traineeId, mealName = "الوجبة 2", contents = "200g صدور دجاج مشوية + 200g أرز أبيض مسلوق + كوب فاصوليا خضراء", calories = 720, protein = 58.0, carbs = 90.0, fats = 8.0)
            )
        }
        meals.forEach { repository.insertMeal(it) }

        // Start Chat Message
        ChatMessage(
            traineeId = traineeId,
            senderId = 1, // Coach simulation
            senderName = "الكابتن أحمد علي",
            messageText = "مرحباً بك يا بطل! تم إنشاء خطتك للـ $goal بنجاح. سنعمل سوياً على تحقيق هدفك. دعنا نتابع وزنك وصور التقدم وصحّتك العامة هنا."
        ).also { repository.insertMessage(it) }
    }
}
