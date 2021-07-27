# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Jackson
-keep class com.fasterxml.jackson.** { *; }

# SqlCipher rules
-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

# Crypto stuff
-keep class org.jose4j.keys.EllipticCurves { *; }
-keep class org.bouncycastle.** { *; }
-keep interface org.bouncycastle.** { *; }

# Generated classes from safe args plugin
-keep class de.gematik.ti.erp.app.**Args { *; }
-keep class de.gematik.ti.erp.app.settings.ui.SettingsScrollTo

# Fhir
-keepnames class ca.uhn.fhir.**
-keep class org.hl7.fhir.r4.hapi.ctx.FhirR4
-keep class org.hl7.fhir.r4.context.**
-keep class org.hl7.fhir.r4.model.** { *; }
-keep class org.hl7.fhir.utilities.**

-keep class androidx.fragment.app.FragmentContainerView
-keep class de.gematik.ti.erp.app.common.usecase.model.** { *; }

# -printusage r8/usages.txt