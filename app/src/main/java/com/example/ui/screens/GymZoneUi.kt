QuickEdit Text Editor:package com.example.ui.screens

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
                    email = "emadking3540@gmail.com",
                    name = "الكابتن عماد العبيدي (المدرب الرئيسي)",
                    isCoach = true,
                    onClick = { onAccountSelected("emadking3540@gmail.com") }
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
        // Premium motivational backgr
