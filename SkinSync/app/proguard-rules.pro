# Keep domain models (used by serialization / reflection-free, but safe).
-keep class com.oo.skinsync.domain.** { *; }
# MediaPipe tasks-vision
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**
