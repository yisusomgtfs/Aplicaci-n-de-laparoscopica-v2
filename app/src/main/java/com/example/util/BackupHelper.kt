package com.example.util

import android.content.Context
import android.os.Environment
import com.example.data.model.AuditRecord
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {

    fun buildBackupJsonString(records: List<AuditRecord>): String {
        val jsonArray = JSONArray()
        for (r in records) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("timestamp", r.timestamp)
                put("fecha", r.fecha)
                put("expediente", r.expediente)
                put("folioCurp", r.folioCurp)
                put("paciente", r.paciente)
                put("edad", r.edad)
                put("curp", r.curp)
                put("fotoCurpUri", r.fotoCurpUri ?: "")
                put("noAfiliacionImss", r.noAfiliacionImss)
                put("noAfiliacionIssste", r.noAfiliacionIssste)
                put("tipoCirugia", r.tipoCirugia)
                put("diagnostico", r.diagnostico)
                put("cirugiaRealizada", r.cirugiaRealizada)
                put("medicoTratante", r.medicoTratante)
                put("enfermeraInstrumentista", r.enfermeraInstrumentista)
                put("fotoHojaQuirurgicaUri", r.fotoHojaQuirurgicaUri ?: "")
                put("tListo", r.tListo)
                put("tInicio", r.tInicio)
                put("tFin", r.tFin)
                put("duracionSegundos", r.duracionSegundos)
                put("adsLlegada", r.adsLlegada)
                put("adsSalida", r.adsSalida)
                put("adsEstanciaMin", r.adsEstanciaMin)
                put("adsNoEntro", r.adsNoEntro)
                put("causa", r.causa)
                put("observaciones", r.observaciones)
                put("nombreTecnico", r.nombreTecnico)
                put("cargoTecnico", r.cargoTecnico)
                put("nombreTestigo", r.nombreTestigo)
                put("firmaTecnicoBase64", r.firmaTecnicoBase64 ?: "")
                put("firmaTestigoBase64", r.firmaTestigoBase64 ?: "")
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    /**
     * Creates a manual backup file of all Room database records in the device's external storage
     * (under Documents/HGV_Respaldos_Quirurgicos/) for disaster recovery and offline redundancy.
     */
    fun backupToExternalStorage(context: Context, records: List<AuditRecord>): File {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.getExternalFilesDir(null)
            ?: File(context.filesDir, "external_backup")

        val backupDir = File(externalDir, "HGV_Respaldos_Quirurgicos").apply {
            if (!exists()) mkdirs()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "RESPALDO_ROOM_HGV_${timeStamp}.json"
        val file = File(backupDir, fileName)

        val jsonString = buildBackupJsonString(records)
        FileOutputStream(file).use { out ->
            out.write(jsonString.toByteArray(Charsets.UTF_8))
        }

        return file
    }

    /**
     * Writes backup JSON directly into any user-selected OutputStream (e.g. via Storage Access Framework).
     */
    fun writeBackupToStream(outputStream: OutputStream, records: List<AuditRecord>): Boolean {
        return try {
            val jsonString = buildBackupJsonString(records)
            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exportBackupJson(context: Context, records: List<AuditRecord>): File {
        val fileName = "Respaldo_HGV_${TimeHelper.getTodayDate()}.json"
        val file = File(context.cacheDir, fileName)

        val jsonString = buildBackupJsonString(records)
        FileOutputStream(file).use { out ->
            out.write(jsonString.toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun parseBackupJson(jsonString: String): List<AuditRecord> {
        val list = mutableListOf<AuditRecord>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val record = AuditRecord(
                id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                fecha = obj.optString("fecha"),
                expediente = obj.optString("expediente"),
                folioCurp = obj.optString("folioCurp"),
                paciente = obj.optString("paciente"),
                edad = obj.optString("edad"),
                curp = obj.optString("curp"),
                fotoCurpUri = obj.optString("fotoCurpUri").ifBlank { null },
                noAfiliacionImss = obj.optString("noAfiliacionImss", "NO_AFILIADO"),
                noAfiliacionIssste = obj.optString("noAfiliacionIssste", "NO_AFILIADO"),
                tipoCirugia = obj.optString("tipoCirugia", "Electiva"),
                diagnostico = obj.optString("diagnostico"),
                cirugiaRealizada = obj.optString("cirugiaRealizada"),
                medicoTratante = obj.optString("medicoTratante"),
                enfermeraInstrumentista = obj.optString("enfermeraInstrumentista"),
                fotoHojaQuirurgicaUri = obj.optString("fotoHojaQuirurgicaUri").ifBlank { null },
                tListo = obj.optString("tListo"),
                tInicio = obj.optString("tInicio"),
                tFin = obj.optString("tFin"),
                duracionSegundos = obj.optLong("duracionSegundos", 0L),
                adsLlegada = obj.optString("adsLlegada"),
                adsSalida = obj.optString("adsSalida"),
                adsEstanciaMin = obj.optInt("adsEstanciaMin", 0),
                adsNoEntro = obj.optBoolean("adsNoEntro", false),
                causa = obj.optString("causa", "Sin Desviación"),
                observaciones = obj.optString("observaciones"),
                nombreTecnico = obj.optString("nombreTecnico"),
                cargoTecnico = obj.optString("cargoTecnico"),
                nombreTestigo = obj.optString("nombreTestigo"),
                firmaTecnicoBase64 = obj.optString("firmaTecnicoBase64").ifBlank { null },
                firmaTestigoBase64 = obj.optString("firmaTestigoBase64").ifBlank { null }
            )
            list.add(record)
        }
        return list
    }
}
