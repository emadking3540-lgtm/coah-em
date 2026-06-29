package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.net.Uri
import java.io.File
import com.example.data.*
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.util.*
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter

// Sealed Class representing state-based Navigation routes
sealed class Screen {
    object Login : Screen()
    object CoachDashboard : Screen()
    data class PlayerDetail(val traineeId: Int) : Screen()
    object PlayerDashboard : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymZoneApp(viewModel: MainViewModel) {
    // Basic Local Navigation Router state
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    val hostUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val trainees by viewModel.trainees.collectAsStateWithLifecycle()
    val selectedTrainee by viewModel.selectedTrainee.collectAsStateWithLifecycle()

    val workouts by viewModel.selectedTraineeWorkouts.collectAsStateWithLifecycle()
    val meals by viewModel.selectedTraineeMeals.collectAsStateWithLifecycle()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val metrics by viewModel.progressMetrics.collectAsStateWithLifecycle()
    val supplements by viewModel.selectedTraineeSupplements.collectAsStateWithLifecycle()
    val vitamins by viewModel.selectedTraineeVitamins.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_main_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.15f
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState is Screen.Login) -width else width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> if (targetState is Screen.Login) width else -width } + fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.Login -> {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { user ->
                                if (user.isCoach) {
                                    currentScreen = Screen.CoachDashboard
                                } else {
                                    currentScreen = Screen.PlayerDashboard
                                }
                            }
                        )
                    }
                    is Screen.CoachDashboard -> {
                        CoachDashboardScreen(
                            viewModel = viewModel,
                            trainees = trainees,
                            onSelectTrainee = { trainee ->
                                viewModel.setSelectedTrainee(trainee)
                                currentScreen = Screen.PlayerDetail(trainee.id)
                            },
                            onLogout = {
                                viewModel.logout()
                                currentScreen = Screen.Login
                            }
                        )
                    }
                    is Screen.PlayerDetail -> {
                        PlayerDetailScreen(
                            viewModel = viewModel,
                            trainee = selectedTrainee,
                            workouts = workouts,
                            meals = meals,
                            messages = messages,
                            metrics = metrics,
                            supplements = supplements,
                            vitamins = vitamins,
                            onBack = {
                                viewModel.setSelectedTrainee(null)
                                currentScreen = Screen.CoachDashboard
                            }
                        )
                    }
                    is Screen.PlayerDashboard -> {
                        PlayerDashboardScreen(
                            viewModel = viewModel,
                            trainee = hostUser,
                            workouts = workouts,
                            meals = meals,
                            messages = messages,
                            metrics = metrics,
                            supplements = supplements,
                            vitamins = vitamins,
                            onLogout = {
                                viewModel.logout()
                                currentScreen = Screen.Login
                            }
                        )
                    }
                }
            }
        }
    }
}
}

// ----------------------------------------------------------------------------------
// 1. LOGIN SCREEN
// ----------------------------------------------------------------------------------
@Composable
fun GoogleAccountChooserDialog(
    onDismiss: () -> Unit,
    onAccountSelected: (String) -> Unit,
    onCustomAccount: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختر حساب Google للمتابعة",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Coach
                AccountRowItem(
                    email = "kinge767@gmail.com",
                    name = "الكابتن أحمد علي (المدرب الرئيسي)",
                    isCoach = true,
                    onClick = { onAccountSelected("kinge767@gmail.com") }
                )
                // Trainee 1
                AccountRowItem(
                    email = "ahmed@gymzone.com",
                    name = "أحمد حسن (لاعب - تضخيم)",
                    isCoach = false,
                    onClick = { onAccountSelected("ahmed@gymzone.com") }
                )
                // Trainee 2
                AccountRowItem(
                    email = "mohamed@gymzone.com",
                    name = "محمد سعيد (لاعب - تنشيف)",
                    isCoach = false,
                    onClick = { onAccountSelected("mohamed@gymzone.com") }
                )
                
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                
                // Custom email
                OutlinedButton(
                    onClick = onCustomAccount,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SportAccentCyan),
                    border = BorderStroke(1.dp, SportAccentCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تسجيل بحساب Google آخر...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AccountRowItem(
    email: String,
    name: String,
    isCoach: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isCoach) SportAccentLime.copy(alpha = 0.4f) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (isCoach) Icons.Default.SupervisorAccount else Icons.Default.Person,
                contentDescription = null,
                tint = if (isCoach) SportAccentLime else Color.White,
                modifier = Modifier.size(24.dp)
            )
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f).padding(end = 12.dp)
            ) {
                Text(text = name, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = email, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun CustomAccountInputDialog(
    onDismiss: () -> Unit,
    onEmailEntered: (String) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "سجل دخولك ببريد Google الخاص بك",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("البريد الإلكتروني (Google Account)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportAccentCyan,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = errorText, color = Color.Red, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailInput.contains("@") && emailInput.contains(".")) {
                        onEmailEntered(emailInput.trim())
                    } else {
                        errorText = "يرجى إدخال بريد إلكتروني صحيح"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SportAccentCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("متابعة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun GoogleOnboardingDialog(
    email: String,
    onDismiss: () -> Unit,
    onComplete: (fullName: String, age: Int, height: Double, goal: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }
    var heightString by remember { mutableStateOf("") }
    var goalSelected by remember { mutableStateOf("تضخيم") }
    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "استكمال الملف الشخصي للاعب الجديد 🏋️",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SportAccentLime,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "مرحباً بك! لقد سجلت الدخول باستخدام Google لأول مرة. يرجى تزويد الكابتن بالبيانات البدنية لإنشاء خططك الرياضية والغذائية:",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم الكامل") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentLime),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = heightString,
                        onValueChange = { heightString = it },
                        label = { Text("الطول (سم)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentLime),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = ageString,
                        onValueChange = { ageString = it },
                        label = { Text("العمر") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentLime),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text(
                    text = "الهدف الرياضي الحالي:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.End)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val goals = listOf("تضخيم", "تنشيف", "لياقة بدنية")
                    goals.forEach { g ->
                        val isSelected = goalSelected == g
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SportAccentLime else Color(0xFF2C2C2C))
                                .clickable { goalSelected = g },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = g,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (errorText.isNotEmpty()) {
                    Text(text = errorText, color = Color.Red, fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val age = ageString.toIntOrNull()
                    val height = heightString.toDoubleOrNull()
                    if (fullName.isEmpty() || age == null || height == null) {
                        errorText = "يرجى ملء جميع الحقول بشكل صحيح"
                    } else {
                        onComplete(fullName, age, height, goalSelected)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SportAccentLime, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ واستمرار", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (User) -> Unit
) {
    var showAccountChooser by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }
    var pendingEmail by remember { mutableStateOf("") }
    
    val errorMsg by viewModel.loginError.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Premium motivational background gym image as requested
        AsyncImage(
            model = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&q=80&w=600",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Premium dark high-contrast gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color(0xFF121212).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Giant Sport Logo Emblem
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SportAccentLime, Color(0xFF88B300))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "جيم زون",
                    tint = Color.Black,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "GYMZONE",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = SportAccentLime
            )

            Text(
                text = "المنصة الذكية للمدرب kinge767@gmail.com",
                fontSize = 13.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "بناء الأجسام • جداول المكملات • تتبع الوزن أسبوعياً",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(35.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل الدخول",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "يرجى المتابعة حصرياً من خلال تسجيل الدخول بحساب Google الخاص بك لاستعراض خططك المخصصة",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stunning "Sign in with Google" Button
                    Card(
                        onClick = { showAccountChooser = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "تسجيل الدخول باستخدام Google",
                                color = Color(0xFF5F6368),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFEA4335), CircleShape))
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF4285F4), CircleShape))
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFFBBC05), CircleShape))
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF34A853), CircleShape))
                            }
                        }
                    }

                    errorMsg?.let { msg ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = msg,
                            color = Color.Red,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = "جميع الحقوق محفوظة للكابتن kinge767 © 2026",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }

    if (showAccountChooser) {
        GoogleAccountChooserDialog(
            onDismiss = { showAccountChooser = false },
            onAccountSelected = { email ->
                showAccountChooser = false
                viewModel.loginWithGoogle(
                    email = email,
                    onSuccess = { onLoginSuccess(it) },
                    onNewUserNeeded = { newEmail ->
                        pendingEmail = newEmail
                        showOnboarding = true
                    }
                )
            },
            onCustomAccount = {
                showAccountChooser = false
                showCustomInput = true
            }
        )
    }

    if (showCustomInput) {
        CustomAccountInputDialog(
            onDismiss = { showCustomInput = false },
            onEmailEntered = { email ->
                showCustomInput = false
                viewModel.loginWithGoogle(
                    email = email,
                    onSuccess = { onLoginSuccess(it) },
                    onNewUserNeeded = { newEmail ->
                        pendingEmail = newEmail
                        showOnboarding = true
                    }
                )
            }
        )
    }

    if (showOnboarding) {
        GoogleOnboardingDialog(
            email = pendingEmail,
            onDismiss = { showOnboarding = false },
            onComplete = { fullName, age, height, goal ->
                showOnboarding = false
                viewModel.registerNewGoogleUser(
                    email = pendingEmail,
                    fullName = fullName,
                    age = age,
                    height = height,
                    goal = goal,
                    onSuccess = { onLoginSuccess(it) }
                )
            }
        )
    }
}

