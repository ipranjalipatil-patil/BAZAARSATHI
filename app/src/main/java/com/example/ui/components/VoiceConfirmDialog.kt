package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ParsedVoiceCommand
import com.example.ai.VoiceIntent
import com.example.data.model.PaymentMethod
import com.example.ui.localization.StringsDefinition
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.IndigoUpi
import com.example.ui.theme.RedExpense
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.TealContainerLight
import com.example.ui.theme.TealPrimary

@Composable
fun VoiceTransactionDialog(
    isOpen: Boolean,
    candidateCommand: ParsedVoiceCommand?,
    strings: StringsDefinition,
    onDismiss: () -> Unit,
    onProcessText: (String) -> Unit,
    onConfirmCommand: (ParsedVoiceCommand) -> Unit,
    onEditCommand: (ParsedVoiceCommand) -> Unit
) {
    if (!isOpen) return

    var manualInputText by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Editable fields if user taps Edit
    var editAmount by remember(candidateCommand) {
        mutableStateOf(candidateCommand?.amount?.toInt()?.toString() ?: "")
    }
    var editProduct by remember(candidateCommand) {
        mutableStateOf(candidateCommand?.productOrDescription ?: "")
    }
    var editPaymentMethod by remember(candidateCommand) {
        mutableStateOf(candidateCommand?.paymentMethod ?: PaymentMethod.CASH)
    }

    val pulseTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_transaction_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.voiceEntryTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (candidateCommand == null) {
                    // Pulsing Microphone Visual
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(TealContainerLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(TealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Listening",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = strings.voiceListening,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = strings.voiceSpeakNow,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Tap Voice Samples in English, Hindi, and Marathi
                    Text(
                        text = "Or tap a quick example:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickVoiceSampleChip(
                            label = "🇮🇳 Hindi: 'आज 500 रुपये की बिक्री हुई (UPI)'",
                            onClick = { onProcessText("आज 500 रुपये की बिक्री हुई UPI") }
                        )
                        QuickVoiceSampleChip(
                            label = "🚩 Marathi: 'आज पाचशे रुपयांची विक्री झाली'",
                            onClick = { onProcessText("आज पाचशे रुपयांची विक्री झाली") }
                        )
                        QuickVoiceSampleChip(
                            label = "☕ Hindi: 'दूध के लिए 300 रुपये खर्च हुए'",
                            onClick = { onProcessText("दूध के लिए 300 रुपये खर्च हुए") }
                        )
                        QuickVoiceSampleChip(
                            label = "🇬🇧 English: 'Sold chai for 450 rupees via Cash'",
                            onClick = { onProcessText("Sold chai for 450 rupees via Cash") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text Input Alternative
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualInputText,
                            onValueChange = { manualInputText = it },
                            placeholder = { Text("Or type transaction here...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("voice_manual_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (manualInputText.isNotBlank()) {
                                    onProcessText(manualInputText)
                                    manualInputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(TealPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // CONFIRMATION VIEW (Mandatory confirmation before saving!)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(TealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.voiceUnderstood + ":",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!isEditing) {
                                // Display Confirmation Summary
                                val isSale = candidateCommand.intent == VoiceIntent.ADD_SALE
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSale) TealContainerLight else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (isSale) "SALE TRANSACTION" else "EXPENSE OUTFLOW",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSale) TealPrimary else RedExpense
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "₹${candidateCommand.amount?.toInt() ?: 0}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Item: ${candidateCommand.productOrDescription ?: "General"} • ${candidateCommand.paymentMethod.name} • Today",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action buttons: [Confirm] [Edit]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { isEditing = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("voice_edit_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit")
                                    }

                                    Button(
                                        onClick = { onConfirmCommand(candidateCommand) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("voice_confirm_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.confirm)
                                    }
                                }
                            } else {
                                // EDIT FORM
                                OutlinedTextField(
                                    value = editAmount,
                                    onValueChange = { editAmount = it },
                                    label = { Text("Amount (₹)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editProduct,
                                    onValueChange = { editProduct = it },
                                    label = { Text("Description / Item") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PaymentPill(
                                        label = "Cash",
                                        isSelected = editPaymentMethod == PaymentMethod.CASH,
                                        onClick = { editPaymentMethod = PaymentMethod.CASH },
                                        modifier = Modifier.weight(1f)
                                    )
                                    PaymentPill(
                                        label = "UPI",
                                        isSelected = editPaymentMethod == PaymentMethod.UPI,
                                        onClick = { editPaymentMethod = PaymentMethod.UPI },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val amt = editAmount.toDoubleOrNull() ?: 100.0
                                        val updatedCmd = candidateCommand.copy(
                                            amount = amt,
                                            productOrDescription = editProduct,
                                            paymentMethod = editPaymentMethod
                                        )
                                        onConfirmCommand(updatedCmd)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                ) {
                                    Text("Save Updated Transaction")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun QuickVoiceSampleChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PaymentPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
