package com.estoyok.app.features.tracking.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

    var selectedPayProvider by remember { mutableStateOf("stripe") }

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 Mi Núcleo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = circle.inviteCode,
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
                                            imageVector = Icons.Default.ExitToApp,
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
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.joinCircle(inviteCodeInput)
                            inviteCodeInput = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = inviteCodeInput.length == 10 && !viewModel.isActionInProgress,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Unirse al Núcleo", fontWeight = FontWeight.Bold)
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
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.createCircle(newCircleName)
                            newCircleName = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newCircleName.isNotBlank() && !viewModel.isActionInProgress,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Crear Núcleo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- PREMIUM PROMOTION SECTION ---
        item {
            val isPremium = viewModel.user?.isPremium == true

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isPremium) DarkSurfaceVariant else Color(0xFF0F1E15)), // Green hue if free
                border = if (isPremium) null else BorderStroke(1.dp, PrimaryEmerald)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isPremium) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Plan Premium Activo 👑",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tienes acceso ilimitado a todas las características del sistema de seguridad familiar.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "¡Pásate a Premium! 👑",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryEmerald
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Protege a tu familia con las mejores características:",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text("✓ Historial de geolocalización ilimitado.", fontSize = 12.sp, color = TextSecondary)
                            Text("✓ Detección de colisiones e impactos G.", fontSize = 12.sp, color = TextSecondary)
                            Text("✓ Webhooks automáticos SMS/WhatsApp.", fontSize = 12.sp, color = TextSecondary)
                            Text("✓ Alertas inteligentes de velocidad en coche.", fontSize = 12.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Payment selector chips
                        Text(
                            text = "Seleccionar Medio de Pago:",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("stripe" to "Stripe", "mercadopago" to "MercadoPago", "paypal" to "PayPal").forEach { (id, label) ->
                                val selected = selectedPayProvider == id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary else DarkSurfaceVariant)
                                        .clickable { selectedPayProvider = id }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.White else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Subscribe CTA Button
                        Button(
                            onClick = {
                                viewModel.checkoutSubscription(selectedPayProvider) { checkoutUrl ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.checkoutLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                        ) {
                            if (viewModel.checkoutLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Text("Suscribirse ahora ($4.99/mes)", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
