# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\program\Android.SDK.Tools.Bundle.v24.3.4.Windows.www.fileniko.com/tools/proguard/proguard-android.txt
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
# Preserve all fundamental application classes.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.view.View
-keep public class * extends android.preference.Preference
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Preserve Android support libraries` classes and interfaces
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-keep public class ir.adad.client.** { *;}




#Ronash proguard
#-keep public class co.ronash.pushe.Pushe {
#    public static void initialize(android.content.Context, boolean);
#    public static void subscribe(android.content.Context, java.lang.String);
#    public static void unsubscribe(android.content.Context, java.lang.String);
#}
#
#-keep public class co.ronash.pushe.PusheListenerService {
#    public void onMessageReceived(org.json.JSONObject, org.json.JSONObject);
#}

# what is the usage of these ?
#-keep public class co.ronash.pushe.log.handlers.*
#-keepclassmembers public class co.ronash.pushe.log.handlers.* {
#    public <init>(android.content.Context, java.util.Map);
#}

-keep public class co.ronash.pushe.receiver.UpdateReceiver


# google gms proguard
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**

# google paly services proguard
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;}


-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn javax.**
-dontwarn io.realm.**

-dontwarn android.test.**

-keep class * implements com.coremedia.iso.boxes.Box { *; }
-dontwarn com.coremedia.iso.boxes.**
-dontwarn com.googlecode.mp4parser.authoring.tracks.mjpeg.**
-dontwarn com.googlecode.mp4parser.authoring.tracks.ttml.**

-keep class com.qiniu.**{*;}
-keep class com.qiniu.**{public <init>();}
-dontwarn com.qiniu.**
-keep class com.pili.pldroid.player.** { *; }
-keep class tv.danmaku.ijk.media.player.** {*;}