@file:Suppress("LongMethod")

package org.noiseplanet.noisecapture.shared.ui.theme

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun clinicalNotes(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "clinical_notes",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 1f,
                pathFillType = NonZero
            ) {
                moveTo(28.125f, 25.958f)
                quadToRelative(-2.042f, 0f, -3.479f, -1.437f)
                quadToRelative(-1.438f, -1.438f, -1.438f, -3.479f)
                quadToRelative(0f, -2.084f, 1.438f, -3.5f)
                quadToRelative(1.437f, -1.417f, 3.479f, -1.417f)
                quadToRelative(2.083f, 0f, 3.5f, 1.437f)
                quadToRelative(1.417f, 1.438f, 1.417f, 3.48f)
                quadToRelative(0f, 2.041f, -1.438f, 3.479f)
                quadToRelative(-1.437f, 1.437f, -3.479f, 1.437f)
                close()
                moveToRelative(0f, -2.625f)
                quadToRelative(1f, 0f, 1.646f, -0.645f)
                quadToRelative(0.646f, -0.646f, 0.646f, -1.646f)
                quadToRelative(0f, -1f, -0.667f, -1.646f)
                quadToRelative(-0.667f, -0.646f, -1.625f, -0.646f)
                quadToRelative(-1f, 0f, -1.646f, 0.667f)
                quadToRelative(-0.646f, 0.666f, -0.646f, 1.625f)
                quadToRelative(0f, 1f, 0.646f, 1.646f)
                quadToRelative(0.646f, 0.645f, 1.646f, 0.645f)
                close()
                moveTo(19.5f, 37.625f)
                quadToRelative(-0.542f, 0f, -0.917f, -0.375f)
                reflectiveQuadToRelative(-0.375f, -0.917f)
                verticalLineToRelative(-3.458f)
                quadToRelative(0f, -0.875f, 0.396f, -1.625f)
                reflectiveQuadToRelative(1.146f, -1.208f)
                quadToRelative(1.208f, -0.709f, 2.062f, -1.042f)
                quadToRelative(0.855f, -0.333f, 2.23f, -0.667f)
                quadToRelative(0.416f, -0.041f, 0.833f, 0.021f)
                quadToRelative(0.417f, 0.063f, 0.667f, 0.396f)
                lineTo(28.125f, 32f)
                lineToRelative(2.583f, -3.25f)
                quadToRelative(0.25f, -0.333f, 0.667f, -0.396f)
                quadToRelative(0.417f, -0.062f, 0.833f, -0.021f)
                quadToRelative(1.375f, 0.334f, 2.209f, 0.667f)
                quadToRelative(0.833f, 0.333f, 2.041f, 1.042f)
                quadToRelative(0.75f, 0.458f, 1.167f, 1.208f)
                quadToRelative(0.417f, 0.75f, 0.417f, 1.583f)
                verticalLineToRelative(3.5f)
                quadToRelative(0f, 0.542f, -0.375f, 0.917f)
                reflectiveQuadToRelative(-0.917f, 0.375f)
                close()
                moveTo(20.833f, 35f)
                horizontalLineToRelative(6.292f)
                lineToRelative(-3.167f, -4.042f)
                quadToRelative(-0.833f, 0.334f, -1.625f, 0.709f)
                quadToRelative(-0.791f, 0.375f, -1.5f, 0.833f)
                close()
                moveToRelative(8.292f, 0f)
                horizontalLineToRelative(6.292f)
                verticalLineToRelative(-2.5f)
                quadToRelative(-0.709f, -0.458f, -1.479f, -0.833f)
                quadToRelative(-0.771f, -0.375f, -1.605f, -0.709f)
                close()
                moveToRelative(-2f, 0f)
                close()
                moveToRelative(2f, 0f)
                close()
                moveToRelative(-1f, -13.958f)
                close()
                moveTo(7.875f, 32.083f)
                verticalLineTo(7.875f)
                verticalLineToRelative(6.75f)
                verticalLineToRelative(-1.167f)
                verticalLineToRelative(18.625f)
                close()
                moveToRelative(0f, 2.625f)
                quadToRelative(-1.083f, 0f, -1.854f, -0.77f)
                quadToRelative(-0.771f, -0.771f, -0.771f, -1.855f)
                verticalLineTo(7.875f)
                quadToRelative(0f, -1.083f, 0.771f, -1.854f)
                quadToRelative(0.771f, -0.771f, 1.854f, -0.771f)
                horizontalLineToRelative(24.25f)
                quadToRelative(1.083f, 0f, 1.854f, 0.771f)
                quadToRelative(0.771f, 0.771f, 0.771f, 1.854f)
                verticalLineToRelative(9.5f)
                quadToRelative(-0.458f, -0.833f, -1.125f, -1.542f)
                quadToRelative(-0.667f, -0.708f, -1.5f, -1.208f)
                verticalLineToRelative(-6.75f)
                horizontalLineTo(7.875f)
                verticalLineToRelative(24.208f)
                horizontalLineToRelative(7.708f)
                quadToRelative(0f, 0.209f, -0.021f, 0.396f)
                quadToRelative(-0.02f, 0.188f, -0.02f, 0.396f)
                verticalLineToRelative(1.833f)
                close()
                moveToRelative(5.25f, -20.25f)
                horizontalLineTo(24.75f)
                quadToRelative(0.75f, -0.5f, 1.625f, -0.729f)
                quadToRelative(0.875f, -0.229f, 1.833f, -0.271f)
                verticalLineToRelative(-1.375f)
                quadToRelative(-0.083f, -0.166f, -0.208f, -0.229f)
                quadToRelative(-0.125f, -0.062f, -0.292f, -0.062f)
                horizontalLineTo(13.125f)
                quadToRelative(-0.542f, 0f, -0.937f, 0.396f)
                quadToRelative(-0.396f, 0.395f, -0.396f, 0.937f)
                reflectiveQuadToRelative(0.396f, 0.938f)
                quadToRelative(0.395f, 0.395f, 0.937f, 0.395f)
                close()
                moveToRelative(0f, 6.834f)
                horizontalLineToRelative(7.458f)
                quadToRelative(-0.041f, -0.667f, 0.063f, -1.334f)
                quadToRelative(0.104f, -0.666f, 0.271f, -1.291f)
                horizontalLineToRelative(-7.792f)
                quadToRelative(-0.542f, 0f, -0.937f, 0.395f)
                quadToRelative(-0.396f, 0.396f, -0.396f, 0.938f)
                quadToRelative(0f, 0.542f, 0.396f, 0.917f)
                quadToRelative(0.395f, 0.375f, 0.937f, 0.375f)
                close()
                moveToRelative(0f, 6.875f)
                horizontalLineToRelative(4.75f)
                quadToRelative(0.708f, -0.542f, 1.479f, -0.917f)
                quadToRelative(0.771f, -0.375f, 1.646f, -0.708f)
                lineToRelative(-0.083f, -0.417f)
                quadToRelative(-0.084f, -0.292f, -0.292f, -0.437f)
                quadToRelative(-0.208f, -0.146f, -0.583f, -0.146f)
                horizontalLineToRelative(-6.917f)
                quadToRelative(-0.542f, 0f, -0.937f, 0.375f)
                quadToRelative(-0.396f, 0.375f, -0.396f, 0.916f)
                quadToRelative(0f, 0.584f, 0.396f, 0.959f)
                quadToRelative(0.395f, 0.375f, 0.937f, 0.375f)
                close()
            }
        }.build()
    }
}


