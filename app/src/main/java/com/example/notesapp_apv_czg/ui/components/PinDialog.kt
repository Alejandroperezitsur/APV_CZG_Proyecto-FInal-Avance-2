package com.example.notesapp_apv_czg.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.notesapp_apv_czg.security.PinManager
import com.example.notesapp_apv_czg.R

@Composable
fun PinDialog(
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    title: String? = null
) {
    val context = LocalContext.current
    val isSet = remember { PinManager.isPinSet(context) }
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val dialogTitle = title
        ?: stringResource(R.string.unlock_note_title)

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(dialogTitle) },
        text = {
            Column {
                if (!isSet) {
                    Text(stringResource(R.string.pin_setup_message))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter { ch -> ch.isDigit() } },
                        label = { Text(stringResource(R.string.pin_label)) },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it.filter { ch -> ch.isDigit() } },
                        label = { Text(stringResource(R.string.confirm_pin_label)) },
                        visualTransformation = PasswordVisualTransformation()
                    )
                } else {
                    Text(stringResource(R.string.enter_pin))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter { ch -> ch.isDigit() } },
                        label = { Text(stringResource(R.string.pin_label)) },
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                if (error != null) {
                    Text(text = error!!)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (!isSet) {
                    if (pin.length < 4) {
                        error = context.getString(R.string.pin_too_short_error)
                        return@Button
                    }
                    if (pin != confirm) {
                        error = context.getString(R.string.pin_mismatch_error)
                        return@Button
                    }
                    PinManager.setPin(context, pin)
                    onSuccess()
                } else {
                    if (PinManager.verifyPin(context, pin)) {
                        onSuccess()
                    } else {
                        error = context.getString(R.string.pin_incorrect_error)
                    }
                }
            }) {
                Text(if (!isSet) stringResource(R.string.save) else stringResource(R.string.unlock))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
        }
    )
}