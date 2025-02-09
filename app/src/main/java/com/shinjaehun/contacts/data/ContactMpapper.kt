package com.shinjaehun.contacts.data

import com.shinjaehun.contacts.domain.Contact
import kotlinx.datetime.Clock

suspend fun ContactEntity.toContact(imageStorage: ImageStorage): Contact {
    return Contact(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phoneNumber = phoneNumber,
//        imagePath = imagePath,
        photoBytes = imagePath?.let { imageStorage.getImage(it) },
    )
}

fun Contact.toContactEntity(imagePath: String?): ContactEntity {
    return ContactEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phoneNumber = phoneNumber,
        imagePath = imagePath,
        createdAt = Clock.System.now().toEpochMilliseconds(),
    )
}