# HEX BLADE Android Overlay wrapper

This Android Studio project wraps the supplied Firebase-license HTML panel and adds a real Android `Display over other apps` permission flow. Press **START CHEAT** after license login; Android opens the system overlay-permission screen if needed. Grant permission and return to the app to display the movable floating UI.

## Build
1. Open this folder in Android Studio (JDK 17 recommended).
2. Allow Gradle sync and install the Android SDK platform/build tools requested by the project.
3. Build > Build APK(s), or run on a physical Android device.
4. On the device, authenticate using your existing Firebase license flow. Tap **START CHEAT** and grant **Display over other apps**.

The project is source code, not a prebuilt APK. A working build requires Android Studio/Gradle and testing on the target phone. The overlay controls are visual UI only and do not alter or control other apps or games. Firebase config is inherited from the supplied HTML; protect your Firestore rules and do not treat client-side configuration as a secret.