@Composable
fun calibrate(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "Calibration", defaultWidth = 40.0.dp, defaultHeight = 40.0.dp,
            viewportWidth = 40.0f, viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 1f,
                pathFillType = NonZero
            ) {
                moveToRelative(19.1401f, 39.9806f)
                curveToRelative(-4.6432f, -0.209f, -8.9809f, -1.9697f, -12.4464f, -5.0521f)
                lineToRelative(-0.3115f, -0.277f)
                lineToRelative(-1.7193f, 1.7161f)
                curveToRelative(-0.9456f, 0.9439f, -1.7546f, 1.7338f, -1.7978f, 1.7553f)
                curveToRelative(-0.4297f, 0.2149f, -0.9594f, -0.0332f, -1.0551f, -0.4941f)
                curveToRelative(-0.0316f, -0.1523f, -0.0055f, -0.3545f, 0.0636f, -0.4925f)
                curveToRelative(0.0225f, -0.0449f, 0.813f, -0.8553f, 1.7567f, -1.8008f)
                lineToRelative(1.7158f, -1.7192f)
                lineToRelative(-0.2127f, -0.2367f)
                curveToRelative(-2.3761f, -2.6439f, -4.0082f, -5.8685f, -4.7164f, -9.3188f)
                curveToRelative(-0.5561f, -2.7092f, -0.5561f, -5.4136f, 0.0f, -8.1228f)
                curveToRelative(0.689f, -3.3564f, 2.2631f, -6.5233f, 4.5194f, -9.0924f)
                curveToRelative(0.1374f, -0.1565f, 0.2854f, -0.3255f, 0.329f, -0.3756f)
                lineToRelative(0.0792f, -0.0912f)
                lineToRelative(-1.6942f, -1.6925f)
                curveToRelative(-0.9318f, -0.9309f, -1.7216f, -1.7384f, -1.755f, -1.7946f)
                curveToRelative(-0.0335f, -0.0562f, -0.0727f, -0.1661f, -0.0871f, -0.2442f)
                curveToRelative(-0.1085f, -0.5871f, 0.5153f, -1.0426f, 1.057f, -0.7717f)
                curveToRelative(0.0431f, 0.0215f, 0.8521f, 0.8115f, 1.7978f, 1.7554f)
                lineToRelative(1.7193f, 1.7161f)
                lineToRelative(0.3115f, -0.277f)
                curveToRelative(1.1123f, -0.9894f, 2.3494f, -1.8681f, 3.6256f, -2.5752f)
                curveToRelative(1.4458f, -0.8012f, 3.0927f, -1.4571f, 4.6595f, -1.8558f)
                curveToRelative(3.0516f, -0.7765f, 6.0694f, -0.8452f, 9.1448f, -0.2082f)
                curveToRelative(3.3895f, 0.702f, 6.554f, 2.3009f, 9.1822f, 4.6393f)
                lineToRelative(0.3115f, 0.2771f)
                lineToRelative(1.7193f, -1.7163f)
                curveToRelative(0.9456f, -0.944f, 1.7546f, -1.7339f, 1.7978f, -1.7554f)
                curveToRelative(0.4303f, -0.2145f, 0.9594f, 0.0333f, 1.0551f, 0.4942f)
                curveToRelative(0.0351f, 0.1692f, 0.0011f, 0.3768f, -0.0853f, 0.5219f)
                curveToRelative(-0.0335f, 0.0562f, -0.8232f, 0.8637f, -1.755f, 1.7946f)
                lineToRelative(-1.6942f, 1.6925f)
                lineToRelative(0.0792f, 0.0912f)
                curveToRelative(0.0436f, 0.0501f, 0.1916f, 0.2192f, 0.329f, 0.3756f)
                curveToRelative(2.5749f, 2.932f, 4.2322f, 6.5862f, 4.7565f, 10.4877f)
                curveToRelative(0.3105f, 2.3103f, 0.2207f, 4.5676f, -0.2745f, 6.9019f)
                curveToRelative(-0.3943f, 1.8587f, -1.1066f, 3.7594f, -2.0403f, 5.4443f)
                curveToRelative(-0.6737f, 1.2157f, -1.5357f, 2.442f, -2.4417f, 3.4737f)
                curveToRelative(-0.1374f, 0.1564f, -0.2854f, 0.3255f, -0.329f, 0.3756f)
                lineToRelative(-0.0792f, 0.0912f)
                lineToRelative(1.6942f, 1.6925f)
                curveToRelative(0.9318f, 0.9308f, 1.7216f, 1.7384f, 1.755f, 1.7945f)
                curveToRelative(0.0335f, 0.0562f, 0.0727f, 0.1661f, 0.0871f, 0.2442f)
                curveToRelative(0.1085f, 0.5871f, -0.5146f, 1.0422f, -1.057f, 0.7719f)
                curveToRelative(-0.0431f, -0.0215f, -0.8521f, -0.8114f, -1.7978f, -1.7554f)
                lineToRelative(-1.7193f, -1.7163f)
                lineToRelative(-0.3115f, 0.2771f)
                curveToRelative(-1.3455f, 1.1971f, -2.7934f, 2.1777f, -4.402f, 2.9813f)
                curveToRelative(-2.99f, 1.4936f, -6.4214f, 2.2214f, -9.7637f, 2.071f)
                close()
                moveTo(20.8247f, 38.5122f)
                curveToRelative(2.1961f, -0.1082f, 4.1436f, -0.5325f, 6.1521f, -1.3406f)
                curveToRelative(1.1632f, -0.468f, 2.3217f, -1.0873f, 3.4013f, -1.8181f)
                curveToRelative(0.7019f, -0.4752f, 1.5093f, -1.1078f, 2.0059f, -1.5716f)
                lineToRelative(0.1869f, -0.1746f)
                lineToRelative(-1.61f, -1.6116f)
                lineToRelative(-1.61f, -1.6116f)
                lineToRelative(-0.3274f, 0.2758f)
                curveToRelative(-2.2902f, 1.9287f, -5.0092f, 3.0381f, -8.0145f, 3.2698f)
                curveToRelative(-0.7656f, 0.059f, -1.8892f, 0.0316f, -2.7036f, -0.0662f)
                curveToRelative(-2.6973f, -0.3237f, -5.2987f, -1.4662f, -7.3724f, -3.2381f)
                lineToRelative(-0.2833f, -0.2421f)
                lineToRelative(-1.6042f, 1.604f)
                curveToRelative(-0.8823f, 0.8822f, -1.6042f, 1.6117f, -1.6042f, 1.6212f)
                curveToRelative(0.0f, 0.0252f, 0.5917f, 0.5495f, 0.9078f, 0.8043f)
                curveToRelative(2.2181f, 1.7883f, 4.7947f, 3.0394f, 7.5396f, 3.6608f)
                curveToRelative(1.5974f, 0.3617f, 3.3455f, 0.517f, 4.936f, 0.4387f)
                close()
                moveTo(8.0125f, 30.9521f)
                lineTo(9.6176f, 29.3469f)
                lineTo(9.513f, 29.2266f)
                curveToRelative(-1.6382f, -1.884f, -2.7332f, -4.1061f, -3.2027f, -6.4989f)
                curveToRelative(-0.1996f, -1.0173f, -0.2516f, -1.5818f, -0.2516f, -2.7284f)
                curveToRelative(0.0f, -0.9764f, 0.0242f, -1.3409f, 0.1382f, -2.0805f)
                curveToRelative(0.4147f, -2.691f, 1.5279f, -5.0902f, 3.3161f, -7.1467f)
                lineToRelative(0.1046f, -0.1203f)
                lineToRelative(-1.6051f, -1.6052f)
                curveToRelative(-0.8828f, -0.8829f, -1.6164f, -1.6016f, -1.6301f, -1.5971f)
                curveToRelative(-0.0404f, 0.013f, -0.4916f, 0.5245f, -0.7953f, 0.9014f)
                curveToRelative(-0.626f, 0.777f, -1.0605f, 1.4039f, -1.5887f, 2.2923f)
                curveToRelative(-0.1927f, 0.3242f, -0.3328f, 0.5848f, -0.5811f, 1.0813f)
                curveToRelative(-0.8939f, 1.7878f, -1.481f, 3.6632f, -1.7557f, 5.6087f)
                curveToRelative(-0.259f, 1.8341f, -0.259f, 3.498f, 0.0f, 5.3321f)
                curveToRelative(0.233f, 1.6502f, 0.6754f, 3.199f, 1.3699f, 4.7964f)
                curveToRelative(0.1493f, 0.3433f, 0.5893f, 1.2317f, 0.7614f, 1.5371f)
                curveToRelative(0.5366f, 0.9524f, 1.0791f, 1.7569f, 1.7524f, 2.599f)
                curveToRelative(0.3505f, 0.4384f, 0.8114f, 0.9593f, 0.8487f, 0.9593f)
                curveToRelative(0.0074f, 0.0f, 0.7358f, -0.7223f, 1.6186f, -1.6052f)
                close()
                moveTo(33.6428f, 32.5382f)
                curveToRelative(0.014f, -0.0101f, 0.1437f, -0.1529f, 0.2883f, -0.3173f)
                curveToRelative(0.8622f, -0.9807f, 1.6079f, -2.0364f, 2.2753f, -3.2211f)
                curveToRelative(0.1545f, -0.2742f, 0.5787f, -1.1236f, 0.7244f, -1.4507f)
                curveToRelative(0.6934f, -1.5557f, 1.1717f, -3.2159f, 1.4073f, -4.8836f)
                curveToRelative(0.259f, -1.8341f, 0.259f, -3.498f, 0.0f, -5.3321f)
                curveToRelative(-0.209f, -1.4803f, -0.5828f, -2.8629f, -1.1653f, -4.3106f)
                curveToRelative(-0.7605f, -1.8901f, -1.9504f, -3.8068f, -3.2859f, -5.2929f)
                curveToRelative(-0.1346f, -0.1497f, -0.2559f, -0.2759f, -0.2696f, -0.2803f)
                curveToRelative(-0.0137f, -0.004f, -0.7473f, 0.7143f, -1.6301f, 1.5971f)
                lineToRelative(-1.6051f, 1.6052f)
                lineToRelative(0.1046f, 0.1203f)
                curveToRelative(0.8306f, 0.9552f, 1.4816f, 1.9328f, 2.0376f, 3.0596f)
                curveToRelative(0.7446f, 1.5089f, 1.1819f, 3.0431f, 1.3711f, 4.8098f)
                curveToRelative(0.0355f, 0.3318f, 0.0454f, 0.626f, 0.0454f, 1.3579f)
                curveToRelative(0.0f, 0.732f, -0.0098f, 1.0261f, -0.0454f, 1.3579f)
                curveToRelative(-0.1892f, 1.7667f, -0.6265f, 3.3008f, -1.3711f, 4.8098f)
                curveToRelative(-0.556f, 1.1268f, -1.2071f, 2.1043f, -2.0376f, 3.0596f)
                lineToRelative(-0.1046f, 0.1203f)
                lineToRelative(1.6051f, 1.6052f)
                curveToRelative(0.8828f, 0.8828f, 1.6107f, 1.605f, 1.6176f, 1.6048f)
                curveToRelative(0.0069f, -3.0E-4f, 0.024f, -0.009f, 0.038f, -0.0187f)
                close()
                moveTo(20.5729f, 32.4821f)
                curveToRelative(1.8905f, -0.1035f, 3.5707f, -0.5504f, 5.1464f, -1.3688f)
                curveToRelative(3.5334f, -1.8353f, 5.9832f, -5.1964f, 6.6115f, -9.0709f)
                curveToRelative(0.2928f, -1.8057f, 0.1953f, -3.67f, -0.2815f, -5.382f)
                curveToRelative(-0.5807f, -2.0852f, -1.6725f, -3.9545f, -3.2022f, -5.4829f)
                curveToRelative(-2.0376f, -2.0358f, -4.6131f, -3.2789f, -7.4519f, -3.5968f)
                curveToRelative(-1.7239f, -0.193f, -3.4908f, -0.0246f, -5.1206f, 0.488f)
                curveToRelative(-1.9723f, 0.6203f, -3.668f, 1.6512f, -5.1335f, 3.121f)
                curveToRelative(-1.811f, 1.8162f, -3.0253f, 4.1544f, -3.4491f, 6.6418f)
                curveToRelative(-0.4198f, 2.4637f, -0.1264f, 4.93f, 0.854f, 7.1778f)
                curveToRelative(0.9305f, 2.1334f, 2.5213f, 4.0413f, 4.457f, 5.3452f)
                curveToRelative(1.9099f, 1.2866f, 4.0312f, 1.9967f, 6.349f, 2.1254f)
                curveToRelative(0.5341f, 0.0297f, 0.7123f, 0.03f, 1.221f, 0.002f)
                close()
                moveTo(19.788f, 27.1172f)
                curveToRelative(-0.2185f, -0.0718f, -0.373f, -0.218f, -0.4785f, -0.4528f)
                curveToRelative(-0.0422f, -0.0941f, -0.0448f, -0.263f, -0.0448f, -3.0024f)
                verticalLineToRelative(-2.9028f)
                lineToRelative(-2.9179f, -0.0125f)
                lineToRelative(-2.9179f, -0.0125f)
                lineToRelative(-0.1716f, -0.0873f)
                curveToRelative(-0.1375f, -0.07f, -0.1903f, -0.1146f, -0.2655f, -0.2242f)
                curveToRelative(-0.1856f, -0.2706f, -0.1856f, -0.5763f, 0.0f, -0.8469f)
                curveToRelative(0.0752f, -0.1097f, 0.128f, -0.1543f, 0.2655f, -0.2243f)
                lineToRelative(0.1716f, -0.0873f)
                lineToRelative(2.9179f, -0.0125f)
                lineToRelative(2.9179f, -0.0125f)
                verticalLineToRelative(-2.9027f)
                verticalLineToRelative(-2.9028f)
                lineToRelative(0.0653f, -0.1392f)
                curveToRelative(0.1342f, -0.2862f, 0.3622f, -0.4339f, 0.6698f, -0.4339f)
                curveToRelative(0.3076f, 0.0f, 0.5356f, 0.1477f, 0.6698f, 0.4339f)
                lineToRelative(0.0653f, 0.1392f)
                verticalLineToRelative(2.9028f)
                verticalLineToRelative(2.9028f)
                lineToRelative(2.9179f, 0.0125f)
                lineToRelative(2.9179f, 0.0125f)
                lineToRelative(0.1716f, 0.0873f)
                curveToRelative(0.1375f, 0.07f, 0.1903f, 0.1146f, 0.2655f, 0.2243f)
                curveToRelative(0.1856f, 0.2706f, 0.1856f, 0.5763f, 0.0f, 0.8469f)
                curveToRelative(-0.0752f, 0.1097f, -0.128f, 0.1543f, -0.2655f, 0.2242f)
                lineToRelative(-0.1716f, 0.0873f)
                lineToRelative(-2.9179f, 0.0125f)
                lineToRelative(-2.9179f, 0.0125f)
                verticalLineToRelative(2.9028f)
                verticalLineToRelative(2.9028f)
                lineToRelative(-0.0653f, 0.1392f)
                curveToRelative(-0.124f, 0.2643f, -0.3475f, 0.4198f, -0.6278f, 0.4367f)
                curveToRelative(-0.0867f, 0.005f, -0.1977f, -0.005f, -0.2538f, -0.0235f)
                close()
                moveTo(10.9795f, 9.3361f)
                curveToRelative(1.9415f, -1.6351f, 4.2271f, -2.6947f, 6.7155f, -3.1132f)
                curveToRelative(1.2584f, -0.2116f, 2.7022f, -0.2433f, 3.9993f, -0.0876f)
                curveToRelative(2.6875f, 0.3225f, 5.2253f, 1.4317f, 7.3292f, 3.2036f)
                lineToRelative(0.3274f, 0.2757f)
                lineToRelative(1.6038f, -1.6035f)
                curveToRelative(0.8821f, -0.8819f, 1.6038f, -1.6113f, 1.6038f, -1.6208f)
                curveToRelative(0.0f, -0.0252f, -0.5917f, -0.5495f, -0.9078f, -0.8043f)
                curveToRelative(-2.2181f, -1.7883f, -4.7947f, -3.0394f, -7.5396f, -3.6608f)
                curveToRelative(-1.5499f, -0.3509f, -3.2732f, -0.5095f, -4.8061f, -0.4424f)
                curveToRelative(-2.2755f, 0.0996f, -4.2253f, 0.5169f, -6.2821f, 1.3444f)
                curveToRelative(-1.8852f, 0.7585f, -3.7647f, 1.9232f, -5.2639f, 3.2622f)
                curveToRelative(-0.1747f, 0.1561f, -0.3177f, 0.2914f, -0.3177f, 0.3008f)
                curveToRelative(0.0f, 0.0152f, 3.1928f, 3.219f, 3.2079f, 3.219f)
                curveToRelative(0.0034f, 0.0f, 0.152f, -0.1229f, 0.3303f, -0.273f)
                close()
            }
        }.build()
    }
}


