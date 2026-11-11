package com.example.util

import java.util.Calendar

object CurpHelper {

    val OFFICIAL_IMSS_NO_AFILIACION_URL = "https://serviciosdigitales.imss.gob.mx/semanascotizadas-web/solicitud/constanciaNoAfiliacion"
    val OFFICIAL_ISSSTE_NO_AFILIACION_URL = "https://oficinavirtual.issste.gob.mx/Servicios/Constancia-de-No-Afiliacion"

    /**
     * Validates Mexican CURP 18-character syntax.
     */
    fun validateCurpSyntax(curpRaw: String): Boolean {
        val curp = curpRaw.trim().uppercase()
        val regex = Regex("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$")
        return regex.matches(curp)
    }

    /**
     * Extracts estimated age from Mexican CURP (18 alphanumeric characters).
     * Format: AAAA YY MM DD H/M SS XXX NN
     * Returns the calculated age, or null if invalid or incomplete.
     */
    fun calculateAgeFromCurp(curpRaw: String): Int? {
        val curp = curpRaw.trim().uppercase()
        if (curp.length < 10) return null

        try {
            val yyStr = curp.substring(4, 6)
            val mmStr = curp.substring(6, 8)
            val ddStr = curp.substring(8, 10)

            val yy = yyStr.toIntOrNull() ?: return null
            val mm = mmStr.toIntOrNull() ?: return null
            val dd = ddStr.toIntOrNull() ?: return null

            if (mm !in 1..12 || dd !in 1..31) return null

            val today = Calendar.getInstance()
            val currentYear = today.get(Calendar.YEAR)
            val currentTwoDigitYear = currentYear % 100

            val fullYear = if (curp.length >= 17) {
                val centuryChar = curp[16]
                if (centuryChar in 'A'..'Z') {
                    2000 + yy
                } else {
                    1900 + yy
                }
            } else {
                if (yy <= currentTwoDigitYear) 2000 + yy else 1900 + yy
            }

            val birthCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, fullYear)
                set(Calendar.MONTH, mm - 1)
                set(Calendar.DAY_OF_MONTH, dd)
            }

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            return if (age in 0..125) age else null
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Generates a structured clinical Folio derived from CURP when Expediente is blank.
     * Example: CURP LOMA880210MDFRRN09 -> "FOL-LOMA880210-HGV"
     */
    fun generateFolioFromCurp(curpRaw: String): String {
        val curp = curpRaw.trim().uppercase()
        if (curp.length >= 10) {
            val prefix = curp.take(10)
            return "FOL-$prefix-HGV"
        }
        val fallbackHash = Math.abs(curpRaw.hashCode() % 10000)
        return "FOL-HGV-$fallbackHash"
    }
}
