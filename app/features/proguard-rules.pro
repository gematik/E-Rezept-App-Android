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

# DB stuff
-keep class de.gematik.ti.erp.app.database.realm.schema.LatestManualMigration { *; }

# Crypto stuff
-keep class org.jose4j.keys.EllipticCurves { *; }

-keep class de.gematik.ti.erp.app.fhir.prescription.model.RequestIntent { *; }
