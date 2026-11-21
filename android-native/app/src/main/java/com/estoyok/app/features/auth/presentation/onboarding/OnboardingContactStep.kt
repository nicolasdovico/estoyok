package com.estoyok.app.features.auth.presentation.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estoyok.app.core.theme.*

@Composable
fun OnboardingContactStep(
    onContactSaved: (String) -> Unit,
    onSkip: () -> Unit,
    onSaveContactApi: (name: String, phone: String, email: String, relationship: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Familiar") }
    var isSaving by remember { mutableStateOf(false) }

    val relationships = listOf("Madre", "Padre", "Hijo/a", "Pareja", "Hermano/a", "Amigo/a")

    // Launcher nativo para elegir contacto desde la agenda del teléfono
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            if (contactUri != null) {
                try {
                    val cursor = context.contentResolver.query(
                        contactUri,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null,
                        null,
                        null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (nameIndex != -1) {
                                name = it.getString(nameIndex) ?: ""
                            }
                            if (numberIndex != -1) {
                                val rawNumber = it.getString(numberIndex) ?: ""
                                phone = rawNumber.replace(" ", "").replace("-", "")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "No se pudo leer el contacto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Validaciones
    val cleanedPhone = phone.trim().replace(" ", "").replace("-", "")
    val withPlus = if (cleanedPhone.startsWith("+")) cleanedPhone else "+$cleanedPhone"
    val digitsOnly = withPlus.drop(1)
    val isDigitsValid = digitsOnly.all { it.isDigit() }
    val isArgPrefix = withPlus.startsWith("+54")
    val minPhoneDigits = if (isArgPrefix) 12 else 10
    val maxPhoneDigits = if (isArgPrefix) 13 else 15

    val isNameValid = name.trim().isNotBlank()
    val isPhoneValid = phone.trim().isNotBlank() && isDigitsValid && digitsOnly.length in minPhoneDigits..maxPhoneDigits
    val isEmailValid = email.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isFormValid = isNameValid && isPhoneValid && isEmailValid

    BackHandler {
        onSkip()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar con Omitir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "PASO FINAL DE ACTIVACIÓN 🛡️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryEmerald,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                TextButton(
                    onClick = onSkip,
                    colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
                ) {
                    Text(
                        text = "Omitir por ahora",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ícono visual
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimaryEmerald.copy(alpha = 0.12f))
                    .border(2.dp, PrimaryEmerald.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = PrimaryEmerald,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Título y Bajada
            Text(
                text = "¿A quién debemos cuidar y avisar?",
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Agregá a un familiar o persona de confianza para que reciba tu alerta por WhatsApp si no confirmás tu reporte o si activás un SOS.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Botón rápido para elegir de contactos
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                    contactPickerLauncher.launch(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryEmerald.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryEmerald
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContactPhone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Elegir de mis contactos del celular",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tarjeta con Campos del Formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Campo Nombre
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre o Apodo *", fontSize = 12.sp) },
                        placeholder = { Text("Ej. Mamá, Lucas, María", fontSize = 12.sp, color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = PrimaryEmerald
                        )
                    )

                    // Campo Teléfono WhatsApp
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("WhatsApp (Con código de país y área) *", fontSize = 12.sp) },
                        placeholder = { Text("Ej: +54 9 11 1234-5678", fontSize = 12.sp, color = TextMuted) },
                        supportingText = {
                            if (phone.isNotEmpty() && !isPhoneValid) {
                                Text(
                                    text = if (!isDigitsValid) "Solo números y '+' al inicio" else "Mínimo $minPhoneDigits dígitos con código de país y área",
                                    color = PrimaryRed,
                                    fontSize = 10.5.sp
                                )
                            } else {
                                Text(
                                    text = "Recibirá la ubicación en tiempo real ante alertas.",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        },
                        isError = phone.isNotEmpty() && !isPhoneValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = PrimaryEmerald,
                            errorBorderColor = PrimaryRed
                        )
                    )

                    // Parentesco (Pastillas Rápidas)
                    Text(
                        text = "Parentesco:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        relationships.take(3).forEach { rel ->
                            val isSelected = relationship.equals(rel, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { relationship = rel },
                                label = { Text(rel, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryEmerald.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryEmerald,
                                    containerColor = DarkBackground,
                                    labelColor = TextMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) PrimaryEmerald else BorderColor
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        relationships.drop(3).forEach { rel ->
                            val isSelected = relationship.equals(rel, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { relationship = rel },
                                label = { Text(rel, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryEmerald.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryEmerald,
                                    containerColor = DarkBackground,
                                    labelColor = TextMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) PrimaryEmerald else BorderColor
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Botón de Confirmación Principal
            Button(
                onClick = {
                    if (isFormValid && !isSaving) {
                        isSaving = true
                        val formattedPhone = if (cleanedPhone.startsWith("+")) cleanedPhone else "+$cleanedPhone"
                        val savedName = name.trim()
                        onSaveContactApi(
                            savedName,
                            formattedPhone,
                            email.trim(),
                            relationship.trim(),
                            { finalName ->
                                isSaving = false
                                onContactSaved(finalName)
                            },
                            { errorMsg ->
                                isSaving = false
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                enabled = isFormValid && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryEmerald,
                    contentColor = TextOnPrimary,
                    disabledContainerColor = PrimaryEmerald.copy(alpha = 0.4f),
                    disabledContentColor = TextOnPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextOnPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Guardando contacto...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Guardar y Activar Protección 🛡️",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
