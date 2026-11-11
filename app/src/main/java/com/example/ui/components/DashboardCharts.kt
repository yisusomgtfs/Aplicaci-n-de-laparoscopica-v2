package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.model.AuditRecord
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.ClinicalNavyCard
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TealClinical
import com.example.util.TimeHelper
import java.util.Locale
import kotlin.math.max

/**
 * Metric KPI Card for Dashboard summary items with modern glass depth and glowing accents.
 */
@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(Locale.getDefault()),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.14f))
                        .border(1.dp, iconColor.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Custom Compose Canvas Donut Chart for surgery type distribution (Electiva vs Urgencia).
 */
@Composable
fun SurgicalDistributionDonutChart(
    electivasCount: Int,
    urgenciasCount: Int,
    modifier: Modifier = Modifier
) {
    val total = electivasCount + urgenciasCount

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(electivasCount, urgenciasCount) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val electivaPercent = if (total > 0) (electivasCount.toFloat() / total) * 100f else 0f
    val urgenciaPercent = if (total > 0) (urgenciasCount.toFloat() / total) * 100f else 0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut Chart Canvas with Center Text
        Box(
            modifier = Modifier.size(154.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(144.dp)) {
                val strokeWidth = 24.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeftOffset = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                if (total == 0) {
                    drawArc(
                        color = Color(0xFFD6DBDF).copy(alpha = 0.5f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                } else {
                    val fullSweep = 360f * animationProgress.value
                    val electivaSweep = (electivasCount.toFloat() / total) * fullSweep
                    val urgenciaSweep = (urgenciasCount.toFloat() / total) * fullSweep

                    // Electiva Arc (Cyan / Blue Gradient look)
                    drawArc(
                        color = BlueAccent,
                        startAngle = -90f,
                        sweepAngle = electivaSweep,
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Urgencia Arc (Crimson Danger)
                    drawArc(
                        color = CrimsonDanger,
                        startAngle = -90f + electivaSweep,
                        sweepAngle = urgenciaSweep,
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Center counter pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Text(
                    text = "$total",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "PROCEDIMIENTOS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Legend Breakdown Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            // Electiva Legend Item
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BlueAccent.copy(alpha = 0.08f))
                    .border(1.dp, BlueAccent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BlueAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Electivas: $electivasCount",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%% del volumen", electivaPercent),
                            fontSize = 10.5.sp,
                            color = BlueAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Urgencia Legend Item
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CrimsonDanger.copy(alpha = 0.08f))
                    .border(1.dp, CrimsonDanger.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CrimsonDanger)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Urgencias: $urgenciasCount",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%% del volumen", urgenciaPercent),
                            fontSize = 10.5.sp,
                            color = CrimsonDanger,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Compose Canvas Bar Chart comparing average duration (minutes) for Electivas vs Urgencias.
 */
@Composable
fun DurationComparisonBarChart(
    avgMinutesElectiva: Double,
    avgMinutesUrgencia: Double,
    countElectiva: Int,
    countUrgencia: Int,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(avgMinutesElectiva, avgMinutesUrgencia) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val maxVal = max(avgMinutesElectiva, avgMinutesUrgencia).coerceAtLeast(30.0)
    val maxAxis = (Math.ceil(maxVal / 20.0) * 20.0).toFloat().coerceAtLeast(60f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("duration_comparison_bar_chart")
    ) {
        // Chart Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                val chartWidth = size.width
                val chartHeight = size.height - 24.dp.toPx()
                val bottomY = chartHeight

                val gridLines = 3
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                // Draw horizontal guide lines
                for (i in 0..gridLines) {
                    val ratio = i.toFloat() / gridLines
                    val y = bottomY - (ratio * chartHeight)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f,
                        pathEffect = dashedEffect
                    )
                }

                // Baseline
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(0f, bottomY),
                    end = Offset(chartWidth, bottomY),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )

                val barWidth = 60.dp.toPx().coerceAtMost(chartWidth / 3.8f)
                val spacing = chartWidth / 3f

                val electivaX = spacing - (barWidth / 2f)
                val urgenciaX = (spacing * 2f) - (barWidth / 2f)

                // 1. Electiva Bar (Vibrant Blue/Cyan Gradient)
                val electivaTargetHeight = ((avgMinutesElectiva.toFloat() / maxAxis) * chartHeight).coerceAtLeast(0f)
                val currentElectivaHeight = electivaTargetHeight * animationProgress.value

                if (currentElectivaHeight > 0) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(CyanHUD, BlueAccent),
                            startY = bottomY - currentElectivaHeight,
                            endY = bottomY
                        ),
                        topLeft = Offset(electivaX, bottomY - currentElectivaHeight),
                        size = Size(barWidth, currentElectivaHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }

                // 2. Urgencia Bar (Crimson/Coral Gradient)
                val urgenciaTargetHeight = ((avgMinutesUrgencia.toFloat() / maxAxis) * chartHeight).coerceAtLeast(0f)
                val currentUrgenciaHeight = urgenciaTargetHeight * animationProgress.value

                if (currentUrgenciaHeight > 0) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF7675), CrimsonDanger),
                            startY = bottomY - currentUrgenciaHeight,
                            endY = bottomY
                        ),
                        topLeft = Offset(urgenciaX, bottomY - currentUrgenciaHeight),
                        size = Size(barWidth, currentUrgenciaHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }
        }

        // Labels beneath the bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Electiva column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (avgMinutesElectiva > 0) TimeHelper.formatMinutesHuman(avgMinutesElectiva) else "--",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = BlueAccent
                )
                Text(
                    text = "Electiva",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$countElectiva registradas",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Urgencia column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (avgMinutesUrgencia > 0) TimeHelper.formatMinutesHuman(avgMinutesUrgencia) else "--",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = CrimsonDanger
                )
                Text(
                    text = "Urgencia",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$countUrgencia registradas",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Comparative insight banner
        val diffMin = kotlin.math.abs(avgMinutesElectiva - avgMinutesUrgencia).toInt()
        val insightText = when {
            countElectiva == 0 && countUrgencia == 0 -> "Inicie una cirugía con cronómetro para calcular y comparar los promedios en tiempo real."
            countElectiva == 0 -> "Se han registrado únicamente cirugías de Urgencia en la base de datos."
            countUrgencia == 0 -> "Se han registrado únicamente cirugías Electivas en la base de datos."
            avgMinutesElectiva > avgMinutesUrgencia -> "Las cirugías Electivas duran en promedio $diffMin min más que las Urgencias."
            avgMinutesUrgencia > avgMinutesElectiva -> "Las cirugías de Urgencia duran en promedio $diffMin min más que las Electivas."
            else -> "Ambos tipos de cirugía promedian una duración idéntica."
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Text(
                text = "⚡ $insightText",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class DoctorTrendStats(
    val doctorName: String,
    val count: Int,
    val avgMinutes: Double,
    val minMinutes: Double,
    val maxMinutes: Double,
    val varianceMinutes: Double
)

/**
 * Panel de Tendencia de Tiempos Quirúrgicos por Médico durante el último mes.
 * Permite identificar variaciones operativas, promedios y rango de duración.
 */
@Composable
fun DoctorSurgicalTrendChart(
    records: List<AuditRecord>,
    modifier: Modifier = Modifier
) {
    var selectedDoctor by remember { mutableStateOf("TODOS") }

    fun getMinutes(r: AuditRecord): Double {
        if (r.duracionSegundos > 0) return r.duracionSegundos / 60.0
        return TimeHelper.calculateMinutesBetween(r.tInicio, r.tFin).toDouble()
    }

    // Filter records from the last month (last 30 days)
    val nowMillis = System.currentTimeMillis()
    val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
    val lastMonthRecords = remember(records) {
        records.filter { r ->
            (nowMillis - r.timestamp) <= thirtyDaysMillis || r.timestamp == 0L
        }
    }

    val doctorStatsList = remember(lastMonthRecords) {
        lastMonthRecords
            .filter { it.medicoTratante.isNotBlank() }
            .groupBy { it.medicoTratante.trim() }
            .map { (docName, docRecords) ->
                val durations = docRecords.map { getMinutes(it) }.filter { it > 0 }
                val avg = if (durations.isNotEmpty()) durations.average() else 0.0
                val min = if (durations.isNotEmpty()) durations.minOrNull() ?: 0.0 else 0.0
                val max = if (durations.isNotEmpty()) durations.maxOrNull() ?: 0.0 else 0.0
                val variance = max - min
                DoctorTrendStats(
                    doctorName = docName,
                    count = docRecords.size,
                    avgMinutes = avg,
                    minMinutes = min,
                    maxMinutes = max,
                    varianceMinutes = variance
                )
            }
            .sortedByDescending { it.count }
    }

    val displayedStats = remember(doctorStatsList, selectedDoctor) {
        if (selectedDoctor == "TODOS") doctorStatsList.take(5)
        else doctorStatsList.filter { it.doctorName.equals(selectedDoctor, ignoreCase = true) }
    }

    val maxVal = displayedStats.maxOfOrNull { it.maxMinutes }?.coerceAtLeast(40.0) ?: 60.0
    val maxAxis = (Math.ceil(maxVal / 20.0) * 20.0).toFloat().coerceAtLeast(60f)

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(displayedStats, selectedDoctor) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("doctor_surgical_trend_chart")
    ) {
        // Doctor Filter Chips
        if (doctorStatsList.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cirujano:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilterChip(
                    selected = selectedDoctor == "TODOS",
                    onClick = { selectedDoctor = "TODOS" },
                    label = { Text("Top Cirujanos", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlueAccent,
                        selectedLabelColor = Color.White
                    )
                )

                doctorStatsList.forEach { doc ->
                    val isSelected = selectedDoctor.equals(doc.doctorName, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDoctor = if (isSelected) "TODOS" else doc.doctorName },
                        label = { Text(doc.doctorName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealClinical,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (displayedStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin registros de tiempos por médico durante el último mes.",
                    fontSize = 12.5.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Trend Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                    val chartWidth = size.width
                    val chartHeight = size.height - 28.dp.toPx()
                    val bottomY = chartHeight

                    val gridLines = 4
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                    // Draw guide lines & minute labels
                    for (i in 0..gridLines) {
                        val ratio = i.toFloat() / gridLines
                        val y = bottomY - (ratio * chartHeight)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.18f),
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = 1f,
                            pathEffect = dashedEffect
                        )
                    }

                    // Baseline
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.4f),
                        start = Offset(0f, bottomY),
                        end = Offset(chartWidth, bottomY),
                        strokeWidth = 2f
                    )

                    val count = displayedStats.size
                    val itemWidth = chartWidth / count
                    val barWidth = (itemWidth * 0.45f).coerceAtMost(48.dp.toPx())

                    displayedStats.forEachIndexed { idx, stat ->
                        val centerX = (idx * itemWidth) + (itemWidth / 2f)
                        val leftX = centerX - (barWidth / 2f)

                        // Height calculations scaled by animation
                        val avgH = ((stat.avgMinutes.toFloat() / maxAxis) * chartHeight) * animationProgress.value
                        val maxH = ((stat.maxMinutes.toFloat() / maxAxis) * chartHeight) * animationProgress.value
                        val minH = ((stat.minMinutes.toFloat() / maxAxis) * chartHeight) * animationProgress.value

                        // 1. Operational Variation Bar (Min-Max Range Whisker Line)
                        if (maxH > minH && maxH > 0) {
                            drawLine(
                                color = AmberWarning,
                                start = Offset(centerX, bottomY - minH),
                                end = Offset(centerX, bottomY - maxH),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            // Top & Bottom caps for variance whisker
                            drawLine(
                                color = AmberWarning,
                                start = Offset(centerX - 10f, bottomY - maxH),
                                end = Offset(centerX + 10f, bottomY - maxH),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = AmberWarning,
                                start = Offset(centerX - 10f, bottomY - minH),
                                end = Offset(centerX + 10f, bottomY - minH),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // 2. Average Duration Bar
                        if (avgH > 0) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(BlueAccent, ClinicalNavyCard),
                                    startY = bottomY - avgH,
                                    endY = bottomY
                                ),
                                topLeft = Offset(leftX, bottomY - avgH),
                                size = Size(barWidth, avgH),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }

                        // 3. Average Marker Point
                        drawCircle(
                            color = CyanHUD,
                            radius = 4.dp.toPx(),
                            center = Offset(centerX, bottomY - avgH)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-Axis Labels & Doctor Variance Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                displayedStats.forEach { stat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stat.doctorName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Prom: ${stat.avgMinutes.toInt()}m",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = BlueAccent
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (stat.varianceMinutes > 40) CrimsonDanger.copy(alpha = 0.15f)
                                    else if (stat.varianceMinutes > 20) AmberWarning.copy(alpha = 0.15f)
                                    else EmeraldGreen.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Var: ±${(stat.varianceMinutes / 2).toInt()}m",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stat.varianceMinutes > 40) CrimsonDanger else if (stat.varianceMinutes > 20) AmberWarning else EmeraldGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Variance Operational Insight Box
            val maxVarianceDoc = displayedStats.maxByOrNull { it.varianceMinutes }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AmberWarning.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "VARIACIÓN OPERATIVA ÚLTIMO MES",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (maxVarianceDoc != null && maxVarianceDoc.varianceMinutes > 15) {
                                "Mayor variabilidad detectada: ${maxVarianceDoc.doctorName} (rango de ${maxVarianceDoc.minMinutes.toInt()}m a ${maxVarianceDoc.maxMinutes.toInt()}m en ${maxVarianceDoc.count} cirugías)."
                            } else {
                                "Tiempos quirúrgicos estables sin desviaciones críticas de tiempo durante el último mes."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
