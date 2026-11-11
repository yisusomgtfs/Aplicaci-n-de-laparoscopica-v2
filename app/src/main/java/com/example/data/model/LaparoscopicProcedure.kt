package com.example.data.model

data class LaparoscopicProcedure(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nombre: String,
    val especialidad: String = "Cirugía Laparoscópica",
    val diagnosticoDefecto: String = "",
    val descripcionQuirurgica: String = "",
    val puntosClave: List<String> = emptyList()
)
