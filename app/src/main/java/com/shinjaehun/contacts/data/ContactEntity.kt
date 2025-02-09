package com.shinjaehun.contacts.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val imagePath: String?,
    val createdAt:Long
)
