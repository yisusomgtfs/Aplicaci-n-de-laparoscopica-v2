package com.example.ui.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.AuditViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profile by viewModel.profile.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Recovery dialog state
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryWordInput by remember { mutableStateOf("") }
    var recoveryStage by remember { mutableStateOf(0) } // 0: enter word, 1: enter new pin
    var newPinInput by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf<String?>(null) }

    fun triggerBiometricPrompt() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.unlockByBiometrics()
                    viewModel.showToast("✔ Autenticación biométrica exitosa")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel.showToast("Huella no disponible: $errString")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Quirúrgico HGV")
            .setSubtitle("Autenticación biométrica del técnico")
            .setNegativeButtonText("Usar PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun submitPin() {
        scope.launch {
            if (pinInput.length < 4) {
                loginError = "El PIN debe tener 4 dígitos"
                return@launch
            }
            val valid = viewModel.verifyPin(pinInput)
            if (valid) {
                loginError = null
                pinInput = ""
            } else {
                loginError = "PIN incorrecto. Acceso denegado."
                pinInput = ""
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyPrimary)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF22303C))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Medical Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BlueAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = "HGV Hospital Logo",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AUDITORÍA HGV",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "DEPARTAMENTO DE LAPAROSCOPIA",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3498DB),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Autor: ${profile.nombre}",
                fontSize = 12.sp,
                color = Color(0xFFBDC3C7),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Input Field
            OutlinedTextField(
                value = pinInput,
                onValueChange = {
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        pinInput = it
                        loginError = null
                        if (it.length == 4) {
                            submitPin()
                        }
                    }
                },
                placeholder = {
                    Text(
                        "••••",
                        color = Color(0xFF7F8C8D),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                singleLine = true,
                visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submitPin() }),
                trailingIcon = {
                    IconButton(onClick = { pinVisible = !pinVisible }) {
                        Icon(
                            imageVector = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (pinVisible) "Ocultar PIN" else "Mostrar PIN",
                            tint = Color(0xFFBDC3C7)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = Color(0xFF4A5568),
                    focusedContainerColor = Color(0xFF1A252F),
                    unfocusedContainerColor = Color(0xFF1A252F)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(200.dp)
                    .testTag("pin_input")
            )

            AnimatedVisibility(visible = loginError != null) {
                Text(
                    text = loginError ?: "",
                    color = Color(0xFFE74C3C),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons: PIN and Biometrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { submitPin() },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("login_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PIN", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { triggerBiometricPrompt() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("biometric_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HUELLA", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = {
                    showRecoveryDialog = true
                    recoveryStage = 0
                    recoveryWordInput = ""
                    newPinInput = ""
                    recoveryError = null
                },
                modifier = Modifier.testTag("forgot_pin_button")
            ) {
                Text(
                    text = "¿Olvidó su PIN?",
                    color = Color(0xFFBDC3C7),
                    fontSize = 13.sp
                )
            }
        }
    }

    // Recovery Modal
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = {
                Text(
                    text = if (recoveryStage == 0) "Recuperar Acceso" else "Establecer Nuevo PIN",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    if (recoveryStage == 0) {
                        Text(
                            text = "Ingrese su Palabra Secreta de Recuperación (Por defecto: HGV)",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = recoveryWordInput,
                            onValueChange = {
                                recoveryWordInput = it
                                recoveryError = null
                            },
                            placeholder = { Text("Palabra Secreta...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("recovery_word_input")
                        )
                    } else {
                        Text(
                            text = "Palabra correcta. Ingrese el nuevo PIN de 4 dígitos:",
                            fontSize = 13.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    newPinInput = it
                                    recoveryError = null
                                }
                            },
                            placeholder = { Text("Nuevo PIN (4 dígitos)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth().testTag("new_pin_input")
                        )
                    }

                    if (recoveryError != null) {
                        Text(
                            text = recoveryError ?: "",
                            color = Color(0xFFE74C3C),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (recoveryStage == 0) {
                                val match = viewModel.verifyRecoveryWord(recoveryWordInput)
                                if (match) {
                                    recoveryStage = 1
                                    recoveryError = null
                                } else {
                                    recoveryError = "Palabra Secreta Incorrecta."
                                }
                            } else {
                                if (newPinInput.length == 4) {
                                    val success = viewModel.resetPin(newPinInput)
                                    if (success) {
                                        viewModel.showToast("PIN actualizado con éxito")
                                        showRecoveryDialog = false
                                    }
                                } else {
                                    recoveryError = "El PIN debe tener 4 dígitos numéricos."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text(if (recoveryStage == 0) "Validar Palabra" else "Guardar Nuevo PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
