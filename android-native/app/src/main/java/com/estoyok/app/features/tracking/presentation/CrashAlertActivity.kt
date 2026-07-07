package com.estoyok.app.features.tracking.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.estoyok.app.core.theme.EstoyOkTheme
import com.estoyok.app.core.theme.PrimaryRed
import com.estoyok.app.core.util.Resource
import com.estoyok.app.features.wellbeing.domain.repository.EmergencyContactsRepository
import com.estoyok.app.features.tracking.domain.repository.CrashRepository
import com.estoyok.app.services.TrackingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class CrashAlertActivity : ComponentActivity() {

    @Inject
    lateinit var crashRepository: CrashRepository

    @Inject
    lateinit var contactsRepository: EmergencyContactsRepository

    private var ringtone: Ringtone? = null
    private var countdownJob: Job? = null

    // Extras
    private var latitude = 0.0
    private var longitude = 0.0
    private var speed = 0.0f
    private var gForce = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        speed = intent.getFloatExtra("speed", 0.0f)
        gForce = intent.getFloatExtra("g_force", 0.0f)

        // Start siren alarm sound
        startSiren()

        setContent {
            EstoyOkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1C0D0D) // Red-dark background
                ) {
                    CrashAlertContent(
                        gForce = gForce,
                        onCancel = {
                            cancelAlert()
                        },
                        onTimeoutExpired = {
                            triggerEmergencyProtocol()
                        }
                    )
                }
            }
        }
    }

    private fun startSiren() {
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSiren() {
        ringtone?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
        ringtone = null
    }

    private fun cancelAlert() {
        stopSiren()
        finish()
    }

    private fun triggerEmergencyProtocol() {
        stopSiren()

        // 1. Report crash to backend API
        CoroutineScope(Dispatchers.IO).launch {
            crashRepository.reportCrash(latitude, longitude, speed, gForce).collectLatest { resource ->
                // Handled in backend, dispatches notifications & Twilio jobs
            }

            // 2. Fetch emergency contacts and send local fallback SMS
            contactsRepository.getContacts().collectLatest { resource ->
                if (resource is Resource.Success) {
                    val contactsList = resource.data ?: emptyList()
                    val smsText = "[Estoy Ok] ALERTA CRÍTICA: Se ha detectado una colisión vehicular. Mi ubicación actual: https://maps.google.com/?q=$latitude,$longitude (Lat: $latitude, Lng: $longitude). Por favor contáctame urgente."
                    
                    if (ActivityCompat.checkSelfPermission(
                            this@CrashAlertActivity,
                            Manifest.permission.SEND_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val smsManager = SmsManager.getDefault()
                        for (contact in contactsList) {
                            if (contact.isActive) {
                                try {
                                    smsManager.sendTextMessage(contact.phone, null, smsText, null, null)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Accelerate tracking frequency to 5s
        val trackingIntent = Intent(this, TrackingService::class.java).apply {
            action = TrackingService.ACTION_UPDATE_INTERVAL
            putExtra(TrackingService.EXTRA_INTERVAL, 5000L)
        }
        startService(trackingIntent)

        // Close countdown activity
        finish()
    }

    override fun onBackPressed() {
        // Intercept back button to prevent accidental bypass
        // User must click the "Estoy bien" button explicitly
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSiren()
    }
}

@Composable
fun CrashAlertContent(
    gForce: Float,
    onCancel: () -> Unit,
    onTimeoutExpired: () -> Unit
) {
    var secondsRemaining by remember { mutableStateOf(15) }

    LaunchedEffect(key1 = true) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        onTimeoutExpired()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Text(
                text = "¡PATRÓN DE IMPACTO DETECTADO!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryRed,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Fuerza G: ${String.format("%.2f", gForce)}G",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "El sistema enviará una alerta de emergencia máxima a tus contactos si no respondes a esta pre-alerta.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // Big countdown indicator
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(PrimaryRed.copy(alpha = 0.1f), CircleShape)
                .background(PrimaryRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = secondsRemaining.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryRed
            )
        }

        // Cancel Button
        Button(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = PrimaryRed
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "ESTOY BIEN • CANCELAR",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
