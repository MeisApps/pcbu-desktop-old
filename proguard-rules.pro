-keep class com.meisapps.pcbiounlock.MainKt { *; }

-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn com.google.common.**
-dontwarn com.formdev.flatlaf.**
-dontwarn org.bouncycastle.**

-keep class com.github.** { *; }
-keep class com.sun.** { *; }
-keep class com.formdev.** { *; }
-keep class com.beust.** { *; }

-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class okio.** { *; }
-keep class okhttp3.** { *; }

# JNA
-keepclassmembers class * extends com.sun.jna.** {
    <fields>;
    <methods>;
}

# kotlinx.serialization
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
