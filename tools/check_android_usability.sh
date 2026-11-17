#!/usr/bin/env bash
# Keep the emulator alive while collecting evidence, including after an instrumentation failure.
set -euo pipefail
cd "$(dirname "$0")/.."
collect_device_evidence() {
    local usability_status=$?
    mkdir -p build/device-captures
    timeout 15s adb pull /sdcard/Pictures/Loom-usability build/device-captures || true
    timeout 15s adb logcat -d > build/device-captures/logcat.txt || true
    exit "$usability_status"
}
trap collect_device_evidence EXIT
adb shell setprop log.tag.WindowOnBackDispatcher DEBUG
# Wait for the launcher's first draw; compiling before boot avoids starving its startup on CI.
timeout 45s adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.HOME > /dev/null
./gradlew :androidApp:connectedDebugAndroidTest --no-daemon --no-parallel --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8'
