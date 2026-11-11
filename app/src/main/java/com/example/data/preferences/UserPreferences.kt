package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hgv_settings")

object SecurityUtils {
    private const val SALT = "HGV_Laparoscopia_Sec_2026"

    fun hashSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest((input + SALT).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

data class UserProfile(
    val nombre: String = "Jesús Abraham Aguilar Osorio",
    val cargo: String = "Técnico de Laparoscopia",
    val fotoUri: String? = null
)

class UserPreferencesRepository(private val context: Context) {
    companion object {
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_RECOVERY_HASH = stringPreferencesKey("recovery_hash")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_NIGHT_LIGHT = booleanPreferencesKey("night_light")
        private val KEY_PROFILE_NOMBRE = stringPreferencesKey("profile_nombre")
        private val KEY_PROFILE_CARGO = stringPreferencesKey("profile_cargo")
        private val KEY_PROFILE_FOTO = stringPreferencesKey("profile_foto")

        // Notification Preferences
        private val KEY_ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
        private val KEY_ALERT_INTERVAL_MIN = intPreferencesKey("alert_interval_min")
        private val KEY_ALERT_VIBRATION = booleanPreferencesKey("alert_vibration")

        // Staff Directories Preferences (Pipe-separated lists)
        private val KEY_DOCTOR_DIRECTORY = stringPreferencesKey("doctor_directory")
        private val KEY_NURSE_DIRECTORY = stringPreferencesKey("nurse_directory")
    }

    val defaultPinHash: String = SecurityUtils.hashSha256("2026")
    val defaultRecoveryHash: String = SecurityUtils.hashSha256("HGV")

    private val defaultDoctors = listOf(
        "Dr. Alejandro Morales",
        "Dra. Brenda Sánchez",
        "Dr. Carlos Ruiz",
        "Dr. Fernando Castro",
        "Dra. Diana Reyes",
        "Dr. Manuel Gutiérrez"
    )

    private val defaultNurses = listOf(
        "L.E. Rosa Méndez",
        "Enf. Carmen Soto",
        "L.E. Jorge Vázquez",
        "Enf. María Gutiérrez",
        "L.E. Sofia Delgado"
    )

    val pinHashFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_HASH] ?: defaultPinHash
    }

    val recoveryHashFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_RECOVERY_HASH] ?: defaultRecoveryHash
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: false
    }

    val nightLightFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NIGHT_LIGHT] ?: false
    }

    val profileFlow: Flow<UserProfile> = context.dataStore.data.map { preferences ->
        UserProfile(
            nombre = preferences[KEY_PROFILE_NOMBRE] ?: "Jesús Abraham Aguilar Osorio",
            cargo = preferences[KEY_PROFILE_CARGO] ?: "Técnico de Laparoscopia",
            fotoUri = preferences[KEY_PROFILE_FOTO]
        )
    }

    val alertsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ALERTS_ENABLED] ?: true
    }

    val alertIntervalMinFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_ALERT_INTERVAL_MIN] ?: 30
    }

    val alertVibrationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ALERT_VIBRATION] ?: true
    }

    val doctorDirectoryFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_DOCTOR_DIRECTORY]
        if (raw.isNullOrBlank()) defaultDoctors else raw.split("|").filter { it.isNotBlank() }
    }

    val nurseDirectoryFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_NURSE_DIRECTORY]
        if (raw.isNullOrBlank()) defaultNurses else raw.split("|").filter { it.isNotBlank() }
    }

    suspend fun setPin(pin: String) {
        val hash = SecurityUtils.hashSha256(pin)
        context.dataStore.edit { preferences ->
            preferences[KEY_PIN_HASH] = hash
        }
    }

    suspend fun setRecoveryWord(word: String) {
        val hash = SecurityUtils.hashSha256(word)
        context.dataStore.edit { preferences ->
            preferences[KEY_RECOVERY_HASH] = hash
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setNightLight(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NIGHT_LIGHT] = enabled
        }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setAlertIntervalMin(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALERT_INTERVAL_MIN] = minutes
        }
    }

    suspend fun setAlertVibration(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALERT_VIBRATION] = enabled
        }
    }

    suspend fun saveProfile(nombre: String, cargo: String, fotoUri: String?) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PROFILE_NOMBRE] = nombre
            preferences[KEY_PROFILE_CARGO] = cargo
            if (fotoUri != null) {
                preferences[KEY_PROFILE_FOTO] = fotoUri
            } else {
                preferences.remove(KEY_PROFILE_FOTO)
            }
        }
    }

    suspend fun saveDoctorDirectory(doctors: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DOCTOR_DIRECTORY] = doctors.distinct().filter { it.isNotBlank() }.joinToString("|")
        }
    }

    suspend fun addDoctor(doctorName: String) {
        val current = doctorDirectoryFlow.map { it }.let { flow ->
            context.dataStore.data.map { preferences ->
                val raw = preferences[KEY_DOCTOR_DIRECTORY]
                if (raw.isNullOrBlank()) defaultDoctors else raw.split("|").filter { it.isNotBlank() }
            }
        }
        context.dataStore.edit { preferences ->
            val raw = preferences[KEY_DOCTOR_DIRECTORY]
            val list = (if (raw.isNullOrBlank()) defaultDoctors else raw.split("|")).toMutableList()
            if (!list.contains(doctorName.trim())) {
                list.add(doctorName.trim())
            }
            preferences[KEY_DOCTOR_DIRECTORY] = list.filter { it.isNotBlank() }.joinToString("|")
        }
    }

    suspend fun removeDoctor(doctorName: String) {
        context.dataStore.edit { preferences ->
            val raw = preferences[KEY_DOCTOR_DIRECTORY]
            val list = (if (raw.isNullOrBlank()) defaultDoctors else raw.split("|")).toMutableList()
            list.remove(doctorName.trim())
            preferences[KEY_DOCTOR_DIRECTORY] = list.filter { it.isNotBlank() }.joinToString("|")
        }
    }

    suspend fun addNurse(nurseName: String) {
        context.dataStore.edit { preferences ->
            val raw = preferences[KEY_NURSE_DIRECTORY]
            val list = (if (raw.isNullOrBlank()) defaultNurses else raw.split("|")).toMutableList()
            if (!list.contains(nurseName.trim())) {
                list.add(nurseName.trim())
            }
            preferences[KEY_NURSE_DIRECTORY] = list.filter { it.isNotBlank() }.joinToString("|")
        }
    }

    suspend fun removeNurse(nurseName: String) {
        context.dataStore.edit { preferences ->
            val raw = preferences[KEY_NURSE_DIRECTORY]
            val list = (if (raw.isNullOrBlank()) defaultNurses else raw.split("|")).toMutableList()
            list.remove(nurseName.trim())
            preferences[KEY_NURSE_DIRECTORY] = list.filter { it.isNotBlank() }.joinToString("|")
        }
    }
}
