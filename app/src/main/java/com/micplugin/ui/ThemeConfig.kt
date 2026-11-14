package com.micplugin.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 主题与材质配置存储
 */
object ThemeConfig {
    private const val PREF_THEME_MODE = "pref_theme_mode"
    private const val PREF_MATERIAL_STYLE = "pref_material_style"

    // 主题模式枚举
    enum class ThemeMode(val value: Int) {
        SYSTEM(0),    // 跟随系统
        LIGHT(1),     // 浅色模式
        DARK(2);      // 深色模式

        companion object {
            fun fromValue(value: Int): ThemeMode {
                return entries.find { it.value == value } ?: DARK
            }
        }
    }

    // 界面材质枚举
    enum class MaterialStyle(val value: Int) {
        GLASS(0),     // 液态玻璃
        STANDARD(1);  // 标准 Material

        companion object {
            fun fromValue(value: Int): MaterialStyle {
                return entries.find { it.value == value } ?: STANDARD
            }
        }
    }

    private val themeModeKey = intPreferencesKey(PREF_THEME_MODE)
    private val materialStyleKey = intPreferencesKey(PREF_MATERIAL_STYLE)

    /**
     * 保存主题模式
     */
    suspend fun saveThemeMode(dataStore: DataStore<Preferences>, mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.value
        }
    }

    /**
     * 读取主题模式
     */
    fun readThemeMode(dataStore: DataStore<Preferences>): Flow<ThemeMode> {
        return dataStore.data.map { preferences ->
            ThemeMode.fromValue(preferences[themeModeKey] ?: ThemeMode.SYSTEM.value)
        }
    }

    /**
     * 保存界面材质
     */
    suspend fun saveMaterialStyle(dataStore: DataStore<Preferences>, style: MaterialStyle) {
        dataStore.edit { preferences ->
            preferences[materialStyleKey] = style.value
        }
    }

    /**
     * 读取界面材质
     */
    fun readMaterialStyle(dataStore: DataStore<Preferences>): Flow<MaterialStyle> {
        return dataStore.data.map { preferences ->
            MaterialStyle.fromValue(preferences[materialStyleKey] ?: MaterialStyle.GLASS.value)
        }
    }
}
