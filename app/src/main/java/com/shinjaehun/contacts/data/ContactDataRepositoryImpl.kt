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
        val beforeUpdateContact = contact.id?.let { dao.getContactById(it) }
        val beforeUpdateContactBytes = beforeUpdateContact?.imagePath?.let {
            Log.i(TAG, "beforeUpdateContact path: $it")
            imageStorage.getImage(it)
        }

        // Contact에 imagePath를 넣어두면 db 호출을 한번 덜 할 수 있는데
        // 문제가 photoBytes와 다른 이미지를 가리키는 경우가 존재할 수 있음...
//        val bytes = contact.imagePath?.let {
//            Log.i(TAG, "Contact image path: $it")
//            imageStorage.getImage(it)
//        }

        // 멍청하게도... 지금까지 계속 photoBytes를 이미지 사이즈로만 이해하고 있었음...
        // 이거 자체가 byteArray인 이미지 자체임
        val updateContactBytes = contact.photoBytes
        // viewModel에서 OnPhotoPicked 이벤트로 변경된 이미지의 바이트 어레이가 저장되어 있음...
        val isSameImage = beforeUpdateContactBytes != null && updateContactBytes != null &&
                beforeUpdateContactBytes.contentEquals(updateContactBytes)
        Log.i(TAG, "isSameImage: $isSameImage")

        val imagePath: String? = if (isSameImage) {
            beforeUpdateContact?.imagePath
        } else {
            // 이미지가 바뀌었으면 바뀌기 전 이미지는 삭제
            beforeUpdateContact?.imagePath?.let { imageStorage.deleteImage(it) }
            imageStorage.saveImage(updateContactBytes!!)
        }

//        val imagePath: String? = if (isSameImage) {
//            contact.imagePath
//        } else {
//            contact.imagePath?.let { imageStorage.deleteImage(it) }
//            imageStorage.saveImage(updateContactBytes!!)
//        }

        Log.i(TAG, "new Contact image path: $imagePath")

        // 이렇게 하면 안 되는거지...
//        Log.i(TAG, "contact path: ${contact.imagePath}")
//        Log.i(TAG, "in db path? ${contact.id?.let { dao.getContactById(it).imagePath }}")
//        val imagePath = contact.photoBytes?.let {
//            imageStorage.saveImage(it)
//        }
        dao.insertContactEntity(
            contact.toContactEntity(imagePath)
        )
    }

    override suspend fun deleteContact(id: Long) {
        val entity = dao.getContactById(id)
        entity.imagePath?.let {
            imageStorage.deleteImage(it)
        }
        dao.deleteContact(id)
    }
}