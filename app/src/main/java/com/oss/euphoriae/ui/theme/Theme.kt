@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)
@file:Suppress("RestrictedApi")

package com.oss.euphoriae.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeContent
import com.google.android.material.color.utilities.SchemeTonalSpot
import com.oss.euphoriae.data.preferences.ThemeColorOption

// Seed colors for each theme
private const val SEED_PURPLE = 0xFF6750A4.toInt()
private const val SEED_BLUE = 0xFF0061A4.toInt()
private const val SEED_GREEN = 0xFF386A20.toInt()
private const val SEED_ORANGE = 0xFF8B5000.toInt()
private const val SEED_PINK = 0xFFBC004B.toInt()
private const val SEED_RED = 0xFFBA1A1A.toInt()

/**
 * Generate a complete Material 3 ColorScheme from a seed color using Material Color Utilities
 */
private fun generateColorScheme(seedColor: Int, isDark: Boolean): ColorScheme {
    val scheme = SchemeTonalSpot(
        Hct.fromInt(seedColor),
        isDark,
        0.0 // contrast level
    )
    
    val dynamicColors = MaterialDynamicColors()
    
    return if (isDark) {
        darkColorScheme(
            primary = Color(dynamicColors.primary().getArgb(scheme)),
            onPrimary = Color(dynamicColors.onPrimary().getArgb(scheme)),
            primaryContainer = Color(dynamicColors.primaryContainer().getArgb(scheme)),
            onPrimaryContainer = Color(dynamicColors.onPrimaryContainer().getArgb(scheme)),
            inversePrimary = Color(dynamicColors.inversePrimary().getArgb(scheme)),
            secondary = Color(dynamicColors.secondary().getArgb(scheme)),
            onSecondary = Color(dynamicColors.onSecondary().getArgb(scheme)),
            secondaryContainer = Color(dynamicColors.secondaryContainer().getArgb(scheme)),
            onSecondaryContainer = Color(dynamicColors.onSecondaryContainer().getArgb(scheme)),
            tertiary = Color(dynamicColors.tertiary().getArgb(scheme)),
            onTertiary = Color(dynamicColors.onTertiary().getArgb(scheme)),
            tertiaryContainer = Color(dynamicColors.tertiaryContainer().getArgb(scheme)),
            onTertiaryContainer = Color(dynamicColors.onTertiaryContainer().getArgb(scheme)),
            background = Color(dynamicColors.background().getArgb(scheme)),
            onBackground = Color(dynamicColors.onBackground().getArgb(scheme)),
            surface = Color(dynamicColors.surface().getArgb(scheme)),
            onSurface = Color(dynamicColors.onSurface().getArgb(scheme)),
            surfaceVariant = Color(dynamicColors.surfaceVariant().getArgb(scheme)),
            onSurfaceVariant = Color(dynamicColors.onSurfaceVariant().getArgb(scheme)),
            surfaceTint = Color(dynamicColors.primary().getArgb(scheme)),
            inverseSurface = Color(dynamicColors.inverseSurface().getArgb(scheme)),
            inverseOnSurface = Color(dynamicColors.inverseOnSurface().getArgb(scheme)),
            error = Color(dynamicColors.error().getArgb(scheme)),
            onError = Color(dynamicColors.onError().getArgb(scheme)),
            errorContainer = Color(dynamicColors.errorContainer().getArgb(scheme)),
            onErrorContainer = Color(dynamicColors.onErrorContainer().getArgb(scheme)),
            outline = Color(dynamicColors.outline().getArgb(scheme)),
            outlineVariant = Color(dynamicColors.outlineVariant().getArgb(scheme)),
            scrim = Color(0xFF000000),
            surfaceBright = Color(dynamicColors.surfaceBright().getArgb(scheme)),
            surfaceContainer = Color(dynamicColors.surfaceContainer().getArgb(scheme)),
            surfaceContainerHigh = Color(dynamicColors.surfaceContainerHigh().getArgb(scheme)),
            surfaceContainerHighest = Color(dynamicColors.surfaceContainerHighest().getArgb(scheme)),
            surfaceContainerLow = Color(dynamicColors.surfaceContainerLow().getArgb(scheme)),
            surfaceContainerLowest = Color(dynamicColors.surfaceContainerLowest().getArgb(scheme)),
            surfaceDim = Color(dynamicColors.surfaceDim().getArgb(scheme))
        )
    } else {
        lightColorScheme(
            primary = Color(dynamicColors.primary().getArgb(scheme)),
            onPrimary = Color(dynamicColors.onPrimary().getArgb(scheme)),
            primaryContainer = Color(dynamicColors.primaryContainer().getArgb(scheme)),
            onPrimaryContainer = Color(dynamicColors.onPrimaryContainer().getArgb(scheme)),
            inversePrimary = Color(dynamicColors.inversePrimary().getArgb(scheme)),
            secondary = Color(dynamicColors.secondary().getArgb(scheme)),
            onSecondary = Color(dynamicColors.onSecondary().getArgb(scheme)),
            secondaryContainer = Color(dynamicColors.secondaryContainer().getArgb(scheme)),
            onSecondaryContainer = Color(dynamicColors.onSecondaryContainer().getArgb(scheme)),
            tertiary = Color(dynamicColors.tertiary().getArgb(scheme)),
            onTertiary = Color(dynamicColors.onTertiary().getArgb(scheme)),
            tertiaryContainer = Color(dynamicColors.tertiaryContainer().getArgb(scheme)),
            onTertiaryContainer = Color(dynamicColors.onTertiaryContainer().getArgb(scheme)),
            background = Color(dynamicColors.background().getArgb(scheme)),
            onBackground = Color(dynamicColors.onBackground().getArgb(scheme)),
            surface = Color(dynamicColors.surface().getArgb(scheme)),
            onSurface = Color(dynamicColors.onSurface().getArgb(scheme)),
            surfaceVariant = Color(dynamicColors.surfaceVariant().getArgb(scheme)),
            onSurfaceVariant = Color(dynamicColors.onSurfaceVariant().getArgb(scheme)),
            surfaceTint = Color(dynamicColors.primary().getArgb(scheme)),
            inverseSurface = Color(dynamicColors.inverseSurface().getArgb(scheme)),
            inverseOnSurface = Color(dynamicColors.inverseOnSurface().getArgb(scheme)),
            error = Color(dynamicColors.error().getArgb(scheme)),
            onError = Color(dynamicColors.onError().getArgb(scheme)),
            errorContainer = Color(dynamicColors.errorContainer().getArgb(scheme)),
            onErrorContainer = Color(dynamicColors.onErrorContainer().getArgb(scheme)),
            outline = Color(dynamicColors.outline().getArgb(scheme)),
            outlineVariant = Color(dynamicColors.outlineVariant().getArgb(scheme)),
            scrim = Color(0xFF000000),
            surfaceBright = Color(dynamicColors.surfaceBright().getArgb(scheme)),
            surfaceContainer = Color(dynamicColors.surfaceContainer().getArgb(scheme)),
            surfaceContainerHigh = Color(dynamicColors.surfaceContainerHigh().getArgb(scheme)),
            surfaceContainerHighest = Color(dynamicColors.surfaceContainerHighest().getArgb(scheme)),
            surfaceContainerLow = Color(dynamicColors.surfaceContainerLow().getArgb(scheme)),
            surfaceContainerLowest = Color(dynamicColors.surfaceContainerLowest().getArgb(scheme)),
            surfaceDim = Color(dynamicColors.surfaceDim().getArgb(scheme))
        )
    }
}

