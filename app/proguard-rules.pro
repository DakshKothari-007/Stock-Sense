# StockSense ProGuard / R8 Rules

# Keep generic signatures, annotations, and inner classes for Gson & Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# Gson
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * extends com.google.gson.TypeAdapterFactory

# App DTOs / Models / AvatarCode serialized by Gson
-keep class com.simats.stocksense.data.model.** { *; }
-keep class com.simats.stocksense.core.avatar.AvatarCode { *; }

# Retrofit & OkHttp
-keepattributes Exceptions, Signature, InnerClasses, EnclosingMethod
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class com.simats.stocksense.core.network.**ApiService { *; }
-keepclassmembers interface com.simats.stocksense.core.network.**ApiService { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Coil & SVG
-keep class coil.** { *; }
-keep class coil3.** { *; }
-keep class com.caverock.androidsvg.** { *; }
-dontwarn com.caverock.androidsvg.**

# Firebase Messaging & Google Play Services
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# AndroidX Security Crypto / Tink
-dontwarn com.google.crypto.tink.**
-keep class com.google.crypto.tink.** { *; }

# Compose & Lifecycle
-keep class androidx.compose.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