@Composable
fun overview(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "overview",
            defaultWidth = 24.0.dp, defaultHeight = 24.0.dp, viewportWidth = 960.0f,
            viewportHeight = 960.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(787.0f, 815.0f)
                lineToRelative(28.0f, -28.0f)
                lineToRelative(-75.0f, -75.0f)
                verticalLineToRelative(-112.0f)
                horizontalLineToRelative(-40.0f)
                verticalLineToRelative(128.0f)
                lineToRelative(87.0f, 87.0f)
                close()
                moveTo(200.0f, 840.0f)
                quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
                reflectiveQuadTo(120.0f, 760.0f)
                verticalLineToRelative(-560.0f)
                quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
                reflectiveQuadTo(200.0f, 120.0f)
                horizontalLineToRelative(560.0f)
                quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
                reflectiveQuadTo(840.0f, 200.0f)
                verticalLineToRelative(268.0f)
                quadToRelative(-19.0f, -9.0f, -39.0f, -15.5f)
                reflectiveQuadToRelative(-41.0f, -9.5f)
                verticalLineToRelative(-243.0f)
                lineTo(200.0f, 200.0f)
                verticalLineToRelative(560.0f)
                horizontalLineToRelative(242.0f)
                quadToRelative(3.0f, 22.0f, 9.5f, 42.0f)
                reflectiveQuadToRelative(15.5f, 38.0f)
                lineTo(200.0f, 840.0f)
                close()
                moveTo(200.0f, 720.0f)
                verticalLineToRelative(40.0f)
                verticalLineToRelative(-560.0f)
                verticalLineToRelative(243.0f)
                verticalLineToRelative(-3.0f)
                verticalLineToRelative(280.0f)
                close()
                moveTo(280.0f, 680.0f)
                horizontalLineToRelative(163.0f)
                quadToRelative(3.0f, -21.0f, 9.5f, -41.0f)
                reflectiveQuadToRelative(14.5f, -39.0f)
                lineTo(280.0f, 600.0f)
                verticalLineToRelative(80.0f)
                close()
                moveTo(280.0f, 520.0f)
                horizontalLineToRelative(244.0f)
                quadToRelative(32.0f, -30.0f, 71.5f, -50.0f)
                reflectiveQuadToRelative(84.5f, -27.0f)
                verticalLineToRelative(-3.0f)
                lineTo(280.0f, 440.0f)
                verticalLineToRelative(80.0f)
                close()
                moveTo(280.0f, 360.0f)
                horizontalLineToRelative(400.0f)
                verticalLineToRelative(-80.0f)
                lineTo(280.0f, 280.0f)
                verticalLineToRelative(80.0f)
                close()
                moveTo(720.0f, 920.0f)
                quadToRelative(-83.0f, 0.0f, -141.5f, -58.5f)
                reflectiveQuadTo(520.0f, 720.0f)
                quadToRelative(0.0f, -83.0f, 58.5f, -141.5f)
                reflectiveQuadTo(720.0f, 520.0f)
                quadToRelative(83.0f, 0.0f, 141.5f, 58.5f)
                reflectiveQuadTo(920.0f, 720.0f)
                quadToRelative(0.0f, 83.0f, -58.5f, 141.5f)
                reflectiveQuadTo(720.0f, 920.0f)
                close()
            }
        }.build()
    }
}


