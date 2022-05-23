// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.core.Animatable
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import b13.compose.chart.composable.*
import de.phase6.ui.Bar
import de.phase6.ui.BarChart
import de.phase6.ui.BarChartColorDefault
import de.phase6.ui.BarChartColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Chart Compose Demo"
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        ChartComposeView()
    }
}

private fun fillChartData(count: Int): List<Bar> =
    mutableListOf<Bar>()
        .apply {
            add(Bar(235, "Start"))
            if (count > 1) {
                repeat(count - 2) {
                    add(Bar(Random.nextLong(0, 100), "Col ${it + 1}"))
                }
                add(Bar(35, "End"))
            }
        }

@Composable
private fun ChartComposeView() {
    val sliderState = remember { mutableStateOf(8f) }
    val sliderProgressState = remember { mutableStateOf(0f) }
    val data = remember(sliderState.value.toInt()) { mutableStateOf(fillChartData(sliderState.value.toInt())) }
    val selectedBar = remember { mutableStateOf(0) }
    val allItemsCount = remember(data.value) { mutableStateOf(data.value.sumOf { it.value }) }

    Column(
        modifier = Modifier.padding(
            start = Dimen.padding4,
            end = Dimen.padding4,
            top = Dimen.padding4
        )
            .fillMaxHeight()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(all = Dimen.padding4)
        ) {
            Slider(
                value = sliderState.value,
                onValueChange = {
                    sliderState.value = it
                },
                valueRange = 1.0f..20.0f,
                steps = 20,
                modifier = Modifier.weight(1f),
            )
            Text(
                modifier = Modifier.size(20.dp),
                textAlign = TextAlign.Center,
                text = sliderState.value.toInt().toString(),
                fontSize = 14.sp
            )
        }
        if (data.value.size == 1) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = sliderProgressState.value,
                    onValueChange = {
                        sliderProgressState.value = it
                    },
                    valueRange = 0.0f..100.0f,
                    steps = 100,
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = Dimen.padding4),
                )
                BarItem(
                    modifier = Modifier.width(40.dp)
                        .height(240.dp),
                    value = sliderProgressState.value.toLong(),
                    label = "Col 1",
                    maxValue = 100,
                    primaryColor = BarChartColorDefault.primary,
                    backgroundColor = BarChartColorDefault.secondary
                )
            }
        } else {
            BarChart(
                series = data.value,
                selectedBar = selectedBar.value,
                onBarSelectChanged = {
                    selectedBar.value = it
                    println("selectedBar -> ${selectedBar.value}")
                },
                modifier = Modifier
                    .height(320.dp)
                    .fillMaxWidth()
            ) { _, bar, colors ->
                BarItem(
                    value = bar.value,
                    label = bar.label,
                    maxValue = allItemsCount.value,
                    primaryColor = colors.primary,
                    backgroundColor = colors.background
                )
            }
        }
        Text(
            modifier = Modifier.padding(top = Dimen.padding4).fillMaxWidth(),
            text = "Selected column index: ${selectedBar.value}\n" +
                    "Current column value: ${data.value.getOrNull(selectedBar.value)?.value}\n" +
                    "All columns value sum: ${allItemsCount.value}",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BarItem(
    modifier: Modifier = Modifier,
    value: Long,
    label: String,
    maxValue: Long,
    primaryColor: Color,
    backgroundColor: Color
) {
    val progressAnimation = remember(value) { Animatable(initialValue = 0f) }
    LaunchedEffect(value) {
        progressAnimation.animateTo(targetValue = value.toFloat() / maxValue)
    }
    val progress = progressAnimation.value

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.padding(
                start = Dimen.padding2,
                end = Dimen.padding2
            )
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
            )
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxSize()
            ) {
                if (progress < 0.95)
                    Text(
                        textAlign = TextAlign.Center,
                        text = value.toString(),
                        color = primaryColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = Dimen.padding1)
                            .fillMaxWidth()
                    )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = progress)
                        .background(color = primaryColor)
                ) {
                    if (progress >= 0.95)
                        Text(
                            textAlign = TextAlign.Center,
                            text = value.toString(),
                            color = backgroundColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = Dimen.padding1)
                                .fillMaxWidth()
                        )
                }
            }
        }
        Text(
            textAlign = TextAlign.Center,
            text = label,
            color = primaryColor,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = Dimen.padding1)
                .fillMaxWidth()
        )
    }
}