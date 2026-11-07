package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusHoliday
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusPresent

@Composable
fun StatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (color, icon, text) = when (status) {
        AttendanceStatus.PRESENT -> Triple(StatusPresent, Icons.Default.CheckCircle, "Present")
        AttendanceStatus.LATE -> Triple(StatusLate, Icons.Default.Schedule, "Late Arrival")
        AttendanceStatus.HALF_DAY -> Triple(StatusHalfDay, Icons.Default.HourglassBottom, "Half Day")
        AttendanceStatus.ABSENT -> Triple(StatusAbsent, Icons.Default.TimerOff, "Absent")
        AttendanceStatus.HOLIDAY -> Triple(StatusHoliday, Icons.Default.HolidayVillage, "Holiday")
        AttendanceStatus.ON_LEAVE -> Triple(StatusLeave, Icons.Default.EventBusy, "On Leave")
    }

    val bgTint = color.copy(alpha = 0.12f)
    val borderTint = color.copy(alpha = 0.35f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgTint)
            .border(1.dp, borderTint, RoundedCornerShape(50))
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 3.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(if (compact) 12.dp else 14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
