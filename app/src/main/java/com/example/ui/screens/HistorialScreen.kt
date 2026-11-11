package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.example.data.model.AuditRecord
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AuditViewModel
import com.example.util.PdfGenerator
import com.example.util.TimeHelper
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HistorialScreen(
    viewModel: AuditViewModel,
    onNavigateToForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val records by viewModel.allRecords.collectAsState()
    val isRecordsUnlocked by viewModel.isRecordsUnlocked.collectAsState()

    // PIN fallback dialog for unlocking records
    var showPinFallbackDialog by remember { mutableStateOf(false) }
    var pinFallbackInput by remember { mutableStateOf("") }
    var pinFallbackVisible by remember { mutableStateOf(false) }
    var pinFallbackError by remember { mutableStateOf<String?>(null) }

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var searchScope by remember { mutableStateOf("ALL") } // "ALL", "DOCTOR", "DIAGNOSIS"
    var selectedTipoFilter by remember { mutableStateOf("TODAS") } // "TODAS", "ELECTIVA", "URGENCIA"

    var recordToDelete by remember { mutableStateOf<AuditRecord?>(null) }
    var recordToEdit by remember { mutableStateOf<AuditRecord?>(null) }

    fun triggerBiometricPrompt() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.unlockRecordsByBiometrics()
                    viewModel.showToast("✔ Identidad verificada por biometría. Registros desbloqueados.")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        viewModel.showToast("Biometría: $errString")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    viewModel.showToast("Huella no reconocida. Intente nuevamente.")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso a Registros Quirúrgicos")
            .setSubtitle("Autenticación biométrica para auditoría forense")
            .setDescription("Verifique su identidad para acceder al historial confidencial de cirugías.")
            .setNegativeButtonText("Usar PIN de Seguridad")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            viewModel.showToast("Error iniciando biometría: ${e.message}")
        }
    }

    // Auto-prompt biometric dialog on screen entry if records are locked
    LaunchedEffect(isRecordsUnlocked) {
        if (!isRecordsUnlocked) {
            triggerBiometricPrompt()
        }
    }

    // --- BIOMETRIC SECURITY GATE (When records are locked) ---
    if (!isRecordsUnlocked) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biometric_lock_card")
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Fingerprint Icon in colored circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Autenticación Biométrica",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "REGISTROS QUIRÚRGICOS PROTEGIDOS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyPrimary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Auditoría Forense y Protección de Datos Clínicos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BlueAccent,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "El acceso a la base de datos de cirugías, diagnósticos y hojas firmadas requiere autenticación biométrica de seguridad para garantizar la cadena de custodia.",
                        fontSize = 11.5.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Biometric Authentication Primary Button
                    Button(
                        onClick = { triggerBiometricPrompt() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("biometric_auth_records_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTENTICAR CON HUELLA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fallback to Security PIN
                    OutlinedButton(
                        onClick = {
                            showPinFallbackDialog = true
                            pinFallbackInput = ""
                            pinFallbackError = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("pin_fallback_records_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DESBLOQUEAR CON PIN",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                }
            }
        }

        // PIN Fallback Dialog
        if (showPinFallbackDialog) {
            AlertDialog(
                onDismissRequest = { showPinFallbackDialog = false },
                icon = { Icon(Icons.Default.Security, contentDescription = null, tint = BlueAccent) },
                title = { Text("PIN de Seguridad", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Ingrese su PIN de 4 dígitos para acceder a los registros quirúrgicos:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinFallbackInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pinFallbackInput = it
                                    pinFallbackError = null
                                }
                            },
                            placeholder = { Text("••••") },
                            singleLine = true,
                            visualTransformation = if (pinFallbackVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            trailingIcon = {
                                IconButton(onClick = { pinFallbackVisible = !pinFallbackVisible }) {
                                    Icon(
                                        imageVector = if (pinFallbackVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("pin_fallback_input")
                        )

                        if (pinFallbackError != null) {
                            Text(
                                text = pinFallbackError ?: "",
                                color = CrimsonDanger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                if (pinFallbackInput.length == 4) {
                                    val valid = viewModel.unlockRecordsByPin(pinFallbackInput)
                                    if (valid) {
                                        showPinFallbackDialog = false
                                        viewModel.showToast("✔ Acceso concedido a registros quirúrgicos")
                                    } else {
                                        pinFallbackError = "PIN incorrecto. Intente nuevamente."
                                    }
                                } else {
                                    pinFallbackError = "El PIN debe tener 4 dígitos."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                    ) {
                        Text("Desbloquear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinFallbackDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        return
    }

    // --- UNLOCKED SURGERY RECORDS SCREEN ---
    val availableDoctors = remember(records) {
        records.map { it.medicoTratante.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(6)
    }

    val availableDiagnoses = remember(records) {
        records.map { it.diagnostico.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(6)
    }

    val trimmedQuery = searchQuery.trim()

    val filteredRecords = records.filter { r ->
        val matchesType = when (selectedTipoFilter) {
            "ELECTIVA" -> r.tipoCirugia.equals("Electiva", ignoreCase = true)
            "URGENCIA" -> r.tipoCirugia.equals("Urgencia", ignoreCase = true)
            else -> true
        }

        val matchesSearch = if (trimmedQuery.isBlank()) {
            true
        } else {
            when (searchScope) {
                "DOCTOR" -> r.medicoTratante.contains(trimmedQuery, ignoreCase = true)
                "DIAGNOSIS" -> r.diagnostico.contains(trimmedQuery, ignoreCase = true)
                else -> r.medicoTratante.contains(trimmedQuery, ignoreCase = true) ||
                        r.diagnostico.contains(trimmedQuery, ignoreCase = true) ||
                        r.cirugiaRealizada.contains(trimmedQuery, ignoreCase = true) ||
                        r.paciente.contains(trimmedQuery, ignoreCase = true) ||
                        r.expediente.contains(trimmedQuery, ignoreCase = true) ||
                        r.folioCurp.contains(trimmedQuery, ignoreCase = true) ||
                        r.curp.contains(trimmedQuery, ignoreCase = true) ||
                        r.fecha.contains(trimmedQuery, ignoreCase = true) ||
                        r.causa.contains(trimmedQuery, ignoreCase = true)
            }
        }

        matchesType && matchesSearch
    }

    fun shareFile(file: File, mimeType: String, subject: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir Archivo"))
        } catch (e: Exception) {
            viewModel.showToast("Error al compartir: ${e.message}")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("surgery_list_screen")
    ) {
        // --- 1. SEARCH BAR AT THE TOP ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = when (searchScope) {
                        "DOCTOR" -> "Filtrar por nombre del médico..."
                        "DIAGNOSIS" -> "Filtrar por diagnóstico quirúrgico..."
                        else -> "Buscar médico, diagnóstico o cirugía..."
                    },
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = BlueAccent
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar búsqueda",
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_records_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. AUDIT SCOPE SELECTOR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Auditar:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FilterChip(
                selected = searchScope == "ALL",
                onClick = { searchScope = "ALL" },
                label = { Text("Todo", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_scope_all")
            )

            FilterChip(
                selected = searchScope == "DOCTOR",
                onClick = { searchScope = "DOCTOR" },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text("Por Médico", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BlueAccent,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_scope_doctor")
            )

            FilterChip(
                selected = searchScope == "DIAGNOSIS",
                onClick = { searchScope = "DIAGNOSIS" },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text("Por Diagnóstico", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldGreen,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_scope_diagnosis")
            )

            // Divider & surgery type chips
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(4.dp))

            listOf("TODAS" to "Todas", "ELECTIVA" to "Electivas", "URGENCIA" to "Urgencias").forEach { (key, label) ->
                val isSelected = selectedTipoFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTipoFilter = key },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (key == "URGENCIA") CrimsonDanger else if (key == "ELECTIVA") BlueAccent else NavyPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_chip_${key.lowercase()}")
                )
            }
        }

        // --- 3. QUICK 1-TAP AUDIT SUGGESTIONS ---
        if (availableDoctors.isNotEmpty() || availableDiagnoses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sugerencias:",
                    fontSize = 10.5.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )

                availableDoctors.forEach { doc ->
                    val isChosen = searchQuery.equals(doc, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isChosen) BlueAccent else MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.5.dp, if (isChosen) BlueAccent else Color.LightGray, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        TextButton(
                            onClick = {
                                searchQuery = if (isChosen) "" else doc
                                searchScope = "DOCTOR"
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                text = "👨‍⚕️ $doc",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                availableDiagnoses.forEach { diag ->
                    val isChosen = searchQuery.equals(diag, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isChosen) EmeraldGreen else MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.5.dp, if (isChosen) EmeraldGreen else Color.LightGray, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        TextButton(
                            onClick = {
                                searchQuery = if (isChosen) "" else diag
                                searchScope = "DIAGNOSIS"
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                text = "🔬 $diag",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 4. EXPORT BUTTONS ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.exportSummaryPdf(context) { file ->
                        shareFile(file, "application/pdf", "Auditoría HGV - Reporte Consolidado")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("export_summary_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("REPORTE PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.exportCsv(context) { file ->
                        shareFile(file, "text/csv", "Auditoría HGV - Base de Datos Forense")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("export_csv_button")
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("EXCEL CSV", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 5. AUDIT RESULTS COUNTER & LOCK ACTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (trimmedQuery.isNotBlank() || selectedTipoFilter != "TODAS") {
                        "Auditando: ${filteredRecords.size} de ${records.size} cirugías"
                    } else {
                        "Cirugías Registradas (${records.size})"
                    },
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (trimmedQuery.isNotBlank()) BlueAccent else MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Lock records on demand button
                IconButton(
                    onClick = {
                        viewModel.lockRecords()
                        viewModel.showToast("🔒 Registros quirúrgicos bloqueados")
                    },
                    modifier = Modifier.size(24.dp).testTag("lock_records_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloquear registros",
                        tint = CrimsonDanger,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (trimmedQuery.isNotBlank() || selectedTipoFilter != "TODAS" || searchScope != "ALL") {
                TextButton(
                    onClick = {
                        searchQuery = ""
                        searchScope = "ALL"
                        selectedTipoFilter = "TODAS"
                    },
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Limpiar filtros", fontSize = 11.sp, color = CrimsonDanger, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 6. SURGERY LIST WITH HIGHLIGHTS ---
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MedicalInformation,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (records.isEmpty()) {
                            "Aún no hay cirugías guardadas en la base de datos local."
                        } else {
                            "No se encontraron cirugías para \"$trimmedQuery\"."
                        },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    if (trimmedQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Verifique el nombre del médico o diagnóstico, o seleccione 'Todo' en el selector de auditoría.",
                            fontSize = 11.5.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { searchQuery = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                        ) {
                            Text("Ver Todas las Cirugías", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    RecordItemCard(
                        record = record,
                        searchQuery = trimmedQuery,
                        onSharePdf = {
                            val file = PdfGenerator.generateAuditPdf(context, record)
                            shareFile(file, "application/pdf", "Auditoría HGV - ${record.paciente}")
                        },
                        onEdit = { recordToEdit = record },
                        onDelete = { recordToDelete = record }
                    )
                }
            }
        }
    }

    // Confirm Delete Dialog
    if (recordToDelete != null) {
        val target = recordToDelete!!
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonDanger) },
            title = { Text("🗑️ Eliminar Registro", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Está completamente seguro de eliminar el registro de ${target.paciente} del ${target.fecha}?\n\nEsta acción es irreversible.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecord(target.id)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirm Edit Dialog
    if (recordToEdit != null) {
        val target = recordToEdit!!
        AlertDialog(
            onDismissRequest = { recordToEdit = null },
            icon = { Icon(Icons.Default.Edit, contentDescription = null, tint = AmberWarning) },
            title = { Text("✏️ Corregir Registro", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Desea cargar este registro de ${target.paciente} en el formulario principal para corregirlo?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadRecordForEditing(target)
                        recordToEdit = null
                        onNavigateToForm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning)
                ) {
                    Text("Cargar Registro")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToEdit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RecordItemCard(
    record: AuditRecord,
    searchQuery: String = "",
    onSharePdf: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val durationText = if (record.duracionSegundos > 0) {
        TimeHelper.formatDurationHuman(record.duracionSegundos)
    } else {
        val totalMin = TimeHelper.calculateMinutesBetween(record.tInicio, record.tFin)
        if (totalMin > 0) "$totalMin min" else "--"
    }

    val isUrgencia = record.tipoCirugia.equals("Urgencia", ignoreCase = true)

    val matchesDoctor = searchQuery.isNotBlank() && record.medicoTratante.contains(searchQuery, ignoreCase = true)
    val matchesDiagnosis = searchQuery.isNotBlank() && record.diagnostico.contains(searchQuery, ignoreCase = true)
    val matchesProcedure = searchQuery.isNotBlank() && record.cirugiaRealizada.contains(searchQuery, ignoreCase = true)

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (matchesDoctor || matchesDiagnosis) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Paciente + Tipo de Cirugia Badge + Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 ${record.paciente}" + if (record.edad.isNotBlank()) " (${record.edad} años)" else "",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Tipo Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isUrgencia) CrimsonDanger else BlueAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.tipoCirugia.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Date tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEAECEE))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.fecha,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2C3E50)
                        )
                    }
                }
            }

            // Expediente & Folio CURP Strip
            if (record.expediente.isNotBlank() || record.folioCurp.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (record.expediente.isNotBlank()) {
                        Text(
                            text = "Exp: ${record.expediente}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueAccent
                        )
                    }
                    if (record.folioCurp.isNotBlank()) {
                        Text(
                            text = "Folio: ${record.folioCurp}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // IMSS & ISSSTE Non-Affiliation Badges
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isImssNoAfiliado = record.noAfiliacionImss == "NO_AFILIADO"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isImssNoAfiliado) EmeraldGreen.copy(alpha = 0.12f) else CrimsonDanger.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isImssNoAfiliado) "✔ IMSS No Afiliado" else "❌ IMSS Afiliado Detectado",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isImssNoAfiliado) EmeraldGreen else CrimsonDanger
                    )
                }

                val isIsssteNoAfiliado = record.noAfiliacionIssste == "NO_AFILIADO"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isIsssteNoAfiliado) EmeraldGreen.copy(alpha = 0.12f) else CrimsonDanger.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isIsssteNoAfiliado) "✔ ISSSTE No Afiliado" else "❌ ISSSTE Afiliado Detectado",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIsssteNoAfiliado) EmeraldGreen else CrimsonDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Procedimiento
            if (record.cirugiaRealizada.isNotBlank()) {
                Box(
                    modifier = if (matchesProcedure) {
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BlueAccent.copy(alpha = 0.12f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    } else Modifier
                ) {
                    Text(
                        text = "Cx: ${record.cirugiaRealizada}" + if (matchesProcedure) " ✔" else "",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueAccent
                    )
                }
            }

            // Diagnóstico
            if (record.diagnostico.isNotBlank()) {
                Box(
                    modifier = if (matchesDiagnosis) {
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .border(0.5.dp, EmeraldGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    } else Modifier
                ) {
                    Text(
                        text = "Dx: ${record.diagnostico}" + if (matchesDiagnosis) " [Coincidencia Diagnóstico]" else "",
                        fontSize = 11.5.sp,
                        fontWeight = if (matchesDiagnosis) FontWeight.Bold else FontWeight.Normal,
                        color = if (matchesDiagnosis) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Médico Tratante e Instrumentista
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.medicoTratante.isNotBlank()) {
                    Box(
                        modifier = if (matchesDoctor) {
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BlueAccent.copy(alpha = 0.15f))
                                .border(0.5.dp, BlueAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        } else Modifier
                    ) {
                        Text(
                            text = "Cirujano: ${record.medicoTratante}" + if (matchesDoctor) " [Coincidencia Médico]" else "",
                            fontSize = 11.sp,
                            fontWeight = if (matchesDoctor) FontWeight.Bold else FontWeight.Normal,
                            color = if (matchesDoctor) BlueAccent else Color.DarkGray
                        )
                    }
                }
                if (record.enfermeraInstrumentista.isNotBlank()) {
                    Text(
                        text = "Inst: ${record.enfermeraInstrumentista}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Times summary strip with Concrete Real-Time Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("CRONOMETRADO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        text = "${record.tInicio.ifBlank { "--" }} a ${record.tFin.ifBlank { "--" }} ($durationText)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueAccent
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("ADSCRITO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    if (record.adsNoEntro) {
                        Text(
                            text = "NO INGRESÓ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonDanger
                        )
                    } else {
                        Text(
                            text = "${record.adsLlegada.ifBlank { "--" }} a ${record.adsSalida.ifBlank { "--" }}" + if (record.adsEstanciaMin > 0) " (${record.adsEstanciaMin}m)" else "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            // Evidence indicator badge
            if (!record.fotoHojaQuirurgicaUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hoja quirúrgica con firma de instrumentista adjunta",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldGreen
                    )
                }
            }

            if (record.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Obs: ${record.observaciones}",
                    fontSize = 11.sp,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSharePdf,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1.2f).height(38.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EDITAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BORRAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
