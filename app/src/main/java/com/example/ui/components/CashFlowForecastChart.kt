package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.DailyBalance
import com.example.util.CurrencyUtils
import java.util.Locale

/**
 * High-performance 30-Day Cash Flow Forecast Line Chart.
 * Uses drawWithCache to eliminate GC allocations during scroll and rendering.
 */
@Composable
fun CashFlowForecastChart(
    dailyBalances: List<DailyBalance>,
    currencyCode: String = "USD",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val startBalance = remember(dailyBalances) { dailyBalances.firstOrNull()?.projectedBalance ?: 0.0 }
            val endBalance = remember(dailyBalances) { dailyBalances.lastOrNull()?.projectedBalance ?: startBalance }
            val netProjectedChange = remember(dailyBalances) { endBalance - startBalance }
            val isPositiveTrend = remember(dailyBalances) { netProjectedChange >= 0 }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "30-DAY CASH FLOW FORECAST",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = CurrencyUtils.formatCurrency(endBalance, currencyCode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(
                            color = if (isPositiveTrend) Color(0xFF00C853).copy(alpha = 0.15f) else Color(0xFFD50000).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositiveTrend) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositiveTrend) Color(0xFF00C853) else Color(0xFFD50000),
                        modifier = Modifier.height(16.dp)
                    )
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%s%s",
                            if (isPositiveTrend) "+" else "-",
                            CurrencyUtils.formatCurrency(kotlin.math.abs(netProjectedChange), currencyCode)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositiveTrend) Color(0xFF00C853) else Color(0xFFD50000)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Zero-allocation Cached Canvas Chart
            val chartLineColor = if (isPositiveTrend) Color(0xFF00E676) else Color(0xFFFF5252)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (dailyBalances.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .drawWithCache {
                                val width = size.width
                                val height = size.height
                                val topPadding = 16f
                                val bottomPadding = 24f
                                val effectiveHeight = height - topPadding - bottomPadding

                                val minVal = dailyBalances.minOf { it.projectedBalance }
                                val maxVal = dailyBalances.maxOf { it.projectedBalance }
                                val range = if (maxVal == minVal) 1.0 else (maxVal - minVal)

                                val chartGradient = Brush.verticalGradient(
                                    colors = listOf(
                                        chartLineColor.copy(alpha = 0.45f),
                                        chartLineColor.copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                )

                                val stepX = width / (dailyBalances.size - 1).coerceAtLeast(1)
                                val strokePath = Path()
                                val fillPath = Path()

                                dailyBalances.forEachIndexed { index, balance ->
                                    val x = index * stepX
                                    val normalizedY = ((maxVal - balance.projectedBalance) / range).toFloat()
                                    val y = topPadding + normalizedY * effectiveHeight

                                    if (index == 0) {
                                        strokePath.moveTo(x, y)
                                        fillPath.moveTo(x, height)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        val prevX = (index - 1) * stepX
                                        val prevNormalizedY = ((maxVal - dailyBalances[index - 1].projectedBalance) / range).toFloat()
                                        val prevY = topPadding + prevNormalizedY * effectiveHeight

                                        val controlPointX1 = prevX + (x - prevX) / 2f
                                        val controlPointY1 = prevY
                                        val controlPointX2 = prevX + (x - prevX) / 2f
                                        val controlPointY2 = y

                                        strokePath.cubicTo(
                                            controlPointX1, controlPointY1,
                                            controlPointX2, controlPointY2,
                                            x, y
                                        )
                                        fillPath.cubicTo(
                                            controlPointX1, controlPointY1,
                                            controlPointX2, controlPointY2,
                                            x, y
                                        )
                                    }

                                    if (index == dailyBalances.size - 1) {
                                        fillPath.lineTo(x, height)
                                        fillPath.close()
                                    }
                                }

                                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                val strokeStyle = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                                val gridLineColor = Color.Gray.copy(alpha = 0.2f)
                                val gridStrokeWidth = 1.dp.toPx()

                                onDrawBehind {
                                    // 1. Draw horizontal guide lines
                                    val steps = 3
                                    for (i in 0..steps) {
                                        val y = topPadding + effectiveHeight * (i.toFloat() / steps)
                                        drawLine(
                                            color = gridLineColor,
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = gridStrokeWidth,
                                            pathEffect = dashEffect
                                        )
                                    }

                                    // 2. Draw area under curve
                                    drawPath(
                                        path = fillPath,
                                        brush = chartGradient
                                    )

                                    // 3. Draw trend stroke line
                                    drawPath(
                                        path = strokePath,
                                        color = chartLineColor,
                                        style = strokeStyle
                                    )

                                    // 4. Draw indicator circles
                                    val startNormalizedY = ((maxVal - startBalance) / range).toFloat()
                                    val startY = topPadding + startNormalizedY * effectiveHeight
                                    drawCircle(
                                        color = chartLineColor,
                                        radius = 4.dp.toPx(),
                                        center = Offset(0f, startY)
                                    )

                                    val endNormalizedY = ((maxVal - endBalance) / range).toFloat()
                                    val endY = topPadding + endNormalizedY * effectiveHeight
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6.dp.toPx(),
                                        center = Offset(width, endY)
                                    )
                                    drawCircle(
                                        color = chartLineColor,
                                        radius = 4.dp.toPx(),
                                        center = Offset(width, endY)
                                    )
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-Axis Date Markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dailyBalances.firstOrNull()?.dayLabel ?: "Today",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dailyBalances.getOrNull(dailyBalances.size / 2)?.dayLabel ?: "Day 15",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dailyBalances.lastOrNull()?.dayLabel ?: "Day 30",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
