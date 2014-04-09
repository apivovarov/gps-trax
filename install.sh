set -e
ant clean debug
adb install -r bin/gps-trax-debug.apk
