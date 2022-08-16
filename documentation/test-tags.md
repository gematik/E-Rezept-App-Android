# Visualize Test Tags

Build the Android App with visual test tags:

```shell
gradle :android:assembleGoogle(Pu|Tu|Ru)InternalDebug -Pbuildkonfig.flavor=google(Pu|Tu|Ru)Internal -PDEBUG_VISUAL_TEST_TAGS=true
```

and install the app:

```shell
adb install android/build/outputs/apk/google(Pu|Tu|Ru)Internal/debug/android-google(Pu|Tu|Ru)Internal-debug.apk
```

To change any test tags edit `android/src/main/java/de/gematik/ti/erp/app/TestTags.kt`.
