#!/usr/bin/env bash
ADB=~/Android/Sdk/platform-tools/adb
DEVICE="192.168.2.92:45801"
FILENAME="screenshot_$(date +%Y%m%d_%H%M%S).png"

"$ADB" -s "$DEVICE" shell screencap -p /sdcard/screen.png
"$ADB" -s "$DEVICE" pull /sdcard/screen.png "./$FILENAME"
echo "Saved: $FILENAME"
