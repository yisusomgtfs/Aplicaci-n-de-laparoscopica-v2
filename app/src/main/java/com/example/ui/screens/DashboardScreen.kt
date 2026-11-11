package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditRecord
import com.example.ui.components.DoctorSurgicalTrendChart
import com.example.ui.components.DurationComparisonBarChart
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SurgicalDistributionDonutChart
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.ClinicalNavy
import com.example.ui.theme.ClinicalNavyCard
import com.example.ui.theme.ClinicalNavyElevated
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TealClinical
import com.example.ui.viewmodel.AuditViewModel
import com.example.util.TimeHelper
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AuditViewModel,
    onNavigateToForm: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val records by viewModel.allRecords.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val formState by viewModel.formState.collectAsState()

    // --- Statistics Computation ---
    val totalProcedures = records.size

    val electivas = records.filter { it.tipoCirugia.equals("Electiva", ignoreCase = true) }
    val urgencias = records.filter { it.tipoCirugia.equals("Urgencia", ignoreCase = true) }

    fun getDurationMinutes(r: AuditRecord): Double {
        if (r.duracionSegundos > 0) return r.duracionSegundos / 60.0
        val min = TimeHelper.calculateMinutesBetween(r.tInicio, r.tFin)
        return min.toDouble()
    }

    val electivasWithDuration = electivas.map { getDurationMinutes(it) }.filter { it > 0 }
    val urgenciasWithDuration = urgencias.map { getDurationMinutes(it) }.filter { it > 0 }

    val avgDurationElectiva = if (electivasWithDuration.isNotEmpty()) electivasWithDuration.average() else 0.0
    val avgDurationUrgencia = if (urgenciasWithDuration.isNotEmpty()) urgenciasWithDuration.average() else 0.0

    val todayDate = TimeHelper.getTodayDate()
    val todayProceduresCount = records.count { it.fecha == todayDate }

    val withSupervisorCount = records.count { !it.adsNoEntro && it.adsEstanciaMin > 0 }
    val supervisorCompliancePercent = if (totalProcedures > 0) (withSupervisorCount.toFloat() / totalProcedures) * 100f else 0f

    val withSheetEvidenceCount = records.count { !it.fotoHojaQuirurgicaUri.isNullOrBlank() }
    val evidenceCompliancePercent = if (totalProcedures > 0) (withSheetEvidenceCount.toFloat() / totalProcedures) * 100f else 0f

    val totalAccumulatedMinutes = records.sumOf { getDurationMinutes(it) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        // Hero Surgical Cockpit Card
        Card(
            shape = RoundedCornerShape(18.dp),
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
                    .padding(18.dp)
            ) {
                // Top row with institution and date pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HOSPITAL GENERAL HGV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanHUD,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Cockpit Quirúrgico",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = todayDate,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live status banner inside cockpit
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (formState.isTimerRunning) {
                                CrimsonDanger.copy(alpha = 0.15f)
                            } else {
                                EmeraldGreen.copy(alpha = 0.12f)
                            }
                        )
                        .border(
                            1.dp,
                            if (formState.isTimerRunning) CrimsonDanger.copy(alpha = 0.5f) else EmeraldGreen.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onNavigateToForm() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (formState.isTimerRunning) CrimsonDanger else EmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (formState.isTimerRunning) {
                                        "CIRUGÍA EN TIEMPO REAL ACTIVA"
                                    } else {
                                        "QUIRÓFANO DE LAPAROSCOPIA LISTO"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (formState.isTimerRunning) Color(0xFFFF8B8B) else EmeraldGreen,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (formState.isTimerRunning) {
                                        "Paciente: ${formState.paciente.ifBlank { "Sin nombre" }} • ${formState.cirugiaRealizada.ifBlank { "Procedimiento en curso" }}"
                                    } else {
                                        "Toque para abrir bitácora y arrancar cronómetro"
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        if (formState.isTimerRunning) {
                            Text(
                                text = TimeHelper.formatDurationHms(formState.liveElapsedSeconds),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Technician profile footer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BlueAccent.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CyanHUD,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Responsable: ${profile.nombre} • ${profile.cargo}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick KPI Metric Cards Grid (2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = "Total Cirugías",
                value = "$totalProcedures",
                subtitle = "En base de datos Room",
                icon = Icons.Default.MedicalServices,
                iconColor = BlueAccent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_total_cx")
            )

            MetricStatCard(
                title = "Cirugías Hoy",
                value = "$todayProceduresCount",
                subtitle = "Turno operativo",
                icon = Icons.Default.Today,
                iconColor = EmeraldGreen,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_today_cx")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = "Prom. Electivas",
                value = if (avgDurationElectiva > 0) TimeHelper.formatMinutesHuman(avgDurationElectiva) else "--",
                subtitle = "${electivas.size} procedimientos",
                icon = Icons.Default.AccessTime,
                iconColor = CyanHUD,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_avg_electiva")
            )

            MetricStatCard(
                title = "Prom. Urgencias",
                value = if (avgDurationUrgencia > 0) TimeHelper.formatMinutesHuman(avgDurationUrgencia) else "--",
                subtitle = "${urgencias.size} procedimientos",
                icon = Icons.Default.Timer,
                iconColor = CrimsonDanger,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_avg_urgencia")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Card 1: Comparativa de Tiempo por Tipo (Urgencia vs Electiva)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TIEMPOS QUIRÚRGICOS PROMEDIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Urgencia vs Electiva",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BlueAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = BlueAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DurationComparisonBarChart(
                    avgMinutesElectiva = avgDurationElectiva,
                    avgMinutesUrgencia = avgDurationUrgencia,
                    countElectiva = electivas.size,
                    countUrgencia = urgencias.size
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chart Card 2: Distribución de Procedimientos (Donut Chart)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DISTRIBUCIÓN QUIRÚRGICA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Volumen por Tipo de Cirugía",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealClinical.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = TealClinical,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SurgicalDistributionDonutChart(
                    electivasCount = electivas.size,
                    urgenciasCount = urgencias.size
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 3: Tendencia de Tiempos Quirúrgicos por Médico (Último Mes)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().testTag("card_doctor_trend_panel")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TENDENCIA POR CIRUJANO (ÚLTIMO MES)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Variaciones Operativas",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BlueAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupervisorAccount,
                            contentDescription = null,
                            tint = BlueAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                DoctorSurgicalTrendChart(records = records)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 3: Auditoría Forense y Métricas de Cumplimiento Operativo
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AUDITORÍA FORENSE Y CUMPLIMIENTO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Control de Calidad Operativo",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Adscrito Compliance Row with progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(BlueAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SupervisorAccount,
                                    contentDescription = null,
                                    tint = BlueAccent,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Permanencia de Adscrito", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("$withSupervisorCount de $totalProcedures con estancia auditada", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f%%", supervisorCompliancePercent),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (supervisorCompliancePercent >= 80f) EmeraldGreen else AmberWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { if (totalProcedures > 0) supervisorCompliancePercent / 100f else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (supervisorCompliancePercent >= 80f) EmeraldGreen else AmberWarning,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instrumentist Sheet Evidence Row with progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Hoja Quirúrgica Firmada (CameraX)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("$withSheetEvidenceCount de $totalProcedures con evidencia física", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f%%", evidenceCompliancePercent),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (evidenceCompliancePercent >= 80f) EmeraldGreen else AmberWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { if (totalProcedures > 0) evidenceCompliancePercent / 100f else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (evidenceCompliancePercent >= 80f) EmeraldGreen else AmberWarning,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Total Operating Time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BlueAccent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Tiempo Total en Sala", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Horas quirúrgicas acumuladas", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = TimeHelper.formatMinutesHuman(totalAccumulatedMinutes),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Navigation Action Buttons Hub
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToForm,
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(52.dp)
                    .testTag("dashboard_btn_new_surgery")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("NUEVA CIRUGÍA", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }

            OutlinedButton(
                onClick = onNavigateToHistory,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("dashboard_btn_view_history")
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("HISTORIAL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
