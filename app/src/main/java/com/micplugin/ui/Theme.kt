package com.micplugin.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.micplugin.ui.ThemeConfig.MaterialStyle
import com.micplugin.ui.ThemeConfig.ThemeMode

// ── Studio color palette ──────────────────────────────────────────────────────
object StudioColors {
    // 深色主题颜色
    val Background   = Color(0xFF050508)
    val Surface      = Color(0xFF0E0E14)
    val Card         = Color(0xFF15151E)
    val CardElevated = Color(0xFF1C1C2A)
    val Accent       = Color(0xFF7C5CFC)
    val AccentDim    = Color(0xFF4A3598)
    val Secondary    = Color(0xFFFC5CF8)
    val MeterGreen   = Color(0xFF3DFCAC)
    val MeterAmber   = Color(0xFFFCB43D)
    val MeterRed     = Color(0xFFFC3D5C)
    val GainReduction = Color(0xFFFC8C3D)
    val TextPrimary  = Color(0xFFE8E8F0)
    val TextMuted    = Color(0xFF666680)
    val Border       = Color(0xFF252535)

    // Plugin format badge colors
    val Lv2Color     = Color(0xFF00BFA5)
    val ClapColor    = Color(0xFF69F0AE)
    val Vst3Color    = Color(0xFFAA00FF)
    val ApkColor     = Color(0xFFFF6D00)

    // 浅色主题颜色
    val LightBackground   = Color(0xFFF5F5F7)
    val LightSurface      = Color(0xFFFFFFFF)
    val LightCard         = Color(0xFFF9F9FB)
    val LightTextPrimary  = Color(0xFF1A1A1E)
    val LightTextMuted    = Color(0xFF6B6B75)
    val LightBorder       = Color(0xFFE5E5EA)
}

// ── 主题颜色方案 ──────────────────────────────────────────────────────
private val darkColorScheme = darkColorScheme(
    primary          = StudioColors.Accent,
    onPrimary        = Color.White,
    primaryContainer = StudioColors.AccentDim,
    secondary        = StudioColors.Secondary,
    background       = StudioColors.Background,
    surface          = StudioColors.Surface,
    surfaceVariant   = StudioColors.Card,
    onBackground     = StudioColors.TextPrimary,
    onSurface        = StudioColors.TextPrimary,
    onSurfaceVariant = StudioColors.TextMuted,
    outline          = StudioColors.Border,
    error            = StudioColors.MeterRed,
)

private val lightColorScheme = lightColorScheme(
    primary          = StudioColors.Accent,
    onPrimary        = Color.White,
    primaryContainer = StudioColors.AccentDim,
    secondary        = StudioColors.Secondary,
    background       = StudioColors.LightBackground,
    surface          = StudioColors.LightSurface,
    surfaceVariant   = StudioColors.LightCard,
    onBackground     = StudioColors.LightTextPrimary,
    onSurface        = StudioColors.LightTextPrimary,
    onSurfaceVariant = StudioColors.LightTextMuted,
    outline          = StudioColors.LightBorder,
    error            = StudioColors.MeterRed,
)

// ── 玻璃材质背景效果 ──────────────────────────────────────────────────────
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 0.6f,
    blurAmount: Int = 25,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Color.White.copy(alpha = alpha),
                androidx.compose.ui.draw.clip(RoundedCornerShape(16.dp))
            )
            .blur(blurAmount.dp)
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.7f),
        tonalElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier
                .background(
                    androidx.compose.ui.draw.clip(RoundedCornerShape(16.dp))
                )
                .blur(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun MicPluginTheme(
    content: @Composable () -> Unit,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    materialStyle: MaterialStyle = MaterialStyle.GLASS,
) {
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> lightColorScheme
        ThemeMode.DARK  -> darkColorScheme
        ThemeMode.SYSTEM -> if (androidx.compose.ui.platform.LocalContext.current.isSystemInDarkTheme())
            darkColorScheme else lightColorScheme
    }

    // 根据材质风格调整效果
    val isGlass = materialStyle == MaterialStyle.GLASS

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
