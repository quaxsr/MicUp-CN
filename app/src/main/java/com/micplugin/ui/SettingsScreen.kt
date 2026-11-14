package com.micplugin.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.micplugin.CrashReporter
import com.micplugin.service.ShizukuState
import com.micplugin.service.VirtualMicTier

@Composable
fun SettingsScreen(navController: NavController, vm: AudioViewModel = hiltViewModel()) {
    val status      by vm.engineStatus.collectAsState()
    val tier        by vm.virtualMicTier.collectAsState()
    val shizukuState by vm.shizukuState.collectAsState()
    val context     = LocalContext.current

    // 主题与材质来自全局状态（ThemeState），修改后即时生效并持久化
    val themeMode = ThemeState.themeMode
    val materialStyle = ThemeState.materialStyle

    var crashReports by remember { mutableStateOf<List<java.io.File>>(emptyList()) }
    var viewingCrash by remember { mutableStateOf<java.io.File?>(null) }

    LaunchedEffect(Unit) { crashReports = CrashReporter.getCrashReports(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioColors.Background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        Surface(color = StudioColors.Surface, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "返回", tint = StudioColors.TextPrimary)
                }
                Text("设置", style = MaterialTheme.typography.titleMedium,
                    color = StudioColors.TextPrimary, letterSpacing = 2.sp)
            }
        }

        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // ── Audio ─────────────────────────────────────────────────────────
            SettingsSection("音频") {
                SettingsInfoRow("采样率", "${status.sampleRate} Hz")
                SettingsInfoRow("每周期帧数", "${status.framesPerBurst}")
                SettingsInfoRow("延迟（估算）", "~${status.latencyMs.toInt()} ms")
            }

            // ── Shizuku ───────────────────────────────────────────────────────
            SettingsSection("Shizuku（ADB 权限）") {
                val (stateColor, stateLabel) = when (shizukuState) {
                    ShizukuState.READY       -> Color(0xFF3DFCAC) to "已就绪"
                    ShizukuState.NEED_GRANT  -> Color(0xFFFFD700) to "需要授权"
                    ShizukuState.UNAVAILABLE -> Color(0xFFFF6B6B) to "未运行"
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("状态", fontSize = 12.sp, color = StudioColors.TextMuted)
                        Text(stateLabel, fontSize = 11.sp, color = stateColor)
                    }
                    if (shizukuState == ShizukuState.NEED_GRANT) {
                        Button(
                            onClick = { vm.requestShizukuPermission() },
                            colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text("授权", fontSize = 11.sp)
                        }
                    }
                }
                if (shizukuState == ShizukuState.UNAVAILABLE) {
                    Text(
                        "请先安装 Shizuku，并通过 ADB 或无线调试启动它，然后重新打开本应用。",
                        fontSize = 10.sp, color = StudioColors.TextMuted, lineHeight = 14.sp,
                    )
                }
                if (shizukuState == ShizukuState.READY) {
                    Text(
                        "Shizuku 已激活 — 可使用 ALSA 内录与 appops 路由。",
                        fontSize = 10.sp, color = StudioColors.TextMuted, lineHeight = 14.sp,
                    )
                }
            }

            // ── Virtual Mic ───────────────────────────────────────────────────
            SettingsSection("虚拟麦克风") {
                Surface(
                    shape  = RoundedCornerShape(7.dp),
                    color  = Color(tier.badgeColorHex).copy(alpha = 0.05f),
                    border = BorderStroke(0.5.dp, Color(tier.badgeColorHex).copy(alpha = 0.4f)),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("当前档位", fontSize = 8.sp, color = StudioColors.TextMuted, letterSpacing = 1.sp)
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape  = RoundedCornerShape(4.dp),
                                color  = Color(tier.badgeColorHex).copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, Color(tier.badgeColorHex)),
                            ) {
                                Text(tier.displayName, fontSize = 9.sp, color = Color(tier.badgeColorHex),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(tier.description, fontSize = 11.sp, color = StudioColors.TextMuted)
                    }
                }

                VirtualMicTier.values().forEach { t ->
                    val reason = vm.checkTierAvailability(t)
                    TierRow(
                        tier              = t,
                        isActive          = t == tier,
                        unavailableReason = reason,
                        onClick           = {
                            if (t == VirtualMicTier.SHIZUKU_ADB &&
                                shizukuState == ShizukuState.NEED_GRANT) {
                                vm.requestShizukuPermission()
                            } else {
                                vm.setTier(t)
                            }
                        },
                    )
                }
            }


            SettingsSection("监听输出设备") {
                val devices    by vm.outputDevices.collectAsState()
                val selectedId by vm.selectedOutputDeviceId.collectAsState()
                LaunchedEffect(Unit) { vm.loadOutputDevices(context) }
                if (devices.isEmpty()) {
                    Text("未找到设备", fontSize = 12.sp, color = StudioColors.TextMuted)
                } else {
                    devices.forEach { device ->
                        val isSelected = device.id == selectedId
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.setOutputDevice(context, device.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = isSelected,
                                onClick = { vm.setOutputDevice(context, device.id) },
                                colors  = RadioButtonDefaults.colors(selectedColor = StudioColors.Accent))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(device.name, fontSize = 12.sp,
                                    color = if (isSelected) StudioColors.Accent else StudioColors.TextPrimary)
                                if (device.id == -1)
                                    Text("优先耳机，其次听筒，绝不外放",
                                        fontSize = 9.sp, color = StudioColors.TextMuted)
                            }
                        }
                    }
                }
            }

            SettingsSection("输入设备") {
                val devices    by vm.inputDevices.collectAsState()
                val selectedId by vm.selectedInputDeviceId.collectAsState()
                LaunchedEffect(Unit) { vm.loadInputDevices(context) }
                if (devices.isEmpty()) {
                    Text("未找到设备", fontSize = 12.sp, color = StudioColors.TextMuted)
                } else {
                    devices.forEach { device ->
                        val isSelected = device.id == selectedId
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.setInputDevice(context, device.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = isSelected,
                                onClick = { vm.setInputDevice(context, device.id) },
                                colors  = RadioButtonDefaults.colors(selectedColor = StudioColors.Accent))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(device.name, fontSize = 12.sp,
                                    color = if (isSelected) StudioColors.Accent else StudioColors.TextPrimary)
                                if (device.id == -1)
                                    Text("跟随系统认定的默认麦克风",
                                        fontSize = 9.sp, color = StudioColors.TextMuted)
                            }
                        }
                    }
                }
            }

            // ── Plugins ───────────────────────────────────────────────────────
            SettingsSection("插件") {
                SettingsActionRow("管理插件路径", Icons.Default.FolderOpen) { navController.navigate("plugin_paths") }
                SettingsActionRow("重新扫描插件目录", Icons.Default.Refresh) { vm.rescan() }
            }

            // ── Theme & Material ───────────────────────────────────────────────────
            SettingsSection("主题与材质") {
                // 主题模式
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("主题模式", fontSize = 11.sp, color = StudioColors.TextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeModeOption(
                            isSelected = themeMode == ThemeMode.SYSTEM,
                            icon = Icons.Default.BrightnessAuto,
                            label = "跟随系统",
                            description = "自动跟随系统深浅色设置",
                            onClick = { ThemeState.saveThemeMode(context, ThemeMode.SYSTEM) }
                        )
                        ThemeModeOption(
                            isSelected = themeMode == ThemeMode.LIGHT,
                            icon = Icons.Default.LightMode,
                            label = "浅色模式",
                            description = "低饱和柔和粉/紫调主色",
                            onClick = { ThemeState.saveThemeMode(context, ThemeMode.LIGHT) }
                        )
                        ThemeModeOption(
                            isSelected = themeMode == ThemeMode.DARK,
                            icon = Icons.Default.DarkMode,
                            label = "深色模式",
                            description = "深蓝/灰调配合紫色点缀",
                            onClick = { ThemeState.saveThemeMode(context, ThemeMode.DARK) }
                        )
                    }
                }

                // 界面材质
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Text("界面材质", fontSize = 11.sp, color = StudioColors.TextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MaterialStyleOption(
                            isSelected = materialStyle == MaterialStyle.GLASS,
                            icon = Icons.Default.BlurOn,
                            label = "液态玻璃",
                            description = "半透明磨砂 + 氛围光，质感最佳",
                            onClick = { ThemeState.saveMaterialStyle(context, MaterialStyle.GLASS) }
                        )
                        MaterialStyleOption(
                            isSelected = materialStyle == MaterialStyle.STANDARD,
                            icon = Icons.Default.Layers,
                            label = "标准 Material",
                            description = "不透明材质，性能最佳",
                            onClick = { ThemeState.saveMaterialStyle(context, MaterialStyle.STANDARD) }
                        )
                    }
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            SettingsSection("关于") {
                SettingsInfoRow("版本", com.micplugin.BuildConfig.VERSION_NAME)
                SettingsInfoRow("构建", "MicUp · Oboe 1.8.1 · CLAP 1.2.1 · LV2")
                SettingsInfoRow("原生", "C++17 · NDK 26 · AAudio/OpenSL ES")
            }

            // ── Crash Reports ─────────────────────────────────────────────────
            SettingsSection("崩溃日志") {
                if (crashReports.isEmpty()) {
                    Text("暂无崩溃记录。", fontSize = 12.sp, color = StudioColors.TextMuted)
                } else {
                    crashReports.forEach { file ->
                        Row(Modifier.fillMaxWidth().clickable { viewingCrash = file },
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(file.name.removePrefix("crash_").removeSuffix(".txt"),
                                fontSize = 11.sp, color = StudioColors.MeterRed)
                            Icon(Icons.Default.ChevronRight, null, tint = StudioColors.TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    SettingsActionRow("清除全部崩溃日志", Icons.Default.DeleteForever) {
                        CrashReporter.clearAll(context); crashReports = emptyList()
                    }
                }
            }
        }
    }

    viewingCrash?.let { file ->
        Dialog(onDismissRequest = { viewingCrash = null }) {
            Surface(shape = RoundedCornerShape(12.dp), color = StudioColors.Card,
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("崩溃报告", style = MaterialTheme.typography.titleSmall, color = StudioColors.MeterRed)
                        IconButton(onClick = { viewingCrash = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = StudioColors.TextMuted)
                        }
                    }
                    Spacer(Modifier.height(8.dp)); HorizontalDivider(color = StudioColors.Border); Spacer(Modifier.height(8.dp))
                    Box(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Text(file.readText(), fontSize = 9.sp, color = StudioColors.TextMuted,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, lineHeight = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { file.delete(); crashReports = CrashReporter.getCrashReports(context); viewingCrash = null },
                        modifier = Modifier.align(Alignment.End)) { Text("删除", color = StudioColors.MeterRed) }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(title)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioColors.Card,
            border = BorderStroke(0.5.dp, StudioColors.Border),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    // 玻璃材质下给卡片加轻微阴影，增强悬浮感
                    if (StudioColors.glassEnabled) Modifier.shadow(
                        8.dp, RoundedCornerShape(8.dp),
                        ambientColor = StudioColors.Accent.copy(alpha = 0.30f),
                        spotColor = StudioColors.Accent.copy(alpha = 0.30f),
                    ) else Modifier
                ),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
        }
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = StudioColors.TextMuted)
        Text(value, fontSize = 12.sp, color = StudioColors.TextPrimary)
    }
}

