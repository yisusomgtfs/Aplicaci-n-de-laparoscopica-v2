package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AuditRecord
import com.example.util.BackupHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HGV Auditoría", appName)
    }

    @Test
    fun `test room local database persistence for surgical records`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = inMemoryDb.auditDao()

        val record = AuditRecord(
            id = "cx-001",
            paciente = "López Morales, Ana",
            edad = "38",
            curp = "LOMA880210MDFRRN09",
            tipoCirugia = "Urgencia",
            diagnostico = "Apendicitis aguda fase III",
            cirugiaRealizada = "Apendicectomía Laparoscópica",
            medicoTratante = "Dr. Alejandro Morales",
            enfermeraInstrumentista = "L.E. Rosa Méndez",
            tListo = "14:10",
            tInicio = "14:25",
            tFin = "15:40",
            duracionSegundos = 4500L
        )

        dao.insertRecord(record)

        val retrieved = dao.getRecordById("cx-001")
        assertNotNull(retrieved)
        assertEquals("López Morales, Ana", retrieved!!.paciente)
        assertEquals("Urgencia", retrieved.tipoCirugia)
        assertEquals("Apendicitis aguda fase III", retrieved.diagnostico)
        assertEquals("Dr. Alejandro Morales", retrieved.medicoTratante)
        assertEquals("14:25", retrieved.tInicio)
        assertEquals("15:40", retrieved.tFin)

        val urgencias = dao.getRecordsByTipo("Urgencia").first()
        assertEquals(1, urgencias.size)

        val electivas = dao.getRecordsByTipo("Electiva").first()
        assertEquals(0, electivas.size)

        inMemoryDb.close()
    }

    @Test
    fun `test backup json parsing in robolectric`() {
        val jsonString = """
            [
              {
                "id": "test-123",
                "timestamp": 1700000000000,
                "fecha": "11-11-2026",
                "paciente": "García Hernández, Juan",
                "edad": "45",
                "curp": "GAHJ810101HDFRRN01",
                "tipoCirugia": "Urgencia",
                "diagnostico": "Apendicitis Aguda",
                "cirugiaRealizada": "Apendicectomía Laparoscópica",
                "medicoTratante": "Dr. Fernando Ruiz",
                "enfermeraInstrumentista": "L.E. Carmen Soto",
                "tListo": "08:15",
                "tInicio": "08:30",
                "tFin": "10:00",
                "duracionSegundos": 5400,
                "adsLlegada": "08:25",
                "adsSalida": "09:50",
                "adsEstanciaMin": 85,
                "adsNoEntro": false,
                "causa": "Sin Desviación",
                "observaciones": "Procedimiento laparoscópico sin incidencias",
                "nombreTecnico": "Jesús Abraham Aguilar Osorio",
                "cargoTecnico": "Técnico de Laparoscopia",
                "nombreTestigo": "Enf. Circulante"
              }
            ]
        """.trimIndent()

        val parsed = BackupHelper.parseBackupJson(jsonString)
        assertEquals(1, parsed.size)
        assertEquals("test-123", parsed[0].id)
        assertEquals("García Hernández, Juan", parsed[0].paciente)
        assertEquals("Urgencia", parsed[0].tipoCirugia)
        assertEquals("Apendicitis Aguda", parsed[0].diagnostico)
        assertEquals("Dr. Fernando Ruiz", parsed[0].medicoTratante)
        assertEquals(5400L, parsed[0].duracionSegundos)
    }

    @Test
    fun `test surgery statistics calculation for urgency vs elective`() {
        val r1 = AuditRecord(
            id = "1",
            paciente = "P1",
            tipoCirugia = "Electiva",
            duracionSegundos = 3600L // 60 min
        )
        val r2 = AuditRecord(
            id = "2",
            paciente = "P2",
            tipoCirugia = "Electiva",
            duracionSegundos = 5400L // 90 min
        )
        val r3 = AuditRecord(
            id = "3",
            paciente = "P3",
            tipoCirugia = "Urgencia",
            duracionSegundos = 2700L // 45 min
        )

        val records = listOf(r1, r2, r3)
        val electivas = records.filter { it.tipoCirugia.equals("Electiva", ignoreCase = true) }
        val urgencias = records.filter { it.tipoCirugia.equals("Urgencia", ignoreCase = true) }

        val avgElectiva = electivas.map { it.duracionSegundos / 60.0 }.average()
        val avgUrgencia = urgencias.map { it.duracionSegundos / 60.0 }.average()

        assertEquals(75.0, avgElectiva, 0.01)
        assertEquals(45.0, avgUrgencia, 0.01)
        assertEquals("1h 15m", com.example.util.TimeHelper.formatMinutesHuman(avgElectiva))
        assertEquals("45 min", com.example.util.TimeHelper.formatMinutesHuman(avgUrgencia))
    }

    @Test
    fun `test filtering surgery records by doctor name and diagnosis for audits`() {
        val r1 = AuditRecord(
            id = "1",
            paciente = "García, Juan",
            medicoTratante = "Dr. Morales",
            diagnostico = "Colelitiasis sintomática",
            cirugiaRealizada = "Colecistectomía Laparoscópica"
        )
        val r2 = AuditRecord(
            id = "2",
            paciente = "Hernández, María",
            medicoTratante = "Dra. Sánchez",
            diagnostico = "Apendicitis aguda",
            cirugiaRealizada = "Apendicectomía Laparoscópica"
        )
        val r3 = AuditRecord(
            id = "3",
            paciente = "López, Carlos",
            medicoTratante = "Dr. Morales",
            diagnostico = "Hernia inguinal incarcerada",
            cirugiaRealizada = "Plastia inguinal TAPP"
        )

        val all = listOf(r1, r2, r3)

        // Filter by Doctor Name
        val byDoctor = all.filter { it.medicoTratante.contains("Morales", ignoreCase = true) }
        assertEquals(2, byDoctor.size)
        assertEquals("García, Juan", byDoctor[0].paciente)
        assertEquals("López, Carlos", byDoctor[1].paciente)

        // Filter by Diagnosis
        val byDiagnosis = all.filter { it.diagnostico.contains("Apendicitis", ignoreCase = true) }
        assertEquals(1, byDiagnosis.size)
        assertEquals("Hernández, María", byDiagnosis[0].paciente)
        assertEquals("Dra. Sánchez", byDiagnosis[0].medicoTratante)
    }

    @Test
    fun `test surgery records biometric and pin lock state`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val repository = com.example.data.repository.AuditRepository(inMemoryDb.auditDao())
        val prefRepo = com.example.data.preferences.UserPreferencesRepository(context)

        val vm = com.example.ui.viewmodel.AuditViewModel(repository, prefRepo)
        assertEquals(false, vm.isRecordsUnlocked.value)

        vm.unlockRecordsByBiometrics()
        assertEquals(true, vm.isRecordsUnlocked.value)

        vm.lockRecords()
        assertEquals(false, vm.isRecordsUnlocked.value)

        val unlockedByPin = vm.unlockRecordsByPin("2026")
        assertEquals(true, unlockedByPin)
        assertEquals(true, vm.isRecordsUnlocked.value)

        inMemoryDb.close()
    }

    @Test
    fun `test surgical notification channels creation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.util.NotificationHelper.createNotificationChannels(context)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val alertChannel = nm.getNotificationChannel(com.example.util.NotificationHelper.CHANNEL_ID_SURGICAL_ALERTS)
        val ongoingChannel = nm.getNotificationChannel(com.example.util.NotificationHelper.CHANNEL_ID_SURGICAL_ONGOING)

        assertNotNull(alertChannel)
        assertEquals("Alertas de Tiempo Quirúrgico", alertChannel.name.toString())
        assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH, alertChannel.importance)

        assertNotNull(ongoingChannel)
        assertEquals("Monitoreo Quirúrgico en Vivo", ongoingChannel.name.toString())
        assertEquals(android.app.NotificationManager.IMPORTANCE_LOW, ongoingChannel.importance)
    }

    @Test
    fun `test surgical alert preferences configuration`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefRepo = com.example.data.preferences.UserPreferencesRepository(context)

        prefRepo.setAlertsEnabled(true)
        assertEquals(true, prefRepo.alertsEnabledFlow.first())

        prefRepo.setAlertIntervalMin(45)
        assertEquals(45, prefRepo.alertIntervalMinFlow.first())

        prefRepo.setAlertVibration(false)
        assertEquals(false, prefRepo.alertVibrationFlow.first())
    }
}
