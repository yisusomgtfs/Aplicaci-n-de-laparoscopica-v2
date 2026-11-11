package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AuditViewModel
import com.example.util.TimeHelper
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun AjustesScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val profile by viewModel.profile.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isNightLight by viewModel.isNightLight.collectAsState()
    val alertsEnabled by viewModel.alertsEnabled.collectAsState()
    val alertIntervalMin by viewModel.alertIntervalMin.collectAsState()
    val alertVibration by viewModel.alertVibration.collectAsState()

    val doctorDirectory by viewModel.doctorDirectory.collectAsState()
    val nurseDirectory by viewModel.nurseDirectory.collectAsState()

    var editNombre by remember(profile.nombre) { mutableStateOf(profile.nombre) }
    var editCargo by remember(profile.cargo) { mutableStateOf(profile.cargo) }
    var photoUri by remember(profile.fotoUri) { mutableStateOf(profile.fotoUri) }

    var newDoctorName by remember { mutableStateOf("") }
    var newNurseName by remember { mutableStateOf("") }

    var nuevoPin by remember { mutableStateOf("") }
    var nuevaPalabra by remember { mutableStateOf("") }

    var showFormatDbDialog by remember { mutableStateOf(false) }
    var lastBackupPath by remember { mutableStateOf<String?>(null) }

    // Avatar photo picker
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
            viewModel.saveProfile(editNombre, editCargo, uri.toString())
        }
    }

    // JSON backup import picker
    val jsonImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val content = reader.readText()
                    viewModel.importJsonBackup(content)
                }
            } catch (e: Exception) {
                viewModel.showToast("Error importando archivo: ${e.message}")
            }
        }
    }

    // SAF Document Creator for saving JSON backup to user-selected external storage
    val createBackupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportBackupToUri(context, uri)
            lastBackupPath = uri.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Perfil de Autor
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "👤 Mi Perfil de Autor",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBDC3C7))
                            .border(3.dp, BlueAccent, CircleShape)
                            .clickable {
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto Perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Subir Foto",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = editNombre,
                            onValueChange = { editNombre = it },
                            label = { Text("Nombre Completo") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editCargo,
                            onValueChange = { editCargo = it },
                            label = { Text("Cargo / Puesto") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("profile_role_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.saveProfile(editNombre, editCargo, photoUri) },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_profile_button")
                ) {
                    Text("Guardar Datos de Perfil", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Card: Directorio de Personal Quirúrgico (Médicos y Enfermeros) - Room DB
        var showFullRoomCatalogDialog by remember { mutableStateOf(false) }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("card_staff_directory_settings")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BlueAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Directorio de Personal Quirúrgico (Room DB)",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Base de datos local de Médicos Cirujanos y Enfermeros Instrumentistas",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showFullRoomCatalogDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_open_catalog_room")
                    ) {
                        Text("Catálogo CRUD", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section: Médicos Adscritos / Cirujanos
                Text(
                    text = "MÉDICOS ADSCRITOS Y CIRUJANOS REGISTRADOS:",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueAccent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    doctorDirectory.forEach { docName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(docName, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { viewModel.removeDoctorFromDirectory(docName) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = CrimsonDanger, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newDoctorName,
                        onValueChange = { newDoctorName = it },
                        label = { Text("Registrar Nuevo Cirujano / Adscrito") },
                        placeholder = { Text("Ej. Dr. Roberto Gómez") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("add_doctor_input")
                    )

                    Button(
                        onClick = {
                            if (newDoctorName.isNotBlank()) {
                                viewModel.addPersonalSalud(nombre = newDoctorName, rol = "MÉDICO")
                                newDoctorName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_add_doctor")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AGREGAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Enfermeros Instrumentistas
                Text(
                    text = "ENFERMEROS INSTRUMENTISTAS REGISTRADOS:",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    nurseDirectory.forEach { nurseName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(nurseName, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { viewModel.removeNurseFromDirectory(nurseName) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = CrimsonDanger, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newNurseName,
                        onValueChange = { newNurseName = it },
                        label = { Text("Registrar Instrumentista") },
                        placeholder = { Text("Ej. L.E. Patricia Soto") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("add_nurse_input")
                    )

                    Button(
                        onClick = {
                            if (newNurseName.isNotBlank()) {
                                viewModel.addPersonalSalud(nombre = newNurseName, rol = "ENFERMERO")
                                newNurseName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_add_nurse")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AGREGAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Full Room Catalog Modal Dialog
        if (showFullRoomCatalogDialog) {
            AlertDialog(
                onDismissRequest = { showFullRoomCatalogDialog = false },
                title = { Text("Gestión Completa de Personal (Room DB)", fontWeight = FontWeight.Bold) },
                text = {
                    Box(modifier = Modifier.size(width = 380.dp, height = 480.dp)) {
                        PersonalSaludScreen(viewModel = viewModel)
                    }
                },
                confirmButton = {
                    Button(onClick = { showFullRoomCatalogDialog = false }) {
                        Text("Cerrar Catálogo")
                    }
                }
            )
        }

        // Card 2: Personalización Visual
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎨 Personalización Visual",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dark mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Nightlight, contentDescription = null, tint = BlueAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Modo Oscuro", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                            Text("Reduce brillo de pantalla", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BlueAccent)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Night light filter toggle (amber overlay)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbIncandescent, contentDescription = null, tint = AmberWarning)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Luz Nocturna (Filtro Ámbar)", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                            Text("Ideal para quirófanos oscuros de laparoscopia", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = isNightLight,
                        onCheckedChange = { viewModel.toggleNightLight(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AmberWarning)
                    )
                }
            }
        }

        // Card 3: Monitoreo y Alertas en Cirugía Activa (Segundo Plano)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("card_surgical_alerts_settings")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BlueAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Alertas en Cirugía Activa",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Monitoreo en segundo plano y pantalla bloqueada",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = alertsEnabled,
                        onCheckedChange = { viewModel.setAlertsEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BlueAccent)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Alert interval selector
                Text(
                    text = "INTERVALO DE AVISO DE TIEMPO:",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                val intervals = listOf(15, 30, 45, 60, 90)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    intervals.forEach { mins ->
                        FilterChip(
                            selected = alertIntervalMin == mins,
                            onClick = { viewModel.setAlertIntervalMin(mins) },
                            label = { Text("${mins}m", fontWeight = FontWeight.Bold, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlueAccent,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Vibration toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Vibración en Quirófano", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Patrón táctil perceptible con guantes quirúrgicos", fontSize = 10.5.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = alertVibration,
                        onCheckedChange = { viewModel.setAlertVibration(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test notification button
                OutlinedButton(
                    onClick = { viewModel.triggerTestNotification(context) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PROBAR ALERTA DE TIEMPO AHORA", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Card 4: Seguridad y Acceso Cifrado
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔒 Seguridad y Acceso Cifrado (SHA-256)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberWarning
                )

                Spacer(modifier = Modifier.height(12.dp))

                // New PIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevoPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) nuevoPin = it },
                        label = { Text("Nuevo PIN") },
                        placeholder = { Text("4 dígitos") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f).testTag("new_pin_settings_input")
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                if (nuevoPin.length == 4) {
                                    viewModel.resetPin(nuevoPin)
                                    nuevoPin = ""
                                    viewModel.showToast("✔ PIN encriptado y guardado")
                                } else {
                                    viewModel.showToast("❌ El PIN debe tener 4 dígitos")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // New Recovery Word
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevaPalabra,
                        onValueChange = { nuevaPalabra = it },
                        label = { Text("Palabra Secreta") },
                        placeholder = { Text("Recuperación") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f).testTag("recovery_word_settings_input")
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                if (nuevaPalabra.length >= 3) {
                                    viewModel.saveNewRecoveryWord(nuevaPalabra)
                                    nuevaPalabra = ""
                                    viewModel.showToast("✔ Palabra guardada")
                                } else {
                                    viewModel.showToast("❌ La palabra es muy corta")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card 4: Respaldo y Redundancia Extra en Almacenamiento Externo
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("backup_redundancy_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF8E44AD))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💾 Respaldo en Almacenamiento Externo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E44AD)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Genera una copia manual de la base de datos Room a un archivo JSON en el almacenamiento externo del dispositivo para redundancia y prevención de pérdida de datos forenses.",
                    fontSize = 11.5.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action: 1-Tap Manual External Storage Backup
                Button(
                    onClick = {
                        viewModel.triggerManualExternalBackup(context) { file ->
                            lastBackupPath = file.absolutePath
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad Base de Datos Room HGV")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir/Guardar Respaldo JSON"))
                            } catch (e: Exception) {
                                viewModel.showToast("Respaldo creado en: ${file.name}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E44AD)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("manual_backup_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESPALDO MANUAL A ARCHIVO (.JSON)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action: Choose custom external location via SAF (SD Card / Downloads / USB Drive)
                OutlinedButton(
                    onClick = {
                        createBackupFileLauncher.launch("RESPALDO_ROOM_HGV_${TimeHelper.getTodayDate()}.json")
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("choose_storage_backup_button")
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF8E44AD))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ELEGIR DESTINO EXTERNO (SAF / TARJETA SD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E44AD))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Import / Restore Button
                Button(
                    onClick = {
                        jsonImportLauncher.launch(arrayOf("application/json", "text/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("import_backup_button")
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESTAURAR BASE DE DATOS (.JSON)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                // Last backup indicator
                if (lastBackupPath != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldGreen.copy(alpha = 0.12f))
                            .border(0.5.dp, EmeraldGreen, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "✔ Respaldo generado en almacenamiento:\n$lastBackupPath",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }

        // Card 5: Zona de Peligro & Bloqueo
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ Zona de Control y Seguridad",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonDanger
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.lockApp() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔒 BLOQUEAR SESIÓN AHORA", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showFormatDbDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🗑️ FORMATEAR BASE DE DATOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showFormatDbDialog) {
        AlertDialog(
            onDismissRequest = { showFormatDbDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonDanger) },
            title = { Text("⚠️ ALERTA DE DESTRUCCIÓN TOTAL", fontWeight = FontWeight.Bold, color = CrimsonDanger) },
            text = {
                Text("Está a punto de borrar TODA la base de datos de auditoría laparoscópica.\n\nEsta acción es irreversible y eliminará todas las evidencias de cirugías registradas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllRecords()
                        showFormatDbDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                ) {
                    Text("Sí, Formatear Todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFormatDbDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
