package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.AuditRecord
import com.example.ui.components.CameraXCaptureDialog
import com.example.ui.components.SignatureCanvas
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.ClinicalNavy
import com.example.ui.theme.ClinicalNavyCard
import com.example.ui.theme.ClinicalNavyDark
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TealClinical
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.VerifiedUser
import com.example.ui.viewmodel.AuditViewModel
import com.example.util.CurpHelper
import com.example.util.TimeHelper
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraScreen(
    viewModel: AuditViewModel,
    onNavigateToDashboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val records by viewModel.allRecords.collectAsState()
    val alertsEnabled by viewModel.alertsEnabled.collectAsState()
    val alertIntervalMin by viewModel.alertIntervalMin.collectAsState()
    val doctorDirectory by viewModel.doctorDirectory.collectAsState()
    val nurseDirectory by viewModel.nurseDirectory.collectAsState()
    val scrollState = rememberScrollState()

    var showCleanDialog by remember { mutableStateOf(false) }
    var showDigitalSignatures by remember { mutableStateOf(false) }
    var showCameraX by remember { mutableStateOf(false) }
    var customSplitNote by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            viewModel.showToast("Para recibir alertas de tiempo sin mantener la app abierta, habilite las notificaciones.")
        }
    }

    // Pulsing animation for running timer
    val infiniteTransition = rememberInfiniteTransition(label = "TimerPulse")
    val timerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showCameraX = true
        } else {
            viewModel.showToast("Se requiere permiso de cámara para capturar la hoja quirúrgica.")
        }
    }

    fun launchCameraXCapture() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            showCameraX = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Media picker for CURP / Wristband photo
    val curpPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateFotoCurpUri(uri.toString())
            viewModel.showToast("Foto de identificación adjuntada")
        }
    }

    // Media picker for Operating Room Sheet (Hoja quirúrgica con firma de instrumentista)
    val surgicalSheetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateFotoHojaQuirurgicaUri(uri.toString())
            viewModel.showToast("✔ Hoja quirúrgica de instrumentista adjuntada")
        }
    }

    val laparoscopicProcedures by viewModel.laparoscopicProcedures.collectAsState()

    val excelCatalogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importLaparoscopicCatalogFromUri(context, uri)
        }
    }

    fun openTimePicker(initialValue: String, onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        var hour = cal.get(Calendar.HOUR_OF_DAY)
        var min = cal.get(Calendar.MINUTE)

        if (initialValue.contains(":")) {
            val parts = initialValue.split(":")
            hour = parts.getOrNull(0)?.toIntOrNull() ?: hour
            min = parts.getOrNull(1)?.toIntOrNull() ?: min
        }

        TimePickerDialog(
            context,
            { _, h, m ->
                val formatted = String.format("%02d:%02d", h, m)
                onTimeSelected(formatted)
            },
            hour,
            min,
            true
        ).show()
    }

    fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Auditoría Quirúrgica HGV - ${formState.paciente}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir Bitácora PDF Oficial"))
        } catch (e: Exception) {
            viewModel.showToast("Error al abrir PDF: ${e.message}")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. MINI-DASHBOARD BANNER ---
        val totalCx = records.size
        val electivasList = records.filter { it.tipoCirugia.equals("Electiva", ignoreCase = true) }
        val urgenciasList = records.filter { it.tipoCirugia.equals("Urgencia", ignoreCase = true) }

        fun getMinutes(r: AuditRecord): Double {
            if (r.duracionSegundos > 0) return r.duracionSegundos / 60.0
            return TimeHelper.calculateMinutesBetween(r.tInicio, r.tFin).toDouble()
        }

        val avgElectivaMin = electivasList.map { getMinutes(it) }.filter { it > 0 }.let { if (it.isNotEmpty()) it.average() else 0.0 }
        val avgUrgenciaMin = urgenciasList.map { getMinutes(it) }.filter { it > 0 }.let { if (it.isNotEmpty()) it.average() else 0.0 }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToDashboard() }
                .testTag("mini_dashboard_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BlueAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = BlueAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ESTADÍSTICAS QUIRÚRGICAS HGV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ver métricas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueAccent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = BlueAccent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TOTAL REGISTROS", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalCx cx", fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PROM. ELECTIVA", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                        Text(
                            text = if (avgElectivaMin > 0) TimeHelper.formatMinutesHuman(avgElectivaMin) else "--",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = BlueAccent
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("PROM. URGENCIA", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = CrimsonDanger)
                        Text(
                            text = if (avgUrgenciaMin > 0) TimeHelper.formatMinutesHuman(avgUrgenciaMin) else "--",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = CrimsonDanger
                        )
                    }
                }
            }
        }

        // --- 2. CRONOMETRADO QUIRÚRGICO EN TIEMPO REAL (HUD PRINCIPAL) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicalNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ClinicalNavyCard, ClinicalNavy)
                        )
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Header Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (formState.isTimerRunning) CrimsonDanger.copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (formState.isTimerRunning) CrimsonDanger.copy(alpha = timerAlpha) else EmeraldGreen.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (formState.isTimerRunning) Icons.Default.FiberManualRecord else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (formState.isTimerRunning) CrimsonDanger else EmeraldGreen,
                        modifier = Modifier
                            .size(14.dp)
                            .then(if (formState.isTimerRunning) Modifier.alpha(timerAlpha) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            formState.isTimerRunning -> "CIRUGÍA EN TIEMPO REAL (CRONÓMETRO ACTIVO)"
                            formState.duracionSegundos > 0 -> "CIRUGÍA CONCLUIDA • DURACIÓN CERTIFICADA"
                            else -> "CRONÓMETRO QUIRÚRGICO EN ESPERA"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (formState.isTimerRunning) Color(0xFFFF8B8B) else EmeraldGreen,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High-visibility Digital Clock
                Text(
                    text = TimeHelper.formatDurationHms(formState.liveElapsedSeconds),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                if (formState.duracionSegundos > 0 && !formState.isTimerRunning) {
                    Text(
                        text = "Duración Concreta: ${TimeHelper.formatDurationHuman(formState.duracionSegundos)}",
                        fontSize = 13.sp,
                        color = CyanHUD,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Background notification alert indicator
                if (alertsEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = CyanHUD,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Aviso en segundo plano cada ${alertIntervalMin}m",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Timestamp Trigger Badges (Listo, Inicio, Fin)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Técnico Listo
                    TimestampBadge(
                        title = "1. TÉCNICO LISTO",
                        time = formState.tListo,
                        accentColor = EmeraldGreen,
                        icon = Icons.Default.Check,
                        isLocked = formState.tListo.isNotBlank(),
                        onClick = { viewModel.stampListo() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stamp_listo")
                    )

                    // Inicio Cirugía (Start chronometer)
                    TimestampBadge(
                        title = "2. INICIO CIRUGÍA",
                        time = formState.tInicio,
                        accentColor = BlueAccent,
                        icon = Icons.Default.PlayArrow,
                        isLocked = formState.tInicio.isNotBlank(),
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                if (!com.example.util.NotificationHelper.hasNotificationPermission(context)) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            viewModel.stampInicio()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stamp_inicio")
                    )

                    // Fin Cirugía (Stop chronometer)
                    TimestampBadge(
                        title = "3. FIN CIRUGÍA",
                        time = formState.tFin,
                        accentColor = CrimsonDanger,
                        icon = Icons.Default.Stop,
                        isLocked = formState.tFin.isNotBlank(),
                        onClick = { viewModel.stampFin() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stamp_fin")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- HITOS INTRAOPERATORIOS (SPLIT TIME) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = CyanHUD,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HITOS INTRAOPERATORIOS (SPLIT TIME)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanHUD,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            if (formState.splitTimes.isNotEmpty()) {
                                Text(
                                    text = "${formState.splitTimes.size} parciales",
                                    fontSize = 10.sp,
                                    color = Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Custom note input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = customSplitNote,
                                onValueChange = { customSplitNote = it },
                                placeholder = { Text("Hito personalizado...", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CyanHUD,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("custom_split_input")
                            )

                            Button(
                                onClick = {
                                    if (customSplitNote.isNotBlank()) {
                                        viewModel.addSplitTime(customSplitNote)
                                        customSplitNote = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanHUD),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("add_custom_split_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar Parcial", tint = ClinicalNavy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PARCIAL", fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = ClinicalNavy)
                            }
                        }

                        // Display registered split times timeline
                        if (formState.splitTimes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                formState.splitTimes.forEach { split ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(CyanHUD)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = split.elapsedHms,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = ClinicalNavy
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = split.timeLabel,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = split.note,
                                                fontSize = 9.5.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.removeSplitTime(split.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar parcial",
                                                tint = CrimsonDanger,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. DATOS DE LA CIRUGÍA Y PACIENTE ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ModernSectionHeader(
                    title = "IDENTIFICACIÓN Y TIPO DE CIRUGÍA",
                    subtitle = "Seleccione el tipo y capture expediente, CURP o antecedente",
                    icon = Icons.Default.Person,
                    accentColor = BlueAccent
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Large Dual Selector: Electiva vs Urgencia
                Text(
                    text = "TIPO DE PROCEDIMIENTO:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Electiva Card
                    val isElectiva = formState.tipoCirugia.equals("Electiva", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isElectiva) BlueAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (isElectiva) 2.dp else 1.dp,
                                color = if (isElectiva) BlueAccent else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.updateTipoCirugia("Electiva") }
                            .padding(vertical = 12.dp, horizontal = 10.dp)
                            .testTag("chip_electiva"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isElectiva) Icons.Default.CheckCircle else Icons.Default.EventNote,
                                contentDescription = null,
                                tint = if (isElectiva) BlueAccent else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ELECTIVA",
                                fontSize = 13.sp,
                                fontWeight = if (isElectiva) FontWeight.Black else FontWeight.Bold,
                                color = if (isElectiva) BlueAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Urgencia Card
                    val isUrgencia = formState.tipoCirugia.equals("Urgencia", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isUrgencia) CrimsonDanger.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (isUrgencia) 2.dp else 1.dp,
                                color = if (isUrgencia) CrimsonDanger else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.updateTipoCirugia("Urgencia") }
                            .padding(vertical = 12.dp, horizontal = 10.dp)
                            .testTag("chip_urgencia"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isUrgencia) Icons.Default.CheckCircle else Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = if (isUrgencia) CrimsonDanger else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "URGENCIA",
                                fontSize = 13.sp,
                                fontWeight = if (isUrgencia) FontWeight.Black else FontWeight.Bold,
                                color = if (isUrgencia) CrimsonDanger else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Patient Name Input
                OutlinedTextField(
                    value = formState.paciente,
                    onValueChange = { viewModel.updatePaciente(it) },
                    label = { Text("Nombre del Paciente (Apellidos, Nombre)") },
                    placeholder = { Text("Ej. Pérez Gómez, Juan Carlos") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BlueAccent) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Expediente Clínico HGV & Folio CURP fields with Auto-Fill Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Expediente Clínico HGV
                    OutlinedTextField(
                        value = formState.expediente,
                        onValueChange = { viewModel.updateExpediente(it) },
                        label = { Text("N° Expediente HGV") },
                        placeholder = { Text("Ej. EXP-2026-891") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.FolderShared, contentDescription = null, tint = TealClinical) },
                        trailingIcon = {
                            if (formState.expediente.isNotBlank()) {
                                IconButton(onClick = { viewModel.autoFillPatientFromExpedienteOrCurp(formState.expediente) }) {
                                    Icon(Icons.Default.FindInPage, contentDescription = "Buscar Expediente", tint = BlueAccent)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("expediente_input")
                    )

                    // Folio Autogenerado derivado de CURP
                    OutlinedTextField(
                        value = formState.folioCurp,
                        onValueChange = { viewModel.updateFolioCurp(it) },
                        label = { Text("Folio Autogenerado") },
                        placeholder = { Text("FOL-CURP-HGV") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TealClinical,
                            unfocusedTextColor = TealClinical
                        ),
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("folio_curp_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // CURP / Wristband ID
                    OutlinedTextField(
                        value = formState.curp,
                        onValueChange = { viewModel.updateCurp(it) },
                        label = { Text("CURP / Registro") },
                        placeholder = { Text("18 Dígitos") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = BlueAccent) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (formState.curp.isNotBlank()) {
                                    IconButton(onClick = { viewModel.autoFillPatientFromExpedienteOrCurp(formState.curp) }) {
                                        Icon(Icons.Default.FindInPage, contentDescription = "Buscar Antecedentes", tint = EmeraldGreen)
                                    }
                                }
                                IconButton(onClick = {
                                    curpPhotoLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Foto ID",
                                        tint = BlueAccent
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("curp_input")
                    )

                    // Calculated / Manual Age
                    OutlinedTextField(
                        value = formState.edad,
                        onValueChange = { viewModel.updateEdad(it) },
                        label = { Text("Edad") },
                        placeholder = { Text("Años") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("age_input")
                    )
                }

                // ID Photo preview if attached
                if (formState.fotoCurpUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = formState.fotoCurpUri,
                                    contentDescription = "Foto ID",
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Identificación / Pulsera adjunta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Certificado para auditoría", fontSize = 10.5.sp, color = EmeraldGreen)
                                }
                            }
                            IconButton(onClick = { viewModel.updateFotoCurpUri(null) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = CrimsonDanger)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- MODULO DE VERIFICACION DE NO AFILIACIÓN IMSS / ISSSTE ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VERIFICACIÓN NO AFILIACIÓN (POBLACIÓN ABIERTA SSA)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.3.sp
                                )
                            }

                            // CURP syntax validation status
                            if (formState.curp.isNotBlank()) {
                                val isValidSyntax = CurpHelper.validateCurpSyntax(formState.curp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isValidSyntax) EmeraldGreen.copy(alpha = 0.15f) else CrimsonDanger.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isValidSyntax) "CURP VÁLIDA ✔" else "CURP SINTAXIS ❌",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isValidSyntax) EmeraldGreen else CrimsonDanger
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Toggles & Badges for IMSS & ISSSTE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // IMSS Status Badge
                            val isImssNoAfiliado = formState.noAfiliacionImss == "NO_AFILIADO"
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isImssNoAfiliado) EmeraldGreen.copy(alpha = 0.12f) else CrimsonDanger.copy(alpha = 0.15f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isImssNoAfiliado) EmeraldGreen else CrimsonDanger
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val next = if (isImssNoAfiliado) "AFILIADO_DETECTADO" else "NO_AFILIADO"
                                        viewModel.updateNoAfiliacionImss(next)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isImssNoAfiliado) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (isImssNoAfiliado) EmeraldGreen else CrimsonDanger,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isImssNoAfiliado) "IMSS: NO AFILIADO ✔" else "IMSS: AFILIADO ❌",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isImssNoAfiliado) EmeraldGreen else CrimsonDanger
                                    )
                                }
                            }

                            // ISSSTE Status Badge
                            val isIsssteNoAfiliado = formState.noAfiliacionIssste == "NO_AFILIADO"
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isIsssteNoAfiliado) EmeraldGreen.copy(alpha = 0.12f) else CrimsonDanger.copy(alpha = 0.15f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isIsssteNoAfiliado) EmeraldGreen else CrimsonDanger
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val next = if (isIsssteNoAfiliado) "AFILIADO_DETECTADO" else "NO_AFILIADO"
                                        viewModel.updateNoAfiliacionIssste(next)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isIsssteNoAfiliado) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (isIsssteNoAfiliado) EmeraldGreen else CrimsonDanger,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isIsssteNoAfiliado) "ISSSTE: NO AFILIADO ✔" else "ISSSTE: AFILIADO ❌",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isIsssteNoAfiliado) EmeraldGreen else CrimsonDanger
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Official Web Portals External Launchers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CurpHelper.OFFICIAL_IMSS_NO_AFILIACION_URL))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.showToast("No se pudo abrir navegador: ${e.message}")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Portal IMSS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CurpHelper.OFFICIAL_ISSSTE_NO_AFILIACION_URL))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.showToast("No se pudo abrir navegador: ${e.message}")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Portal ISSSTE", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- 4. DATOS CLÍNICOS Y EQUIPO QUIRÚRGICO (CON CHIPS DE SUGERENCIA RÁPIDA) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ModernSectionHeader(
                    title = "PROCEDIMIENTO Y EQUIPO QUIRÚRGICO",
                    subtitle = "Escriba o toque una sugerencia rápida de 1-toque",
                    icon = Icons.Default.MedicalServices,
                    accentColor = TealClinical
                )

                Spacer(modifier = Modifier.height(10.dp))

                // --- CATÁLOGO Y BÚSQUEDA AUTOCOMPLETABLE DE CIRUGÍA LAPAROSCÓPICA HGV ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATÁLOGO Y TIPO DE CIRUGÍA LAPAROSCÓPICA:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TealClinical,
                        letterSpacing = 0.3.sp
                    )

                    TextButton(
                        onClick = { excelCatalogLauncher.launch("*/*") },
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp), tint = BlueAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Subir Excel/CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                var procedureDropdownExpanded by remember { mutableStateOf(false) }
                val filteredProcedures = remember(formState.cirugiaRealizada, laparoscopicProcedures) {
                    if (formState.cirugiaRealizada.isBlank()) {
                        laparoscopicProcedures
                    } else {
                        val query = formState.cirugiaRealizada.trim()
                        val matches = laparoscopicProcedures.filter {
                            it.nombre.contains(query, ignoreCase = true) ||
                            it.especialidad.contains(query, ignoreCase = true) ||
                            it.diagnosticoDefecto.contains(query, ignoreCase = true)
                        }
                        if (matches.isNotEmpty()) matches else laparoscopicProcedures
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = procedureDropdownExpanded,
                    onExpandedChange = { procedureDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = formState.cirugiaRealizada,
                        onValueChange = { newValue ->
                            viewModel.updateCirugiaRealizada(newValue)
                            procedureDropdownExpanded = true
                        },
                        label = { Text("Buscar o seleccionar tipo de cirugía laparoscópica...") },
                        placeholder = { Text("Ej. Colecistectomía Laparoscópica, Apendicectomía...") },
                        leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = TealClinical) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = procedureDropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("cirugia_realizada_dropdown_input")
                    )

                    ExposedDropdownMenu(
                        expanded = procedureDropdownExpanded,
                        onDismissRequest = { procedureDropdownExpanded = false }
                    ) {
                        filteredProcedures.forEach { proc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = proc.nombre,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TealClinical
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (proc.especialidad.isNotBlank()) {
                                                Text(
                                                    text = proc.especialidad,
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (proc.diagnosticoDefecto.isNotBlank()) {
                                                Text(
                                                    text = "• ${proc.diagnosticoDefecto}",
                                                    fontSize = 10.sp,
                                                    color = EmeraldGreen,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectLaparoscopicProcedure(proc)
                                    procedureDropdownExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (formState.cirugiaRealizada.equals(proc.nombre, ignoreCase = true)) Icons.Default.CheckCircle else Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = if (formState.cirugiaRealizada.equals(proc.nombre, ignoreCase = true)) EmeraldGreen else TealClinical
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Diagnóstico Input
                OutlinedTextField(
                    value = formState.diagnostico,
                    onValueChange = { viewModel.updateDiagnostico(it) },
                    label = { Text("Diagnóstico Clínico (Pre / Postoperatorio)") },
                    placeholder = { Text("Ej. Colelitiasis sintomática, Apendicitis aguda fase II...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diagnostico_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick surgeon suggestions (Combining custom registered directory + defaults)
                val combinedDoctors = remember(doctorDirectory) {
                    val defaults = listOf("Dr. Morales", "Dr. Méndez", "Dra. Reyes", "Dr. Ruiz", "Dr. Castro", "Dra. Sánchez")
                    (doctorDirectory + defaults).distinct()
                }

                Text(
                    text = "DIRECTORIO DE CIRUJANOS ADSCRITOS HGV (1-TOQUE):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    combinedDoctors.forEach { doc ->
                        val isSelected = formState.medicoTratante.equals(doc, ignoreCase = true)
                        SuggestionChip(
                            onClick = { viewModel.updateMedicoTratante(doc) },
                            label = { Text(doc, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) BlueAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (isSelected) BlueAccent else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Médico Tratante Input
                OutlinedTextField(
                    value = formState.medicoTratante,
                    onValueChange = { viewModel.updateMedicoTratante(it) },
                    label = { Text("Médico Tratante / Cirujano Responsable") },
                    placeholder = { Text("Ej. Dr. Morales") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medico_tratante_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick nurse instrumentist suggestions (Custom directory + defaults)
                val combinedNurses = remember(nurseDirectory) {
                    val defaults = listOf("L.E. Rosa Méndez", "E.Q. Carmen Silva", "Enf. Juan Pérez", "L.E. Sofia Vega")
                    (nurseDirectory + defaults).distinct()
                }

                Text(
                    text = "DIRECTORIO DE ENFERMERÍA INSTRUMENTISTA HGV (1-TOQUE):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    combinedNurses.forEach { nurse ->
                        val isSelected = formState.enfermeraInstrumentista.equals(nurse, ignoreCase = true)
                        SuggestionChip(
                            onClick = { viewModel.updateEnfermeraInstrumentista(nurse) },
                            label = { Text(nurse, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Enfermera Instrumentista Input
                OutlinedTextField(
                    value = formState.enfermeraInstrumentista,
                    onValueChange = { viewModel.updateEnfermeraInstrumentista(it) },
                    label = { Text("Enfermera Instrumentista") },
                    placeholder = { Text("Ej. L.E. Rosa Méndez") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instrumentista_input")
                )
            }
        }

        // --- 5. EVIDENCIA DE HOJA QUIRÚRGICA CON FIRMA (CAMERAX) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ModernSectionHeader(
                    title = "EVIDENCIA FÍSICA • HOJA QUIRÚRGICA",
                    subtitle = "Capture la hoja con la firma física de la instrumentista",
                    icon = Icons.Default.AssignmentTurnedIn,
                    accentColor = EmeraldGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (formState.fotoHojaQuirurgicaUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldGreen.copy(alpha = 0.08f))
                            .border(1.5.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = formState.fotoHojaQuirurgicaUri,
                                contentDescription = "Hoja Quirúrgica",
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Hoja Foliada Adjunta",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = EmeraldGreen
                                    )
                                }
                                Text(
                                    text = "Firma: ${formState.enfermeraInstrumentista.ifBlank { "Instrumentista en turno" }}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Guardado en Room para auditoría forense",
                                    fontSize = 10.5.sp,
                                    color = BlueAccent
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { launchCameraXCapture() }) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Re-tomar foto",
                                        tint = BlueAccent
                                    )
                                }
                                IconButton(onClick = { viewModel.updateFotoHojaQuirurgicaUri(null) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar hoja",
                                        tint = CrimsonDanger
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { launchCameraXCapture() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(50.dp)
                                .testTag("camerax_take_photo_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TOMAR CON CAMERAX", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                        }

                        OutlinedButton(
                            onClick = {
                                surgicalSheetLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(50.dp)
                                .testTag("attach_sheet_gallery_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GALERÍA", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 6. SUPERVISIÓN DEL MÉDICO ADSCRITO ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ModernSectionHeader(
                    title = "SUPERVISIÓN DEL MÉDICO ADSCRITO",
                    subtitle = "Registro de presencia y permanencia en sala",
                    icon = Icons.Default.SupervisorAccount,
                    accentColor = BlueAccent
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.adsLlegada,
                        onValueChange = { viewModel.updateAdsLlegada(it) },
                        readOnly = true,
                        label = { Text("Llegada") },
                        placeholder = { Text("00:00") },
                        trailingIcon = {
                            IconButton(onClick = {
                                openTimePicker(formState.adsLlegada) { viewModel.updateAdsLlegada(it) }
                            }) {
                                Icon(Icons.Default.AccessTime, contentDescription = "Elegir llegada")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adscrito_llegada")
                    )

                    OutlinedTextField(
                        value = if (formState.adsEstanciaMin > 0) "${formState.adsEstanciaMin} min" else "--",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estancia") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = EmeraldGreen,
                            unfocusedTextColor = EmeraldGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = formState.adsSalida,
                        onValueChange = { viewModel.updateAdsSalida(it) },
                        readOnly = true,
                        label = { Text("Salida") },
                        placeholder = { Text("00:00") },
                        trailingIcon = {
                            IconButton(onClick = {
                                openTimePicker(formState.adsSalida) { viewModel.updateAdsSalida(it) }
                            }) {
                                Icon(Icons.Default.AccessTime, contentDescription = "Elegir salida")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adscrito_salida")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Adscrito no entró checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.updateAdsNoEntro(!formState.adsNoEntro) }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = formState.adsNoEntro,
                        onCheckedChange = { viewModel.updateAdsNoEntro(it) },
                        colors = CheckboxDefaults.colors(checkedColor = CrimsonDanger)
                    )
                    Text(
                        text = "El médico adscrito NO ingresó a sala quirúrgica",
                        fontSize = 12.5.sp,
                        fontWeight = if (formState.adsNoEntro) FontWeight.Bold else FontWeight.Normal,
                        color = if (formState.adsNoEntro) CrimsonDanger else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- 7. DESVIACIONES Y OBSERVACIONES ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ModernSectionHeader(
                    title = "CAUSAS DE DESVIACIÓN Y OBSERVACIONES",
                    subtitle = "Seleccione el factor y documente incidencias",
                    icon = Icons.Default.Warning,
                    accentColor = AmberWarning
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "FACTOR PRIMARIO DE DESVIACIÓN:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Deviation chips
                val options = listOf(
                    "Sin Desviación",
                    "Camilleros",
                    "Enfermería",
                    "Paciente",
                    "Ausencia Adscrito",
                    "Anestesiología",
                    "Falta Equipo",
                    "Material"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.forEach { item ->
                        val isSelected = formState.causa.equals(item, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateCausa(item) },
                            label = { Text(item, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (item == "Sin Desviación") EmeraldGreen else AmberWarning,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = formState.observaciones,
                    onValueChange = { viewModel.updateObservaciones(it) },
                    label = { Text("Observaciones Forenses y Notas de la Cirugía") },
                    placeholder = { Text("Detalles del instrumental de laparoscopia, conteo de gasas, incidencias operativas...") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("observations_input")
                )
            }
        }

        // --- 8. FIRMAS DIGITALES EN PANTALLA (OPCIONAL) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDigitalSignatures = !showDigitalSignatures }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = BlueAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Firmas Digitales en Pantalla (Opcional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (showDigitalSignatures) "Ocultar ▲" else "Mostrar ▼",
                        fontSize = 11.sp,
                        color = BlueAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(visible = showDigitalSignatures) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text(
                            text = "Firma del Técnico de Laparoscopia",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${profile.nombre} (${profile.cargo})",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        SignatureCanvas(
                            initialBase64 = formState.firmaTecnicoBase64,
                            onSignatureChanged = { viewModel.updateFirmaTecnico(it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Firma de Testigo / Instrumentista",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = formState.nombreTestigo,
                            onValueChange = { viewModel.updateNombreTestigo(it) },
                            label = { Text("Nombre y Cargo del Testigo") },
                            placeholder = { Text("Ej. Enf. Circulante / Instrumentista") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        )
                        SignatureCanvas(
                            initialBase64 = formState.firmaTestigoBase64,
                            onSignatureChanged = { viewModel.updateFirmaTestigo(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- 9. ACTION BAR (GENERAR PDF & LIMPIAR) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    viewModel.saveAndGeneratePdf(
                        context = context,
                        onSuccess = { file -> sharePdf(file) },
                        onError = { msg -> viewModel.showToast(msg) }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp)
                    .testTag("generate_pdf_button")
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERAR PDF OFICIAL", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }

            Button(
                onClick = { showCleanDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("clean_form_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("LIMPIAR", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // Shielded Clean Modal (Limpieza Blindada)
    if (showCleanDialog) {
        val hasTimestamps = formState.tListo.isNotBlank() || formState.tInicio.isNotBlank() || formState.tFin.isNotBlank()

        AlertDialog(
            onDismissRequest = { showCleanDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning) },
            title = {
                Text(
                    text = if (hasTimestamps) "🛡️ Protección de Tiempos y Cronómetro" else "⚠️ Nueva Cirugía",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (hasTimestamps) {
                        "Se han detectado sellos de tiempo / cronómetro en el formulario.\n\n¿Desea MANTENER estas horas para el siguiente paciente consecutivo o destruir todo el registro?"
                    } else {
                        "¿Desea limpiar el formulario para registrar un nuevo paciente?"
                    }
                )
            },
            confirmButton = {
                if (hasTimestamps) {
                    Button(
                        onClick = {
                            viewModel.executeCleanForm(keepTimestamps = true)
                            showCleanDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("Mantener Tiempos")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.executeCleanForm(keepTimestamps = false)
                            showCleanDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                    ) {
                        Text("Limpiar Todo")
                    }
                }
            },
            dismissButton = {
                if (hasTimestamps) {
                    Button(
                        onClick = {
                            viewModel.executeCleanForm(keepTimestamps = false)
                            showCleanDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                    ) {
                        Text("Borrar Todo")
                    }
                } else {
                    TextButton(onClick = { showCleanDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    // CameraX Capture Dialog for surgical log sheets
    if (showCameraX) {
        CameraXCaptureDialog(
            onDismissRequest = { showCameraX = false },
            onPhotoCaptured = { savedPath ->
                showCameraX = false
                viewModel.updateFotoHojaQuirurgicaUri(savedPath)
                viewModel.showToast("✔ Hoja quirúrgica capturada con CameraX y guardada en base de datos")
            },
            onError = { err ->
                showCameraX = false
                viewModel.showToast("❌ $err")
            }
        )
    }
}

@Composable
fun ModernSectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            )
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimestampBadge(
    title: String,
    time: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLocked) accentColor else Color.White.copy(alpha = 0.2f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else icon,
                    contentDescription = null,
                    tint = if (isLocked) accentColor else Color(0xFFCBD5E1),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = title,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isLocked) accentColor else Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time.ifBlank { "Tocar aquí" },
                fontSize = if (time.isNotBlank()) 15.sp else 10.5.sp,
                fontWeight = if (time.isNotBlank()) FontWeight.Black else FontWeight.Normal,
                fontFamily = if (time.isNotBlank()) FontFamily.Monospace else FontFamily.Default,
                color = if (time.isNotBlank()) Color.White else Color(0xFF94A3B8)
            )
        }
    }
}
