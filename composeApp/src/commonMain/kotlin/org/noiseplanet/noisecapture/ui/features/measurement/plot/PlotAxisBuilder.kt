package org.noiseplanet.noisecapture.ui.features.measurement.plot

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class PlotAxisBuilder {

    val tickStroke = 2.dp
    val tickLength = 4.dp

    fun timeAxisFormater(timeValue: Double): String {
        return "+${round(timeValue).toInt()}s"
    }

    fun noiseLevelAxisFormater(timeValue: Double): String {
        return "${round(timeValue).toInt()} dB"
    }

    // TODO: Cleanup legend generation functions
    @Suppress("LongParameterList")
    fun makeXLegend(
        textMeasurer: TextMeasurer,
        xValue: Double,
        legendWidth: Float,
        xPerPixel: Double,
        depth: Int,
        formater: (x: Double) -> String,
        ascending: Boolean,
    ): LegendElement {
        val xPos =
            when {
                ascending -> (xValue / xPerPixel).toFloat()
                else -> (legendWidth - xValue / xPerPixel).toFloat()
            }
        val legendText = buildAnnotatedString {
            withStyle(style = SpanStyle()) {
                append(formater(xValue))
            }
        }
        val textLayout = textMeasurer.measure(legendText)
        val textPos = min(
            legendWidth - textLayout.size.width,
            max(0F, xPos - textLayout.size.width / 2)
        )
        return LegendElement(textLayout, xPos, textPos, depth)
    }

    // TODO: Cleanup legend generation functions
    @Suppress("LongParameterList")
    fun recursiveLegendBuild(
        textMeasurer: TextMeasurer,
        timeValue: Double,
        legendWidth: Float,
        timePerPixel: Double,
        minPixel: Float,
        maxPixel: Float,
        xLeftValue: Double,
        xRightValue: Double,
        feedElements: ArrayList<LegendElement>,
        depth: Int,
        formater: (x: Double) -> String,
    ) {
        val legendElement =
            makeXLegend(
                textMeasurer,
                timeValue,
                legendWidth,
                timePerPixel,
                depth,
                formater,
                xLeftValue < xRightValue
            )
        // Add sub axis element if the text does not overlap with neighboring texts
        if (legendElement.textPos > minPixel && legendElement.xPos + legendElement.text.size.width / 2 < maxPixel) {
            feedElements.add(legendElement)
            // left legend, + x seconds
            recursiveLegendBuild(
                textMeasurer,
                xLeftValue + (timeValue - xLeftValue) / 2,
                legendWidth,
                timePerPixel,
                minPixel,
                legendElement.textPos,
                xLeftValue,
                timeValue,
                feedElements,
                depth + 1,
                formater
            )
            // right legend, - x seconds
            recursiveLegendBuild(
                textMeasurer,
                timeValue + (xRightValue - timeValue) / 2,
                legendWidth,
                timePerPixel,
                legendElement.textPos + legendElement.text.size.width,
                maxPixel,
                timeValue,
                xRightValue,
                feedElements,
                depth + 1,
                formater
            )
        }
    }

    fun makeXLabels(
        textMeasurer: TextMeasurer,
        leftValue: Double,
        rightValue: Double,
        xLegendWidth: Float,
        formater: (x: Double) -> String,
    ): ArrayList<LegendElement> {
        val xPerPixel = abs(leftValue - rightValue) / xLegendWidth
        val legendElements = ArrayList<LegendElement>()
        val leftLegend =
            makeXLegend(
                textMeasurer,
                leftValue,
                xLegendWidth,
                xPerPixel,
                -1,
                formater,
                leftValue < rightValue
            )
        val rightLegend =
            makeXLegend(
                textMeasurer,
                rightValue,
                xLegendWidth,
                xPerPixel,
                -1,
                formater,
                leftValue < rightValue
            )
        legendElements.add(leftLegend)
        legendElements.add(rightLegend)
        // Add axis texts between left and rightmost axis texts (until it overlaps)
        recursiveLegendBuild(
            textMeasurer,
            abs(leftValue - rightValue) / 2,
            xLegendWidth,
            xPerPixel,
            leftLegend.text.size.width.toFloat(),
            rightLegend.xPos - rightLegend.text.size.width,
            leftValue,
            rightValue,
            legendElements,
            0,
            formater
        )
        // find depth index with maximum number of elements (to generate same intervals on legend)
        val legendDepthCount = IntArray(legendElements.maxOf { it.depth } + 1) { 0 }
        legendElements.forEach {
            if (it.depth >= 0) {
                legendDepthCount[it.depth] += 1
            }
        }
        // remove sub-axis texts with isolated depth (should produce same intervals between axis text)
        legendElements.removeAll {
            it.depth > 0 && legendDepthCount[it.depth] != (2.0.pow(it.depth)).toInt()
        }
        return legendElements
    }
}
