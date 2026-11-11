package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Base64
import com.example.data.model.AuditRecord
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PdfGenerator {

    fun generateAuditPdf(context: Context, record: AuditRecord): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.rgb(200, 205, 210)
        }

        // Header Top Bar
        paintFill.color = Color.rgb(26, 37, 47) // Navy #1A252F
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 48f, paintFill)

        paintText.color = Color.WHITE
        paintText.textSize = 14f
        paintText.isFakeBoldText = true
        paintText.textAlign = Paint.Align.CENTER
        canvas.drawText("HOSPITAL GENERAL HGV", pageWidth / 2f, 24f, paintText)

        paintText.textSize = 9f
        paintText.isFakeBoldText = false
        canvas.drawText("BITÁCORA DE EVIDENCIA OPERATIVA • DEPARTAMENTO DE LAPAROSCOPIA", pageWidth / 2f, 38f, paintText)

        var y = 66f
        val left = 32f
        val right = pageWidth - 32f
        val contentWidth = right - left

        // Subtitle: Folio, Tipo de Cirugía badge, Fecha
        paintText.color = Color.rgb(41, 128, 185) // Blue accent
        paintText.textSize = 10f
        paintText.isFakeBoldText = true
        paintText.textAlign = Paint.Align.LEFT
        canvas.drawText("FOLIO: ${record.id.take(8).uppercase()}", left, y, paintText)

        // Badge Tipo de Cirugia
        val tipoColor = if (record.tipoCirugia.equals("Urgencia", ignoreCase = true)) Color.rgb(192, 57, 43) else Color.rgb(41, 128, 185)
        paintFill.color = tipoColor
        val badgeW = 90f
        val badgeH = 16f
        val badgeX = pageWidth / 2f - badgeW / 2f
        canvas.drawRoundRect(RectF(badgeX, y - 11f, badgeX + badgeW, y + 4f), 4f, 4f, paintFill)
        paintText.color = Color.WHITE
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = true
        paintText.textAlign = Paint.Align.CENTER
        canvas.drawText(record.tipoCirugia.uppercase(), pageWidth / 2f, y, paintText)

        paintText.color = Color.rgb(100, 110, 120)
        paintText.textSize = 9f
        paintText.isFakeBoldText = false
        paintText.textAlign = Paint.Align.RIGHT
        canvas.drawText("FECHA: ${record.fecha}", right, y, paintText)

        y += 8f
        paintStroke.color = Color.rgb(41, 128, 185)
        paintStroke.strokeWidth = 1.2f
        canvas.drawLine(left, y, right, y, paintStroke)

        // Section: Datos del Paciente y Quirúrgicos
        y += 16f
        drawSectionHeader(canvas, left, y, contentWidth, "DATOS GENERALES Y EQUIPO QUIRÚRGICO")
        y += 18f

        val patientBoxHeight = 72f
        drawBox(canvas, left, y, contentWidth, patientBoxHeight, Color.rgb(248, 249, 250))

        paintText.textAlign = Paint.Align.LEFT
        paintText.color = Color.rgb(44, 62, 80)

        // Row 1: Paciente, Expediente / Folio, CURP, Edad
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("PACIENTE:", left + 8f, y + 14f, paintText)
        paintText.textSize = 9f
        paintText.isFakeBoldText = false
        canvas.drawText(record.paciente.ifBlank { "NO ESPECIFICADO" }, left + 8f, y + 26f, paintText)

        val colExp = left + contentWidth * 0.40f
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("EXPEDIENTE / FOLIO:", colExp, y + 14f, paintText)
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = true
        paintText.color = Color.rgb(41, 128, 185)
        val expLabel = record.expediente.ifBlank { record.folioCurp.ifBlank { CurpHelper.generateFolioFromCurp(record.curp) } }
        canvas.drawText(expLabel, colExp, y + 26f, paintText)

        val col2 = left + contentWidth * 0.68f
        paintText.color = Color.rgb(44, 62, 80)
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("CURP:", col2, y + 14f, paintText)
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = false
        canvas.drawText(record.curp.ifBlank { "N/A" }, col2, y + 26f, paintText)

        val col3 = left + contentWidth * 0.88f
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("EDAD:", col3, y + 14f, paintText)
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = false
        val edadStr = if (record.edad.isNotBlank()) "${record.edad} AÑOS" else "N/A"
        canvas.drawText(edadStr, col3, y + 26f, paintText)

        // Row 2: Estatus No Afiliación IMSS e ISSSTE
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("ESTATUS NO AFILIACIÓN:", left + 8f, y + 42f, paintText)
        paintText.textSize = 8f
        paintText.isFakeBoldText = true
        paintText.color = if (record.noAfiliacionImss == "AFILIADO_DETECTADO") Color.rgb(192, 57, 43) else Color.rgb(39, 174, 96)
        val imssLabel = if (record.noAfiliacionImss == "AFILIADO_DETECTADO") "❌ IMSS AFILIADO" else "✔ IMSS NO AFILIADO (Población Abierta)"
        val isssteLabel = if (record.noAfiliacionIssste == "AFILIADO_DETECTADO") "❌ ISSSTE AFILIADO" else "✔ ISSSTE NO AFILIADO"
        canvas.drawText("$imssLabel  |  $isssteLabel", left + 120f, y + 42f, paintText)

        // Row 3: Médico Tratante, Enfermera Instrumentista
        paintText.color = Color.rgb(44, 62, 80)
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("MÉDICO TRATANTE / CIRUJANO:", left + 8f, y + 58f, paintText)
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = false
        canvas.drawText(record.medicoTratante.ifBlank { "NO ESPECIFICADO" }, left + 140f, y + 58f, paintText)

        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("ENFERMERA INSTRUMENTISTA:", colExp, y + 58f, paintText)
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = false
        canvas.drawText(record.enfermeraInstrumentista.ifBlank { "NO ESPECIFICADA" }, colExp + 130f, y + 58f, paintText)

        y += patientBoxHeight + 14f

        // Section: Diagnóstico y Cirugía Realizada
        drawSectionHeader(canvas, left, y, contentWidth, "PROCEDIMIENTO Y DIAGNÓSTICO QUIRÚRGICO")
        y += 18f

        val diagBoxHeight = 44f
        drawBox(canvas, left, y, contentWidth, diagBoxHeight, Color.rgb(248, 249, 250))

        paintText.textAlign = Paint.Align.LEFT
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        paintText.color = Color.rgb(100, 110, 120)
        canvas.drawText("DIAGNÓSTICO:", left + 8f, y + 14f, paintText)
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = false
        paintText.color = Color.rgb(44, 62, 80)
        canvas.drawText(record.diagnostico.ifBlank { "No especificado" }, left + 8f, y + 27f, paintText)

        val colCirugia = left + contentWidth * 0.45f
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        paintText.color = Color.rgb(100, 110, 120)
        canvas.drawText("CIRUGÍA REALIZADA:", colCirugia, y + 14f, paintText)
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = true
        paintText.color = Color.rgb(41, 128, 185)
        canvas.drawText(record.cirugiaRealizada.ifBlank { "Procedimiento laparoscópico estándar" }, colCirugia, y + 27f, paintText)

        y += diagBoxHeight + 14f

        // Section I: Tiempos Quirúrgicos & Cronometraje Concreto
        drawSectionHeader(canvas, left, y, contentWidth, "I. TIEMPOS QUIRÚRGICOS Y CRONOMETRADO CONCRETO")
        y += 18f

        val durationLabel = if (record.duracionSegundos > 0) {
            TimeHelper.formatDurationHuman(record.duracionSegundos)
        } else {
            val calcMins = TimeHelper.calculateMinutesBetween(record.tInicio, record.tFin)
            if (calcMins > 0) "$calcMins MIN" else "--"
        }

        val boxW = (contentWidth - 18f) / 4f
        val boxH = 44f

        drawTimeCard(canvas, left, y, boxW, boxH, "TÉCNICO LISTO", record.tListo.ifBlank { "--:--" }, Color.rgb(39, 174, 96))
        drawTimeCard(canvas, left + boxW + 6f, y, boxW, boxH, "INICIO CIRUGÍA", record.tInicio.ifBlank { "--:--" }, Color.rgb(41, 128, 185))
        drawTimeCard(canvas, left + (boxW + 6f) * 2f, y, boxW, boxH, "FIN CIRUGÍA", record.tFin.ifBlank { "--:--" }, Color.rgb(192, 57, 43))
        drawTimeCard(canvas, left + (boxW + 6f) * 3f, y, boxW, boxH, "DURACIÓN CONCRETA", durationLabel, Color.rgb(52, 73, 94))

        y += boxH + 14f

        // Section II: Supervisión Adscrito
        drawSectionHeader(canvas, left, y, contentWidth, "II. SUPERVISIÓN DEL MÉDICO ADSCRITO")
        y += 18f

        val adscritoH = 40f
        drawBox(canvas, left, y, contentWidth, adscritoH, Color.rgb(248, 249, 250))

        if (record.adsNoEntro) {
            paintFill.color = Color.rgb(253, 237, 236)
            canvas.drawRect(left + 2f, y + 2f, right - 2f, y + adscritoH - 2f, paintFill)

            paintText.textAlign = Paint.Align.CENTER
            paintText.color = Color.rgb(192, 57, 43)
            paintText.textSize = 9.5f
            paintText.isFakeBoldText = true
            canvas.drawText("⚠️ ALERTA FORENSE: EL MÉDICO ADSCRITO NO INGRESÓ A LA SALA QUIRÚRGICA", pageWidth / 2f, y + 24f, paintText)
        } else {
            val adscW = contentWidth / 3f

            paintText.textAlign = Paint.Align.LEFT
            paintText.color = Color.rgb(44, 62, 80)

            paintText.textSize = 7f
            paintText.isFakeBoldText = true
            canvas.drawText("HORA LLEGADA:", left + 10f, y + 15f, paintText)
            paintText.textSize = 9.5f
            paintText.isFakeBoldText = false
            canvas.drawText(record.adsLlegada.ifBlank { "--:--" }, left + 10f, y + 30f, paintText)

            val colAds2 = left + adscW
            paintText.textSize = 7f
            paintText.isFakeBoldText = true
            canvas.drawText("HORA SALIDA:", colAds2 + 10f, y + 15f, paintText)
            paintText.textSize = 9.5f
            paintText.isFakeBoldText = false
            canvas.drawText(record.adsSalida.ifBlank { "--:--" }, colAds2 + 10f, y + 30f, paintText)

            val colAds3 = left + adscW * 2f
            paintText.textSize = 7f
            paintText.isFakeBoldText = true
            canvas.drawText("ESTANCIA TOTAL:", colAds3 + 10f, y + 15f, paintText)
            paintText.textSize = 9.5f
            paintText.isFakeBoldText = true
            paintText.color = Color.rgb(39, 174, 96)
            val estMin = if (record.adsEstanciaMin > 0) "${record.adsEstanciaMin} MINUTOS" else "${TimeHelper.calculateMinutesBetween(record.adsLlegada, record.adsSalida)} MINUTOS"
            canvas.drawText(estMin, colAds3 + 10f, y + 30f, paintText)
        }

        y += adscritoH + 14f

        // Section III: Causas de Desviación y Observaciones
        drawSectionHeader(canvas, left, y, contentWidth, "III. CAUSAS DE DESVIACIÓN Y OBSERVACIONES")
        y += 18f

        val obsBoxH = 50f
        drawBox(canvas, left, y, contentWidth, obsBoxH, Color.rgb(248, 249, 250))

        paintText.textAlign = Paint.Align.LEFT
        paintText.color = Color.rgb(44, 62, 80)
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("FACTOR PRIMARIO DE DESVIACIÓN:", left + 8f, y + 14f, paintText)

        paintText.textSize = 8.5f
        paintText.color = if (record.causa == "Sin Desviación") Color.rgb(39, 174, 96) else Color.rgb(211, 84, 0)
        paintText.isFakeBoldText = true
        canvas.drawText(record.causa, left + 175f, y + 14f, paintText)

        paintText.color = Color.rgb(100, 110, 120)
        paintText.textSize = 7f
        paintText.isFakeBoldText = true
        canvas.drawText("OBSERVACIONES:", left + 8f, y + 28f, paintText)

        paintText.color = Color.rgb(44, 62, 80)
        paintText.textSize = 8f
        paintText.isFakeBoldText = false
        val obsText = record.observaciones.ifBlank { "Sin observaciones adicionales registradas." }
        drawMultilineText(canvas, obsText, left + 8f, y + 40f, contentWidth - 16f, paintText)

        y += obsBoxH + 14f

        // Section IV: Evidencia Documental (Hoja Quirúrgica / Firmas)
        drawSectionHeader(canvas, left, y, contentWidth, "IV. EVIDENCIA DOCUMENTAL Y CONFORMIDAD QUIRÚRGICA")
        y += 18f

        // Check if physical sheet photo is present
        var sheetBitmap: Bitmap? = null
        if (!record.fotoHojaQuirurgicaUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(record.fotoHojaQuirurgicaUri)
                val input: InputStream? = context.contentResolver.openInputStream(uri)
                sheetBitmap = BitmapFactory.decodeStream(input)
                input?.close()
            } catch (_: Exception) {}
        }

        val evBoxH = 110f
        if (sheetBitmap != null) {
            drawBox(canvas, left, y, contentWidth, evBoxH, Color.rgb(255, 255, 255))
            val imgW = 140f
            val imgH = evBoxH - 12f
            val destRect = RectF(left + 8f, y + 6f, left + 8f + imgW, y + 6f + imgH)
            canvas.drawBitmap(sheetBitmap, null, destRect, null)

            // Right side info
            paintText.textAlign = Paint.Align.LEFT
            paintText.color = Color.rgb(44, 62, 80)
            paintText.textSize = 8.5f
            paintText.isFakeBoldText = true
            canvas.drawText("HOJA QUIRÚRGICA ADJUNTADA CON FIRMA FÍSICA", left + imgW + 20f, y + 22f, paintText)

            paintText.textSize = 8f
            paintText.isFakeBoldText = false
            paintText.color = Color.rgb(100, 110, 120)
            canvas.drawText("Firma física validada de: ${record.enfermeraInstrumentista.ifBlank { "Enfermera Instrumentista" }}", left + imgW + 20f, y + 40f, paintText)
            canvas.drawText("Técnico Responsable: ${record.nombreTecnico.ifBlank { "Jesús Abraham Aguilar Osorio" }}", left + imgW + 20f, y + 56f, paintText)
            canvas.drawText("Estado: Documento médico-legal digitalizado conforme a protocolo.", left + imgW + 20f, y + 72f, paintText)
        } else {
            val sigBoxW = (contentWidth - 14f) / 2f
            drawSignatureBox(canvas, left, y, sigBoxW, evBoxH, "TÉCNICO DE LAPAROSCOPIA", record.nombreTecnico.ifBlank { "Jesús Abraham Aguilar Osorio" }, record.cargoTecnico.ifBlank { "Técnico de Laparoscopia" }, record.firmaTecnicoBase64)
            drawSignatureBox(canvas, left + sigBoxW + 14f, y, sigBoxW, evBoxH, "INSTRUMENTISTA / CIRCULANTE", record.enfermeraInstrumentista.ifBlank { record.nombreTestigo.ifBlank { "Enfermería de Quirófano" } }, "Firma y Conformidad Quirúrgica", record.firmaTestigoBase64)
        }

        // Footer
        val footerY = pageHeight - 28f
        paintStroke.strokeWidth = 0.5f
        paintStroke.color = Color.rgb(200, 205, 210)
        canvas.drawLine(left, footerY - 8f, right, footerY - 8f, paintStroke)

        paintText.textAlign = Paint.Align.CENTER
        paintText.color = Color.rgb(140, 150, 160)
        paintText.textSize = 7f
        paintText.isFakeBoldText = false
        canvas.drawText("DOCUMENTO MÉDICO LEGAL OFICIAL • HOSPITAL GENERAL HGV", pageWidth / 2f, footerY, paintText)
        canvas.drawText("SISTEMA DE EVIDENCIA OPERATIVA • PROPIEDAD INTELECTUAL: ${record.nombreTecnico.ifBlank { "JESÚS ABRAHAM AGUILAR OSORIO" }}", pageWidth / 2f, footerY + 9f, paintText)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "Auditoria_HGV_${record.fecha}_${record.paciente.replace("\\s+".toRegex(), "_")}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawSectionHeader(canvas: Canvas, x: Float, y: Float, width: Float, title: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(26, 37, 47) // Navy #1A252F
        }
        canvas.drawRoundRect(RectF(x, y - 10f, x + width, y + 5f), 3f, 3f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 7f
            isFakeBoldText = true
        }
        canvas.drawText(title, x + 8f, y + 1f, textPaint)
    }

    private fun drawBox(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, bgColor: Int) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.rgb(222, 226, 230)
        }
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 4f, 4f, fillPaint)
        canvas.drawRoundRect(rect, 4f, 4f, strokePaint)
    }

    private fun drawTimeCard(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, label: String, value: String, accentColor: Int) {
        drawBox(canvas, x, y, width, height, Color.rgb(248, 249, 250))

        val fillAccent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        canvas.drawRoundRect(RectF(x, y, x + width, y + 3.5f), 2f, 2f, fillAccent)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }

        textPaint.textSize = 6.2f
        textPaint.color = Color.rgb(100, 110, 120)
        textPaint.isFakeBoldText = true
        canvas.drawText(label, x + width / 2f, y + 16f, textPaint)

        textPaint.textSize = 10f
        textPaint.color = accentColor
        textPaint.isFakeBoldText = true
        canvas.drawText(value, x + width / 2f, y + 34f, textPaint)
    }

    private fun drawSignatureBox(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, title: String, name: String, role: String, base64Sig: String?) {
        drawBox(canvas, x, y, width, height, Color.rgb(255, 255, 255))

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }

        textPaint.textSize = 7f
        textPaint.color = Color.rgb(100, 110, 120)
        textPaint.isFakeBoldText = true
        canvas.drawText(title, x + width / 2f, y + 14f, textPaint)

        val sigAreaTop = y + 18f
        val sigAreaBottom = y + height - 28f

        if (!base64Sig.isNullOrBlank()) {
            try {
                val cleanBase64 = if (base64Sig.contains(",")) base64Sig.substringAfter(",") else base64Sig
                val decoded = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                if (bitmap != null) {
                    val destRect = RectF(x + 12f, sigAreaTop, x + width - 12f, sigAreaBottom)
                    canvas.drawBitmap(bitmap, null, destRect, null)
                }
            } catch (_: Exception) {}
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.rgb(50, 50, 50)
        }
        canvas.drawLine(x + 20f, sigAreaBottom + 4f, x + width - 20f, sigAreaBottom + 4f, strokePaint)

        textPaint.textSize = 8f
        textPaint.color = Color.rgb(30, 30, 30)
        textPaint.isFakeBoldText = true
        canvas.drawText(name, x + width / 2f, sigAreaBottom + 16f, textPaint)

        textPaint.textSize = 6.5f
        textPaint.color = Color.rgb(120, 130, 140)
        textPaint.isFakeBoldText = false
        canvas.drawText(role, x + width / 2f, sigAreaBottom + 25f, textPaint)
    }

    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint) {
        val words = text.split(" ")
        var currentLine = ""
        var currentY = y

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) < maxWidth) {
                currentLine = testLine
            } else {
                canvas.drawText(currentLine, x, currentY, paint)
                currentLine = word
                currentY += 10f
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
        }
    }

    fun generateSummaryReportPdf(
        context: Context,
        records: List<AuditRecord>,
        technicianName: String = "Jesús Abraham Aguilar Osorio"
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 842 // A4 Landscape
        val pageHeight = 595
        val left = 24f
        val right = pageWidth - 24f
        val contentWidth = right - left

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
            color = Color.rgb(210, 215, 220)
        }

        val rowsPerPage = 9
        val totalPages = if (records.isEmpty()) 1 else ((records.size - 1) / rowsPerPage) + 1
        var recordIndex = 0

        for (pageNumber in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Header Banner
            paintFill.color = Color.rgb(26, 37, 47) // Navy #1A252F
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 42f, paintFill)

            paintText.color = Color.WHITE
            paintText.textSize = 13f
            paintText.isFakeBoldText = true
            paintText.textAlign = Paint.Align.CENTER
            canvas.drawText("HOSPITAL GENERAL HGV • REPORTE CONSOLIDADO DE CIRUGÍAS LAPAROSCÓPICAS", pageWidth / 2f, 20f, paintText)

            paintText.textSize = 8.5f
            paintText.isFakeBoldText = false
            canvas.drawText("DEPARTAMENTO DE LAPAROSCOPIA • AUDITORÍA Y TRAZABILIDAD QUIRÚRGICA", pageWidth / 2f, 34f, paintText)

            var y = 56f

            // Summary stats banner on first page
            if (pageNumber == 1) {
                val totalCirugias = records.size
                val urgencias = records.count { it.tipoCirugia.equals("Urgencia", ignoreCase = true) }
                val electivas = totalCirugias - urgencias
                val totalSegundos = records.sumOf { it.duracionSegundos }
                val totalHorasMins = if (totalSegundos > 0) TimeHelper.formatDurationHuman(totalSegundos) else "${records.sumOf { TimeHelper.calculateMinutesBetween(it.tInicio, it.tFin) }} min"

                val statBoxW = (contentWidth - 24f) / 4f
                val statBoxH = 34f

                drawStatMiniBox(canvas, left, y, statBoxW, statBoxH, "TOTAL CIRUGÍAS", "$totalCirugias PROCEDIMIENTOS", Color.rgb(41, 128, 185))
                drawStatMiniBox(canvas, left + statBoxW + 8f, y, statBoxW, statBoxH, "CIRUGÍAS ELECTIVAS", "$electivas CIRUGÍAS", Color.rgb(39, 174, 96))
                drawStatMiniBox(canvas, left + (statBoxW + 8f) * 2f, y, statBoxW, statBoxH, "CIRUGÍAS DE URGENCIA", "$urgencias CIRUGÍAS", Color.rgb(192, 57, 43))
                drawStatMiniBox(canvas, left + (statBoxW + 8f) * 3f, y, statBoxW, statBoxH, "TIEMPO QUIRÚRGICO TOTAL", totalHorasMins, Color.rgb(52, 73, 94))

                y += statBoxH + 12f
            }

            // Table Header
            val tableTop = y
            paintFill.color = Color.rgb(41, 128, 185)
            canvas.drawRoundRect(RectF(left, tableTop, right, tableTop + 22f), 3f, 3f, paintFill)

            paintText.color = Color.WHITE
            paintText.textSize = 7.5f
            paintText.isFakeBoldText = true
            paintText.textAlign = Paint.Align.LEFT

            canvas.drawText("FECHA", left + 6f, tableTop + 14f, paintText)
            canvas.drawText("PACIENTE (EDAD)", left + 64f, tableTop + 14f, paintText)
            canvas.drawText("TIPO", left + 190f, tableTop + 14f, paintText)
            canvas.drawText("DIAGNÓSTICO", left + 250f, tableTop + 14f, paintText)
            canvas.drawText("CIRUGÍA REALIZADA", left + 395f, tableTop + 14f, paintText)
            canvas.drawText("MÉDICO TRATANTE", left + 545f, tableTop + 14f, paintText)
            canvas.drawText("INICIO - FIN", left + 655f, tableTop + 14f, paintText)
            canvas.drawText("DURACIÓN", left + 735f, tableTop + 14f, paintText)

            y += 24f
            val rowH = 36f

            val pageRecords = records.drop(recordIndex).take(rowsPerPage)
            for ((idx, rec) in pageRecords.withIndex()) {
                val rowBg = if (idx % 2 == 0) Color.rgb(255, 255, 255) else Color.rgb(245, 248, 250)
                paintFill.color = rowBg
                canvas.drawRect(left, y, right, y + rowH, paintFill)

                canvas.drawLine(left, y + rowH, right, y + rowH, paintStroke)

                // Fecha
                paintText.color = Color.rgb(50, 60, 70)
                paintText.textSize = 7.5f
                paintText.isFakeBoldText = false
                paintText.textAlign = Paint.Align.LEFT
                canvas.drawText(rec.fecha, left + 6f, y + 14f, paintText)

                // Paciente
                paintText.color = Color.rgb(26, 37, 47)
                paintText.textSize = 8f
                paintText.isFakeBoldText = true
                val pacName = rec.paciente.take(22)
                canvas.drawText(pacName, left + 64f, y + 14f, paintText)
                paintText.textSize = 6.8f
                paintText.color = Color.rgb(100, 110, 120)
                paintText.isFakeBoldText = false
                val subPac = if (rec.edad.isNotBlank()) "${rec.edad} a • ${rec.curp.take(10)}" else rec.curp.take(10)
                canvas.drawText(subPac, left + 64f, y + 25f, paintText)

                // Tipo badge
                val isUrg = rec.tipoCirugia.equals("Urgencia", ignoreCase = true)
                paintFill.color = if (isUrg) Color.rgb(253, 237, 236) else Color.rgb(235, 245, 251)
                canvas.drawRoundRect(RectF(left + 188f, y + 5f, left + 242f, y + 21f), 3f, 3f, paintFill)
                paintText.color = if (isUrg) Color.rgb(192, 57, 43) else Color.rgb(41, 128, 185)
                paintText.textSize = 7f
                paintText.isFakeBoldText = true
                paintText.textAlign = Paint.Align.CENTER
                canvas.drawText(rec.tipoCirugia.uppercase(), left + 215f, y + 16f, paintText)

                // Diagnóstico
                paintText.textAlign = Paint.Align.LEFT
                paintText.color = Color.rgb(44, 62, 80)
                paintText.textSize = 7.5f
                paintText.isFakeBoldText = false
                canvas.drawText(rec.diagnostico.ifBlank { "N/E" }.take(28), left + 250f, y + 14f, paintText)

                // Cirugía Realizada
                paintText.color = Color.rgb(41, 128, 185)
                paintText.textSize = 7.5f
                paintText.isFakeBoldText = true
                canvas.drawText(rec.cirugiaRealizada.ifBlank { "Laparoscopía" }.take(28), left + 395f, y + 14f, paintText)
                if (rec.enfermeraInstrumentista.isNotBlank()) {
                    paintText.color = Color.rgb(120, 130, 140)
                    paintText.textSize = 6.5f
                    paintText.isFakeBoldText = false
                    canvas.drawText("Inst: ${rec.enfermeraInstrumentista.take(24)}", left + 395f, y + 25f, paintText)
                }

                // Médico Tratante
                paintText.color = Color.rgb(44, 62, 80)
                paintText.textSize = 7.5f
                paintText.isFakeBoldText = false
                canvas.drawText(rec.medicoTratante.ifBlank { "N/E" }.take(22), left + 545f, y + 14f, paintText)

                // Inicio - Fin
                paintText.color = Color.rgb(60, 70, 80)
                paintText.textSize = 7.5f
                canvas.drawText("${rec.tInicio.ifBlank { "--" }} a ${rec.tFin.ifBlank { "--" }}", left + 655f, y + 14f, paintText)

                // Duración Concreta
                val durStr = if (rec.duracionSegundos > 0) TimeHelper.formatDurationHuman(rec.duracionSegundos) else "${TimeHelper.calculateMinutesBetween(rec.tInicio, rec.tFin)} min"
                paintText.color = Color.rgb(39, 174, 96)
                paintText.isFakeBoldText = true
                canvas.drawText(durStr, left + 735f, y + 14f, paintText)

                y += rowH
                recordIndex++
            }

            // Page Footer
            val footerY = pageHeight - 16f
            canvas.drawLine(left, footerY - 6f, right, footerY - 6f, paintStroke)
            paintText.textAlign = Paint.Align.LEFT
            paintText.color = Color.rgb(130, 140, 150)
            paintText.textSize = 7f
            paintText.isFakeBoldText = false
            canvas.drawText("REPORTE OFICIAL GENERADO POR SISTEMA DE AUDITORÍA HGV • TÉCNICO: ${technicianName.uppercase()}", left, footerY, paintText)

            paintText.textAlign = Paint.Align.RIGHT
            canvas.drawText("Página $pageNumber de $totalPages", right, footerY, paintText)

            pdfDocument.finishPage(page)
        }

        val outputFile = File(context.cacheDir, "Reporte_General_Cirugias_HGV_${TimeHelper.getTodayDate()}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawStatMiniBox(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, title: String, value: String, accentColor: Int) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(248, 249, 250) }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.rgb(222, 226, 230)
        }
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 4f, 4f, fillPaint)
        canvas.drawRoundRect(rect, 4f, 4f, strokePaint)

        val accentBar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        canvas.drawRoundRect(RectF(x, y, x + 3.5f, y + height), 2f, 2f, accentBar)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = 6.2f
        textPaint.color = Color.rgb(110, 120, 130)
        textPaint.isFakeBoldText = true
        canvas.drawText(title, x + 10f, y + 12f, textPaint)

        textPaint.textSize = 8.5f
        textPaint.color = accentColor
        textPaint.isFakeBoldText = true
        canvas.drawText(value, x + 10f, y + 25f, textPaint)
    }
}
