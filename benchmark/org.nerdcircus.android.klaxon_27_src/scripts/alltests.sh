#!/bin/bash

set -e #exit on error.

#AVDS="1.5 1.6 nexus"
AVDS="1.6 2.1 2.2 2.3.3 4.0"
PIDFILE=`mktemp -t androidtests.XXXXXXX`
EMULATOR_BIN=`which emulator`

for avd in $AVDS; do
    echo "starting avd: ${avd}"
    start-stop-daemon -p ${PIDFILE} -S --exec ${EMULATOR_BIN} -m -b -- \
      -avd ${avd} -sdcard ~/.android/avd/${avd}.avd/sdcard.img -no-boot-anim

    adb wait-for-device
    sleep 30
    echo "installing...."
    ant debug install >/dev/null
    cd tests && ant debug install >/dev/null
    cd ..
    adb shell am instrument -w org.nerdcircus.android.klaxon.tests/android.test.InstrumentationTestRunner

    #spin down the emulator.
    start-stop-daemon -K -p ${PIDFILE}
    rm ${PIDFILE}
    sleep 5
done
