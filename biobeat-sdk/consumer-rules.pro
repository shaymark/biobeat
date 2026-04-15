# Keep all public API classes
-keep class com.biobeat.sdk.BioBeatSdk { *; }
-keep class com.biobeat.sdk.BioBeatSdkConfig { *; }
-keep class com.biobeat.sdk.BioBeatSdkConfig$LogLevel { *; }
-keep class com.biobeat.sdk.connection.DeviceConnection { *; }
-keep class com.biobeat.sdk.connection.ConnectionState { *; }
-keep class com.biobeat.sdk.connection.ConnectionState$* { *; }
-keep class com.biobeat.sdk.model.** { *; }
-keep class com.biobeat.sdk.exception.** { *; }

# Keep Kotlin metadata for data classes
-keep class kotlin.Metadata { *; }
