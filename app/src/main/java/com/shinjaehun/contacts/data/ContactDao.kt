package com.shinjaehun.contacts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM ContactEntity")
    fun getContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM ContactEntity ORDER BY createdAt DESC LIMIT :amount")
    fun getRecentContacts(amount: Int): Flow<List<ContactEntity>>

    @Query("SELECT * FROM ContactEntity WHERE id = :id")
    suspend fun getContactById(id: Long): ContactEntity

    @Query("DELETE FROM ContactEntity WHERE id = :id")
    suspend fun deleteContact(id: Long)

    @Upsert
    suspend fun insertContactEntity(contactEntity: ContactEntity)
}