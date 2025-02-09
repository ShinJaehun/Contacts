package com.shinjaehun.contacts.presentation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class ImagePickerFactory {

    @Composable
    fun createPicker(): ImagePicker {
        val activity = LocalActivity.current
        return remember(activity) {
            ImagePicker(activity)
        }
    }
}