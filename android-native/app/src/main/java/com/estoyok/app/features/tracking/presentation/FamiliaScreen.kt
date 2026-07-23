package com.estoyok.app.features.tracking.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.estoyok.app.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaScreen(
    viewModel: FamiliaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var isCircleDropdownExpanded by remember { mutableStateOf(false) }

    // Inputs
    var newCircleName by remember { mutableStateOf("") }
    var inviteCodeInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Avoid overlap with bottom nav bar
    ) {
        // --- HEADER ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 Mi Núcleo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { viewModel.refreshData() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refrescar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Error Banner
        if (viewModel.errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryRed.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = PrimaryRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.errorMessage!!,
                            color = PrimaryRed,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.refreshData() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = PrimaryRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- ACTIVE CIRCLE SELECTOR ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.clickable { isCircleDropdownExpanded = true }
                    ) {
                        Text(
                            text = "Seleccionar Núcleo",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = viewModel.selectedCircle?.name ?: "Sin núcleos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isCircleDropdownExpanded,
                            onDismissRequest = { isCircleDropdownExpanded = false }
                        ) {
                            if (viewModel.circles.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No tienes núcleos aún") },
                                    onClick = { isCircleDropdownExpanded = false }
                                )
                            } else {
                                viewModel.circles.forEach { circle ->
                                    DropdownMenuItem(
                                        text = { Text(circle.name) },
                                        onClick = {
                                            viewModel.selectCircle(circle)
                                            isCircleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Copy Code Button
                    viewModel.selectedCircle?.let { circle ->
                        Button(
                            onClick = {
                                val clip = ClipData.newPlainText("invite_code", circle.inviteCode)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Código copiado: ${circle.inviteCode}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryEmerald,
                                contentColor = TextOnPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar",
                                tint = TextOnPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = circle.inviteCode,
                                color = TextOnPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- CIRCLE MEMBERS LIST ---
        viewModel.selectedCircle?.let { activeCircle ->
            item {
                Text(
                    text = "Miembros del Núcleo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (activeCircle.members.isEmpty()) {
                item {
                    Text(
                        text = "No hay miembros en este núcleo.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            } else {
                items(activeCircle.members) { member ->
                    val isCurrentUser = member.id == (viewModel.user?.id ?: -1)
                    val isOwner = activeCircle.ownerId == viewModel.user?.id
                    val isMemberOwner = member.id == activeCircle.ownerId

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isCurrentUser) "${member.name} (Tú)" else member.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (isMemberOwner) "Creador / Admin" else "Miembro",
                                        fontSize = 11.sp,
                                        color = if (isMemberOwner) PrimaryTeal else TextMuted
                                    )
                                }
                            }

                            // Expel / Leave Action Buttons
                            if (isCurrentUser) {
                                // Leave Circle (only if not the owner)
                                if (!isOwner) {
                                    IconButton(
                                        onClick = { viewModel.removeMember(activeCircle.id, member.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Abandonar",
                                            tint = PrimaryRed
                                        )
                                    }
                                }
                            } else if (isOwner) {
                                // Owner can expel other members
                                IconButton(
                                    onClick = { viewModel.removeMember(activeCircle.id, member.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Expulsar",
                                        tint = PrimaryRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Delete circle button (if owner)
            if (activeCircle.ownerId == (viewModel.user?.id ?: -1)) {
                item {
                    Button(
                        onClick = { viewModel.deleteCircle(activeCircle.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed.copy(alpha = 0.1f), contentColor = PrimaryRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Eliminar Círculo")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar Círculo Completo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- CREATE AND JOIN SECTION ---
        item {
            Text(
                text = "Administrar Núcleos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Join Circle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Unirse a un Núcleo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inviteCodeInput,
                        onValueChange = { inviteCodeInput = it.take(10).uppercase() },
                        label = { Text("Código de invitación") },
                        placeholder = { Text("Ej. ABC123XYZ0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.joinCircle(inviteCodeInput)
                            inviteCodeInput = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = inviteCodeInput.length == 10 && !viewModel.isActionInProgress,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryEmerald,
                            contentColor = TextOnPrimary,
                            disabledContainerColor = PrimaryEmerald.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Unirse al Núcleo", color = TextOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Create Circle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Crear Nuevo Círculo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCircleName,
                        onValueChange = { newCircleName = it },
                        label = { Text("Nombre del núcleo") },
                        placeholder = { Text("Ej. Mi Familia, Amigos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.createCircle(newCircleName)
                            newCircleName = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newCircleName.isNotBlank() && !viewModel.isActionInProgress,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryEmerald,
                            contentColor = TextOnPrimary,
                            disabledContainerColor = PrimaryEmerald.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Crear Núcleo", color = TextOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
