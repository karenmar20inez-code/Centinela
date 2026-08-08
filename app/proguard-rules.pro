# Reglas para Mapbox
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**

# Reglas para Retrofit y OkHttp (usados en las rutas)
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, AnnotationDefault
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Evitar que se borren clases de Compose y Material3
-keep class androidx.compose.** { *; }
-keep class androidx.material3.** { *; }
