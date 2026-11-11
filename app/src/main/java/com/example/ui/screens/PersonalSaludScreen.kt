package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonalSalud
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealClinical
import com.example.ui.viewmodel.AuditViewModel

@Composable
fun PersonalSaludScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val personalList by viewModel.allPersonalSalud.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRolFilter by remember { mutableStateOf("TODOS") } // "TODOS", "MÉDICO", "ENFERMERO"

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PersonalSalud?>(null) }
    var itemToDelete by remember { mutableStateOf<PersonalSalud?>(null) }

    // Dialog state fields
    var inputNombre by remember { mutableStateOf("") }
    var inputRol by remember { mutableStateOf("MÉDICO") }
    var inputEspecialidad by remember { mutableStateOf("") }
    var inputCedula by remember { mutableStateOf("") }

    val filteredList = personalList.filter { item ->
        val matchesRol = when (selectedRolFilter) {
            "MÉDICO" -> item.rol.equals("MÉDICO", ignoreCase = true)
            "ENFERMERO" -> item.rol.equals("ENFERMERO", ignoreCase = true)
            else -> true
        }

        val q = searchQuery.trim()
        val matchesQuery = if (q.isBlank()) true else {
            item.nombre.contains(q, ignoreCase = true) ||
            item.especialidad.contains(q, ignoreCase = true) ||
            item.cedula.contains(q, ignoreCase = true) ||
            item.rol.contains(q, ignoreCase = true)
        }

        matchesRol && matchesQuery
    }

    fun openCreateDialog() {
        editingItem = null
        inputNombre = ""
        inputRol = "MÉDICO"
        inputEspecialidad = ""
        inputCedula = ""
        showAddEditDialog = true
    }

    fun openEditDialog(item: PersonalSalud) {
        editingItem = item
        inputNombre = item.nombre
        inputRol = item.rol
        inputEspecialidad = item.especialidad
        inputCedula = item.cedula
        showAddEditDialog = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- HEADER BANNER WITH NEW REGISTRATION BUTTON ---
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CATÁLOGO DE PERSONAL DE SALUD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Médicos Cirujanos e Instrumentistas (Room DB)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { openCreateDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_nuevo_personal")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REGISTRAR", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- SEARCH BAR & ROLE FILTERS ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar médico, enfermero, especialidad o cédula...", fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BlueAccent) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_personal_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtrar por Rol:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            listOf("TODOS", "MÉDICO", "ENFERMERO").forEach { rol ->
                val isSelected = selectedRolFilter == rol
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedRolFilter = rol },
                    label = { Text(rol, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (rol == "ENFERMERO") Icons.Default.Badge else Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (rol == "ENFERMERO") EmeraldGreen else NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // --- STAFF LISTING (ROOM REPOSITORY FLOW) ---
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (personalList.isEmpty()) {
                            "Aún no hay personal registrado en el catálogo de Room DB."
                        } else {
                            "No se encontraron registros para \"$searchQuery\"."
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { openCreateDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("Registrar Primer Personal")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    PersonalItemCard(
                        personal = item,
                        onEdit = { openEditDialog(item) },
                        onDelete = { itemToDelete = item }
                    )
                }
            }
        }
    }

    // --- CREATE / EDIT DIALOG ---
    if (showAddEditDialog) {
        val isEditing = editingItem != null
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            icon = {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Person,
                    contentDescription = null,
                    tint = BlueAccent
                )
            },
            title = {
                Text(
                    text = if (isEditing) "✏️ Editar Personal de Salud" else "➕ Registrar Personal de Salud",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Role Selector: Médico vs Enfermero
                    Text(
                        text = "ROL PROFESIONAL:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = inputRol == "MÉDICO",
                            onClick = { inputRol = "MÉDICO" },
                            label = { Text("👨‍⚕️ Médico Cirujano", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlueAccent,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = inputRol == "ENFERMERO",
                            onClick = { inputRol = "ENFERMERO" },
                            label = { Text("🩺 Instrumentista", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Full Name Input
                    OutlinedTextField(
                        value = inputNombre,
                        onValueChange = { inputNombre = it },
                        label = { Text("Nombre Completo con Título") },
                        placeholder = { Text(if (inputRol == "MÉDICO") "Ej. Dr. Roberto Gómez" else "Ej. L.E. Patricia Soto") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_personal_nombre")
                    )

                    // Specialty Input
                    OutlinedTextField(
                        value = inputEspecialidad,
                        onValueChange = { inputEspecialidad = it },
                        label = { Text("Especialidad / Unidad") },
                        placeholder = { Text("Ej. Cirugía General / Laparoscopia") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_personal_especialidad")
                    )

                    // Professional License / Cédula
                    OutlinedTextField(
                        value = inputCedula,
                        onValueChange = { inputCedula = it },
                        label = { Text("Cédula Profesional (Opcional)") },
                        placeholder = { Text("Ej. 8492015") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_personal_cedula")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputNombre.isNotBlank()) {
                            if (isEditing) {
                                viewModel.updatePersonalSalud(
                                    editingItem!!.copy(
                                        nombre = inputNombre.trim(),
                                        rol = inputRol,
                                        especialidad = inputEspecialidad.trim(),
                                        cedula = inputCedula.trim()
                                    )
                                )
                            } else {
                                viewModel.addPersonalSalud(
                                    nombre = inputNombre,
                                    rol = inputRol,
                                    especialidad = inputEspecialidad,
                                    cedula = inputCedula
                                )
                            }
                            showAddEditDialog = false
                        } else {
                            viewModel.showToast("Ingrese el nombre completo")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text(if (isEditing) "Actualizar" else "Guardar en Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    if (itemToDelete != null) {
        val target = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonDanger) },
            title = { Text("🗑️ Eliminar Registro de Catálogo", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Está seguro de eliminar a ${target.rol} ${target.nombre} del catálogo de Room DB?\n\nEsta acción quitará al elemento del selector rápido de cirugías.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePersonalSalud(target)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PersonalItemCard(
    personal: PersonalSalud,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isMedico = personal.rol.equals("MÉDICO", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isMedico) BlueAccent.copy(alpha = 0.12f) else EmeraldGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMedico) Icons.Default.Person else Icons.Default.Badge,
                        contentDescription = null,
                        tint = if (isMedico) BlueAccent else EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = personal.nombre,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isMedico) BlueAccent else EmeraldGreen)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = personal.rol.uppercase(),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (personal.especialidad.isNotBlank() || personal.cedula.isNotBlank()) {
                        Text(
                            text = listOfNotNull(
                                personal.especialidad.takeIf { it.isNotBlank() },
                                personal.cedula.takeIf { it.isNotBlank() }?.let { "Céd: $it" }
                            ).joinToString(" • "),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = AmberWarning, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = CrimsonDanger, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
