# Build debug APK manually
./gradlew assembleDebug

# Install debug APK
./gradlew installDebug

# View logs
adb logcat | grep "net.typeblog.socks"