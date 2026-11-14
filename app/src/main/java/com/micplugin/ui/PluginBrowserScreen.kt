package com.micplugin.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.micplugin.plugin.PluginDescriptor
import com.micplugin.plugin.PluginFormat

@Composable
fun PluginBrowserScreen(
    navController: NavController,
    vm: AudioViewModel = hiltViewModel(),
) {
    val discovered  by vm.discovered.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("已安装", "APK 插件")

    val nativePlugins = discovered.filter { it.format != PluginFormat.APK }
    val apkPlugins    = discovered.filter { it.format == PluginFormat.APK }

    val filtered = when (selectedTab) {
        0 -> nativePlugins
        1 -> apkPlugins
        else -> discovered
    }.filter {
        searchQuery.isEmpty() ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.format.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioColors.Background),
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(StudioColors.Surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "返回", tint = StudioColors.TextPrimary)
            }
            Text(
                "插件浏览器",
                style = MaterialTheme.typography.titleMedium,
                color = StudioColors.TextPrimary,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { vm.rescan() }) {
                Icon(Icons.Default.Refresh, "重新扫描", tint = StudioColors.Accent)
            }
        }

        // ── Search bar ───────────────────────────────────────────────────────
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder   = { Text("搜索插件…", color = StudioColors.TextMuted) },
            leadingIcon   = { Icon(Icons.Default.Search, null, tint = StudioColors.TextMuted) },
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = StudioColors.Accent,
                unfocusedBorderColor = StudioColors.Border,
                cursorColor          = StudioColors.Accent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )

        // ── Tabs ─────────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex  = selectedTab,
            containerColor    = StudioColors.Surface,
            contentColor      = StudioColors.Accent,
            indicator = { tabPositions ->
                val pos = tabPositions[selectedTab]
                Box(
                    Modifier
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = pos.left)
                        .width(pos.width)
                        .height(2.dp)
                        .background(StudioColors.Accent)
                )
            },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = { selectedTab = index },
                    text     = {
                        Text(title, fontSize = 10.sp,
                            color = if (selectedTab == index) StudioColors.Accent else StudioColors.TextMuted)
                    },
                )
            }
        }

        // ── Plugin list ──────────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Extension, null,
                        tint = StudioColors.TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("未找到插件", color = StudioColors.TextMuted)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "请将 .clap / .so / .lv2 文件放到：\n" +
                        "文件 > Android > data > com.micplugin > files > plugins",
                        color = StudioColors.TextMuted, fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(filtered, key = { idx, it -> "$idx:${it.id}" }) { _, desc ->
                    PluginBrowserCard(desc, onAdd = {
                        vm.addPlugin(desc)
                        navController.popBackStack()
                    })
                }
            }
        }
    }
}

@Composable
private fun PluginBrowserCard(
    descriptor: PluginDescriptor,
    onAdd: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(8.dp),
        color    = StudioColors.Card,
        border   = BorderStroke(0.5.dp, StudioColors.Border),
    ) {
        Column(Modifier
            .clickable { expanded = !expanded }
            .padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Format icon
                val formatColor = Color(descriptor.format.colorHex)
                Box(
                    Modifier
                        .size(36.dp)
                        .background(formatColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(0.5.dp, formatColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(descriptor.format.name.first().toString(),
                        color = formatColor, fontSize = 14.sp,
                        style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(descriptor.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioColors.TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        PluginFormatBadge(descriptor.format)
                        if (descriptor.params.isNotEmpty()) {
                            Text("${descriptor.params.size} 个参数",
                                fontSize = 8.sp, color = StudioColors.TextMuted)
                        }
                    }
                }
                Button(
                    onClick = onAdd,
                    colors  = ButtonDefaults.buttonColors(containerColor = StudioColors.Accent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("添加", fontSize = 11.sp)
                }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = StudioColors.Border)
                Spacer(Modifier.height(8.dp))
                Text(descriptor.path, fontSize = 9.sp, color = StudioColors.TextMuted)
                if (descriptor.description.isNotEmpty()) {
                    Text(descriptor.description, fontSize = 10.sp, color = StudioColors.TextMuted)
                }
                if (descriptor.format == PluginFormat.VST3) {
                    Text("⚠ VST3 支持仍属实验性，追求稳定请使用 LV2 / CLAP。",
                        fontSize = 10.sp, color = StudioColors.MeterAmber)
                }
            }
        }
    }
}


