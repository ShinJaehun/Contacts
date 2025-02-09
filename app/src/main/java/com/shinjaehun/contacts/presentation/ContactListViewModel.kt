package com.shinjaehun.contacts.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shinjaehun.contacts.domain.Contact
import com.shinjaehun.contacts.domain.ContactDataRepository
import com.shinjaehun.contacts.domain.ContactValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ContactListViewModel"

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repository: ContactDataRepository
): ViewModel() {

//    private val _state = MutableStateFlow(ContactListState(
//        contacts = contacts
//    ))
//    val state = _state.asStateFlow()
//
//    var newContact: Contact? by mutableStateOf(null)
//        private set
//
//    fun onEvent(event: ContactListEvent) {
//
//    }

    private val _state = MutableStateFlow(ContactListState())
    val state = combine(
        _state,
        repository.getContacts(),
        repository.getRecentContacts(20)
    ) { state, contacts, recentContacts ->
        state.copy(
            contacts = contacts,
            recentlyAddedContacts = recentContacts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), ContactListState())

    var newContact: Contact? by mutableStateOf(null)
        private set

    fun onEvent(event: ContactListEvent) {
        when(event) {
            ContactListEvent.DeleteContact -> {
                viewModelScope.launch {
                    _state.value.selectedContact?.id?.let { id ->
                        _state.update { it.copy(
                            isSelectedContactSheetOpen = false
                        ) }
                        repository.deleteContact(id)
                        delay(300L) // Animation delay
                        _state.update { it.copy(
                            selectedContact = null
                        ) }
                    }
                }
            }
            ContactListEvent.DismissContact -> {
                viewModelScope.launch {
                    _state.update { it.copy(
                        isSelectedContactSheetOpen = false,
                        isAddContactSheetOpen = false,
                        firstNameError = null,
                        lastNameError = null,
                        emailError = null,
                        phoneNumberError = null
                    ) }
                    delay(300L) // Animation delay
                    newContact = null
                    _state.update { it.copy(
                        selectedContact = null
                    ) }
                }
            }
            is ContactListEvent.EditContact -> {
//                _state.update { it.copy(
//                    selectedContact = null,
//                    isAddContactSheetOpen = true,
//                    isSelectedContactSheetOpen = false
//                ) }
//                newContact = event.contact
                _state.update { it.copy(
                    selectedContact = null,
                    isAddContactSheetOpen = true,
                    isSelectedContactSheetOpen = false
                ) }
                newContact = event.contact
            }
            ContactListEvent.OnAddNewContactClick -> {
                _state.update { it.copy(
                    isAddContactSheetOpen = true
                ) }
                newContact = Contact(
                    id = null,
                    firstName = "",
                    lastName = "",
                    email = "",
                    phoneNumber = "",
//                    imagePath = null, //////////////////////
                    photoBytes = null
                )
            }
            is ContactListEvent.OnEmailChanged -> {
                newContact = newContact?.copy(
                    email = event.value
                )
            }
            is ContactListEvent.OnFirstNameChanged -> {
                newContact = newContact?.copy(
                    firstName = event.value
                )
            }
            is ContactListEvent.OnLastNameChanged -> {
                newContact = newContact?.copy(
                    lastName = event.value
                )
            }
            is ContactListEvent.OnPhoneNumberChanged -> {
                newContact = newContact?.copy(
                    phoneNumber = event.value
                )
            }
            is ContactListEvent.OnPhotoPicked -> {
                newContact = newContact?.copy(
                    photoBytes = event.bytes
                )
            }
            ContactListEvent.SaveContact -> {
                newContact?.let { contact ->

                    val result = ContactValidator.validateContact(contact)
                    val errors = listOfNotNull(
                        result.firstNameError,
                        result.lastNameError,
                        result.emailError,
                        result.phoneNumberError
                    )

                    if(errors.isEmpty()) {
                        _state.update { it.copy(
                            isAddContactSheetOpen = false,
                            firstNameError = null,
                            lastNameError = null,
                            emailError = null,
                            phoneNumberError = null
                        ) }
                        viewModelScope.launch {
                            repository.insertContact(contact)
                            delay(300L) // Animation delay
                            newContact = null
                        }
                    } else {
                        _state.update { it.copy(
                            firstNameError = result.firstNameError,
                            lastNameError = result.lastNameError,
                            emailError = result.emailError,
                            phoneNumberError = result.phoneNumberError
                        ) }
                    }
                }
            }
            is ContactListEvent.SelectContact -> {
                _state.update { it.copy(
                    selectedContact = event.contact,
                    isSelectedContactSheetOpen = true
                ) }
            }
            else -> Unit
        }
    }
}

//private val contacts = (1..50).map {
//    Contact(
//        id = it.toLong(),
//        firstName = "First$it",
//        lastName = "Last$it",
//        email = "test$it@test.com",
//        phoneNumber = "1234456897954",
//        photoBytes = null
//    )
//}