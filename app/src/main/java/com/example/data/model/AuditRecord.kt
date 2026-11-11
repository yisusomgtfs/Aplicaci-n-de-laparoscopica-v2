package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "audit_records")
data class AuditRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val fecha: String = "",
    val expediente: String = "",         // Número de Expediente Clínico HGV
    val folioCurp: String = "",          // Folio derivado de la CURP
    val paciente: String = "",
    val edad: String = "",
    val curp: String = "",
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
    val adsLlegada: String = "",
    val adsSalida: String = "",
    val adsEstanciaMin: Int = 0,
    val adsNoEntro: Boolean = false,
    val causa: String = "Sin Desviación",
    val observaciones: String = "",
    val nombreTecnico: String = "",
    val cargoTecnico: String = "",
    val nombreTestigo: String = "",
    val firmaTecnicoBase64: String? = null,
    val firmaTestigoBase64: String? = null
)

@Entity(tableName = "audit_draft")
data class AuditDraft(
    @PrimaryKey
    val id: String = "draft_activo",
    val expediente: String = "",
    val folioCurp: String = "",
    val paciente: String = "",
    val curp: String = "",
    val edad: String = "",
    val fotoCurpUri: String? = null,
    val noAfiliacionImss: String = "NO_AFILIADO",
    val noAfiliacionIssste: String = "NO_AFILIADO",
    val tipoCirugia: String = "Electiva",
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
