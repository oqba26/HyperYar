# --- قوانین عمومی اندروید ---
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# --- قوانین اختصاصی پروژه HyperYar ---

# حفظ مدل‌های داده (برای دیتابیس Room و Serialization)
-keep class com.oqba26.hyperyar.data.** { *; }

# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class com.oqba26.hyperyar.data.** {
    *** Companion;
}

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * implements androidx.room.Dao
-keepclassmembers class * {
  @androidx.room.Database *;
  @androidx.room.Dao *;
  @androidx.room.Entity *;
}

# --- Coroutines ---
# قوانین عمومی برای کوروتین‌ها (معمولاً توسط خود کتابخانه مدیریت می‌شود، اما برای اطمینان بیشتر)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# حذف لاگ‌های سیستم در نسخه نهایی برای امنیت بیشتر
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# --- رفع خطاهای R8 مربوط به کتابخانه‌های جانبی (مانند Sheetz/POI) ---
-dontwarn aQute.bnd.annotation.spi.**
-dontwarn org.apache.batik.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.osgi.framework.**
-dontwarn javax.xml.stream.**
-dontwarn javax.xml.namespace.**
-dontwarn org.w3c.dom.events.**
-dontwarn org.apache.poi.**
