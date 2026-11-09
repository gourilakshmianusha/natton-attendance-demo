package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusPresent
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveClockWidget(
    isCheckedIn: Boolean,
    isCheckedOut: Boolean,
    inTime: String?,
    outTime: String?,
    shiftName: String,
    modifier: Modifier = Modifier
) {
    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date()))
    }
    var currentDate by remember {
        mutableStateOf(SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US).format(Date()))
    }
    val isSunday = remember {
        val cal = java.util.Calendar.getInstance()
        cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("hh:mm:ss a", Locale.US).format(now)
            currentDate = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US).format(now)
            delay(1000L)
        }
    }

    val gradientBrush = Brush.linearGradient(
        colors = if (isCheckedIn && !isCheckedOut) {
            listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
        } else {
            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
        }
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSunday) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Sunday Holiday",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF60A5FA),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Status Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        isCheckedIn && !isCheckedOut -> StatusPresent.copy(alpha = 0.2f)
                                        isCheckedOut -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                        else -> Color.Gray.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCheckedIn && !isCheckedOut -> StatusPresent
                                        isCheckedOut -> Color(0xFF3B82F6)
                                        else -> Color.Gray
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isCheckedIn && !isCheckedOut -> "Punched In"
                                isCheckedOut -> "Punched Out"
                                else -> "Not Checked In"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    isCheckedIn && !isCheckedOut -> StatusPresent
                                    isCheckedOut -> Color(0xFF60A5FA)
                                    else -> Color(0xFFCBD5E1)
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

                // Time Display
                AnimatedContent(
                    targetState = currentTime,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "live_clock"
                ) { timeText ->
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Shift & in-time info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = shiftName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    if (inTime != null) {
                        Text(
                            text = "In: $inTime" + if (outTime != null) " | Out: $outTime" else "",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }
            }
        }
    }
}