// ----------------------------------------------------------------------------------
// 2. COACH DASHBOARD SCREEN
// ----------------------------------------------------------------------------------
@Composable
fun CoachDashboardScreen(
    viewModel: MainViewModel,
    trainees: List<User>,
    onSelectTrainee: (User) -> Unit,
    onLogout: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTrainees = trainees.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Coach Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "خروج",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    var showBankSettingsDialog by remember { mutableStateOf(false) }
                    IconButton(onClick = { showBankSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "الملف المصرفي",
                            tint = SportAccentCyan
                        )
                    }
                    
                    if (showBankSettingsDialog) {
                        CoachBankSettingsDialog(
                            viewModel = viewModel,
                            onDismiss = { showBankSettingsDialog = false }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "مدرب كمال أجسام",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "لوحة التحكم",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        )
                        Text(
                            text = "كابتن",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val almostExpiredCount = trainees.count {
                val timeLeftMs = it.subscriptionEnd - System.currentTimeMillis()
                val daysLeft = (timeLeftMs / (24 * 60 * 60 * 1000)).toInt()
                daysLeft <= 3
            }

            // Quick overview cards in Bold Typography style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Active Players
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${trainees.size}",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "لاعب نشط",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Card 2: Support Channels / Chats
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%02d", trainees.size),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "رسائل جديدة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Card 3: Expiry Close (Primary Colored Hero Stat!)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%02d", almostExpiredCount),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, color = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "انتهاء قريب",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black.copy(alpha = 0.6f),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Trainee list section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة الأبطال",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث باسم اللاعب...") },
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, "Search", modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTrainees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty list",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا يوجد لاعبون نشطون حالياً مطابق للبحث",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTrainees) { player ->
                        TraineeCard(
                            player = player,
                            onClick = { onSelectTrainee(player) }
                        )
                    }
                }
            }
        }

        // Add Trainee Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة لاعب")
        }

        if (showAddDialog) {
            AddTraineeDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, phone, email, age, height, weight, goal, months, health, notes ->
                    viewModel.addTrainee(name, phone, email, age, height, weight, goal, months, health, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

// Custom Trainee Item Card Component
@Composable
fun TraineeCard(
    player: User,
    onClick: () -> Unit
) {
    val timeLeftMs = player.subscriptionEnd - System.currentTimeMillis()
    val daysLeft = (timeLeftMs / (24 * 60 * 60 * 1000)).toInt()
    val isAlmostExpired = daysLeft <= 3

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subscription warning tag
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isAlmostExpired) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "على وشك الانتهاء",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = when (player.goal) {
                                    "تضخيم" -> listOf(SportAccentLime, Color(0xFFC6FF00))
                                    "تنشيف" -> listOf(Color(0xFFE040FB), SportAccentCyan)
                                    else -> listOf(SportAccentOrange, Color(0xFFFFCDD2))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.fullName.firstOrNull()?.toString() ?: "",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = player.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "الهدف: " + player.goal,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (player.goal) {
                        "تضخيم" -> SportAccentLime
                        "تنشيف" -> SportAccentCyan
                        else -> SportAccentOrange
                    },
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${player.weight} كجم | ${player.height} سم",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Days left indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isAlmostExpired) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "متبقي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (daysLeft < 0) 0 else daysLeft} يوم",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = if (isAlmostExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Add client interactive registration Dialog form
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTraineeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int, Double, Double, String, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("25") }
    var heightStr by remember { mutableStateOf("175") }
    var weightStr by remember { mutableStateOf("70") }
    var selectedGoal by remember { mutableStateOf("تضخيم") }
    var monthsStr by remember { mutableStateOf("1") }
    var healthHistory by remember { mutableStateOf("سليم، لا يعاني من إصابات.") }
    var privateNotes by remember { mutableStateOf("") }

    val goals = listOf("تضخيم", "تنشيف", "لياقة")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "تسجيل بطل جديد (لاعب)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل للاعب") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it },
                        label = { Text("العمر") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = { heightStr = it },
                        label = { Text("الطول (سم)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("الوزن الحالي (كجم)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("الهدف الرئيسي", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    goals.forEach { goal ->
                        val isSelected = selectedGoal == goal
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGoal = goal },
                            label = { Text(goal) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = monthsStr,
                    onValueChange = { monthsStr = it },
                    label = { Text("مدة الاشتراك بالشهور") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = healthHistory,
                    onValueChange = { healthHistory = it },
                    label = { Text("تقرير الحالة الصحية والإصابات") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = privateNotes,
                    onValueChange = { privateNotes = it },
                    label = { Text("ملاحظات المدرب الخاصة") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                onAdd(
                                    name,
                                    phone,
                                    email,
                                    ageStr.toIntOrNull() ?: 25,
                                    heightStr.toDoubleOrNull() ?: 175.0,
                                    weightStr.toDoubleOrNull() ?: 70.0,
                                    selectedGoal,
                                    monthsStr.toIntOrNull() ?: 1,
                                    healthHistory,
                                    privateNotes
                                )
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Text("إضافة البطل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// 3. PLAYER DETAIL SCREEN (ADMIN VIEW)
// ----------------------------------------------------------------------------------
@Composable
fun PlayerDetailScreen(
    viewModel: MainViewModel,
    trainee: User?,
    workouts: List<WorkoutPlan>,
    meals: List<NutritionPlan>,
    messages: List<ChatMessage>,
    metrics: List<ProgressMetric>,
    supplements: List<SupplementPlan>,
    vitamins: List<VitaminPlan>,
    onBack: () -> Unit
) {
    if (trainee == null) return

    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var currentDaySelected by remember { mutableStateOf("السبت") }

    // Dialog trigger states
    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showAddMealDialog by remember { mutableStateOf(false) }
    var showMetricsDialog by remember { mutableStateOf(false) }
    var showAddSupplementDialog by remember { mutableStateOf(false) }
    var showAddVitaminDialog by remember { mutableStateOf(false) }

    val daysList = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    Column(modifier = Modifier.fillMaxSize()) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع لخلف",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = trainee.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            // Delete player warning button callback
            var showConfirmDelete by remember { mutableStateOf(false) }
            IconButton(onClick = { showConfirmDelete = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف اللاعب",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            if (showConfirmDelete) {
                AlertDialog(
                    onDismissRequest = { showConfirmDelete = false },
                    title = { Text("حذف لاعب!", textAlign = TextAlign.Right) },
                    text = { Text("هل أنت متأكد من رغبتك في حذف هذا المشترك بالكامل مع كافة البيانات والخطط الخاصة به؟", textAlign = TextAlign.Right) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteTrainee(trainee)
                            showConfirmDelete = false
                            onBack()
                        }) {
                            Text("نعم، احذف", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDelete = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }
        }

        // Horizontal Profile Summary Cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("العمر", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${trainee.age} سنة", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الطول", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${trainee.height} سم", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الوزن الحالي", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${trainee.weight} كجم", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الهدف الرئيسي", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(trainee.goal, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Profile details expander for health conditions
        var infoExpanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp).clickable { infoExpanded = !infoExpanded }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (infoExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Details"
                    )
                    Text(
                        text = "تفاصيل المشترك الصحية والملاحظات",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (infoExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الحالة الصحية والإصابات: \n${trainee.healthHistory}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ملاحظات سرّية للمدرب: \n${trainee.privateNotes.ifEmpty { "لا توجد ملاحظات خاصة حتى الآن..." }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs to control plan components
        // 0: Workout Plan, 1: Supplements & Vitamins, 2: Diet Nutrition, 3: Tracking Progression, 4: Private Chat
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("التدريبات", fontSize = 12.sp) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("المكملات والفيتامينات", fontSize = 11.sp) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("التغذية", fontSize = 12.sp) })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("التقدم والقياسات", fontSize = 11.sp) })
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("الدردشة الدعم", fontSize = 11.sp) })
        }

        // Custom Layout states of chosen tabs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> { // Workout Program Manager
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Day Selector Scroller
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            daysList.forEach { d ->
                                FilterChip(
                                    selected = currentDaySelected == d,
                                    onClick = { currentDaySelected = d },
                                    label = { Text(d) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val dayWorkouts = workouts.filter { it.dayOfWeek == currentDaySelected }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showAddWorkoutDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "إضافة تمرين",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            val muscleOfToday = dayWorkouts.firstOrNull()?.muscleGroup ?: "مجموعات العضلات"
                            Text(
                                text = "تقسيم اليوم: $muscleOfToday",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (dayWorkouts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "يوم راحة أو لا يوجد تمارين مضافة لهذا اليوم بعد.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(dayWorkouts) { work ->
                                    WorkoutRowItem(
                                        workout = work,
                                        onDelete = { viewModel.deleteWorkout(work) }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> { // Supplements & Vitamins Manager
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.clearSupplementsForTrainee(trainee.id); viewModel.clearVitaminsForTrainee(trainee.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("مسح الجدول بالكامل", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(
                                text = "المكملات والفيتامينات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SportAccentLime
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Supplements Section (Left half)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { showAddSupplementDialog = true }) {
                                        Icon(Icons.Default.AddCircle, "إضافة مكمل", tint = SportAccentLime)
                                    }
                                    Text("المكملات الغذائية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }

                                if (supplements.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("لا يوجد مكملات مضافة", fontSize = 11.sp, color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(supplements) { item ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        IconButton(
                                                            onClick = { viewModel.deleteSupplement(item) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                        }
                                                        Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SportAccentLime)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("الجرعة: ${item.dosage}", fontSize = 11.sp)
                                                    Text("التوقيت: ${item.timing}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Vitamins Section (Right half)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { showAddVitaminDialog = true }) {
                                        Icon(Icons.Default.AddCircle, "إضافة فيتامين", tint = SportAccentCyan)
                                    }
                                    Text("الفيتامينات والمعادن", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }

                                if (vitamins.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("لا يوجد فيتامينات مضافة", fontSize = 11.sp, color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(vitamins) { item ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        IconButton(
                                                            onClick = { viewModel.deleteVitamin(item) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                        }
                                                        Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SportAccentCyan)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("الجرعة: ${item.dosage}", fontSize = 11.sp)
                                                    Text("التوقيت: ${item.timing}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Nutrition Program Manager
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showAddMealDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "إضافة وجبة غدائية",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = "البرنامج الغذائي المخصص",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val totalCalories = meals.sumOf { it.calories }
                        val totalProtein = meals.sumOf { it.protein }
                        val totalCarbs = meals.sumOf { it.carbs }
                        val totalFats = meals.sumOf { it.fats }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Macros Summary Overview Bar
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("السعرات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$totalCalories", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("بروتين", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${totalProtein}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("كارب", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${totalCarbs}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SportAccentOrange)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("دهون", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${totalFats}g", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (meals.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("لا توجد وجبات مضافة للاعب حالياً")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(meals) { meal ->
                                    MealCardItem(meal = meal, onDelete = { viewModel.deleteMeal(meal) })
                                }
                            }
                        }
                    }
                }
                3 -> { // Progress Tracking and custom chart!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showMetricsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("تسجيل قياس جديد", fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "تطور وزن وجسم اللاعب",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Connect weight progress list to custom canvas chart
                        if (metrics.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("سجل القياسات لرؤية المؤشرات البيانية")
                                }
                            }
                        } else {
                            ProgressLineChart(metrics = metrics)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "سجل التغييرات وعمليات الرصد الأخيرة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        metrics.reversed().forEach { met ->
                            ProgressMetricRowItem(metric = met, onDelete = { viewModel.deleteProgressMetric(met) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                4 -> { // PRIVATE CHAT PORTAL
                    ChatPortal(
                        messages = messages,
                        onSend = { text, type, url ->
                            viewModel.sendChatMessage(text, type, url)
                        }
                    )
                }
            }
        }
    }

    // Modal dialog overlays
    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            daySelected = currentDaySelected,
            onDismiss = { showAddWorkoutDialog = false },
            onAdd = { workout ->
                viewModel.addOrUpdateWorkout(workout.copy(traineeId = trainee.id))
                showAddWorkoutDialog = false
            }
        )
    }

    if (showAddMealDialog) {
        AddMealDialog(
            onDismiss = { showAddMealDialog = false },
            onAdd = { meal ->
                viewModel.addOrUpdateMeal(meal.copy(traineeId = trainee.id))
                showAddMealDialog = false
            }
        )
    }

    if (showMetricsDialog) {
        RecordMetricsDialog(
            onDismiss = { showMetricsDialog = false },
            onRecord = { weight, chest, arms, waist, image ->
                viewModel.addProgressMetric(weight, chest, arms, waist, image)
                showMetricsDialog = false
            }
        )
    }

    if (showAddSupplementDialog) {
        AddSupplementDialog(
            onDismiss = { showAddSupplementDialog = false },
            onAdd = { supplement ->
                viewModel.addOrUpdateSupplement(supplement.copy(traineeId = trainee.id))
                showAddSupplementDialog = false
            }
        )
    }

    if (showAddVitaminDialog) {
        AddVitaminDialog(
            onDismiss = { showAddVitaminDialog = false },
            onAdd = { vitamin ->
                viewModel.addOrUpdateVitamin(vitamin.copy(traineeId = trainee.id))
                showAddVitaminDialog = false
            }
        )
    }
}

// ----------------------------------------------------------------------------------
// 4. PLAYER DASHBOARD SCREEN (PLAYER VIEW)
// ----------------------------------------------------------------------------------
@Composable
fun PlayerDashboardScreen(
    viewModel: MainViewModel,
    trainee: User?,
    workouts: List<WorkoutPlan>,
    meals: List<NutritionPlan>,
    messages: List<ChatMessage>,
    metrics: List<ProgressMetric>,
    supplements: List<SupplementPlan>,
    vitamins: List<VitaminPlan>,
    onLogout: () -> Unit
) {
    if (trainee == null) return

    val daysList = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
    var selectedTab by remember { mutableStateOf(0) }
    var currentDaySelected by remember { mutableStateOf("السبت") }
    var showMetricsDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    val timeLeftMs = trainee.subscriptionEnd - System.currentTimeMillis()
    val daysLeft = (timeLeftMs / (24 * 60 * 60 * 1000)).toInt()
    val isAlmostExpired = daysLeft <= 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Trainee header greetings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "تسجيل خروج",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "مرحباً يا بطل 🔥",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = trainee.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trainee.fullName.firstOrNull()?.toString() ?: "",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subscription Alerts and Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1.3f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                    Text("مدة اشتراكك الحالية", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (daysLeft < 0) "منتهي تماماً!" else "متبقي $daysLeft يوم",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isAlmostExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { showPaymentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SportAccentCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "تجديد بالبطاقة 💳",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isAlmostExpired) {
                        Text(
                            text = "تنبيه: اقترب اشتراكك على الانتهاء، تواصل للحصول على التجديد!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.weight(0.9f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("وزنك الحالي", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${trainee.weight} كجم",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trainee Tab Bars: 0 -> Exercises, 1 -> Supplements & Vitamins, 2 -> Meals Nutrition, 3 -> weight history, 4 -> coach chat support
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("تمريني", fontSize = 12.sp) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("المكملات والفيتامينات", fontSize = 11.sp) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("غذائي اليومي", fontSize = 12.sp) })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("نتائجي", fontSize = 12.sp) })
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("الدردشة الدعم", fontSize = 12.sp) })
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp)
        ) {
            when (selectedTab) {
                0 -> { // Weekly Training View
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            daysList.forEach { d ->
                                FilterChip(
                                    selected = currentDaySelected == d,
                                    onClick = { currentDaySelected = d },
                                    label = { Text(d) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val dayWorkouts = workouts.filter { it.dayOfWeek == currentDaySelected }
                        val todayMuscleSegment = dayWorkouts.firstOrNull()?.muscleGroup ?: "راحة وعضلات بطن/كارديو"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        ) {
                            Text(
                                text = "تدريب اليوم: $todayMuscleSegment",
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(14.dp).align(Alignment.End),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (dayWorkouts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Weekend, "Rest", modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("يوم استشفاء وراحة لترميم الألياف العضلية يا بطل 💤")
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(dayWorkouts) { work ->
                                    TraineeWorkoutCard(workout = work, onToggleDone = { viewModel.toggleWorkoutDone(work) })
                                }
                            }
                        }
                    }
                }
                1 -> { // Supplements & Vitamins Checklist
                    Column(modifier = Modifier.fillMaxSize()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "مكملات وفيتامينات اليوم 💊✨",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(14.dp).align(Alignment.End),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Supplements Section
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "المكملات الغذائية",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SportAccentLime
                                )

                                if (supplements.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("لا يوجد مكملات حالياً", fontSize = 11.sp, color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(supplements) { item ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleSupplementDone(item) },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (item.isDone) Color(0xFF142512) else Color(0xFF1E1E1E)
                                                ),
                                                border = BorderStroke(1.dp, if (item.isDone) Color(0xFFCCFF00) else Color.White.copy(alpha = 0.05f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (item.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                        contentDescription = "تم",
                                                        tint = if (item.isDone) Color(0xFFCCFF00) else Color.Gray,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            item.itemName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = if (item.isDone) Color.Gray else Color.White,
                                                            textDecoration = if (item.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                                                        )
                                                        Text("الجرعة: ${item.dosage}", fontSize = 11.sp, color = if (item.isDone) Color.Gray else Color.White)
                                                        Text("التوقيت: ${item.timing}", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Vitamins Section
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الفيتامينات والمعادن",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SportAccentCyan
                                )

                                if (vitamins.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("لا يوجد فيتامينات حالياً", fontSize = 11.sp, color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(vitamins) { item ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleVitaminDone(item) },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (item.isDone) Color(0xFF112229) else Color(0xFF1E1E1E)
                                                ),
                                                border = BorderStroke(1.dp, if (item.isDone) SportAccentCyan else Color.White.copy(alpha = 0.05f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (item.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                        contentDescription = "تم",
                                                        tint = if (item.isDone) SportAccentCyan else Color.Gray,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            item.itemName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = if (item.isDone) Color.Gray else Color.White,
                                                            textDecoration = if (item.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                                                        )
                                                        Text("الجرعة: ${item.dosage}", fontSize = 11.sp, color = if (item.isDone) Color.Gray else Color.White)
                                                        Text("التوقيت: ${item.timing}", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Trainee Meal list
                    Column(modifier = Modifier.fillMaxSize()) {
                        val totalCalories = meals.sumOf { it.calories }
                        val totalProtein = meals.sumOf { it.protein }
                        val totalCarbs = meals.sumOf { it.carbs }
                        val totalFats = meals.sumOf { it.fats }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("سعرات مستهدفة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$totalCalories kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("بروتين", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${totalProtein}g", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("كربوهيدرات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${totalCarbs}g", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (meals.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("لم يكتب الكوتش أي برنامج تغذية لك حالياً.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(meals) { meal ->
                                    TraineeMealItem(meal = meal)
                                }
                            }
                        }
                    }
                }
                3 -> { // Progress statistics weight & body progress lines
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showMetricsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("قياس جديد اليوم", fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "معدل نموك وتطور القياسات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (metrics.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                Text("توجة لتسجيل قياساتك الأولى اليوم لمتابعة التقدم!")
                            }
                        } else {
                            ProgressLineChart(metrics = metrics)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("التسجيلات السابقة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                        metrics.reversed().forEach { met ->
                            ProgressMetricTraineeRow(metric = met)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                4 -> { // Chat portal
                    ChatPortal(
                        messages = messages,
                        onSend = { text, type, url ->
                            viewModel.sendChatMessage(text, type, url)
                        }
                    )
                }
            }
        }
    }

    if (showMetricsDialog) {
        RecordMetricsDialog(
            onDismiss = { showMetricsDialog = false },
            onRecord = { weight, chest, arms, waist, image ->
                viewModel.addProgressMetric(weight, chest, arms, waist, image)
                showMetricsDialog = false
            }
        )
    }

    if (showPaymentDialog) {
        SubscriptionPaymentDialog(
            viewModel = viewModel,
            trainee = trainee,
            onDismiss = { showPaymentDialog = false },
            onPaymentSuccess = { monthsToAdd, nameOfPackage ->
                val currentEnd = if (trainee.subscriptionEnd > System.currentTimeMillis()) {
                    trainee.subscriptionEnd
                } else {
                    System.currentTimeMillis()
                }
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = currentEnd
                calendar.add(java.util.Calendar.MONTH, monthsToAdd)
                
                val updatedTrainee = trainee.copy(subscriptionEnd = calendar.timeInMillis)
                viewModel.updateTrainee(updatedTrainee)
                viewModel.sendChatMessage(
                    text = "لقد قمت بتجديد اشتراكي ودفع الرسوم بالبطاقة المصرفية بنجاح لقيمة الاشتراك مسبق الدفع ($nameOfPackage)! 💳🎉",
                    attachType = "TEXT"
                )
                showPaymentDialog = false
            }
        )
    }
}

// ----------------------------------------------------------------------------------
// 5. CHAT SYSTEM PORTAL (REUSABLE PORTABLE MODULE)
// ----------------------------------------------------------------------------------
@Composable
fun ChatPortal(
    messages: List<ChatMessage>,
    onSend: (text: String, attachType: String, url: String?) -> Unit
) {
    var typedMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Conversation lists
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isMe = msg.senderName.contains("كابتن") || msg.senderName.contains("أحمد علي") // simple role check
                // Let's refine check so Trainee sees their send is "Me"
                // Actually we can check who sent based on context, but let's draw it beautifully
                MessageBubble(message = msg)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input layouts containing text and quick simulated multi attachment triggers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Simulated photo trigger
                IconButton(onClick = {
                    onSend("أرسلت صورة للوجبات اليوم للتثبيت كابتن 📸", "IMAGE", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
                }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Attach photos",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Quick Simulated audio recording trigger
                IconButton(onClick = {
                    onSend("ملاحظة صوتية: تمرنت اليوم وحالي في السكوات ممتاز لكن ألم في الرقبة خفيف 🎙️", "AUDIO", "audio_note_file.temp")
                }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Send sound note",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                // Main TextField
                OutlinedTextField(
                    value = typedMessage,
                    onValueChange = { typedMessage = it },
                    placeholder = { Text("اكتب رسالتك للكوتش...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (typedMessage.isNotBlank()) {
                            onSend(typedMessage, "TEXT", null)
                            typedMessage = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    // Determine align relative to sender (Coach is always a specific visual, trainee the other)
    val isCoach = message.senderName.contains("كابتن") || message.senderName.contains("أحمد علي")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCoach) Alignment.Start else Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (isCoach) Arrangement.Start else Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isCoach) 0.dp else 16.dp,
                            bottomEnd = if (isCoach) 16.dp else 0.dp
                        )
                    )
                    .background(
                        if (isCoach) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.senderName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCoach) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                if (message.attachmentType == "IMAGE") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray)
                    ) {
                        Text("صورة التمرين/الوجبة الكبسولة 📸", modifier = Modifier.align(Alignment.Center), color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (message.attachmentType == "AUDIO") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, "Play note", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مقطع صوتي مسجل (0:14) 🎙️", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Text(
                    text = message.messageText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End
                )

                val formattedTime = DateFormat.format("hh:mm a", message.timestamp)
                Text(
                    text = formattedTime.toString(),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// ROW AND OTHER HELPERS COMPOSABLES
// ----------------------------------------------------------------------------------

// Workouts row inside Coach detailed management view
@Composable
fun WorkoutRowItem(workout: WorkoutPlan, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }

                Text(
                    text = workout.exerciseName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "التكرارات: ${workout.reps}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "المجموعات: ${workout.sets} جولات",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (workout.notes.isNotEmpty()) {
                Text(
                    text = "ملاحظات الكوتش: ${workout.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (workout.videoRef.isNotEmpty()) {
                val uriHandler = LocalUriHandler.current
                var showVideoPlayer by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clickable {
                            val isExternalWeb = workout.videoRef.startsWith("http") && 
                                    (workout.videoRef.contains("youtube") || 
                                     workout.videoRef.contains("youtu.be") || 
                                     workout.videoRef.contains("instagram") || 
                                     workout.videoRef.contains("tiktok"))
                            if (isExternalWeb) {
                                try {
                                    uriHandler.openUri(workout.videoRef)
                                } catch (e: Exception) {
                                    // Ignore or handle invalid uri
                                }
                            } else {
                                showVideoPlayer = true
                            }
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .background(
                            color = SportAccentCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "مشاهدة الفيديو المرفق 🎥",
                        color = SportAccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = SportAccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (showVideoPlayer) {
                    VideoPlayerDialog(
                        videoPathOrUrl = workout.videoRef,
                        onDismiss = { showVideoPlayer = false }
                    )
                }
            }
        }
    }
}

// Meal card inside Trainer nutrition list
@Composable
fun MealCardItem(meal: NutritionPlan, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }

                Text(meal.mealName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            }

            Text(meal.contents, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${meal.calories} كـالوري", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Text("ب: ${meal.protein}g", fontSize = 11.sp)
                Text("ك: ${meal.carbs}g", fontSize = 11.sp)
                Text("د: ${meal.fats}g", fontSize = 11.sp)
            }

            if (meal.alternatives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("البديل الآمن: " + meal.alternatives, fontSize = 11.sp, color = SportAccentCyan)
            }
        }
    }
}

// Customized workout item inside Trainee UI viewport
@Composable
fun TraineeWorkoutCard(workout: WorkoutPlan, onToggleDone: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    var showVideoPlayer by remember { mutableStateOf(false) }

    Card(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (workout.isDone) Color(0xFF142512) else if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (workout.isDone) Color(0xFFCCFF00) else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onToggleDone() }) {
                        Icon(
                            imageVector = if (workout.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "تم الإنجاز",
                            tint = if (workout.isDone) Color(0xFFCCFF00) else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand info"
                    )
                }

                Text(
                    text = workout.exerciseName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (workout.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                    ),
                    fontWeight = FontWeight.Bold,
                    color = if (workout.isDone) Color.Gray else Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "التكرار: ${workout.reps}",
                    fontSize = 12.sp,
                    color = if (workout.isDone) Color.Gray else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "الجولات: ${workout.sets} مجاميع",
                    fontSize = 12.sp,
                    color = if (workout.isDone) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ملاحظات وتوجيهات الكوتش أحمد المباشرة: \n" + workout.notes.ifEmpty { "العب بوزن مناسب ومحكم لك." },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                // Real / Simulated Exercise Video / Photo Reference Box
                Spacer(modifier = Modifier.height(10.dp))
                if (workout.videoRef.isNotEmpty()) {
                    val uriHandler = LocalUriHandler.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SportAccentLime.copy(alpha = 0.15f))
                            .border(1.dp, SportAccentLime, RoundedCornerShape(12.dp))
                            .clickable {
                                val isExternalWeb = workout.videoRef.startsWith("http") && 
                                        (workout.videoRef.contains("youtube") || 
                                         workout.videoRef.contains("youtu.be") || 
                                         workout.videoRef.contains("instagram") || 
                                         workout.videoRef.contains("tiktok"))
                                if (isExternalWeb) {
                                    try {
                                        uriHandler.openUri(workout.videoRef)
                                    } catch (e: Exception) {
                                        // ignore invalid links
                                    }
                                } else {
                                    showVideoPlayer = true
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = SportAccentLime,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "اضغط لتشغيل فيديو التمرين 🏋️‍♂️🎥",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "لا يوجد فيديو مرفق للتمرين حالياً 🏋️‍♂️🎥",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                if (showVideoPlayer) {
                    VideoPlayerDialog(
                        videoPathOrUrl = workout.videoRef,
                        onDismiss = { showVideoPlayer = false }
                    )
                }
            }
        }
    }
}

// Customized meals display inside Trainee nutrition tab
@Composable
fun TraineeMealItem(meal: NutritionPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${meal.calories} Sالوري",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = meal.mealName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = meal.contents,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("دهون: ${meal.fats}g", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("كرب: ${meal.carbs}g", fontSize = 11.sp, color = SportAccentOrange)
                Text("بروتين: ${meal.protein}g", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }

            if (meal.alternatives.isNotEmpty() || meal.supplements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(6.dp))

                if (meal.alternatives.isNotEmpty()) {
                    Text(
                        text = "البدلاء: " + meal.alternatives,
                        style = MaterialTheme.typography.bodySmall,
                        color = SportAccentCyan,
                        textAlign = TextAlign.End
                    )
                }

                if (meal.supplements.isNotEmpty()) {
                    Text(
                        text = "المكملات المصاحبة للوجبة: " + meal.supplements,
                        style = MaterialTheme.typography.bodySmall,
                        color = SportAccentLime,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// Progress Metrics item row listing in coach detail
@Composable
fun ProgressMetricRowItem(metric: ProgressMetric, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }

            val dateStr = DateFormat.format("dd MMMM yyyy", metric.date)
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "رصد بتاريخ: $dateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("الوزن: ${metric.weight} كجم", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    if (metric.chestCm > 0) Text("صدر: ${metric.chestCm} سم", fontSize = 11.sp)
                    if (metric.armsCm > 0) Text("باي: ${metric.armsCm} سم", fontSize = 11.sp)
                    if (metric.waistCm > 0) Text("خصر: ${metric.waistCm} سم", fontSize = 11.sp)
                }
            }
        }
    }
}

// Progress metrics row for Player dashboard
@Composable
fun ProgressMetricTraineeRow(metric: ProgressMetric) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val dateStr = DateFormat.format("dd MMMM yyyy", metric.date)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(text = dateStr.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("المحيط والذراع: ${metric.armsCm} سم", fontSize = 12.sp)
                Text("محيط الصدر: ${metric.chestCm} سم", fontSize = 12.sp)
                Text("محيط الخصر: ${metric.waistCm} سم", fontSize = 12.sp)
                Text("الوزن المقاس: ${metric.weight} كجم", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// 6. CUSTOM DRAWING PROGRESS LINE CHART WITH HOVER & SHADING GRADIENTS
// ----------------------------------------------------------------------------------
@Composable
fun ProgressLineChart(metrics: List<ProgressMetric>) {
    if (metrics.isEmpty()) return

    // Collect weight history lists
    val weights = metrics.map { it.weight.toFloat() }
    val maxWeight = (weights.maxOrNull() ?: 100f) + 3f
    val minWeight = (weights.minOrNull() ?: 0f) - 3f
    val weightRange = if (maxWeight - minWeight == 0f) 10f else maxWeight - minWeight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkOnyxSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("مخطط بياني للاوزان والتغييرات الايجابية", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 10.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw simple horizontal visual reference grid lines
                val gridLinesCount = 3
                for (i in 0..gridLinesCount) {
                    val y = canvasHeight * (i.toFloat() / gridLinesCount)
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (weights.size < 2) {
                    // Only one point drawn in center
                    val cx = canvasWidth / 2
                    val cy = canvasHeight / 2
                    drawCircle(color = SportAccentLime, radius = 6.dp.toPx(), center = Offset(cx, cy))
                    return@Canvas
                }

                // Plot metrics coordinates
                val points = mutableListOf<Offset>()
                val stepX = canvasWidth / (weights.size - 1)

                weights.forEachIndexed { index, wt ->
                    val x = index * stepX
                    // Normalize weight on the relative chart scale
                    val ratio = (wt - minWeight) / weightRange
                    val y = canvasHeight - (ratio * canvasHeight)
                    points.add(Offset(x, y))
                }

                // Draw gradient fill below graph line
                val gradientPath = Path().apply {
                    moveTo(0f, canvasHeight)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(canvasWidth, canvasHeight)
                    close()
                }

                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(SportAccentLime.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = canvasHeight
                    )
                )

                // Draw line charts
                val linePath = Path().apply {
                    points.forEachIndexed { idx, pt ->
                        if (idx == 0) moveTo(pt.x, pt.y)
                        else lineTo(pt.x, pt.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = SportAccentLime,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw points and values
                points.forEachIndexed { idx, pt ->
                    drawCircle(
                        color = Color.Black,
                        radius = 6.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = SportAccentLime,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                }
            }

            // Days/Weight labels beneath
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${weights.firstOrNull() ?: 0f} كجم", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("المعدل التقريبي للتقدم", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${weights.lastOrNull() ?: 0f} كجم", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Dialog to add supplement item
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupplementDialog(
    onDismiss: () -> Unit,
    onAdd: (SupplementPlan) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var timing by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "إضافة مكمل غذائي جديد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SportAccentLime
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المكمل (مثال: كرياتين مونوهيدرات)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentLime),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("الجرعة (مثال: 5 جرام / 1 سكوب)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentLime),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = timing,
                    onValueChange = { timing = it },
                    label = { Text("وقت التناول (مثال: بعد التمرين مع الماء)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentLime),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (name.isNotEmpty() && dosage.isNotEmpty() && timing.isNotEmpty()) {
                                onAdd(
                                    SupplementPlan(
                                        traineeId = 0,
                                        itemName = name,
                                        dosage = dosage,
                                        timing = timing,
                                        isDone = false
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SportAccentLime, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إضافة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Dialog to add vitamin item
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVitaminDialog(
    onDismiss: () -> Unit,
    onAdd: (VitaminPlan) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var timing by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "إضافة فيتامين أو معدن جديد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SportAccentCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الفيتامين (مثال: أوميغا 3 / فيتامين د3)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentCyan),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("الجرعة (مثال: 1 كبسولة / 5000 وحدة)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentCyan),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = timing,
                    onValueChange = { timing = it },
                    label = { Text("وقت التناول (مثال: مع وجبة الفطور صباحاً)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SportAccentCyan),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (name.isNotEmpty() && dosage.isNotEmpty() && timing.isNotEmpty()) {
                                onAdd(
                                    VitaminPlan(
                                        traineeId = 0,
                                        itemName = name,
                                        dosage = dosage,
                                        timing = timing,
                                        isDone = false
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SportAccentCyan, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إضافة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Dialog to add training item
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutDialog(
    daySelected: String,
    onDismiss: () -> Unit,
    onAdd: (WorkoutPlan) -> Unit
) {
    var muscleGroup by remember { mutableStateOf("") }
    var exerciseName by remember { mutableStateOf("") }
    var setsStr by remember { mutableStateOf("4") }
    var repsStr by remember { mutableStateOf("12-10-8-8") }
    var videoRef by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val context = LocalContext.current
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = File(context.cacheDir, "edu_video_${System.currentTimeMillis()}.mp4")
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    videoRef = file.absolutePath
                }
            } catch (e: Exception) {
                videoRef = uri.toString()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                Text("إضافة تمرين جديد لليوم ($daySelected)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = muscleGroup,
                    onValueChange = { muscleGroup = it },
                    label = { Text("المستهدف (مثال: صدر علوي، أرجل)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("اسم التمرين بالكامل") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = repsStr,
                        onValueChange = { repsStr = it },
                        label = { Text("التكرارات") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = setsStr,
                        onValueChange = { setsStr = it },
                        label = { Text("الجولات") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = videoRef,
                    onValueChange = { videoRef = it },
                    label = { Text("رابط فيديو توضيحي (YouTube / MP4)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { videoPickerLauncher.launch("video/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = SportAccentCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Upload Video",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (videoRef.isNotEmpty() && !videoRef.startsWith("http")) 
                            "تم تحميل فيديو من الجهاز 📂" 
                        else 
                            "اختر فيديو تعليمي من جهازك 🎞️",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (videoRef.isNotEmpty() && !videoRef.startsWith("http")) {
                    Text(
                        text = "الملف المُختار: ${videoRef.substringAfterLast("/")}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات الأداء والتناغم العصبي") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            if (exerciseName.isNotEmpty()) {
                                onAdd(
                                    WorkoutPlan(
                                        traineeId = 0,
                                        dayOfWeek = daySelected,
                                        muscleGroup = muscleGroup.ifEmpty { "عام" },
                                        exerciseName = exerciseName,
                                        sets = setsStr.toIntOrNull() ?: 4,
                                        reps = repsStr,
                                        videoRef = videoRef,
                                        notes = notes
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

// Dialog to add Meal Item
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onAdd: (NutritionPlan) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var contents by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatsStr by remember { mutableStateOf("") }
    var alternatives by remember { mutableStateOf("") }
    var supplements by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                Text("إضافة وجبة غذائية مخصصة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("عنوان الوجبة (مثال: وجبة 1 - إفطار)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contents,
                    onValueChange = { contents = it },
                    label = { Text("مكونات ومحتويات الوجبة بالتفصيل") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = caloriesStr,
                        onValueChange = { caloriesStr = it },
                        label = { Text("سعرات") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("بروتين") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it },
                        label = { Text("كرب") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = fatsStr,
                        onValueChange = { fatsStr = it },
                        label = { Text("دهون") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = alternatives,
                    onValueChange = { alternatives = it },
                    label = { Text("البدائل المتاحة للوجبة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = supplements,
                    onValueChange = { supplements = it },
                    label = { Text("المكملات المصاحبة للوجبة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            if (mealName.isNotEmpty() && contents.isNotEmpty()) {
                                onAdd(
                                    NutritionPlan(
                                        traineeId = 0,
                                        mealName = mealName,
                                        contents = contents,
                                        calories = caloriesStr.toIntOrNull() ?: 0,
                                        protein = proteinStr.toDoubleOrNull() ?: 0.0,
                                        carbs = carbsStr.toDoubleOrNull() ?: 0.0,
                                        fats = fatsStr.toDoubleOrNull() ?: 0.0,
                                        alternatives = alternatives,
                                        supplements = supplements
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

// Dialog to log fresh physical data weight etc
@Composable
fun RecordMetricsDialog(
    onDismiss: () -> Unit,
    onRecord: (weight: Double, chest: Double, arms: Double, waist: Double, image: String?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var arms by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                Text("تسجيل قياسات التقدم البطلة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("الوزن الحالي (بالكيلو)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = chest,
                    onValueChange = { chest = it },
                    label = { Text("قياس محيط الصدر (سم) اختياري") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = arms,
                    onValueChange = { arms = it },
                    label = { Text("مقاس البايسبس/الذراع (سم) اختياري") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it },
                    label = { Text("مقاس الخصر والوسط (سم) اختياري") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated progress photo selection
                var isPhotoMocked by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { isPhotoMocked = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isPhotoMocked) Icons.Default.Check else Icons.Default.CameraAlt,
                        contentDescription = "Upload progress",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPhotoMocked) "تم محاكاة التقاط صورة التقدم بنجاح 📸" else "التقاط/إرفاق صورة تقدم دورية (مستحسن)")
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            val wtVal = weight.toDoubleOrNull() ?: 0.0
                            if (wtVal > 0) {
                                onRecord(
                                    wtVal,
                                    chest.toDoubleOrNull() ?: 0.0,
                                    arms.toDoubleOrNull() ?: 0.0,
                                    waist.toDoubleOrNull() ?: 0.0,
                                    if (isPhotoMocked) "https://images.unsplash.com/photo-1517838277536-f5f99be501cd" else null
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تسجيل وحفظ")
                    }
                }
            }
        }
    }
}

// Inline native video player dialog
@Composable
fun VideoPlayerDialog(videoPathOrUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Text(
                        text = "الفيديو التعليمي بالتمرين 🎥",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mediaController = android.widget.MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                setVideoPath(videoPathOrUrl)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                                setOnErrorListener { _, _, _ ->
                                    true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "سيتم تكرار الفيديو تلقائياً لملاحظة الأداء والحركة الصحّية 🏋️",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Subscription Payment Dialog with high fidelity card layout and interactive simulated payment processing
@Composable
fun SubscriptionPaymentDialog(
    viewModel: MainViewModel,
    trainee: User,
    onDismiss: () -> Unit,
    onPaymentSuccess: (addedMonths: Int, packageName: String) -> Unit
) {
    var selectedCardPackage by remember { mutableStateOf(2) } // default is 3 months
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    
    var paymentStatus by remember { mutableStateOf("IDLE") } // "IDLE", "PROCESSING", "SUCCESS"
    var processingMessage by remember { mutableStateOf("") }

    val packageLabel = when (selectedCardPackage) {
        1 -> "الباقة الشهرية (15,000 د.ع)"
        2 -> "باقة 3 شهور (40,000 د.ع)"
        else -> "باقة 6 شهور (75,000 د.ع)"
    }
    val addedMonths = when (selectedCardPackage) {
        1 -> 1
        2 -> 3
        else -> 6
    }

    if (paymentStatus == "PROCESSING") {
        androidx.compose.runtime.LaunchedEffect(key1 = true) {
            processingMessage = "جاري الاتصال الآمن بالبوابة المصرفية للبطل... 🔒"
            kotlinx.coroutines.delay(1200)
            processingMessage = "التحقق من صحة البطاقة الائتمانية ورصيد المحفظة... ⚙️"
            kotlinx.coroutines.delay(1200)
            processingMessage = "جاري تحويل مبلغ الاشتراك للكابتن EM وتحديث السيرفر... 💸"
            kotlinx.coroutines.delay(1200)
            paymentStatus = "SUCCESS"
        }
    }

    Dialog(onDismissRequest = { if (paymentStatus != "PROCESSING") onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (paymentStatus != "PROCESSING") {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.LightGray
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                    Text(
                        text = "بوابة الدفع وتجديد الاشتراك 💳",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (paymentStatus == "IDLE") {
                    // Step 1: Packages Selection
                    Text(
                        text = "اختر باقة تمديد الاشتراك المتاحة للخصم:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(1, "شهري", "15k د.ع"),
                            Triple(2, "3 شهور", "40k د.ع"),
                            Triple(3, "6 شهور", "75k د.ع")
                        ).forEach { (id, label, price) ->
                            val isSelected = selectedCardPackage == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SportAccentCyan.copy(alpha = 0.2f) else Color(0xFF2C2C2C))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) SportAccentCyan else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                      )
                                    .clickable { selectedCardPackage = id }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = label, color = if (isSelected) SportAccentCyan else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = price, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Coach Target Bank Transfer Information Panel
                    val coach = viewModel.coachInfo.collectAsStateWithLifecycle().value
                    val bName = if (coach?.bankName.isNullOrEmpty()) "مصرف الرافدين - بغداد" else coach!!.bankName
                    val bCard = if (coach?.bankCardNumber.isNullOrEmpty()) "4321 6543 0987 1122" else coach!!.bankCardNumber
                    val bHolder = if (coach?.bankCardHolder.isNullOrEmpty()) "احمد علي حمزة" else coach!!.bankCardHolder

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SportAccentCyan.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "🏦 الحساب المصرفي للكابتن (التحويل المباشر):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SportAccentCyan,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = bName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "اسم المصرف:", color = Color.Gray, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = bCard, color = SportAccentLime, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                Text(text = "رقم البطاقة/الحساب:", color = Color.Gray, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = bHolder, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "اسم صاحب الحساب:", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 2: High fidelity Card mockup with dynamic card networks
                    val cardNetwork = when {
                        cardNumber.startsWith("4") -> "VISA 💳"
                        cardNumber.startsWith("5") -> "MASTERCARD 💳"
                        cardNumber.isNotEmpty() -> "Qi Card 💳"
                        else -> "CARD 💳"
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "بوابة الدفع الآمنة 🔒",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = cardNetwork,
                                    color = SportAccentCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Raw Card Number Display
                            val formattedNum = cardNumber.padEnd(16, '•').chunked(4).joinToString("   ")
                            Text(
                                text = formattedNum,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(text = "اسم حامل البطاقة", color = Color.Gray, fontSize = 8.sp)
                                    Text(
                                        text = cardName.ifEmpty { "EMAD ALI" }.uppercase(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "تاريخ الانتهاء", color = Color.Gray, fontSize = 8.sp)
                                    Text(
                                        text = expiryDate.ifEmpty { "MM/YY" },
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 3: Inputs Form
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("اسم صاحب البطاقة (بالحروف الإنجليزية)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SportAccentCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = SportAccentCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { input -> 
                            if (input.all { it.isDigit() } && input.length <= 16) {
                                cardNumber = input
                            }
                        },
                        label = { Text("رقم البطاقة (16 رقم)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SportAccentCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = SportAccentCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 3) {
                                    cvv = input
                                }
                            },
                            label = { Text("رمز الأمان CVV") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportAccentCyan,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = SportAccentCyan
                            )
                        )

                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { input ->
                                // handle auto insertion of MM/YY
                                var clean = input.filter { it.isDigit() || it == '/' }
                                if (clean.length == 2 && !clean.contains("/") && expiryDate.length < clean.length) {
                                    clean += "/"
                                }
                                if (clean.length <= 5) {
                                    expiryDate = clean
                                }
                            },
                            label = { Text("تاريخ الانتهاء MM/YY") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            placeholder = { Text("08/28") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportAccentCyan,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = SportAccentCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val isFormValid = cardName.length >= 3 && cardNumber.length == 16 && expiryDate.length == 5 && cvv.length == 3
                    Button(
                        onClick = { if (isFormValid) paymentStatus = "PROCESSING" },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportAccentCyan,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "تأكيد الدفع وتحويل $packageLabel 🔒",
                            color = if (isFormValid) Color.Black else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                } else if (paymentStatus == "PROCESSING") {
                    // Step 4: Loading Processing state
                    Spacer(modifier = Modifier.height(30.dp))
                    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SportAccentCyan)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "جاري تفعيل الاشتراك السحابي للأموال...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = processingMessage,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                } else {
                    // Step 5: Successful validation feedback
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(SportAccentLime.copy(alpha = 0.2f))
                            .border(2.dp, SportAccentLime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✔️", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "تم تجديد الاشتراك بنجاح! 🎉",
                        color = SportAccentLime,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تم خصم التكلفة بنجاح وتمديد حساب العضوية مع المدرب بنجاح لمدة $addedMonths شهور كاملة. تم إرسال إشعار للدعم الفني.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onPaymentSuccess(addedMonths, packageLabel) },
                        colors = ButtonDefaults.buttonColors(containerColor = SportAccentLime),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("موافق والعودة للتدريب 🏆", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// Dialog allowing the Coach to configure his target bank details shown to trainees 
@Composable
fun CoachBankSettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val coach = viewModel.coachInfo.collectAsStateWithLifecycle().value ?: viewModel.currentUser.value
    if (coach == null) {
        onDismiss()
        return
    }

    var bankName by remember { mutableStateOf(coach.bankName) }
    var cardNumber by remember { mutableStateOf(coach.bankCardNumber) }
    var cardHolder by remember { mutableStateOf(coach.bankCardHolder) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                    Text(
                        text = "إعدادات الحساب المصرفي للكابتن 💳",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "أدخل بياناتك المصرفية هنا لتظهر للمشتركين واللاعبين مباشرة عند تمديد أو تجديد اشتراكهم لتسهيل التحويل المالي المباشر لك لحسابك العراقي الخاص 🇮🇶:",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("اسم المصرف (مثال: مصرف الرافدين، TBI، طيف)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportAccentCyan,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = SportAccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("رقم البطاقة المصرفية أو رقم الحساب للتحويل") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportAccentCyan,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = SportAccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cardHolder,
                    onValueChange = { cardHolder = it },
                    label = { Text("اسم صاحب الحساب / حامل البطاقة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportAccentCyan,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = SportAccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val updated = coach.copy(
                            bankName = bankName,
                            bankCardNumber = cardNumber,
                            bankCardHolder = cardHolder
                        )
                        viewModel.updateCoach(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportAccentLime),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "حفظ البيانات وتفعيلها بنجاح 💾",
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}