// Pre-generated color schemes for better performance
private val PurpleLightColorScheme by lazy { generateColorScheme(SEED_PURPLE, false) }
private val PurpleDarkColorScheme by lazy { generateColorScheme(SEED_PURPLE, true) }
private val BlueLightColorScheme by lazy { generateColorScheme(SEED_BLUE, false) }
private val BlueDarkColorScheme by lazy { generateColorScheme(SEED_BLUE, true) }
private val GreenLightColorScheme by lazy { generateColorScheme(SEED_GREEN, false) }
private val GreenDarkColorScheme by lazy { generateColorScheme(SEED_GREEN, true) }
private val OrangeLightColorScheme by lazy { generateColorScheme(SEED_ORANGE, false) }
private val OrangeDarkColorScheme by lazy { generateColorScheme(SEED_ORANGE, true) }
private val PinkLightColorScheme by lazy { generateColorScheme(SEED_PINK, false) }
private val PinkDarkColorScheme by lazy { generateColorScheme(SEED_PINK, true) }
private val RedLightColorScheme by lazy { generateColorScheme(SEED_RED, false) }
private val RedDarkColorScheme by lazy { generateColorScheme(SEED_RED, true) }

@Composable
fun EuphoriaeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: ThemeColorOption = ThemeColorOption.DYNAMIC,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when (themeColor) {
        ThemeColorOption.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
            }
        }
        ThemeColorOption.PURPLE -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        ThemeColorOption.BLUE -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        ThemeColorOption.GREEN -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        ThemeColorOption.ORANGE -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
        ThemeColorOption.PINK -> if (darkTheme) PinkDarkColorScheme else PinkLightColorScheme
        ThemeColorOption.RED -> if (darkTheme) RedDarkColorScheme else RedLightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography,
        content = content
    )
}
