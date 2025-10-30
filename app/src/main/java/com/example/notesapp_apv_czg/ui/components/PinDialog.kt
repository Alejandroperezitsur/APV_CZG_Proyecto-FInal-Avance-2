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
import com.example.notesapp_apv_czg.security.PinManager

@Composable
fun PinDialog(
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    title: String = "Desbloquear nota"
) {
    val context = LocalContext.current
    val isSet = remember { PinManager.isPinSet(context) }
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Column {
                if (!isSet) {
                    Text("Crea un PIN para proteger tus notas")
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter { ch -> ch.isDigit() } },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Confirmar PIN") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                } else {
                    Text("Introduce tu PIN")
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter { ch -> ch.isDigit() } },
                        label = { Text("PIN") },
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
                        error = "El PIN debe tener al menos 4 dígitos"
                        return@Button
                    }
                    if (pin != confirm) {
                        error = "Los PIN no coinciden"
                        return@Button
                    }
                    PinManager.setPin(context, pin)
                    onSuccess()
                } else {
                    if (PinManager.verifyPin(context, pin)) {
                        onSuccess()
                    } else {
                        error = "PIN incorrecto"
                    }
                }
            }) {
                Text(if (!isSet) "Guardar" else "Desbloquear")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) { Text("Cancelar") }
        }
    )
}