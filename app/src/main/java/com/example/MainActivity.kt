package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.db.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuditRepository
import com.example.ui.screens.AjustesScreen
import com.example.ui.screens.BitacoraScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistorialScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.AmberNightFilter
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.ClinicalNavy
import com.example.ui.theme.ClinicalNavyCard
import com.example.ui.theme.ClinicalNavyDark
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.HGVTheme
import com.example.ui.viewmodel.AuditViewModel
import com.example.util.NotificationHelper
import com.example.util.TimeHelper
import kotlinx.coroutines.flow.collectLatest

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val repository = AuditRepository(db.auditDao(), db.personalSaludDao())
        val prefRepo = UserPreferencesRepository(applicationContext)

        NotificationHelper.createNotificationChannels(applicationContext)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuditViewModel(repository, prefRepo, applicationContext) as T
            }
        }
        val viewModel = ViewModelProvider(this, viewModelFactory)[AuditViewModel::class.java]

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val isNightLight by viewModel.isNightLight.collectAsState()
            val isUnlocked by viewModel.isUnlocked.collectAsState()
            val context = LocalContext.current

            // Notification permission request for Android 13+ (API 33+)
            val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    viewModel.showToast("Para recibir alertas de tiempo de cirugía en segundo plano, active las notificaciones.")
                }
            }

            LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (!NotificationHelper.hasNotificationPermission(context)) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Toast listener
            LaunchedEffect(Unit) {
                viewModel.toastEvent.collectLatest { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }

            // Inactivity / Lifecycle lock on background
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        viewModel.lockApp()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            HGVTheme(darkTheme = isDarkMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!isUnlocked) {
                        LoginScreen(viewModel = viewModel)
                    } else {
                        MainAppContent(viewModel = viewModel)
                    }

                    // Night light amber filter overlay (for darkened laparoscopic operating theaters)
                    if (isNightLight) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AmberNightFilter)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: AuditViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val formState by viewModel.formState.collectAsState()

    // Pulse animation for active chronometer in HUD
    val infiniteTransition = rememberInfiniteTransition(label = "HudPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    // Intercept back button to return to Dashboard tab if currently in another tab
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        topBar = {
            Surface(
                color = ClinicalNavy,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hospital Identity Brand
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedTab = 0 }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(BlueAccent, CyanHUD)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "HGV Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "HGV",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BlueAccent.copy(alpha = 0.25f))
                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = "AUDITORÍA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyanHUD
                                    )
                                }
                            }
                            Text(
                                text = "Laparoscopia Quirúrgica",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Live Surgical Status Capsule (Interactive HUD)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Chronometer Capsule
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (formState.isTimerRunning) {
                                        CrimsonDanger.copy(alpha = 0.18f)
                                    } else if (formState.duracionSegundos > 0) {
                                        EmeraldGreen.copy(alpha = 0.18f)
                                    } else {
                                        Color.White.copy(alpha = 0.08f)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (formState.isTimerRunning) {
                                        CrimsonDanger.copy(alpha = pulseAlpha)
                                    } else if (formState.duracionSegundos > 0) {
                                        EmeraldGreen
                                    } else {
                                        Color.White.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTab = 1 }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (formState.isTimerRunning) Icons.Default.FiberManualRecord else Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (formState.isTimerRunning) CrimsonDanger else if (formState.duracionSegundos > 0) EmeraldGreen else Color(0xFF94A3B8),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .then(if (formState.isTimerRunning) Modifier.alpha(pulseAlpha) else Modifier)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = if (formState.isTimerRunning) {
                                        TimeHelper.formatDurationHms(formState.liveElapsedSeconds)
                                    } else if (formState.duracionSegundos > 0) {
                                        TimeHelper.formatDurationHuman(formState.duracionSegundos)
                                    } else {
                                        "SALA DISP."
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (formState.isTimerRunning) Color(0xFFFF8B8B) else if (formState.duracionSegundos > 0) EmeraldGreen else Color(0xFFCBD5E1)
                                )
                            }
                        }

                        // Instant Lock Action
                        IconButton(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Bloquear App",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = ClinicalNavy,
                contentColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanHUD,
                        indicatorColor = BlueAccent,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("tab_dashboard")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Box {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Bitácora")
                            if (formState.isTimerRunning) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonDanger)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    },
                    label = { Text("Bitácora", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanHUD,
                        indicatorColor = BlueAccent,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("tab_bitacora")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanHUD,
                        indicatorColor = BlueAccent,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("tab_historial")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanHUD,
                        indicatorColor = BlueAccent,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("tab_ajustes")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToForm = { selectedTab = 1 },
                        onNavigateToHistory = { selectedTab = 2 }
                    )
                    1 -> BitacoraScreen(
                        viewModel = viewModel,
                        onNavigateToDashboard = { selectedTab = 0 }
                    )
                    2 -> HistorialScreen(
                        viewModel = viewModel,
                        onNavigateToForm = { selectedTab = 1 }
                    )
                    3 -> AjustesScreen(viewModel = viewModel)
                }
            }
        }
    }
}
