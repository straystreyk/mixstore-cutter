# 🚀 АГРЕССИВНЫЕ PROGUARD ОПТИМИЗАЦИИ ДЛЯ СТАРЫХ УСТРОЙСТВ

# Основные оптимизации
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontoptimize                    # ИЗМЕНЕНО: отключаем оптимизации для стабильности на старых устройствах
-dontpreverify

# Keep аннотации
-keepattributes *Annotation*

# Keep classes для Retrofit и Gson (критично!)
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses

# Retrofit оптимизации
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson оптимизации
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# КРИТИЧНО: Keep data classes модели
-keep class com.pokrikinc.mixpokrikcutter.data.model.** { *; }

# OkHttp оптимизации
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.element.Modifier
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Coil оптимизации для изображений
-dontwarn coil.**
-keep class coil.** { *; }

# Compose оптимизации (КРИТИЧНО!)
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# НОВЫЕ: Агрессивные оптимизации для старых устройств
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 3                   # ИЗМЕНЕНО: меньше проходов для стабильности
-allowaccessmodification
-mergeinterfacesaggressively
-repackageclasses ''

# Keep для сериализации
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Kotlin оптимизации
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.Unit
-dontwarn kotlin.coroutines.**

# Удаляем логи в release (экономия места и производительности)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Serial port библиотека
-keep class com.licheedev.** { *; }
-dontwarn com.licheedev.**

# НОВОЕ: оптимизации для уменьшения размера APK
-dontshrink                      # ВРЕМЕННО: отключаем shrinking для отладки
