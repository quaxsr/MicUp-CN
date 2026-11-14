package com.micplugin.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 主题模式：跟随系统 / 浅色 / 深色 */
enum class ThemeMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromValue(value: Int): ThemeMode =
            values().firstOrNull { it.value == value } ?: SYSTEM
    }
}

/** 界面材质：液态玻璃 / 标准 Material */
enum class MaterialStyle(val value: Int) {
    GLASS(0),
    STANDARD(1);

    companion object {
        fun fromValue(value: Int): MaterialStyle =
            values().firstOrNull { it.value == value } ?: STANDARD
    }
}

/**
 * 主题与材质的全局状态 + 持久化。
 *
 * 持久化使用 SharedPreferences（与工程现有 "micup_prefs" 保持一致，无需新增依赖）。
 * themeMode / materialStyle 是 Compose State：设置页修改后，所有读取处即时重组，
 * 下次启动由 MainActivity 在 setContent 前调用 [load] 恢复上次选择。
 */
object ThemeState {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set
    var materialStyle by mutableStateOf(MaterialStyle.GLASS)
        private set

    private const val PREFS = "micup_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_MATERIAL = "material_style"

    /** App 启动时调用一次（MainActivity.onCreate），同步读取，文件极小无卡顿。 */
    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        themeMode = ThemeMode.fromValue(prefs.getInt(KEY_THEME, ThemeMode.SYSTEM.value))
        materialStyle = MaterialStyle.fromValue(prefs.getInt(KEY_MATERIAL, MaterialStyle.GLASS.value))
    }

    fun saveThemeMode(context: Context, mode: ThemeMode) {
        themeMode = mode
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_THEME, mode.value).apply()
    }

    fun saveMaterialStyle(context: Context, style: MaterialStyle) {
        materialStyle = style
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_MATERIAL, style.value).apply()
    }
}
