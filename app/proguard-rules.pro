# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number and source file information for better stack traces in production logs.
-keepattributes SourceFile,LineNumberTable

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# =========================================================================
# Kotlin Coroutines and Flow ProGuard Rules
# =========================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.AndroidDispatcherFactory {
    public *** createDispatcher(...);
}

# =========================================================================
# Jetpack Compose ProGuard Rules
# =========================================================================
-keepclassmembers class * extends androidx.compose.ui.node.Owner {
    *** getComposeVersion(...);
}
-keep class androidx.compose.foundation.lazy.layout.DefaultLazyKey { *; }

# =========================================================================
# Room Database ProGuard Rules
# =========================================================================
-keep class * extends androidx.room.RoomDatabase
-keep class com.connectkar.data.local.** { *; }
-dontwarn androidx.room.paging.**

# =========================================================================
# WorkManager ProGuard Rules
# =========================================================================
-keep class * extends androidx.work.ListenableWorker
-keep class com.connectkar.data.repository.SyncWorker { *; }

# =========================================================================
# Firebase and Firestore ProGuard Rules
# =========================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# =========================================================================
# Keep our local entity and model classes from obfuscation so Firestore
# and Room mapping works seamlessly.
# =========================================================================
-keepclassmembers class com.connectkar.data.local.UserEntity { *; }
-keepclassmembers class com.connectkar.data.local.ListingEntity { *; }
-keep class com.connectkar.data.local.UserEntity { *; }
-keep class com.connectkar.data.local.ListingEntity { *; }
