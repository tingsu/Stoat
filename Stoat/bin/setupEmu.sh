#!/bin/bash

dev=$1

echo "- Waiting for emulator to boot"
OUT=`adb -s $dev shell getprop init.svc.bootanim` 
while [[ ${OUT:0:7}  != 'stopped' ]]; do
  OUT=`adb -s $dev shell getprop init.svc.bootanim`
  echo '   Waiting for emulator to fully boot...'
  sleep 5
done

echo "Emulator booted!"

echo "Copy all prepared files into the SDcard, wait .... "
adb -s $dev push ./sdcard/1.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/2.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/3.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/4.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/5.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/6.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/7.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/8.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/9.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/10.vcf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/Troy_Wolf.vcf /mnt/sdcard/ > /dev/null

adb -s $dev push ./sdcard/pic1.jpg /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/pic2.jpg /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/pic3.jpg /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/pic4.jpg /mnt/sdcard/ > /dev/null

adb -s $dev push ./sdcard/example1.txt /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/example2.txt /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/license.txt /mnt/sdcard/ > /dev/null

adb -s $dev push ./sdcard/first.img /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sec.img /mnt/sdcard/ > /dev/null

adb -s $dev push ./sdcard/hackers.pdf /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/Hacking_Secrets_Revealed.pdf /mnt/sdcard/ > /dev/null

adb -s $dev push ./sdcard/Heartbeat.mp3 /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/intermission.mp3 /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/mpthreetest.mp3 /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sample.3gp /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sample_iPod.m4v /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sample_mpeg4.mp4 /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sample_sorenson.mov /mnt/sdcard/ > /dev/null

adb -s $dev push ./sdcard/wordnet-3.0-1.html.aar /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sample_3GPP.3gp.zip /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sample_iPod.m4v.zip /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sample_mpeg4.mp4.zip /mnt/sdcard/ > /dev/null
adb -s $dev push ./sdcard/sample_sorenson.mov.zip /mnt/sdcard/ > /dev/null
echo "finish preparing sdcard!"

