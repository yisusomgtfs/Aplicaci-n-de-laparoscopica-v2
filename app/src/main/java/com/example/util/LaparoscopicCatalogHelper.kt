package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.LaparoscopicProcedure
import java.io.BufferedReader
import java.io.InputStreamReader

object LaparoscopicCatalogHelper {

    val DEFAULT_LAPAROSCOPIC_PROCEDURES = listOf(
        LaparoscopicProcedure(
            nombre = "Colecistectomía Laparoscópica",
            especialidad = "Cirugía General",
            diagnosticoDefecto = "Colelitiasis Sintomática / Colecistitis Aguda",
            descripcionQuirurgica = "Abordaje laparoscópico estándar con neumoperitoneo umbilical a 12 mmHg. Colocación de puertos: umbilical 10mm, epigástrico 10mm y 2 subcostales derechos 5mm. Disección minuciosa del triángulo de Calot. Identificación previa del conducto cístico y la arteria cística. Colocación de dobles clips proximales y distales con sección fría. Disección retrógrada de la vesícula biliar de su lecho hepático mediante electrocauterio. Extracción en bolsa endobag. Hemostasia rigurosa y verificación del lecho hepático.",
            puntosClave = listOf("Técnica Hasson/Veress 12mmHg", "Triángulo de Calot", "Clipado Cístico y Arteria", "Lecho Hepático")
        ),
        LaparoscopicProcedure(
            nombre = "Apendicectomía Laparoscópica",
            especialidad = "Cirugía General",
            diagnosticoDefecto = "Apendicitis Aguda (Fase II Flegmonosa / Fase III Supurada)",
            descripcionQuirurgica = "Abordaje laparoscópico con 3 puertos (umbilical 10mm, FII 5mm, suprapúbico 5mm). Neumoperitoneo a 12 mmHg. Localización e insuflación del ciego para identificar el apéndice cecal. Disección de mesoapéndice mediante electrocauterio/bipolar. Hemostasia de arteria apendicular. Ligadura de la base apendicular con asa de Roeder / Endoloop / grapadora lineal. Extracción del apéndice por puerto umbilical en bolsa endobag. Aspiración y lavado peritoneal enfático.",
            puntosClave = listOf("Puertos 10mm + 5mm + 5mm", "Ligadura Base Apendicular", "Extracción Endobag", "Lavado Peritoneal")
        ),
        LaparoscopicProcedure(
            nombre = "Laparoscopía Exploratoria",
            especialidad = "Cirugía General",
            diagnosticoDefecto = "Abdomen Agudo / Dolor Abdominal no Especificado",
            descripcionQuirurgica = "Insuflación neumoperitoneo umbilical a 12 mmHg con técnica abierta Hasson. Introducción de laparoscopio 30°. Exploración metódica por cuadrantes: cámara anterior, cámara posterior, estómago, duodeno, revisión de intestino delgado de válvula ileocecal a ligamento de Treitz, colon completo, lecho hepático, bazo y hueco pélvico. Verificación de sangrado, perforación o líquido peritoneal purulento.",
            puntosClave = listOf("Exploración 4 Cuadrantes", "Revisión Válvula a Treitz", "Evaluación Líquido Libre")
        ),
        LaparoscopicProcedure(
            nombre = "Hernioplastia Laparoscópica TAPP",
            especialidad = "Cirugía General",
            diagnosticoDefecto = "Hernia Inguinal Directa / Indirecta / Recidivante",
            descripcionQuirurgica = "Abordaje laparoscópico transabdominal preperitoneal (TAPP). Insuflación a 12 mmHg. Incisión de colgajo peritoneal de la espina ilíaca anterosuperior al ligamento umbilical medio. Disección del espacio preperitoneal (Bogros y Retzius). Reducción del saco herniario. Identificación del orificio miopectíneo de Fruchaud. Colocación de malla de polipropileno tridimensional de 15x12 cm. Fijación y cierre continuo de colgajo peritoneal.",
            puntosClave = listOf("Colgajo Peritoneal", "Espacio Bogros/Retzius", "Orificio Fruchaud", "Malla 15x12 cm")
        ),
        LaparoscopicProcedure(
            nombre = "Funduplicatura Nissen Laparoscópica",
            especialidad = "Cirugía Gastrointestinal",
            diagnosticoDefecto = "Enfermedad por Reflujo Gastroesofágico / Hernia Hiatal",
            descripcionQuirurgica = "Abordaje laparoscópico superior con 5 puertos. Separación del lóbulo hepático izquierdo con separador de Nathanson. Disección del espacio retroesofágico y pilares diafragmáticos. Liberación del vaso corto fundico. Cierre de pilares diafragmáticos con sutura no absorbible. Confección de valva fundus gástrico simétrica floja de 360° (Nissen) de 2 cm sobre sonda Foucher 48Fr. Verificación de ausencia de tensión.",
            puntosClave = listOf("5 Puertos Laparoscópicos", "Cierre Pilares Diafragmáticos", "Valva Nissen 360° 2cm", "Sonda Foucher")
        ),
        LaparoscopicProcedure(
            nombre = "Salpingectomía Laparoscópica",
            especialidad = "Ginecología Quirúrgica",
            diagnosticoDefecto = "Embarazo Ectópico Tubario / Salpingitis / Hidrosálpinx",
            descripcionQuirurgica = "Neumoperitoneo umbilical a 12 mmHg. Colocación de puertos suprapúbicos y en fosa ilíaca. Inspección de cavidad pélvica y anexos. Coagulación bipolar progresiva de mesosálpinx desde el extremo fimbriado hasta el istmo tubario. Sección con tijera laparoscópica. Extracción de pieza anatómica en bolsa endobag. Lavado y hemostasia cuidadosa.",
            puntosClave = listOf("Coagulación Bipolar Mesosálpinx", "Sección Tubaria Istmo", "Extracción Endobag")
        ),
        LaparoscopicProcedure(
            nombre = "Histerectomía Laparoscópica Total (TLH)",
            especialidad = "Ginecología Quirúrgica",
            diagnosticoDefecto = "Miomatosis Uterina Sintomática / Sangrado Uterino Anómalo",
            descripcionQuirurgica = "Abordaje laparoscópico con movilizador uterino. Coagulación y sección de ligamentos redondos, útero-ováricos o infundibulopélvicos. Esqueleto de ligamentos anchos y descenso de vejiga. Coagulación y corte de pedículos uterinos en el istmo. Colpotomía laparoscópica sobre manipulador. Extracción uterina transvaginal. Cierre continuo de la cúpula vaginal con sutura barbada bidireccional.",
            puntosClave = listOf("Movilizador Uterino", "Descenso de Vejiga", "Pedículos Uterinos Istmo", "Cierre Cúpula Vaginal")
        )
    )