@Composable
private fun SettingsActionRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = StudioColors.Accent)
        Icon(icon, null, tint = StudioColors.Accent, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun TierRow(
    tier: VirtualMicTier,
    isActive: Boolean,
    unavailableReason: String?,
    onClick: () -> Unit,
) {
    val available = unavailableReason == null
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = available || isActive) { if (available) onClick() }
            .padding(vertical = 2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected  = isActive,
                onClick   = if (available) onClick else null,
                colors    = RadioButtonDefaults.colors(
                    selectedColor   = Color(tier.badgeColorHex),
                    unselectedColor = if (available) StudioColors.TextMuted
                                     else StudioColors.TextMuted.copy(alpha = 0.35f),
                ),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                tier.displayName, fontSize = 12.sp,
                color = when {
                    isActive   -> Color(tier.badgeColorHex)
                    !available -> StudioColors.TextMuted.copy(alpha = 0.4f)
                    else       -> StudioColors.TextMuted
                },
                modifier = Modifier.weight(1f),
            )
            if (!available) {
                Icon(
                    Icons.Default.Lock, contentDescription = null,
                    tint     = StudioColors.TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp),
                )
            }
        }
        if (unavailableReason != null) {
            Text(
                unavailableReason,
                fontSize   = 9.sp,
                color      = Color(0xFFFF6B6B).copy(alpha = 0.85f),
                lineHeight = 12.sp,
                modifier   = Modifier.padding(start = 48.dp, bottom = 2.dp),
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) StudioColors.Accent.copy(alpha = 0.15f) else StudioColors.Card,
        border = if (isSelected) {
            BorderStroke(1.dp, StudioColors.Accent)
        } else {
            BorderStroke(0.5.dp, StudioColors.Border)
        },
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, null, tint = if (isSelected) StudioColors.Accent else StudioColors.TextMuted)
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp, color = if (isSelected) StudioColors.Accent else StudioColors.TextPrimary)
                Text(description, fontSize = 10.sp, color = StudioColors.TextMuted)
            }
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = StudioColors.Accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MaterialStyleOption(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) StudioColors.Accent.copy(alpha = 0.15f) else StudioColors.Card,
        border = if (isSelected) {
            BorderStroke(1.dp, StudioColors.Accent)
        } else {
            BorderStroke(0.5.dp, StudioColors.Border)
        },
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, null, tint = if (isSelected) StudioColors.Accent else StudioColors.TextMuted)
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp, color = if (isSelected) StudioColors.Accent else StudioColors.TextPrimary)
                Text(description, fontSize = 10.sp, color = StudioColors.TextMuted)
            }
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = StudioColors.Accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}
