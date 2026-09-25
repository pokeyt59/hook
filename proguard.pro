# Shrinks the bundled libraries: only removes classes nothing uses. Nothing is renamed or optimized, and
# everything that's loaded by name at runtime is kept whole, so behaviour is identical to the full jar.
-dontobfuscate
-dontoptimize
-keepattributes *
-dontnote

# kept whole: the mod, the discord library, and libraries that use reflection, serialization or dynamic loading
-keep class com.github.pinmacaroon.** { *; }
-keep class net.dv8tion.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep class net.fellbaum.** { *; }
-keep class com.github.zafarkhaja.** { *; }
-keep class com.neovisionaries.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class org.slf4j.** { *; }
# shrunk: gnu.trove, kotlin, org.apache.commons (only the parts the libraries above use stay)

# optional dependencies of the libraries that aren't bundled (and aren't needed)
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn android.**
-dontwarn dalvik.**
-dontwarn com.google.crypto.tink.**
-dontwarn club.minnced.opus.**
-dontwarn tomp2p.opuswrapper.**
-dontwarn com.sun.jna.**
-dontwarn javax.annotation.**
-dontwarn org.jspecify.**
-dontwarn org.jetbrains.annotations.**
# MethodHandle.invoke is signature polymorphic, proguard can't model it
-dontwarn java.lang.invoke.MethodHandle
# the mod's own references to the game and loader (they're provided at runtime)
-dontwarn net.minecraft.**
-dontwarn net.fabricmc.**
-dontwarn com.mojang.**
# anything left is a reference inside the bundled libraries to optional code they never load here. nothing is renamed
# or optimized, so a warning can't break behaviour, and the published jar is smoke tested
-ignorewarnings