    fun parseCsvOrTextCatalog(content: String): List<LaparoscopicProcedure> {
        val list = mutableListOf<LaparoscopicProcedure>()
        if (content.isBlank()) return list

        val lines = content.lines()
        val delimiter = when {
            lines.firstOrNull()?.contains(";") == true -> ";"
            lines.firstOrNull()?.contains("\t") == true -> "\t"
            else -> ","
        }

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.lowercase().startsWith("nombre") || trimmed.lowercase().startsWith("procedimiento")) {
                continue
            }
            val parts = trimmed.split(delimiter).map { it.trim().removeSurrounding("\"") }
            if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                val nombre = parts[0]
                val especialidad = parts.getOrNull(1)?.ifBlank { "Cirugía Laparoscópica" } ?: "Cirugía Laparoscópica"
                val diagnostico = parts.getOrNull(2) ?: ""
                val descripcion = parts.getOrNull(3) ?: ""
                val puntos = parts.getOrNull(4)?.split("|", ",")?.map { it.trim() } ?: emptyList()

                list.add(
                    LaparoscopicProcedure(
                        nombre = nombre,
                        especialidad = especialidad,
                        diagnosticoDefecto = diagnostico,
                        descripcionQuirurgica = descripcion,
                        puntosClave = puntos
                    )
                )
            }
        }
        return list
    }

    fun parseCatalogFromUri(context: Context, uri: Uri): List<LaparoscopicProcedure> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
                val content = reader.readText()
                parseCsvOrTextCatalog(content)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
