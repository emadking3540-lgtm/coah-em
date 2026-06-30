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

sealed class Screen {
    object Login : Screen()
    object CoachDashboard : Screen()
    data class PlayerDetail(val traineeId: Int) : Screen()
    object PlayerDashboard : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymZoneApp(viewModel: MainViewModel) {
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
                AccountRowItem(
                    email = "emadking3540@gmail.com",
                    name = "الكابتن عماد العبيدي (المدرب الرئيسي)",
                    isCoach = true,
                    onClick = { onAccountSelected("emadking3540@gmail.com") }
                )
                AccountRowItem(
                    email = "ahmed@gymzone.com",
                    name = "أحمد حسن (لاعب - تضخيم)",
                    isCoach = false,
                    onClick = { onAccountSelected("ahmed@gymzone.com") }
                )
                AccountRowItem(
                    email = "mohamed@gymzone.com",
                    name = "محمد سعيد (لاعب - تنشيف)",
                    isCoach = false,
                    onClick = { onAccountSelected("mohamed@gymzone.com") }
                )
                
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                
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
