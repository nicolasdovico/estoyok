package com.estoyok.app.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity

data class WindowInfo(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val fontScale: Float,
    val isNarrowScreen: Boolean,
    val isHugeFont: Boolean
)

@Composable
fun rememberWindowInfo(): WindowInfo {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    return remember(configuration, density) {
        WindowInfo(
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp,
            fontScale = density.fontScale,
            isNarrowScreen = configuration.screenWidthDp < 365,
            isHugeFont = density.fontScale > 1.2f
        )
    }
}
