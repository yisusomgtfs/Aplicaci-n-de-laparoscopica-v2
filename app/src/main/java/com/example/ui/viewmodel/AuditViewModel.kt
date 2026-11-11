package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuditDraft
import com.example.data.model.AuditRecord
import com.example.data.model.LaparoscopicProcedure
import com.example.data.model.PersonalSalud
import com.example.util.LaparoscopicCatalogHelper
import com.example.data.preferences.SecurityUtils
import com.example.data.preferences.UserProfile
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuditRepository
import com.example.util.BackupHelper
import com.example.util.CsvHelper
import com.example.util.CurpHelper
import com.example.util.NotificationHelper
import com.example.util.PdfGenerator
import com.example.util.TimeHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class SplitTimeItem(
    val id: String = UUID.randomUUID().toString(),
    val timeLabel: String = "",
    val elapsedHms: String = "",
    val note: String = ""
)

data class FormUiState(
    val editingRecordId: String? = null,
    val fecha: String = TimeHelper.getTodayDate(),
    val expediente: String = "",          // Número de Expediente Clínico HGV
    val folioCurp: String = "",           // Folio Autogenerado derivado de CURP
    val paciente: String = "",
    val curp: String = "",
    val edad: String = "",
    val fotoCurpUri: String? = null,
    val noAfiliacionImss: String = "NO_AFILIADO",   // "NO_AFILIADO", "AFILIADO_DETECTADO", "PENDIENTE"
    val noAfiliacionIssste: String = "NO_AFILIADO", // "NO_AFILIADO", "AFILIADO_DETECTADO", "PENDIENTE"
    val tipoCirugia: String = "Electiva",           // "Electiva" o "Urgencia"
    val diagnostico: String = "",
    val cirugiaRealizada: String = "",
    val medicoTratante: String = "",
    val enfermeraInstrumentista: String = "",
    val fotoHojaQuirurgicaUri: String? = null,
    val tListo: String = "",
    val tInicio: String = "",
    val tFin: String = "",
    val inicioMillis: Long? = null,
    val finMillis: Long? = null,
    val duracionSegundos: Long = 0L,
    val isTimerRunning: Boolean = false,
    val liveElapsedSeconds: Long = 0L,
    val splitTimes: List<SplitTimeItem> = emptyList(),
    val adsLlegada: String = "",
    val adsSalida: String = "",
    val adsEstanciaMin: Int = 0,
    val adsNoEntro: Boolean = false,
    val causa: String = "Sin Desviación",
    val observaciones: String = "",
    val nombreTestigo: String = "",
    val firmaTecnicoBase64: String? = null,
    val firmaTestigoBase64: String? = null
)

