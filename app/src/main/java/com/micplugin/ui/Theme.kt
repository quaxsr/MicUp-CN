package com.micplugin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── Studio color palette ──────────────────────────────────────────────────────
//
// 色板是动态的：MicPluginTheme 根据用户选择的主题模式（深 / 浅 / 跟随系统）写入
// darkTheme，所有 getter 都由 Compose State 驱动 —— 切换主题时所有引用处自动重组。
// "液态玻璃"材质下，背景与卡片变为半透明磨砂色，配合底层氛围光形成玻璃质感；
// "标准 Material"材质下保持完全不透明，性能最好。
object StudioColors {
    // 当前主题状态（由 MicPluginTheme 在组合时写入）
    internal var darkTheme by mutableStateOf(true)
    internal var glassEnabled by mutableStateOf(false)

    // ── 深色：深蓝/灰调 + App 现有紫色点缀 ──
    private val dBackground = Color(0xFF070A12)
    private val dSurface    = Color(0xFF0E1220)
    private val dCard       = Color(0xFF141A2A)
    private val dCardElev   = Color(0xFF1C2338)
    private val dTextPri    = Color(0xFFE8EAF4)
    private val dTextMut    = Color(0xFF6B7390)
    private val dBorder     = Color(0xFF232B42)

    // ── 浅色：低饱和柔和粉/紫调 ──
    private val lBackground = Color(0xFFF6F2F8)
    private val lSurface    = Color(0xFFFDFCFF)
    private val lCard       = Color(0xFFFAF6FB)
    private val lCardElev   = Color(0xFFFFFFFF)
    private val lTextPri    = Color(0xFF2B2438)
    private val lTextMut    = Color(0xFF8E86A0)
    private val lBorder     = Color(0xFFE6DFEE)

    // 主色：深色沿用现有紫；浅色用低饱和粉紫
    val Accent   get() = if (darkTheme) Color(0xFF7C5CFC) else Color(0xFFB48CD9)
    val AccentDim get() = if (darkTheme) Color(0xFF4A3598) else Color(0xFFDCC9EE)
    val Secondary get() = if (darkTheme) Color(0xFFFC5CF8) else Color(0xFFD77BD0)

    // 玻璃材质下背景半透明，让底层的氛围光透出来
    val Background get() = when {
        darkTheme && glassEnabled -> dBackground.copy(alpha = 0.40f)
        darkTheme                 -> dBackground
        glassEnabled              -> lBackground.copy(alpha = 0.45f)
        else                      -> lBackground
    }
    val Surface get() = when {
        darkTheme && glassEnabled -> dSurface.copy(alpha = 0.45f)
        darkTheme                 -> dSurface
        glassEnabled              -> lSurface.copy(alpha = 0.55f)
        else                      -> lSurface
    }

    // 玻璃材质：半透明磨砂卡片
    val Card get() = when {
        darkTheme && glassEnabled -> dCard.copy(alpha = 0.62f)
        darkTheme                 -> dCard
        glassEnabled              -> lCard.copy(alpha = 0.62f)
        else                      -> lCard
    }
    val CardElevated get() = if (darkTheme) dCardElev else lCardElev

    // 电平表/状态色两主题通用，浅色下加深以保证对比度
    val MeterGreen     get() = if (darkTheme) Color(0xFF3DFCAC) else Color(0xFF14A97A)
    val MeterAmber     get() = if (darkTheme) Color(0xFFFCB43D) else Color(0xFFC4861B)
    val MeterRed       get() = if (darkTheme) Color(0xFFFC3D5C) else Color(0xFFD93354)
    val GainReduction  get() = if (darkTheme) Color(0xFFFC8C3D) else Color(0xFFCF722B)

    val TextPrimary get() = if (darkTheme) dTextPri else lTextPri
    val TextMuted   get() = if (darkTheme) dTextMut else lTextMut
    val Border      get() = if (darkTheme) dBorder else lBorder

    // Plugin format badge colors
    val Lv2Color  = Color(0xFF00BFA5)
    val ClapColor = Color(0xFF69F0AE)
    val Vst3Color = Color(0xFFAA00FF)
    val ApkColor  = Color(0xFFFF6D00)
}

private fun darkColorSchemeOf() = darkColorScheme(
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

private fun lightColorSchemeOf() = lightColorScheme(
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

@Composable
fun MicPluginTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    materialStyle: MaterialStyle = MaterialStyle.STANDARD,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    // 同一组合帧内写入，子组件读取 getter 时立即拿到新值并订阅后续变化
    StudioColors.darkTheme = dark
    StudioColors.glassEnabled = materialStyle == MaterialStyle.GLASS

    MaterialTheme(
        colorScheme = if (dark) darkColorSchemeOf() else lightColorSchemeOf(),
        content = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (dark) Brush.verticalGradient(listOf(Color(0xFF070A12), Color(0xFF0C1120)))
                        else Brush.verticalGradient(listOf(Color(0xFFF7F3FA), Color(0xFFEFF0F7)))
                    ),
            ) {
                if (StudioColors.glassEnabled) AmbientGlows(dark)
                content()
            }
        },
    )
}

/** 液态玻璃材质下的氛围光斑：为半透明卡片提供可"透"出的背景光。 */
@Composable
private fun AmbientGlows(dark: Boolean) {
    val glowA = if (dark) Color(0xFF7C5CFC) else Color(0xFFC9A6E8)
    val glowB = if (dark) Color(0xFFFC5CF8) else Color(0xFFF2BCD9)
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.95f)
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        0f to glowA.copy(alpha = if (dark) 0.38f else 0.42f),
                        1f to Color.Transparent,
                    )
                ),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.95f)
                .height(280.dp)
                .background(
                    Brush.radialGradient(
                        0f to glowB.copy(alpha = if (dark) 0.20f else 0.34f),
                        1f to Color.Transparent,
                    )
                ),
        )
    }
}
