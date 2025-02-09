package com.shinjaehun.contacts.presentation

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shinjaehun.contacts.presentation.components.ContactListItem
import com.shinjaehun.contacts.presentation.components.RecentlyAddedContacts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shinjaehun.contacts.presentation.components.AddContactSheet
import com.shinjaehun.contacts.presentation.components.ContactDetailSheet

private const val TAG = "ContactListScreen"

@Composable
fun ContactListScreen(
    viewModel: ContactListViewModel = viewModel(),
    imagePicker: ImagePicker
) {
    val state by viewModel.state.collectAsState()
    val onEvent = viewModel::onEvent
    val newContact = viewModel.newContact

    imagePicker.registerPicker { imageBytes ->
        onEvent(ContactListEvent.OnPhotoPicked(imageBytes))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onEvent(ContactListEvent.OnAddNewContactClick)
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Add contact"
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                RecentlyAddedContacts(
                    contacts = state.recentlyAddedContacts,
                    onClick = {
                        onEvent(ContactListEvent.SelectContact(it))
                    }
                )
            }

            item {
                Text(
                    text = "My contacts (${state.contacts.size})",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.contacts) { contact ->
                ContactListItem(
                    contact = contact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onEvent(ContactListEvent.SelectContact(contact))
                        }
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }

    ContactDetailSheet(
        isOpen = state.isSelectedContactSheetOpen,
        selectedContact = state.selectedContact,
        onEvent = onEvent,
    )
    AddContactSheet(
        state = state,
        newContact = newContact,
        isOpen = state.isAddContactSheetOpen,
        onEvent = { event ->
            if(event is ContactListEvent.OnAddPhotoClicked) {
                imagePicker.pickImage()
            }
            onEvent(event)
        },
    )
}