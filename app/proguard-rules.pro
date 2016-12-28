# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/apple/personal/Android/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class com.googlecode.tesseract.** { *; }

# xunfei
-keep class com.iflytek.** { *; }

# butterknife 7.0.1
-keep class butterknife.** { *; }
-keep class **$$ViewBinder { *; }
-dontwarn butterknife.internal.**

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#实体类不参与混淆
-keep class com.example.qkx.translator.data.** { *; }

# retrofit 2.0.1
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# okhttp
-keep class okhttp3.* { *; }
-keep interface okhttp3.* { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

-keepattributes Signature
-keepattributes Annotation
-keepattributes Exceptions