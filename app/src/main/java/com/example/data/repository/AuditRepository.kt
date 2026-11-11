package com.example.data.repository

import com.example.data.db.AuditDao
import com.example.data.db.PersonalSaludDao
import com.example.data.model.AuditDraft
import com.example.data.model.AuditRecord
import com.example.data.model.PersonalSalud
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class AuditRepository(
    private val auditDao: AuditDao,
    private val personalSaludDao: PersonalSaludDao? = null
) {
    val allRecords: Flow<List<AuditRecord>> = auditDao.getAllRecords()

    val allPersonal: Flow<List<PersonalSalud>> = personalSaludDao?.getAllPersonal() ?: emptyFlow()
    val allDoctors: Flow<List<PersonalSalud>> = personalSaludDao?.getPersonalByRol("MÉDICO") ?: emptyFlow()
    val allNurses: Flow<List<PersonalSalud>> = personalSaludDao?.getPersonalByRol("ENFERMERO") ?: emptyFlow()

    fun getRecordsByTipo(tipo: String): Flow<List<AuditRecord>> = auditDao.getRecordsByTipo(tipo)

    fun searchRecords(query: String): Flow<List<AuditRecord>> = auditDao.searchRecords(query)

    suspend fun getRecordById(id: String): AuditRecord? = auditDao.getRecordById(id)

    suspend fun saveRecord(record: AuditRecord) = auditDao.insertRecord(record)

    suspend fun saveRecords(records: List<AuditRecord>) = auditDao.insertRecords(records)

    suspend fun deleteRecord(id: String) = auditDao.deleteRecordById(id)

    suspend fun deleteAllRecords() = auditDao.deleteAllRecords()

    suspend fun getDraft(): AuditDraft? = auditDao.getDraft()

    suspend fun saveDraft(draft: AuditDraft) = auditDao.saveDraft(draft)

    suspend fun clearDraft() = auditDao.clearDraft()

    // PersonalSalud CRUD
    suspend fun insertPersonal(personal: PersonalSalud) {
        personalSaludDao?.insertPersonal(personal)
    }

    suspend fun updatePersonal(personal: PersonalSalud) {
        personalSaludDao?.updatePersonal(personal)
    }

    suspend fun deletePersonal(personal: PersonalSalud) {
        personalSaludDao?.deletePersonal(personal)
    }

    suspend fun deletePersonalById(id: String) {
        personalSaludDao?.deletePersonalById(id)
    }
}
