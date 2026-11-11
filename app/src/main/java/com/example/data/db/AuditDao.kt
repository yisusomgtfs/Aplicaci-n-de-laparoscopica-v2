package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AuditDraft
import com.example.data.model.AuditRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AuditRecord>>

    @Query("SELECT * FROM audit_records WHERE tipoCirugia = :tipo ORDER BY timestamp DESC")
    fun getRecordsByTipo(tipo: String): Flow<List<AuditRecord>>

    @Query("SELECT * FROM audit_records WHERE paciente LIKE '%' || :query || '%' OR diagnostico LIKE '%' || :query || '%' OR medicoTratante LIKE '%' || :query || '%' OR cirugiaRealizada LIKE '%' || :query || '%' OR curp LIKE '%' || :query || '%' OR expediente LIKE '%' || :query || '%' OR folioCurp LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRecords(query: String): Flow<List<AuditRecord>>

    @Query("SELECT * FROM audit_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): AuditRecord?

    @Query("SELECT * FROM audit_records WHERE expediente = :query OR folioCurp = :query OR curp = :query LIMIT 1")
    suspend fun getRecordByExpedienteOrFolio(query: String): AuditRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AuditRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AuditRecord>)

    @Query("DELETE FROM audit_records WHERE id = :id")
    suspend fun deleteRecordById(id: String)

    @Query("DELETE FROM audit_records")
    suspend fun deleteAllRecords()

    @Query("SELECT * FROM audit_draft WHERE id = 'draft_activo' LIMIT 1")
    suspend fun getDraft(): AuditDraft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: AuditDraft)

    @Query("DELETE FROM audit_draft WHERE id = 'draft_activo'")
    suspend fun clearDraft()
}
