# R8 / ProGuard Code Obfuscation & Security Rules for QuickCalc

# Repackage all classes into a single obfuscated package ('a')
-repackageclasses 'a'
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Strip source file names and line numbers to prevent reverse engineering
-renamesourcefileattribute ""
-keepattributes SourceFile,LineNumberTable,Exceptions,InnerClasses,EnclosingMethod,Signature,*Annotation*

# Keep Android System Entry Points
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Hilt & Dependency Injection rules
-keep class * extends javax.inject.Provider
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Google Mobile Ads / AdMob SDK
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }

# Compose UI
-keep class androidx.compose.** { *; }

# Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
