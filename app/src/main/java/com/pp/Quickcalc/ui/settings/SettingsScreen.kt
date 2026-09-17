package com.pp.Quickcalc.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pp.Quickcalc.model.UserPrefs
import com.pp.Quickcalc.ui.theme.DarkBackground

import androidx.compose.material.icons.filled.Refresh

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext

fun openPlayStore(context: Context) {
    val packageName = context.packageName
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

@Composable
fun SettingsScreen(
    prefs: UserPrefs,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    @Suppress("UNUSED_PARAMETER") onThemeToggle: (Boolean) -> Unit,
    onResetProgress: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showPrivacyModal by remember { mutableStateOf(false) }
    var showResetModal by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun shareApp() {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "QuickCalc: Math IQ Game")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out QuickCalc: Math IQ Game! Challenge your mental math speed with this awesome game: https://play.google.com/store/apps/details?id=${context.packageName}"
                )
            }
            context.startActivity(Intent.createChooser(intent, "Share QuickCalc via"))
        } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        SettingsToggleRow(
            label = "Sound Effects",
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            checked = prefs.soundEnabled,
            onCheckedChange = onSoundToggle
        )
        SettingsToggleRow(
            label = "Vibration",
            icon = Icons.Filled.Vibration,
            checked = prefs.vibrationEnabled,
            onCheckedChange = onVibrationToggle
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.15f))

        // Non-toggle rows
        SettingsLinkRow(
            label = "Reset Game Progress",
            icon = Icons.Filled.Refresh,
            iconTint = Color(0xFFEF4444),
            labelColor = Color(0xFFEF4444),
            onClick = { showResetModal = true }
        )

        SettingsLinkRow(label = "Share App", icon = Icons.Filled.Share, onClick = { shareApp() })
        SettingsLinkRow(label = "Rate QuickCalc", icon = Icons.Filled.Star, onClick = { openPlayStore(context) })
        SettingsLinkRow(label = "Privacy Policy", icon = Icons.Filled.Lock, onClick = { showPrivacyModal = true })
        SettingsLinkRow(label = "App Version", icon = Icons.Filled.Info, onClick = {}, trailingText = "1.0.0")

        if (resetSuccessMessage != null) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF22C55E).copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = resetSuccessMessage!!,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    if (showResetModal) {
        ResetConfirmationDialog(
            onConfirm = {
                onResetProgress()
                showResetModal = false
                resetSuccessMessage = "Game progress has been reset to Level 1!"
            },
            onDismiss = { showResetModal = false }
        )
    }

    if (showPrivacyModal) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyModal = false })
    }
}

@Composable
fun ResetConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1B4B),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Reset Game Progress?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Are you sure you want to reset all saved level and question progress back to Level 1? This action cannot be undone.",
                    fontSize = 14.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("RESET", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Privacy Policy",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF06B6D4)
                )
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "NumFlow Privacy Policy\nEffective Date: August 31, 2026\n\n" +
                                "1. Information Collection: NumFlow is built as an offline-first game. We do not collect or store any Personally Identifiable Information (PII) on external servers.\n\n" +
                                "2. Local Storage: Level progress (Levels 1–20), sound preferences, and vibration settings are stored locally on your device via Android DataStore.\n\n" +
                                "3. Data Control: You can reset all saved game progress to Level 1 at any time using the Reset Game Progress option in Settings.\n\n" +
                                "4. Advertising: Google AdMob may process non-identifying device metrics to serve relevant ads in accordance with Google's Privacy Policy.\n\n" +
                                "5. Children's Safety: NumFlow complies with family safety standards and COPPA.\n\n" +
                                "6. Contact: support@numflow.com",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 22.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.White)
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsLinkRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    trailingText: String? = null,
    iconTint: Color = Color.White,
    labelColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = iconTint)
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = labelColor, modifier = Modifier.weight(1f))
        if (trailingText != null) {
            Text(trailingText, fontSize = 14.sp, color = Color.Gray)
        } else {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
