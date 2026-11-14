# ═══════════════════════════════════════════════════════════════════════════════
# MicUp R8 / ProGuard 规则（Debug 开启 minify 后与本文件同时生效）
# 策略：应用自身代码整体保留（稳定性优先），尺寸交给库代码压缩与资源裁剪。
# ═══════════════════════════════════════════════════════════════════════════════

# ─── 1. 应用自身代码：全部保留 ──────────────────────────────────────────────────
# ① JNI：AudioEngine.cpp 用静态注册绑定 Java_com_micplugin_audio_OboeEngine_nativeXxx，
#    类名/方法名一旦被混淆 → UnsatisfiedLinkError 直接闪退；
# ② Hilt 入口（@HiltAndroidApp / @AndroidEntryPoint / @HiltViewModel）全部在本包下；
# ③ Compose UI、AIDL（IAudioPlugin）、传给 JNI 的 Kotlin lambda 合成类也都在本包下。
-keep class com.micplugin.** { *; }
-keep interface com.micplugin.IAudioPlugin { *; }

# ─── 2. JNI 动态回调 ───────────────────────────────────────────────────────────
# C++ 侧对 setMonitorCallback 传入的 ((FloatArray, Int) -> Unit) lambda 执行
# GetMethodID(cls, "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")，
# 保留 kotlin 函数接口，防止 invoke 被 R8 改名导致音频回调断链。
-keep class kotlin.jvm.functions.Function* { *; }

# ─── 3. AIDL / Binder 跨进程接口 ───────────────────────────────────────────────
-keepclassmembers class * extends android.os.Binder { *; }
-keep class * implements android.os.IInterface { *; }

# ─── 4. Hilt / Dagger ─────────────────────────────────────────────────────────
# hilt-android 2.50 自带 consumer 规则，这里是保险层（dagger.** 已包含 dagger.hilt.**）。
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }

# ─── 5. Shizuku ───────────────────────────────────────────────────────────────
# Manifest 中的 ShizukuProvider（rikka.shizuku.*）由 AGP 按清单引用自动保留；
# API 层再整体保留一层，防止跨进程 Binder 代理类被混淆。
-keep class dev.rikka.shizuku.** { *; }
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-dontwarn dev.rikka.shizuku.**
-dontwarn rikka.shizuku.**

# ─── 6. Jetpack Compose ───────────────────────────────────────────────────────
# Compose BOM 2024.02.02 各依赖自带完整 consumer 规则，正常无需手动 keep；
# 保险层仅保留 runtime 内部辅助类（ComposableLambda 等）。
-keep class androidx.compose.runtime.internal.** { *; }

# ─── 7. kotlinx.serialization ─────────────────────────────────────────────────
# 1.6.2 自带 consumer 规则；@Serializable 类（PresetManager 等）均位于
# com.micplugin.**（已整体保留），以下为官方推荐写法的双保险。
-keepclassmembers class com.micplugin.** { *** Companion; }
-keepclasseswithmembers class com.micplugin.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── 8. 通用属性：注解 / 泛型签名 / 内部类 / Kotlin 元数据 ──────────────────────
-keepattributes *Annotation*
-keepattributes Signature,InnerClasses,EnclosingMethod
-keep class kotlin.Metadata { *; }

# ─── 9. 调试日志：Debug 包必须完整保留 Logcat 输出 ──────────────────────────────
# ① 故意不写 android.util.Log 的 assumenosideeffects 剥离规则 → Log.v/d/i/w/e 原样输出；
# ② 保留行号与源文件名，Logcat 崩溃堆栈可直接定位到源码行。
-keepattributes SourceFile,LineNumberTable