class AuditViewModel(
    private val repository: AuditRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val appContext: Context? = null
) : ViewModel() {

    val allRecords: StateFlow<List<AuditRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<UserProfile> = preferencesRepository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val isDarkMode: StateFlow<Boolean> = preferencesRepository.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isNightLight: StateFlow<Boolean> = preferencesRepository.nightLightFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Notification alert preferences
    val alertsEnabled: StateFlow<Boolean> = preferencesRepository.alertsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val alertIntervalMin: StateFlow<Int> = preferencesRepository.alertIntervalMinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val alertVibration: StateFlow<Boolean> = preferencesRepository.alertVibrationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // PersonalSalud (Room Database Catalog)
    val allPersonalSalud: StateFlow<List<PersonalSalud>> = repository.allPersonal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Staff Directories (Room DB + DataStore)
    val doctorDirectory: StateFlow<List<String>> = combine(
        preferencesRepository.doctorDirectoryFlow,
        repository.allDoctors
    ) { dsDocs, roomDocs ->
        (roomDocs.map { it.nombre } + dsDocs).distinct().filter { it.isNotBlank() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nurseDirectory: StateFlow<List<String>> = combine(
        preferencesRepository.nurseDirectoryFlow,
        repository.allNurses
    ) { dsNurses, roomNurses ->
        (roomNurses.map { it.nombre } + dsNurses).distinct().filter { it.isNotBlank() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _laparoscopicProcedures = MutableStateFlow<List<LaparoscopicProcedure>>(LaparoscopicCatalogHelper.DEFAULT_LAPAROSCOPIC_PROCEDURES)
    val laparoscopicProcedures: StateFlow<List<LaparoscopicProcedure>> = _laparoscopicProcedures.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isRecordsUnlocked = MutableStateFlow(false)
    val isRecordsUnlocked: StateFlow<Boolean> = _isRecordsUnlocked.asStateFlow()

    private val _formState = MutableStateFlow(FormUiState())
    val formState: StateFlow<FormUiState> = _formState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private var chronometerJob: Job? = null
    private val alertedMilestones = mutableSetOf<Int>()

    init {
        loadDraft()
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }

    // --- Authentication ---
    suspend fun verifyPin(pin: String): Boolean {
        val currentHash = preferencesRepository.pinHashFlow.first()
        val inputHash = SecurityUtils.hashSha256(pin)
        val valid = inputHash == currentHash
        if (valid) {
            _isUnlocked.value = true
        }
        return valid
    }

    fun unlockByBiometrics() {
        _isUnlocked.value = true
    }

    fun unlockRecordsByBiometrics() {
        _isRecordsUnlocked.value = true
    }

    suspend fun unlockRecordsByPin(pin: String): Boolean {
        val currentHash = preferencesRepository.pinHashFlow.first()
        val inputHash = SecurityUtils.hashSha256(pin)
        val valid = inputHash == currentHash
        if (valid) {
            _isRecordsUnlocked.value = true
        }
        return valid
    }

    fun lockRecords() {
        _isRecordsUnlocked.value = false
    }

    fun lockApp() {
        _isUnlocked.value = false
        _isRecordsUnlocked.value = false
    }

    suspend fun verifyRecoveryWord(word: String): Boolean {
        val currentHash = preferencesRepository.recoveryHashFlow.first()
        val inputHash = SecurityUtils.hashSha256(word)
        return inputHash == currentHash
    }

    suspend fun resetPin(newPin: String): Boolean {
        if (newPin.length < 4) return false
        preferencesRepository.setPin(newPin)
        return true
    }

    suspend fun saveNewRecoveryWord(word: String): Boolean {
        if (word.length < 3) return false
        preferencesRepository.setRecoveryWord(word)
        return true
    }

    // --- Profile, Preferences & Staff Directory ---
    fun saveProfile(nombre: String, cargo: String, fotoUri: String?) {
        viewModelScope.launch {
            preferencesRepository.saveProfile(nombre, cargo, fotoUri)
            showToast("Perfil de autor guardado")
        }
    }

    fun addDoctorToDirectory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            preferencesRepository.addDoctor(name.trim())
            showToast("✔ Doctor ${name.trim()} registrado en directorio")
        }
    }

    fun removeDoctorFromDirectory(name: String) {
        viewModelScope.launch {
            preferencesRepository.removeDoctor(name)
            showToast("Doctor $name eliminado del directorio")
        }
    }

    fun addNurseToDirectory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            preferencesRepository.addNurse(name.trim())
            showToast("✔ Instrumentista ${name.trim()} registrada en directorio")
        }
    }

    fun removeNurseFromDirectory(name: String) {
        viewModelScope.launch {
            preferencesRepository.removeNurse(name)
            showToast("Instrumentista $name eliminada del directorio")
        }
    }

    // --- PersonalSalud Room CRUD ---
    fun addPersonalSalud(nombre: String, rol: String, especialidad: String = "", cedula: String = "") {
        if (nombre.isBlank()) return
        val upperRol = if (rol.uppercase().contains("ENFERM")) "ENFERMERO" else "MÉDICO"
        val item = PersonalSalud(
            nombre = nombre.trim(),
            rol = upperRol,
            especialidad = especialidad.trim(),
            cedula = cedula.trim()
        )
        viewModelScope.launch {
            repository.insertPersonal(item)
            if (upperRol == "MÉDICO") {
                preferencesRepository.addDoctor(item.nombre)
            } else {
                preferencesRepository.addNurse(item.nombre)
            }
            showToast("✔ $upperRol ${item.nombre} guardado en la base de datos Room")
        }
    }

    fun updatePersonalSalud(personal: PersonalSalud) {
        viewModelScope.launch {
            repository.updatePersonal(personal)
            showToast("✔ Registro de ${personal.nombre} actualizado en Room")
        }
    }

    fun deletePersonalSalud(personal: PersonalSalud) {
        viewModelScope.launch {
            repository.deletePersonal(personal)
            preferencesRepository.removeDoctor(personal.nombre)
            preferencesRepository.removeNurse(personal.nombre)
            showToast("Registro de ${personal.nombre} eliminado")
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDarkMode(enabled) }
    }

    fun toggleNightLight(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setNightLight(enabled) }
    }

    fun setAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAlertsEnabled(enabled)
            showToast(if (enabled) "Notificaciones de alerta quirúrgica activadas" else "Alertas de tiempo desactivadas")
        }
    }

    fun setAlertIntervalMin(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setAlertIntervalMin(minutes)
            showToast("Intervalo de alerta establecido en $minutes min")
        }
    }

    fun setAlertVibration(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAlertVibration(enabled)
        }
    }

    fun triggerTestNotification(context: Context) {
        NotificationHelper.triggerMilestoneAlert(
            context = context,
            elapsedMinutes = alertIntervalMin.value,
            paciente = _formState.value.paciente.ifBlank { "Paciente de Monitoreo" },
            cirugiaRealizada = _formState.value.cirugiaRealizada.ifBlank { "Colecistectomía Lap." },
            medicoTratante = _formState.value.medicoTratante.ifBlank { "Dr. Morales" },
            isVibrationEnabled = alertVibration.value
        )
        showToast("🔔 Alerta de prueba enviada a notificaciones")
    }

    // --- Form Clinical Mutations ---
    fun updateExpediente(value: String) {
        _formState.update { it.copy(expediente = value) }
        autoSaveDraft()
    }

    fun updateFolioCurp(value: String) {
        _formState.update { it.copy(folioCurp = value) }
        autoSaveDraft()
    }

    fun updatePaciente(value: String) {
        _formState.update { it.copy(paciente = value) }
        autoSaveDraft()
    }

    fun updateCurp(value: String) {
        val upper = value.uppercase()
        val age = CurpHelper.calculateAgeFromCurp(upper)
        val generatedFolio = CurpHelper.generateFolioFromCurp(upper)

        _formState.update { current ->
            current.copy(
                curp = upper,
                edad = if (age != null) age.toString() else current.edad,
                folioCurp = if (current.folioCurp.isBlank() || current.folioCurp.startsWith("FOL-")) generatedFolio else current.folioCurp
            )
        }
        autoSaveDraft()
    }

    fun updateEdad(value: String) {
        _formState.update { it.copy(edad = value) }
        autoSaveDraft()
    }

    fun updateFotoCurpUri(uri: String?) {
        _formState.update { it.copy(fotoCurpUri = uri) }
        autoSaveDraft()
    }

    fun updateNoAfiliacionImss(status: String) {
        _formState.update { it.copy(noAfiliacionImss = status) }
        if (status == "AFILIADO_DETECTADO") {
            showToast("⚠️ ALERTA: Paciente con afiliación activa detectada en IMSS")
        } else if (status == "NO_AFILIADO") {
            showToast("✔ Certificado: No afiliado a IMSS (Población Abierta HGV)")
        }
        autoSaveDraft()
    }

    fun updateNoAfiliacionIssste(status: String) {
        _formState.update { it.copy(noAfiliacionIssste = status) }
        if (status == "AFILIADO_DETECTADO") {
            showToast("⚠️ ALERTA: Paciente con afiliación activa detectada en ISSSTE")
        } else if (status == "NO_AFILIADO") {
            showToast("✔ Certificado: No afiliado a ISSSTE (Población Abierta HGV)")
        }
        autoSaveDraft()
    }

    fun updateTipoCirugia(tipo: String) {
        _formState.update { it.copy(tipoCirugia = tipo) }
        autoSaveDraft()
    }

    fun updateDiagnostico(value: String) {
        _formState.update { it.copy(diagnostico = value) }
        autoSaveDraft()
    }

    fun updateCirugiaRealizada(value: String) {
        _formState.update { it.copy(cirugiaRealizada = value) }
        autoSaveDraft()
    }

    fun updateMedicoTratante(value: String) {
        _formState.update { it.copy(medicoTratante = value) }
        autoSaveDraft()
    }

    fun updateEnfermeraInstrumentista(value: String) {
        _formState.update { it.copy(enfermeraInstrumentista = value) }
        autoSaveDraft()
    }

    fun updateFotoHojaQuirurgicaUri(uri: String?) {
        _formState.update { it.copy(fotoHojaQuirurgicaUri = uri) }
        autoSaveDraft()
    }

    // --- Laparoscopic Procedure Selection & Excel/CSV Catalog Import ---
    fun selectLaparoscopicProcedure(proc: LaparoscopicProcedure) {
        val currentObs = _formState.value.observaciones
        val header = "📋 TÉCNICA QUIRÚRGICA: ${proc.nombre}"
        val formattedDesc = if (proc.descripcionQuirurgica.isNotBlank()) "$header\n${proc.descripcionQuirurgica}" else ""
        
        val newObs = if (formattedDesc.isBlank()) {
            currentObs
        } else if (currentObs.isBlank()) {
            formattedDesc
        } else if (currentObs.contains(proc.nombre)) {
            currentObs
        } else {
            "$currentObs\n\n$formattedDesc"
        }

        _formState.update { current ->
            current.copy(
                cirugiaRealizada = proc.nombre,
                diagnostico = if (current.diagnostico.isBlank() && proc.diagnosticoDefecto.isNotBlank()) proc.diagnosticoDefecto else current.diagnostico,
                observaciones = newObs
            )
        }
        autoSaveDraft()
        showToast("✔ Procedimiento seleccionado: ${proc.nombre}")
    }

    fun importLaparoscopicCatalogFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val imported = LaparoscopicCatalogHelper.parseCatalogFromUri(context, uri)
            if (imported.isNotEmpty()) {
                val combined = (imported + _laparoscopicProcedures.value).distinctBy { it.nombre.lowercase().trim() }
                _laparoscopicProcedures.value = combined
                showToast("✔ Se importaron ${imported.size} procedimientos laparoscópicos desde el archivo.")
            } else {
                showToast("⚠️ No se encontraron procedimientos válidos en el archivo Excel/CSV.")
            }
        }
    }

    // --- Search & Auto-Fill Patient by Expediente / Folio / CURP ---
    fun autoFillPatientFromExpedienteOrCurp(query: String) {
        val q = query.trim()
        if (q.isBlank()) return

        viewModelScope.launch {
            val record = repository.allRecords.first().find {
                it.expediente.equals(q, ignoreCase = true) ||
                it.folioCurp.equals(q, ignoreCase = true) ||
                it.curp.equals(q, ignoreCase = true) ||
                it.paciente.contains(q, ignoreCase = true)
            }

            if (record != null) {
                _formState.update { current ->
                    current.copy(
                        expediente = record.expediente,
                        folioCurp = record.folioCurp,
                        paciente = record.paciente,
                        curp = record.curp,
                        edad = record.edad,
                        noAfiliacionImss = record.noAfiliacionImss,
                        noAfiliacionIssste = record.noAfiliacionIssste,
                        medicoTratante = if (current.medicoTratante.isBlank()) record.medicoTratante else current.medicoTratante,
                        enfermeraInstrumentista = if (current.enfermeraInstrumentista.isBlank()) record.enfermeraInstrumentista else current.enfermeraInstrumentista
                    )
                }
                showToast("✔ Datos cargados de antecedente: ${record.paciente}")
            } else {
                showToast("No se encontraron antecedentes para: $q")
            }
        }
    }

    // --- Chronometer & Timestamps Engine ---
    fun stampListo(): Boolean {
        if (!TimeHelper.isHardwareClockValid()) {
            showToast("❌ ALERTA: Reloj desincronizado. Verifique año >= 2026.")
            return false
        }
        if (_formState.value.tListo.isNotBlank()) {
            showToast("Sello de tiempo bloqueado.")
            return false
        }
        val now = TimeHelper.getCurrentTime()
        _formState.update { it.copy(tListo = now) }
        autoSaveDraft()
        showToast("Técnico Listo marcado a las $now")
        return true
    }

    fun stampInicio(): Boolean {
        if (!TimeHelper.isHardwareClockValid()) {
            showToast("❌ ALERTA: Reloj desincronizado.")
            return false
        }
        if (_formState.value.tListo.isBlank()) {
            showToast("❌ Marque primero 'Técnico Listo'")
            return false
        }
        if (_formState.value.tInicio.isNotBlank()) {
            showToast("Sello de Inicio bloqueado.")
            return false
        }
        val nowTimeStr = TimeHelper.getCurrentTime()
        val nowMillis = System.currentTimeMillis()

        _formState.update {
            it.copy(
                tInicio = nowTimeStr,
                inicioMillis = nowMillis,
                isTimerRunning = true,
                liveElapsedSeconds = 0L
            )
        }
        startLiveChronometer(nowMillis)
        autoSaveDraft()
        showToast("Inicio de Cirugía marcado a las $nowTimeStr • Cronómetro activo")
        return true
    }

    fun stampFin(): Boolean {
        if (!TimeHelper.isHardwareClockValid()) {
            showToast("❌ ALERTA: Reloj desincronizado.")
            return false
        }
        if (_formState.value.tInicio.isBlank()) {
            showToast("❌ Marque primero 'Inicio Cirugía'")
            return false
        }
        if (_formState.value.tFin.isNotBlank()) {
            showToast("Sello de Fin bloqueado.")
            return false
        }
        val nowTimeStr = TimeHelper.getCurrentTime()
        val nowMillis = System.currentTimeMillis()
        val startMillis = _formState.value.inicioMillis ?: nowMillis
        val durationSec = ((nowMillis - startMillis) / 1000).coerceAtLeast(0)

        stopLiveChronometer()

        _formState.update {
            it.copy(
                tFin = nowTimeStr,
                finMillis = nowMillis,
                duracionSegundos = durationSec,
                liveElapsedSeconds = durationSec,
                isTimerRunning = false
            )
        }

        // Post completion notification and cancel ongoing chronometer notification
        appContext?.let { ctx ->
            NotificationHelper.cancelOngoingSurgeryNotification(ctx)
            NotificationHelper.postSurgeryCompletedNotification(
                context = ctx,
                totalDurationSeconds = durationSec,
                paciente = _formState.value.paciente,
                cirugiaRealizada = _formState.value.cirugiaRealizada
            )
        }

        autoSaveDraft()
        showToast("Fin de Cirugía marcado a las $nowTimeStr • Duración: ${TimeHelper.formatDurationHuman(durationSec)}")
        return true
    }

    fun addSplitTime(note: String = "") {
        val s = _formState.value
        val elapsed = if (s.isTimerRunning || s.liveElapsedSeconds > 0) s.liveElapsedSeconds else 0L
        val formattedHms = TimeHelper.formatDurationHms(elapsed)
        val nowClock = TimeHelper.getCurrentTime()
        val label = if (note.isNotBlank()) note.trim() else "Hito Intraoperatorio"
        val newItem = SplitTimeItem(
            timeLabel = label,
            elapsedHms = formattedHms,
            note = "@ $nowClock"
        )
        val updatedSplits = s.splitTimes + newItem
        val logEntry = "⏱️ Parcial [+$formattedHms @ $nowClock]: $label"
        val updatedObs = if (s.observaciones.isBlank()) logEntry else "${s.observaciones}\n$logEntry"

        _formState.update {
            it.copy(
                splitTimes = updatedSplits,
                observaciones = updatedObs
            )
        }
        autoSaveDraft()
        showToast("⏱️ Parcial registrado: $label ($formattedHms)")
    }

    fun removeSplitTime(id: String) {
        _formState.update { current ->
            current.copy(splitTimes = current.splitTimes.filter { it.id != id })
        }
        autoSaveDraft()
        showToast("Hito parcial eliminado")
    }

    private fun startLiveChronometer(startMillis: Long) {
        chronometerJob?.cancel()
        alertedMilestones.clear()

        chronometerJob = viewModelScope.launch {
            var lastNotificationUpdateSec = -10L

            while (isActive) {
                val elapsed = ((System.currentTimeMillis() - startMillis) / 1000).coerceAtLeast(0)
                _formState.update { it.copy(liveElapsedSeconds = elapsed, isTimerRunning = true) }

                // Background notification and milestone alerting
                appContext?.let { ctx ->
                    if (elapsed - lastNotificationUpdateSec >= 5 || lastNotificationUpdateSec < 0) {
                        lastNotificationUpdateSec = elapsed
                        NotificationHelper.updateOngoingSurgeryNotification(
                            context = ctx,
                            elapsedSeconds = elapsed,
                            paciente = _formState.value.paciente,
                            cirugiaRealizada = _formState.value.cirugiaRealizada,
                            medicoTratante = _formState.value.medicoTratante
                        )
                    }

                    if (alertsEnabled.value) {
                        val interval = alertIntervalMin.value
                        val elapsedMinutes = (elapsed / 60).toInt()
                        if (elapsedMinutes > 0 && elapsedMinutes % interval == 0 && !alertedMilestones.contains(elapsedMinutes)) {
                            alertedMilestones.add(elapsedMinutes)
                            NotificationHelper.triggerMilestoneAlert(
                                context = ctx,
                                elapsedMinutes = elapsedMinutes,
                                paciente = _formState.value.paciente,
                                cirugiaRealizada = _formState.value.cirugiaRealizada,
                                medicoTratante = _formState.value.medicoTratante,
                                isVibrationEnabled = alertVibration.value
                            )
                        }
                    }
                }

                delay(1000)
            }
        }
    }

    private fun stopLiveChronometer() {
        chronometerJob?.cancel()
        chronometerJob = null
    }

    fun updateAdsLlegada(value: String) {
        val estancia = TimeHelper.calculateMinutesBetween(value, _formState.value.adsSalida)
        _formState.update { it.copy(adsLlegada = value, adsEstanciaMin = estancia) }
        autoSaveDraft()
    }

    fun updateAdsSalida(value: String) {
        val estancia = TimeHelper.calculateMinutesBetween(_formState.value.adsLlegada, value)
        _formState.update { it.copy(adsSalida = value, adsEstanciaMin = estancia) }
        autoSaveDraft()
    }

    fun updateAdsNoEntro(value: Boolean) {
        _formState.update { it.copy(adsNoEntro = value) }
        autoSaveDraft()
    }

    fun updateCausa(value: String) {
        _formState.update { it.copy(causa = value) }
        autoSaveDraft()
    }

    fun updateObservaciones(value: String) {
        _formState.update { it.copy(observaciones = value) }
        autoSaveDraft()
    }

    fun updateNombreTestigo(value: String) {
        _formState.update { it.copy(nombreTestigo = value) }
        autoSaveDraft()
    }

    fun updateFirmaTecnico(base64: String?) {
        _formState.update { it.copy(firmaTecnicoBase64 = base64) }
        autoSaveDraft()
    }

    fun updateFirmaTestigo(base64: String?) {
        _formState.update { it.copy(firmaTestigoBase64 = base64) }
        autoSaveDraft()
    }

    // --- Clean Form (Limpieza Blindada) ---
    fun executeCleanForm(keepTimestamps: Boolean) {
        val current = _formState.value
        if (!keepTimestamps) {
            stopLiveChronometer()
            appContext?.let { ctx ->
                NotificationHelper.cancelOngoingSurgeryNotification(ctx)
            }
        }
        _formState.value = FormUiState(
            editingRecordId = null,
            fecha = TimeHelper.getTodayDate(),
            expediente = "",
            folioCurp = "",
            paciente = "",
            curp = "",
            edad = "",
            fotoCurpUri = null,
            noAfiliacionImss = "NO_AFILIADO",
            noAfiliacionIssste = "NO_AFILIADO",
            tipoCirugia = "Electiva",
            diagnostico = "",
            cirugiaRealizada = "",
            medicoTratante = "",
            enfermeraInstrumentista = "",
            fotoHojaQuirurgicaUri = null,
            tListo = if (keepTimestamps) current.tListo else "",
            tInicio = if (keepTimestamps) current.tInicio else "",
            tFin = if (keepTimestamps) current.tFin else "",
            inicioMillis = if (keepTimestamps) current.inicioMillis else null,
            finMillis = if (keepTimestamps) current.finMillis else null,
            duracionSegundos = if (keepTimestamps) current.duracionSegundos else 0L,
            isTimerRunning = if (keepTimestamps) current.isTimerRunning else false,
            liveElapsedSeconds = if (keepTimestamps) current.liveElapsedSeconds else 0L,
            adsLlegada = "",
            adsSalida = "",
            adsEstanciaMin = 0,
            adsNoEntro = false,
            causa = "Sin Desviación",
            observaciones = "",
            nombreTestigo = "",
            firmaTecnicoBase64 = null,
            firmaTestigoBase64 = null
        )
        viewModelScope.launch {
            repository.clearDraft()
            if (keepTimestamps) autoSaveDraft()
        }
        showToast(if (keepTimestamps) "Datos borrados. Tiempos y cronómetro conservados." else "Formulario totalmente limpio.")
    }

    // --- Save & Generate PDF ---
    fun saveAndGeneratePdf(
        context: Context,
        onSuccess: (file: File) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val s = _formState.value
        if (s.paciente.isBlank()) {
            onError("Por favor ingrese el nombre del paciente.")
            return
        }

        // Forensic validation rule: If adscrito didn't enter is NOT checked, arrival and exit are mandatory
        if (!s.adsNoEntro) {
            if (s.adsLlegada.isBlank() || s.adsSalida.isBlank()) {
                onError("❌ Bloqueo Forense: Registre Llegada y Salida del Adscrito, o marque que NO ingresó a sala.")
                return
            }
        }

        // Auto-generate folio derived from CURP if folio and expediente are blank
        val finalFolio = if (s.folioCurp.isBlank()) CurpHelper.generateFolioFromCurp(s.curp) else s.folioCurp

        var finTime = s.tFin
        var finMilli = s.finMillis
        var totalDurationSec = s.duracionSegundos
        if (s.tInicio.isNotBlank() && finTime.isBlank()) {
            finTime = TimeHelper.getCurrentTime()
            finMilli = System.currentTimeMillis()
            val startMilli = s.inicioMillis ?: finMilli
            totalDurationSec = ((finMilli - startMilli) / 1000).coerceAtLeast(0)
            stopLiveChronometer()
        }

        val prof = profile.value
        val record = AuditRecord(
            id = s.editingRecordId ?: UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            fecha = s.fecha.ifBlank { TimeHelper.getTodayDate() },
            expediente = s.expediente.trim(),
            folioCurp = finalFolio,
            paciente = s.paciente.trim(),
            edad = s.edad.trim(),
            curp = s.curp.trim(),
            fotoCurpUri = s.fotoCurpUri,
            noAfiliacionImss = s.noAfiliacionImss,
            noAfiliacionIssste = s.noAfiliacionIssste,
            tipoCirugia = s.tipoCirugia,
            diagnostico = s.diagnostico.trim(),
            cirugiaRealizada = s.cirugiaRealizada.trim(),
            medicoTratante = s.medicoTratante.trim(),
            enfermeraInstrumentista = s.enfermeraInstrumentista.trim(),
            fotoHojaQuirurgicaUri = s.fotoHojaQuirurgicaUri,
            tListo = s.tListo,
            tInicio = s.tInicio,
            tFin = finTime,
            inicioMillis = s.inicioMillis,
            finMillis = finMilli,
            duracionSegundos = totalDurationSec,
            adsLlegada = s.adsLlegada,
            adsSalida = s.adsSalida,
            adsEstanciaMin = if (s.adsEstanciaMin > 0) s.adsEstanciaMin else TimeHelper.calculateMinutesBetween(s.adsLlegada, s.adsSalida),
            adsNoEntro = s.adsNoEntro,
            causa = s.causa,
            observaciones = s.observaciones,
            nombreTecnico = prof.nombre,
            cargoTecnico = prof.cargo,
            nombreTestigo = s.nombreTestigo.ifBlank { "Personal de Quirófano" },
            firmaTecnicoBase64 = s.firmaTecnicoBase64,
            firmaTestigoBase64 = s.firmaTestigoBase64
        )

        viewModelScope.launch {
            repository.saveRecord(record)
            repository.clearDraft()

            try {
                val pdfFile = PdfGenerator.generateAuditPdf(context, record)
                stopLiveChronometer()
                NotificationHelper.cancelOngoingSurgeryNotification(context)

                _formState.value = FormUiState(fecha = TimeHelper.getTodayDate())
                showToast("✔ Registro guardado en base de datos local y PDF generado.")
                onSuccess(pdfFile)
            } catch (e: Exception) {
                onError("Error generando PDF: ${e.localizedMessage}")
            }
        }
    }

    fun loadRecordForEditing(record: AuditRecord) {
        stopLiveChronometer()
        appContext?.let { ctx ->
            NotificationHelper.cancelOngoingSurgeryNotification(ctx)
        }
        _formState.value = FormUiState(
            editingRecordId = record.id,
            fecha = record.fecha,
            expediente = record.expediente,
            folioCurp = record.folioCurp,
            paciente = record.paciente,
            curp = record.curp,
            edad = record.edad,
            fotoCurpUri = record.fotoCurpUri,
            noAfiliacionImss = record.noAfiliacionImss,
            noAfiliacionIssste = record.noAfiliacionIssste,
            tipoCirugia = record.tipoCirugia,
            diagnostico = record.diagnostico,
            cirugiaRealizada = record.cirugiaRealizada,
            medicoTratante = record.medicoTratante,
            enfermeraInstrumentista = record.enfermeraInstrumentista,
            fotoHojaQuirurgicaUri = record.fotoHojaQuirurgicaUri,
            tListo = record.tListo,
            tInicio = record.tInicio,
            tFin = record.tFin,
            inicioMillis = record.inicioMillis,
            finMillis = record.finMillis,
            duracionSegundos = record.duracionSegundos,
            isTimerRunning = false,
            liveElapsedSeconds = record.duracionSegundos,
            adsLlegada = record.adsLlegada,
            adsSalida = record.adsSalida,
            adsEstanciaMin = record.adsEstanciaMin,
            adsNoEntro = record.adsNoEntro,
            causa = record.causa,
            observaciones = record.observaciones,
            nombreTestigo = record.nombreTestigo,
            firmaTecnicoBase64 = record.firmaTecnicoBase64,
            firmaTestigoBase64 = record.firmaTestigoBase64
        )
        showToast("Registro cargado para edición.")
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            repository.deleteRecord(id)
            showToast("Registro eliminado.")
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            repository.deleteAllRecords()
            showToast("Base de datos formateada por completo.")
        }
    }

    // --- Export / Import ---
    fun exportCsv(context: Context, onReady: (File) -> Unit) {
        val records = allRecords.value
        if (records.isEmpty()) {
            showToast("❌ No hay registros para exportar")
            return
        }
        val file = CsvHelper.exportRecordsToCsv(context, records)
        onReady(file)
        showToast("✔ Archivo CSV forense listo")
    }

    fun exportSummaryPdf(context: Context, onReady: (File) -> Unit) {
        val records = allRecords.value
        if (records.isEmpty()) {
            showToast("❌ No hay registros para generar el reporte consolidado")
            return
        }
        val prof = profile.value
        val file = PdfGenerator.generateSummaryReportPdf(context, records, prof.nombre)
        onReady(file)
        showToast("✔ Reporte consolidado en PDF generado")
    }

    fun exportJsonBackup(context: Context, onReady: (File) -> Unit) {
        val records = allRecords.value
        if (records.isEmpty()) {
            showToast("❌ No hay registros para respaldar")
            return
        }
        val file = BackupHelper.backupToExternalStorage(context, records)
        if (file != null) {
            onReady(file)
            showToast("✔ Respaldo guardado en: ${file.name}")
        } else {
            showToast("❌ Error al crear respaldo en almacenamiento externo")
        }
    }

    fun triggerManualExternalBackup(context: Context, onReady: (File) -> Unit = {}) {
        val records = allRecords.value
        if (records.isEmpty()) {
            showToast("⚠️ No hay registros en la base de datos para respaldar.")
            return
        }
        val file = BackupHelper.backupToExternalStorage(context, records)
        onReady(file)
        showToast("✔ Respaldo Room exitoso: ${file.name}")
    }

    fun importJsonBackup(jsonString: String) {
        viewModelScope.launch {
            try {
                val parsed = BackupHelper.parseBackupJson(jsonString)
                if (parsed.isNotEmpty()) {
                    repository.saveRecords(parsed)
                    showToast("✔ Se restauraron ${parsed.size} registros en la base de datos Room.")
                } else {
                    showToast("⚠️ El archivo de respaldo no contiene registros válidos.")
                }
            } catch (e: Exception) {
                showToast("❌ Error al restaurar respaldo: ${e.localizedMessage}")
            }
        }
    }

    fun exportBackupToUri(context: Context, uri: Uri) {
        val records = allRecords.value
        if (records.isEmpty()) {
            showToast("⚠️ No hay registros para exportar.")
            return
        }
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                val success = BackupHelper.writeBackupToStream(outputStream, records)
                if (success) {
                    showToast("✔ Respaldo Room exportado con éxito a la ubicación seleccionada.")
                } else {
                    showToast("❌ Error al escribir el archivo de respaldo.")
                }
            } else {
                showToast("❌ No se pudo abrir el destino de almacenamiento.")
            }
        } catch (e: Exception) {
            showToast("❌ Error: ${e.localizedMessage}")
        }
    }

    fun restoreFromBackupJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val jsonContent = inputStream.bufferedReader().use { it.readText() }
                    val parsedRecords = BackupHelper.parseBackupJson(jsonContent)
                    if (parsedRecords.isNotEmpty()) {
                        repository.saveRecords(parsedRecords)
                        showToast("✔ Se restauraron ${parsedRecords.size} registros en la base de datos Room.")
                    } else {
                        showToast("⚠️ El archivo de respaldo no contiene registros válidos.")
                    }
                } else {
                    showToast("❌ No se pudo leer el archivo seleccionado.")
                }
            } catch (e: Exception) {
                showToast("❌ Error al restaurar respaldo: ${e.localizedMessage}")
            }
        }
    }

    // --- Draft Persistence ---
    private fun autoSaveDraft() {
        viewModelScope.launch {
            val s = _formState.value
            val draft = AuditDraft(
                expediente = s.expediente,
                folioCurp = s.folioCurp,
                paciente = s.paciente,
                curp = s.curp,
                edad = s.edad,
                fotoCurpUri = s.fotoCurpUri,
                noAfiliacionImss = s.noAfiliacionImss,
                noAfiliacionIssste = s.noAfiliacionIssste,
                tipoCirugia = s.tipoCirugia,
                diagnostico = s.diagnostico,
                cirugiaRealizada = s.cirugiaRealizada,
                medicoTratante = s.medicoTratante,
                enfermeraInstrumentista = s.enfermeraInstrumentista,
                fotoHojaQuirurgicaUri = s.fotoHojaQuirurgicaUri,
                tListo = s.tListo,
                tInicio = s.tInicio,
                tFin = s.tFin,
                inicioMillis = s.inicioMillis,
                finMillis = s.finMillis,
                duracionSegundos = s.duracionSegundos,
                adsLlegada = s.adsLlegada,
                adsSalida = s.adsSalida,
                adsEstanciaMin = s.adsEstanciaMin,
                adsNoEntro = s.adsNoEntro,
                causa = s.causa,
                observaciones = s.observaciones,
                nombreTestigo = s.nombreTestigo,
                firmaTecnicoBase64 = s.firmaTecnicoBase64,
                firmaTestigoBase64 = s.firmaTestigoBase64
            )
            repository.saveDraft(draft)
        }
    }

    private fun loadDraft() {
        viewModelScope.launch {
            val draft = repository.getDraft()
            if (draft != null && draft.paciente.isNotBlank()) {
                val isOngoing = draft.tInicio.isNotBlank() && draft.tFin.isBlank() && draft.inicioMillis != null
                _formState.update { current ->
                    current.copy(
                        expediente = draft.expediente,
                        folioCurp = draft.folioCurp,
                        paciente = draft.paciente,
                        curp = draft.curp,
                        edad = draft.edad,
                        fotoCurpUri = draft.fotoCurpUri,
                        noAfiliacionImss = draft.noAfiliacionImss.ifBlank { "NO_AFILIADO" },
                        noAfiliacionIssste = draft.noAfiliacionIssste.ifBlank { "NO_AFILIADO" },
                        tipoCirugia = draft.tipoCirugia.ifBlank { "Electiva" },
                        diagnostico = draft.diagnostico,
                        cirugiaRealizada = draft.cirugiaRealizada,
                        medicoTratante = draft.medicoTratante,
                        enfermeraInstrumentista = draft.enfermeraInstrumentista,
                        fotoHojaQuirurgicaUri = draft.fotoHojaQuirurgicaUri,
                        tListo = draft.tListo,
                        tInicio = draft.tInicio,
                        tFin = draft.tFin,
                        inicioMillis = draft.inicioMillis,
                        finMillis = draft.finMillis,
                        duracionSegundos = draft.duracionSegundos,
                        isTimerRunning = isOngoing,
                        liveElapsedSeconds = if (isOngoing) ((System.currentTimeMillis() - draft.inicioMillis!!) / 1000).coerceAtLeast(0) else draft.duracionSegundos,
                        adsLlegada = draft.adsLlegada,
                        adsSalida = draft.adsSalida,
                        adsEstanciaMin = draft.adsEstanciaMin,
                        adsNoEntro = draft.adsNoEntro,
                        causa = draft.causa,
                        observaciones = draft.observaciones,
                        nombreTestigo = draft.nombreTestigo,
                        firmaTecnicoBase64 = draft.firmaTecnicoBase64,
                        firmaTestigoBase64 = draft.firmaTestigoBase64
                    )
                }

                if (isOngoing) {
                    startLiveChronometer(draft.inicioMillis!!)
                }
            }
        }
    }
}
