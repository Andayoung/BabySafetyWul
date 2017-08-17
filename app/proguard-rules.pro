# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\AndroidSystem\sdk/tools/proguard/proguard-android.txt
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
-keep class java.lang.management.ManagementFactory{ *; }
-keep class java.lang.management.ThreadMXBean{ *; }
-dontwarn java.lang.management.**
-keep class junit.framework.TestCase{ *; }
-keep class junit.framework.TestSuite{ *; }
-dontwarn junit.framework.**
-keep class junit.runner.BaseTestRunner{ *; }
-dontwarn junit.runner.**
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}
