package com.example.util

import android.content.Context
import com.example.data.model.AuditRecord
import java.io.File
import java.io.FileOutputStream

object CsvHelper {

    fun exportRecordsToCsv(context: Context, records: List<AuditRecord>): File {
        val fileName = "Auditoria_HGV_Data_${TimeHelper.getTodayDate()}.csv"
        val file = File(context.cacheDir, fileName)

        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel compatibility
        sb.append("\uFEFF")
        sb.append("ID_Cirugia,Fecha,Expediente,Folio_CURP,Tipo_Cirugia,Paciente,Edad,CURP,No_Afiliacion_IMSS,No_Afiliacion_ISSSTE,Medico_Tratante,Diagnostico,Cirugia_Realizada,Instrumentista,Hora_Listo,Hora_Inicio,Hora_Fin,Duracion_Concreta,Llegada_Adscrito,Salida_Adscrito,Estancia_Adscrito_Min,Causa_Desviacion,Adscrito_Ausente,Observaciones\n")

        for (r in records) {
            val pacSanitized = "\"${r.paciente.replace("\"", "\"\"")}\""
            val medSanitized = "\"${r.medicoTratante.replace("\"", "\"\"")}\""
            val diagSanitized = "\"${r.diagnostico.replace("\"", "\"\"")}\""
            val cxSanitized = "\"${r.cirugiaRealizada.replace("\"", "\"\"")}\""
            val instSanitized = "\"${r.enfermeraInstrumentista.replace("\"", "\"\"")}\""
            val obsSanitized = "\"${r.observaciones.replace("\"", "\"\"")}\""
            val causaSanitized = "\"${r.causa.replace("\"", "\"\"")}\""
            val expSanitized = "\"${r.expediente.replace("\"", "\"\"")}\""
            val folioSanitized = "\"${r.folioCurp.replace("\"", "\"\"")}\""
            val imssSanitized = "\"${r.noAfiliacionImss.replace("\"", "\"\"")}\""
            val isssteSanitized = "\"${r.noAfiliacionIssste.replace("\"", "\"\"")}\""

            val adscritoAusente = if (r.adsNoEntro) "SI" else "NO"
            val duracionStr = if (r.duracionSegundos > 0) TimeHelper.formatDurationHuman(r.duracionSegundos) else "${TimeHelper.calculateMinutesBetween(r.tInicio, r.tFin)} min"

            sb.append("${r.id},${r.fecha},$expSanitized,$folioSanitized,${r.tipoCirugia},$pacSanitized,${r.edad},${r.curp},$imssSanitized,$isssteSanitized,$medSanitized,$diagSanitized,$cxSanitized,$instSanitized,${r.tListo},${r.tInicio},${r.tFin},\"$duracionStr\",${r.adsLlegada},${r.adsSalida},${r.adsEstanciaMin},$causaSanitized,$adscritoAusente,$obsSanitized\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }
}
