#!/usr/bin/env bash
# Keep the emulator alive while collecting evidence, including after an instrumentation failure.
set -euo pipefail
cd "$(dirname "$0")/.."
collect_device_evidence() {
    local usability_status=$?
    mkdir -p build/device-captures
    timeout 15s adb pull /sdcard/Pictures/Loom-usability build/device-captures || true
    timeout 15s adb logcat -d > build/device-captures/logcat.txt || true
    # Surface failing instrumentation assertions in the job log; artifacts are not always reachable.
    find androidApp/build/outputs/androidTest-results -name '*.xml' -exec grep -h -B1 -A30 '<failure' {} + 2>/dev/null | head -200 || true
    exit "$usability_status"
}
trap collect_device_evidence EXIT
adb shell setprop log.tag.WindowOnBackDispatcher DEBUG
# Use a narrow phone at a lower physical resolution to keep software rendering within CI resources.
adb shell wm size 720x1600
adb shell wm density 320
# Reset the system launcher after display configuration; a foreign Quickstep ANR steals Back input.
adb shell am force-stop com.android.launcher3
timeout 45s adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.HOME > /dev/null
./gradlew :androidApp:connectedDebugAndroidTest --no-daemon --no-parallel --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process '-Dorg.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8'
