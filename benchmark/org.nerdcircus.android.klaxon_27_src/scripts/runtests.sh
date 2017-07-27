#!/bin/bash
ant debug install
cd tests && ant debug install
adb shell am instrument -w org.nerdcircus.android.klaxon.tests/android.test.InstrumentationTestRunner
