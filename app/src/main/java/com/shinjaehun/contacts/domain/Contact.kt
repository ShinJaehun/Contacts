package com.shinjaehun.contacts.domain

data class Contact(
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
//    val imagePath: String?,
    val photoBytes: ByteArray?
)