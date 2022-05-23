package de.phase6.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

data class Bar(val value: Long, val label: String)

data class BarChartColors(
    val primary: Color,
    val accent: Color,
    val secondary: Color,
)

data class BarColor(
    val primary: Color,
    val background: Color,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    series: List<Bar>,
    selectedBar: Int,
    colors: BarChartColors = BarChartColorDefault,
    onBarSelectChanged: (index: Int) -> Unit,
    maxVisibleColumnCount: Int = 8,
    fillMaxWidth: Boolean = true,
    barBuilder: @Composable (index: Int, bar: Bar, colors: BarColor) -> Unit,
) {
    val chartWidth = remember(series) { mutableStateOf(-1) }
    val selectedColors =
        remember(colors) { mutableStateOf(BarColor(primary = colors.accent, background = colors.secondary)) }
    val unselectedColors =
        remember(colors) { mutableStateOf(BarColor(primary = colors.primary, background = colors.secondary)) }

    LaunchedEffect(series) {
        if (selectedBar > series.size)
            onBarSelectChanged(0)
    }

    Box(modifier.onPlaced { coordinates ->
        if (chartWidth.value == -1)
            chartWidth.value = coordinates.size.width
    }) {
        if (chartWidth.value != -1) {
            val seriesSize = series.size
            val columnCount = if (seriesSize >= maxVisibleColumnCount) maxVisibleColumnCount else seriesSize
            val columnWidth = LocalDensity.current.run {
                if (fillMaxWidth && seriesSize >= maxVisibleColumnCount)
                    (chartWidth.value / columnCount).toDp()
                else
                    min(columnMaxWidth, (chartWidth.value / columnCount).toDp())
            }

            LazyRow(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                items(series.size) { index ->
                    Box(
                        modifier = Modifier
                            .width(columnWidth)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onBarSelectChanged(index)
                            }
                    ) {

                        barBuilder(
                            index,
                            series[index],
                            if (index == selectedBar) selectedColors.value else unselectedColors.value
                        )
                    }
                }
            }
        }
    }
}

val BarChartColorDefault = BarChartColors(
    primary = Color.Gray,
    accent = Color.Magenta,
    secondary = Color.LightGray
)

private val columnMaxWidth: Dp = 40.dp