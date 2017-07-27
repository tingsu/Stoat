#! /bin/sh
# this script will remove the crashed/old avd, and create a new one

avd_name=$1

rm -rf ~/.android/avd/${avd_name}.*
android create avd -n ${avd_name} -t android-16 -s HVGA -c 128M --abi armeabi-v7a -d "Nexus 7"