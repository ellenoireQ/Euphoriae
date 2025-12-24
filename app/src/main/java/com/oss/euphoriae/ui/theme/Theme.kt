package com.oss.euphoriae.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.oss.euphoriae.data.preferences.ThemeColorOption

// Purple Color Schemes
private val PurpleDarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Pink80,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Pink40,
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

// Blue Color Schemes
private val BlueDarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004880),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = BlueGrey80,
    onSecondary = Color(0xFF263238),
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFCFD8DC),
    tertiary = BlueAccent80,
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7CF)
)

private val BlueLightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = BlueGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFD8DC),
    onSecondaryContainer = Color(0xFF102027),
    tertiary = BlueAccent40,
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E)
)

// Green Color Schemes
private val GreenDarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF005319),
    onPrimaryContainer = Color(0xFFA7F5A0),
    secondary = GreenGrey80,
    onSecondary = Color(0xFF1B3820),
    secondaryContainer = Color(0xFF324F36),
    onSecondaryContainer = Color(0xFFBEEBBE),
    tertiary = GreenAccent80,
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BD)
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7F5A0),
    onPrimaryContainer = Color(0xFF002105),
    secondary = GreenGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBEEBBE),
    onSecondaryContainer = Color(0xFF05210A),
    tertiary = GreenAccent40,
    surface = Color(0xFFFCFDF7),
    onSurface = Color(0xFF1A1C19)
)

// Orange Color Schemes
private val OrangeDarkColorScheme = darkColorScheme(
    primary = Orange80,
    onPrimary = Color(0xFF4E2600),
    primaryContainer = Color(0xFF6F3800),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = OrangeGrey80,
    onSecondary = Color(0xFF3E2D1B),
    secondaryContainer = Color(0xFF564430),
    onSecondaryContainer = Color(0xFFE5D6C6),
    tertiary = OrangeAccent80,
    surface = Color(0xFF1F1B16),
    onSurface = Color(0xFFEAE1D9),
    surfaceVariant = Color(0xFF504539),
    onSurfaceVariant = Color(0xFFD3C4B4)
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Orange40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC2),
    onPrimaryContainer = Color(0xFF2C1600),
    secondary = OrangeGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5D6C6),
    onSecondaryContainer = Color(0xFF231A0D),
    tertiary = OrangeAccent40,
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1F1B16)
)

// Pink Color Schemes
private val PinkDarkColorScheme = darkColorScheme(
    primary = PinkBright80,
    onPrimary = Color(0xFF5D1149),
    primaryContainer = Color(0xFF7B2962),
    onPrimaryContainer = Color(0xFFFFD8E8),
    secondary = PinkGrey80,
    onSecondary = Color(0xFF3B2838),
    secondaryContainer = Color(0xFF533E4F),
    onSecondaryContainer = Color(0xFFE8D6E3),
    tertiary = PinkAccent80,
    surface = Color(0xFF1E1A1D),
    onSurface = Color(0xFFE9E0E4),
    surfaceVariant = Color(0xFF4D444B),
    onSurfaceVariant = Color(0xFFCFC4CB)
)

private val PinkLightColorScheme = lightColorScheme(
    primary = PinkBright40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD8E8),
    onPrimaryContainer = Color(0xFF3E0021),
    secondary = PinkGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8D6E3),
    onSecondaryContainer = Color(0xFF231320),
    tertiary = PinkAccent40,
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1E1A1D)
)

// Red Color Schemes
private val RedDarkColorScheme = darkColorScheme(
    primary = Red80,
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = RedGrey80,
    onSecondary = Color(0xFF442926),
    secondaryContainer = Color(0xFF5D3F3B),
    onSecondaryContainer = Color(0xFFE8D6D4),
    tertiary = RedAccent80,
    surface = Color(0xFF1F1A1A),
    onSurface = Color(0xFFEAE0DF),
    surfaceVariant = Color(0xFF524241),
    onSurfaceVariant = Color(0xFFD7C1BF)
)

private val RedLightColorScheme = lightColorScheme(
    primary = Red40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = RedGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8D6D4),
    onSecondaryContainer = Color(0xFF2C1512),
    tertiary = RedAccent40,
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1F1A1A)
)

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}