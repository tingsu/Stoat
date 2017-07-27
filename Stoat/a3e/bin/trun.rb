#! /usr/bin/env ruby

## Copyright (c) 2011-2012,
##  Jinseong Jeon <jsjeon@cs.umd.edu>
##  Jeff Foster   <jfoster@cs.umd.edu>
## All rights reserved.
##
## Redistribution and use in source and binary forms, with or without
## modification, are permitted provided that the following conditions are met:
##
## 1. Redistributions of source code must retain the above copyright notice,
## this list of conditions and the following disclaimer.
##
## 2. Redistributions in binary form must reproduce the above copyright notice,
## this list of conditions and the following disclaimer in the documentation
## and/or other materials provided with the distribution.
##
## 3. The names of the contributors may not be used to endorse or promote
## products derived from this software without specific prior written
## permission.
##
## THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
## AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
## IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
## ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
## LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
## CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
## SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
## INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
## CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
## ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
## POSSIBILITY OF SUCH DAMAGE.

require 'rubygems'
require 'optparse'

TRUN = File.dirname(__FILE__)

require "#{TRUN}/aapt"
require "#{TRUN}/avd"
require "#{TRUN}/troyd"
require "#{TRUN}/uid"

avd_name = rand(36**8).to_s(36) # random string with size 8
dev_name = ""
avd_opt  = "-no-window"
apks = TRUN + "/../apks"
only = []
OptionParser.new do |opts|
  opts.banner = "Usage: ruby #{__FILE__} [options]"
  opts.on("--only rb", "single run") do |rb|
    only << rb
  end
  opts.on("--dev serial", "serial fo device that you uses") do |s|
    dev_name = s
  end
  opts.on("--opt opt", "avd options") do |o|
    avd_opt = o
  end
  opts.on("--apk dir", "folder that has target apks") do |dir|
    apks = dir
  end
  opts.on_tail("-h", "--help", "show this message") do
    puts opts
    exit
  end
end.parse!

use_emulator = false
#ADB.restart
if not ADB.online?
  # start and synchronize with emulator
  avd = AVD.new(avd_name, avd_opt)
  avd.create
  avd.start
  use_emulator = true
  sleep(19)
end

if dev_name != ""
  ADB.device dev_name
end

TCS = TRUN + "/../testcases"
Dir.glob(TCS + "/*.rb").each do |tc|
  pkg = File.basename(tc, ".rb")
  if only != []
    found = false
    only.each do |rb|
      found |= (rb.include? pkg)
    end
    next unless found
  end

  Dir.glob(apks + "/*.apk").each do |apk|
    next if AAPT.pkg(apk) != pkg

    # rebuild and install troyd
    Troyd.setenv
    Troyd.rebuild pkg

    # resign and install target app
    ADB.uninstall pkg
    shareduid = pkg + ".shareduid.apk"
    Uid.change_uid(apk, shareduid)
    resigned = pkg + ".resigned.apk"
    Resign.resign(shareduid, resigned)
    system("rm -f #{shareduid}")
    ADB.install resigned
    system("rm -f #{resigned}")

    # run the testcase
    puts "apk: #{apk}"
    result = `ruby #{tc}`
    acts = []
    puts_all = false
    result.each do |line|
      act = line.scan(/opened: (.+)/)
      acts << act[0][0] if act and act[0]
      finished = line.scan(/Finished/)
      puts_all = true if finished and finished[0]
      puts line if puts_all
    end
    acts.uniq!
    puts "opened activities: #{acts.length}"
    acts.each do |act|
      puts act
    end
    puts ""
    ADB.uninstall pkg
  end

end

# stop emulator and clean up
if use_emulator
  avd.stop
  avd.delete
end
