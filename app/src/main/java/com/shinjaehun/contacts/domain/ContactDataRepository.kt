package com.shinjaehun.contacts.domain

import kotlinx.coroutines.flow.Flow

interface ContactDataRepository {
    fun getContacts(): Flow<List<Contact>>
    fun getRecentContacts(amount: Int): Flow<List<Contact>>
    suspend fun insertContact(contact: Contact)
    suspend fun deleteContact(id: Long)
}