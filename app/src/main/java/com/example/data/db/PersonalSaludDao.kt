package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PersonalSalud
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalSaludDao {
    @Query("SELECT * FROM personal_salud ORDER BY nombre ASC")
    fun getAllPersonal(): Flow<List<PersonalSalud>>

    @Query("SELECT * FROM personal_salud WHERE rol = :rol AND activo = 1 ORDER BY nombre ASC")
    fun getPersonalByRol(rol: String): Flow<List<PersonalSalud>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonal(personal: PersonalSalud)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(personalList: List<PersonalSalud>)

    @Update
    suspend fun updatePersonal(personal: PersonalSalud)

    @Delete
    suspend fun deletePersonal(personal: PersonalSalud)

    @Query("DELETE FROM personal_salud WHERE id = :id")
    suspend fun deletePersonalById(id: String)
}
