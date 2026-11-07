package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.EmployeeAttendanceReport
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusHoliday
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusPresent
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttendanceDonutChart(
    report: EmployeeAttendanceReport,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        DonutSegment("Present", report.presentDays, StatusPresent),
        DonutSegment("Late", report.lateDays, StatusLate),
        DonutSegment("Half Day", report.halfDays, StatusHalfDay),
        DonutSegment("Absent", report.absentDays, StatusAbsent),
        DonutSegment("Holiday", report.holidayDays, StatusHoliday),
        DonutSegment("Leave", report.leaveDays, StatusLeave)
    ).filter { it.value > 0 }

    val total = items.sumOf { it.value }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val strokeWidth = 26.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                if (total == 0) {
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        center = centerOffset,
                        style = Stroke(width = strokeWidth)
                    )
                } else {
                    var startAngle = -90f
                    items.forEach { segment ->
                        val sweepAngle = (segment.value.toFloat() / total.toFloat()) * 360f
                        drawArc(
                            color = segment.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f, // subtle gap
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.US, "%.1f%%", report.attendancePercentage),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Attendance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { segment ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(segment.color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${segment.label}: ${segment.value}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class DonutSegment(val label: String, val value: Int, val color: Color)

@Composable
fun WeeklyHoursBarChart(
    records: List<AttendanceRecord>,
    modifier: Modifier = Modifier
) {
    val recentRecords = records.take(7).reversed()
    val maxHours = (recentRecords.maxOfOrNull { it.workHours } ?: 8.0).coerceAtLeast(10.0)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Daily Work Hours",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Target: 8.0 hrs/day",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            if (recentRecords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent punch logs available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                recentRecords.forEach { record ->
                    val heightFraction = (record.workHours / maxHours).toFloat().coerceIn(0.05f, 1f)
                    val isOt = record.overtimeHours > 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (record.workHours > 0) "${record.workHours}h" else "-",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isOt) StatusLate else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((100 * heightFraction).dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    when {
                                        record.workHours >= 8.0 -> MaterialTheme.colorScheme.primary
                                        record.workHours in 4.0..7.9 -> StatusHalfDay
                                        record.workHours > 0 -> StatusLate
                                        else -> Color.LightGray.copy(alpha = 0.4f)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val dayLabel = record.date.substringAfterLast("-")
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
