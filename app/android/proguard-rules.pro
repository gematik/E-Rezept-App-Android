# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in pbuild.adle.
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

# Demo mode
-keep class de.gematik.ti.erp.app.demomode.** { *; }

# ContentSquare
-keep class com.contentsquare.android.sdk.** { *; }

# MLKit
# -keep class com.google.android.gms.** { *; }

# Realm
-keep class de.gematik.ti.erp.app.db.** { *; }
-keep class de.gematik.ti.erp.app.db.entities.** { *; }
-keep class de.gematik.ti.erp.app.db.LatestManualMigration { *; }
-keep class de.gematik.ti.erp.app.db.LatestManualMigration$Companion { *; } # companion is autogenerated
-keep class io.realm.** { *; }
-keep class de.gematik.ti.erp.app.di.** { *; }
-keep class de.gematik.ti.erp.app.MessageConversionException

# Serilization
-keep class kotlin.jvm.** { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlinx.serialization.json.** { *; }

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keep, allowobfuscation, allowoptimization class org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

-printusage r8/usages.txt

# PDFbox
-dontwarn com.gemalto.jp2.JP2Decoder
-dontwarn com.gemalto.jp2.JP2Encoder
-dontwarn org.slf4j.impl.StaticLoggerBinder

-keepclassmembers class kotlin.Metadata {
   public <methods>;
}
-keepclasseswithmembers class kotlin.reflect.jvm.internal.** { *; }
-keepclasseswithmembers class java.lang.reflect.** { *; }
-keepclasseswithmembers class java.lang.Class

-keepclasseswithmembers class de.gematik.ti.erp.app.base.BaseActivity

# https://github.com/square/retrofit/issues/3751
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class okhttp3.RequestBody
-keep,allowobfuscation,allowshrinking class okhttp3.ResponseBody

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# https://github.com/square/okhttp/blob/339732e3a1b78be5d792860109047f68a011b5eb/okhttp/src/jvmMain/resources/META-INF/proguard/okhttp3.pro#L11-L14
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-printconfiguration "~/tmp/full-r8-config.txt"