public val ShowChart: ImageVector
    get() {
        if (_showChart != null) {
            return _showChart!!
        }
        _showChart = materialIcon(name = "Outlined.ShowChart") {
            materialPath {
                moveTo(3.5f, 18.49f)
                lineToRelative(6.0f, -6.01f)
                lineToRelative(4.0f, 4.0f)
                lineTo(22.0f, 6.92f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(-7.09f, 7.97f)
                lineToRelative(-4.0f, -4.0f)
                lineTo(2.0f, 16.99f)
                lineToRelative(1.5f, 1.5f)
                close()
            }
        }
        return _showChart!!
    }

private var _showChart: ImageVector? = null


public val Map: ImageVector
    get() {
        if (_map != null) {
            return _map!!
        }
        _map = materialIcon(name = "Outlined.Map") {
            materialPath {
                moveTo(20.5f, 3.0f)
                lineToRelative(-0.16f, 0.03f)
                lineTo(15.0f, 5.1f)
                lineTo(9.0f, 3.0f)
                lineTo(3.36f, 4.9f)
                curveToRelative(-0.21f, 0.07f, -0.36f, 0.25f, -0.36f, 0.48f)
                lineTo(3.0f, 20.5f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                lineToRelative(0.16f, -0.03f)
                lineTo(9.0f, 18.9f)
                lineToRelative(6.0f, 2.1f)
                lineToRelative(5.64f, -1.9f)
                curveToRelative(0.21f, -0.07f, 0.36f, -0.25f, 0.36f, -0.48f)
                lineTo(21.0f, 3.5f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                close()
                moveTo(10.0f, 5.47f)
                lineToRelative(4.0f, 1.4f)
                verticalLineToRelative(11.66f)
                lineToRelative(-4.0f, -1.4f)
                lineTo(10.0f, 5.47f)
                close()
                moveTo(5.0f, 6.46f)
                lineToRelative(3.0f, -1.01f)
                verticalLineToRelative(11.7f)
                lineToRelative(-3.0f, 1.16f)
                lineTo(5.0f, 6.46f)
                close()
                moveTo(19.0f, 17.54f)
                lineToRelative(-3.0f, 1.01f)
                lineTo(16.0f, 6.86f)
                lineToRelative(3.0f, -1.16f)
                verticalLineToRelative(11.84f)
                close()
            }
        }
        return _map!!
    }

private var _map: ImageVector? = null


public val Help: ImageVector
    get() {
        if (_help != null) {
            return _help!!
        }
        _help = materialIcon(name = "Outlined.Help") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(13.0f, 19.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(15.07f, 11.25f)
                lineToRelative(-0.9f, 0.92f)
                curveTo(13.45f, 12.9f, 13.0f, 13.5f, 13.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-0.5f)
                curveToRelative(0.0f, -1.1f, 0.45f, -2.1f, 1.17f, -2.83f)
                lineToRelative(1.24f, -1.26f)
                curveToRelative(0.37f, -0.36f, 0.59f, -0.86f, 0.59f, -1.41f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                lineTo(8.0f, 9.0f)
                curveToRelative(0.0f, -2.21f, 1.79f, -4.0f, 4.0f, -4.0f)
                reflectiveCurveToRelative(4.0f, 1.79f, 4.0f, 4.0f)
                curveToRelative(0.0f, 0.88f, -0.36f, 1.68f, -0.93f, 2.25f)
                close()
            }
        }
        return _help!!
    }

private var _help: ImageVector? = null


public val Info: ImageVector
    get() {
        if (_info != null) {
            return _info!!
        }
        _info = materialIcon(name = "Outlined.Info") {
            materialPath {
                moveTo(11.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.59f, 8.0f, -8.0f, 8.0f)
                close()
            }
        }
        return _info!!
    }

private var _info: ImageVector? = null


public val CenterFocusWeak: ImageVector
    get() {
        if (_centerFocusWeak != null) {
            return _centerFocusWeak!!
        }
        _centerFocusWeak = materialIcon(name = "Filled.CenterFocusWeak") {
            materialPath {
                moveTo(5.0f, 15.0f)
                lineTo(3.0f, 15.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-2.0f)
                lineTo(5.0f, 19.0f)
                verticalLineToRelative(-4.0f)
                close()
                moveTo(5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                lineTo(9.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(2.0f)
                lineTo(5.0f, 5.0f)
                close()
                moveTo(19.0f, 3.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 19.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(12.0f, 8.0f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                close()
                moveTo(12.0f, 14.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _centerFocusWeak!!
    }

private var _centerFocusWeak: ImageVector? = null


public val Settings: ImageVector
    get() {
        if (_settings != null) {
            return _settings!!
        }
        _settings = materialIcon(name = "Outlined.Settings") {
            materialPath {
                moveTo(19.43f, 12.98f)
                curveToRelative(0.04f, -0.32f, 0.07f, -0.64f, 0.07f, -0.98f)
                curveToRelative(0.0f, -0.34f, -0.03f, -0.66f, -0.07f, -0.98f)
                lineToRelative(2.11f, -1.65f)
                curveToRelative(0.19f, -0.15f, 0.24f, -0.42f, 0.12f, -0.64f)
                lineToRelative(-2.0f, -3.46f)
                curveToRelative(-0.09f, -0.16f, -0.26f, -0.25f, -0.44f, -0.25f)
                curveToRelative(-0.06f, 0.0f, -0.12f, 0.01f, -0.17f, 0.03f)
                lineToRelative(-2.49f, 1.0f)
                curveToRelative(-0.52f, -0.4f, -1.08f, -0.73f, -1.69f, -0.98f)
                lineToRelative(-0.38f, -2.65f)
                curveTo(14.46f, 2.18f, 14.25f, 2.0f, 14.0f, 2.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-0.25f, 0.0f, -0.46f, 0.18f, -0.49f, 0.42f)
                lineToRelative(-0.38f, 2.65f)
                curveToRelative(-0.61f, 0.25f, -1.17f, 0.59f, -1.69f, 0.98f)
                lineToRelative(-2.49f, -1.0f)
                curveToRelative(-0.06f, -0.02f, -0.12f, -0.03f, -0.18f, -0.03f)
                curveToRelative(-0.17f, 0.0f, -0.34f, 0.09f, -0.43f, 0.25f)
                lineToRelative(-2.0f, 3.46f)
                curveToRelative(-0.13f, 0.22f, -0.07f, 0.49f, 0.12f, 0.64f)
                lineToRelative(2.11f, 1.65f)
                curveToRelative(-0.04f, 0.32f, -0.07f, 0.65f, -0.07f, 0.98f)
                curveToRelative(0.0f, 0.33f, 0.03f, 0.66f, 0.07f, 0.98f)
                lineToRelative(-2.11f, 1.65f)
                curveToRelative(-0.19f, 0.15f, -0.24f, 0.42f, -0.12f, 0.64f)
                lineToRelative(2.0f, 3.46f)
                curveToRelative(0.09f, 0.16f, 0.26f, 0.25f, 0.44f, 0.25f)
                curveToRelative(0.06f, 0.0f, 0.12f, -0.01f, 0.17f, -0.03f)
                lineToRelative(2.49f, -1.0f)
                curveToRelative(0.52f, 0.4f, 1.08f, 0.73f, 1.69f, 0.98f)
                lineToRelative(0.38f, 2.65f)
                curveToRelative(0.03f, 0.24f, 0.24f, 0.42f, 0.49f, 0.42f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.25f, 0.0f, 0.46f, -0.18f, 0.49f, -0.42f)
                lineToRelative(0.38f, -2.65f)
                curveToRelative(0.61f, -0.25f, 1.17f, -0.59f, 1.69f, -0.98f)
                lineToRelative(2.49f, 1.0f)
                curveToRelative(0.06f, 0.02f, 0.12f, 0.03f, 0.18f, 0.03f)
                curveToRelative(0.17f, 0.0f, 0.34f, -0.09f, 0.43f, -0.25f)
                lineToRelative(2.0f, -3.46f)
                curveToRelative(0.12f, -0.22f, 0.07f, -0.49f, -0.12f, -0.64f)
                lineToRelative(-2.11f, -1.65f)
                close()
                moveTo(17.45f, 11.27f)
                curveToRelative(0.04f, 0.31f, 0.05f, 0.52f, 0.05f, 0.73f)
                curveToRelative(0.0f, 0.21f, -0.02f, 0.43f, -0.05f, 0.73f)
                lineToRelative(-0.14f, 1.13f)
                lineToRelative(0.89f, 0.7f)
                lineToRelative(1.08f, 0.84f)
                lineToRelative(-0.7f, 1.21f)
                lineToRelative(-1.27f, -0.51f)
                lineToRelative(-1.04f, -0.42f)
                lineToRelative(-0.9f, 0.68f)
                curveToRelative(-0.43f, 0.32f, -0.84f, 0.56f, -1.25f, 0.73f)
                lineToRelative(-1.06f, 0.43f)
                lineToRelative(-0.16f, 1.13f)
                lineToRelative(-0.2f, 1.35f)
                horizontalLineToRelative(-1.4f)
                lineToRelative(-0.19f, -1.35f)
                lineToRelative(-0.16f, -1.13f)
                lineToRelative(-1.06f, -0.43f)
                curveToRelative(-0.43f, -0.18f, -0.83f, -0.41f, -1.23f, -0.71f)
                lineToRelative(-0.91f, -0.7f)
                lineToRelative(-1.06f, 0.43f)
                lineToRelative(-1.27f, 0.51f)
                lineToRelative(-0.7f, -1.21f)
                lineToRelative(1.08f, -0.84f)
                lineToRelative(0.89f, -0.7f)
                lineToRelative(-0.14f, -1.13f)
                curveToRelative(-0.03f, -0.31f, -0.05f, -0.54f, -0.05f, -0.74f)
                reflectiveCurveToRelative(0.02f, -0.43f, 0.05f, -0.73f)
                lineToRelative(0.14f, -1.13f)
                lineToRelative(-0.89f, -0.7f)
                lineToRelative(-1.08f, -0.84f)
                lineToRelative(0.7f, -1.21f)
                lineToRelative(1.27f, 0.51f)
                lineToRelative(1.04f, 0.42f)
                lineToRelative(0.9f, -0.68f)
                curveToRelative(0.43f, -0.32f, 0.84f, -0.56f, 1.25f, -0.73f)
                lineToRelative(1.06f, -0.43f)
                lineToRelative(0.16f, -1.13f)
                lineToRelative(0.2f, -1.35f)
                horizontalLineToRelative(1.39f)
                lineToRelative(0.19f, 1.35f)
                lineToRelative(0.16f, 1.13f)
                lineToRelative(1.06f, 0.43f)
                curveToRelative(0.43f, 0.18f, 0.83f, 0.41f, 1.23f, 0.71f)
                lineToRelative(0.91f, 0.7f)
                lineToRelative(1.06f, -0.43f)
                lineToRelative(1.27f, -0.51f)
                lineToRelative(0.7f, 1.21f)
                lineToRelative(-1.07f, 0.85f)
                lineToRelative(-0.89f, 0.7f)
                lineToRelative(0.14f, 1.13f)
                close()
                moveTo(12.0f, 8.0f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                close()
                moveTo(12.0f, 14.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _settings!!
    }

private var _settings: ImageVector? = null


public val Mic: ImageVector
    get() {
        if (_mic != null) {
            return _mic!!
        }
        _mic = materialIcon(name = "Outlined.Mic") {
            materialPath {
                moveTo(12.0f, 14.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
                verticalLineToRelative(6.0f)
                curveTo(9.0f, 12.66f, 10.34f, 14.0f, 12.0f, 14.0f)
                close()
            }
            materialPath {
                moveTo(17.0f, 11.0f)
                curveToRelative(0.0f, 2.76f, -2.24f, 5.0f, -5.0f, 5.0f)
                reflectiveCurveToRelative(-5.0f, -2.24f, -5.0f, -5.0f)
                horizontalLineTo(5.0f)
                curveToRelative(0.0f, 3.53f, 2.61f, 6.43f, 6.0f, 6.92f)
                verticalLineTo(21.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-3.08f)
                curveToRelative(3.39f, -0.49f, 6.0f, -3.39f, 6.0f, -6.92f)
                horizontalLineTo(17.0f)
                close()
            }
        }
        return _mic!!
    }

private var _mic: ImageVector? = null

public val QuestionMark: ImageVector
    get() {
        if (_questionMark != null) {
            return _questionMark!!
        }
        _questionMark = materialIcon(name = "Outlined.QuestionMark") {
            materialPath {
                moveTo(11.07f, 12.85f)
                curveToRelative(0.77f, -1.39f, 2.25f, -2.21f, 3.11f, -3.44f)
                curveToRelative(0.91f, -1.29f, 0.4f, -3.7f, -2.18f, -3.7f)
                curveToRelative(-1.69f, 0.0f, -2.52f, 1.28f, -2.87f, 2.34f)
                lineTo(6.54f, 6.96f)
                curveTo(7.25f, 4.83f, 9.18f, 3.0f, 11.99f, 3.0f)
                curveToRelative(2.35f, 0.0f, 3.96f, 1.07f, 4.78f, 2.41f)
                curveToRelative(0.7f, 1.15f, 1.11f, 3.3f, 0.03f, 4.9f)
                curveToRelative(-1.2f, 1.77f, -2.35f, 2.31f, -2.97f, 3.45f)
                curveToRelative(-0.25f, 0.46f, -0.35f, 0.76f, -0.35f, 2.24f)
                horizontalLineToRelative(-2.89f)
                curveTo(10.58f, 15.22f, 10.46f, 13.95f, 11.07f, 12.85f)
                close()
                moveTo(14.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveTo(14.0f, 18.9f, 14.0f, 20.0f)
                close()
            }
        }
        return _questionMark!!
    }

private var _questionMark: ImageVector? = null


public val Check: ImageVector
    get() {
        if (_check != null) {
            return _check!!
        }
        _check = materialIcon(name = "Filled.Check") {
            materialPath {
                moveTo(9.0f, 16.17f)
                lineTo(4.83f, 12.0f)
                lineToRelative(-1.42f, 1.41f)
                lineTo(9.0f, 19.0f)
                lineTo(21.0f, 7.0f)
                lineToRelative(-1.41f, -1.41f)
                close()
            }
        }
        return _check!!
    }

private var _check: ImageVector? = null

public val Close: ImageVector
    get() {
        if (_close != null) {
            return _close!!
        }
        _close = materialIcon(name = "Outlined.Close") {
            materialPath {
                moveTo(19.0f, 6.41f)
                lineTo(17.59f, 5.0f)
                lineTo(12.0f, 10.59f)
                lineTo(6.41f, 5.0f)
                lineTo(5.0f, 6.41f)
                lineTo(10.59f, 12.0f)
                lineTo(5.0f, 17.59f)
                lineTo(6.41f, 19.0f)
                lineTo(12.0f, 13.41f)
                lineTo(17.59f, 19.0f)
                lineTo(19.0f, 17.59f)
                lineTo(13.41f, 12.0f)
                lineTo(19.0f, 6.41f)
                close()
            }
        }
        return _close!!
    }

private var _close: ImageVector? = null
