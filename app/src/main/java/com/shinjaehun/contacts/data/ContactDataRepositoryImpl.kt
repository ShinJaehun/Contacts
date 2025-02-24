package com.shinjaehun.contacts.data

import android.util.Log
import com.shinjaehun.contacts.domain.Contact
import com.shinjaehun.contacts.domain.ContactDataRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope

private const val TAG = "ContactDataRepositoryImpl"

class ContactDataRepositoryImpl(
    private val dao: ContactDao,
    private val imageStorage: ImageStorage
): ContactDataRepository {

    override fun getContacts(): Flow<List<Contact>> {
        return dao
            .getContacts()
            .map { contactEntities ->
                supervisorScope {
                    contactEntities
                        .map {
                            async { it.toContact(imageStorage) }
                        }
                        .map { it.await() }
                }
            }
    }

    override fun getRecentContacts(amount: Int): Flow<List<Contact>> {
        return dao
            .getRecentContacts(amount)
            .map { recentContactEntities ->
                supervisorScope {
                    recentContactEntities
                        .map {
                            async { it.toContact(imageStorage) }
                        }
                        .map { it.await() }
                }
            }
    }

    override suspend fun insertContact(contact: Contact) {
        Log.i(TAG, "new contact: $contact")
//        val beforeUpdateContact = contact.id?.let { dao.getContactById(it) }
//        val beforeUpdateContactBytes = beforeUpdateContact?.imagePath?.let {
//            Log.i(TAG, "beforeUpdateContact path: $it")
//            imageStorage.getImage(it)
//        }
//
//        val updateContactBytes = contact.photoBytes
//        val isSameImage = beforeUpdateContactBytes != null && updateContactBytes != null &&
//                beforeUpdateContactBytes.contentEquals(updateContactBytes)
//        Log.i(TAG, "isSameImage: $isSameImage")
//
//        val imagePath: String? = if (isSameImage) {
//            beforeUpdateContact?.imagePath
//        } else {
//            beforeUpdateContact?.imagePath?.let { imageStorage.deleteImage(it) }
//            imageStorage.saveImage(updateContactBytes!!)
//        }
//
//        Log.i(TAG, "new Contact image path: $imagePath")
//
//        dao.insertContactEntity(
//            contact.toContactEntity(imagePath)
//        )

        val imagePath: String?
        if (contact.id == null) {
            if (contact.photoBytes != null) {
                Log.i(TAG, "new image!")
                imagePath = contact.photoBytes.let {
                    imageStorage.saveImage(it)
                }
            } else {
                Log.i(TAG, "no image!")
                imagePath = null
            }
        } else {
            val beforeUpdateContact = contact.id.let { dao.getContactById(it) }
            //
            val beforeUpdateContactBytes = beforeUpdateContact.imagePath?.let {
                imageStorage.getImage(it)
            }
            val updateContactBytes = contact.photoBytes
            val isSameImage =
                beforeUpdateContactBytes != null &&
                        updateContactBytes != null &&
                        beforeUpdateContactBytes.contentEquals(updateContactBytes)
            if (isSameImage) {
                Log.i(TAG, "same image!")
                imagePath = beforeUpdateContact.imagePath
            } else {
                Log.i(TAG, "different image!")
                beforeUpdateContact.imagePath?.let {
                    imageStorage.deleteImage(it)
                }
                imagePath = contact.photoBytes?.let {
                    imageStorage.saveImage(it)
                }
            }
            //
//            beforeUpdateContact.imagePath?.let {
//                imageStorage.deleteImage(it)
//            }
//            imagePath = contact.photoBytes?.let {
//                imageStorage.saveImage(it)
//            }
        }
        Log.i(TAG, "new imagePath: $imagePath")
        dao.insertContactEntity(contact.toContactEntity(imagePath))
    }

    override suspend fun deleteContact(id: Long) {
        val entity = dao.getContactById(id)
        entity.imagePath?.let {
            imageStorage.deleteImage(it)
        }
        dao.deleteContact(id)
    }
}