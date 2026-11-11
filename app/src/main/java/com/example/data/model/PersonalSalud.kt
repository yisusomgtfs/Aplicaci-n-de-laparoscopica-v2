package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "personal_salud")
data class PersonalSalud(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val rol: String, // "MÉDICO" o "ENFERMERO"
    val especialidad: String = "",
    val cedula: String = "",
    val activo: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
