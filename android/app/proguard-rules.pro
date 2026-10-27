# Reglas ProGuard del proyecto.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.alanduran.weatherreport.data.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.alanduran.weatherreport.data.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}
