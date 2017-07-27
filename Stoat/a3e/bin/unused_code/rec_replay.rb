#! /usr/bin/env ruby

## Copyright (c) 2011-2012,
##  Jinseong Jeon <jsjeon@cs.umd.edu>
##  Tanzirul Azim <mazim002@cs.ucr.edu>
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

REC = File.dirname(__FILE__)
PARENT  =File.expand_path(File.dirname(__FILE__))+"/../temp"

require_relative 'avd'
require_relative 'troyd'
require_relative 'uid'
require_relative 'cmd'
require_relative 'act'
require_relative 'util'
include Commands


def parse_act_names act_name
	last_act=""
	act_name.each_line do |line|
# 		print ": "+line+"\n"
		last_act=line.split(/:/).last.strip!
# 		print ": "+last_act+"\n"
	end
	last_act=last_act
end

def run_app (act, startr, noloop, record, rec_cmds, apk)
	
	if startr==1
		ADB.ignite act
	end

# 	SOFAR = "sofar"
	pattern = /\(|\s|\)/

	# interact with user
	cmds = ["getViews", "getActivities", "back", "down", "up", "menu",
  	"edit", "clear", "search", "checked", "click", "clickLong","reset",
  	"clickOn", "clickIdx", "clickImg", "clickItem", "drag", "clickImgView", "clickImgBtn", "clickTxtView"]
# 	rec_cmds = []

	# from here just use the get views command and save it to a file
	#now get input from a file and do as described 
	if noloop==false
		out= eval "getViews"
		File.open(PARENT+"/"+"test.out", 'w') do |f2|  
  	
  			f2.puts out  
		end  
# 	test whether the activity was explored before

#     if File.exist?(PARENT+"/"+act+"_command.txt")
#     param="java -Xmx512m -jar TroydWrapper.jar "+PARENT+"/"+"test.out "+PARENT+"/"+act+"_command.txt"
#		system param
#			if File.exist?(PARENT+"/"+act+"_command_history.txt")
#				UTIL.compare_output(PARENT+"/"+act+"_command.txt", PARENT+"/"+act+"_command_history.txt", PARENT+"/"+act+"_command.txt")
#			end
#		    else
#			param="java -Xmx512m -jar TroydWrapper.jar "+PARENT+"/"+"test.out "+PARENT+"/"+act+"_command.txt"
#			system param
#		    end

		current_act=act
		print "current activity: "+act+"\n"
		opt_cmds=ACT.extract_act(PARENT+"/../android/"+"usefulclick_for_a3e.txt")
        puts opt_cmds
		last_act=""
		for c in opt_cmds
			print "executing command...\n"
			print c + "\n"
            if c.eql?("reset\n") then
                puts "[A3E] I: restart the app"
                entry_activity = AAPT.launcher apk
                puts "[A3E] I: the entry activity: #{entry_activity}"
                ADB.ignite entry_activity
                sleep 2
                print "execution completed.\n"
                open(PARENT+"/"+act+"_command_history.txt", 'a') { |f|
                    f.puts c
                }
            else
			out = eval c
			print "execution completed.\n"
            sleep 10
			open(PARENT+"/"+act+"_command_history.txt", 'a') { |f|
  			f.puts c
			}
            end
#			current_act=current_act + " "
# 		saving the previous activity
#			last_act=current_act.strip!
# 		get all activities
#			current_act= eval "getActivities"
# 		parse the names
# now last act is the old activity
#			current_act=parse_act_names current_act
#			if current_act==last_act
# 			print "unchanged" +"\n"
# 			sleep(1)
#				run_app current_act, 0, noloop, record, rec_cmds
#			else
#				print "new activity detected.\n"
## 			last_act=current_act
## 			sleep(3)
#				run_app current_act, 0, noloop, record, rec_cmds
#				out= eval "back"
#			end
		#run_app
		end
#		out= eval "back"
#		run_app last_act, 0, noloop, record, rec_cmds

	# current_act= eval "getActivities"
# 	current_act=parse_act_names current_act
# 	run_app current_act,hashActs,0
	else
		while true
  			print "> "
  			stop = false
  			$stdin.each_line do |line|
    		rec_cmds << line if record
    		cmd = line.split(pattern)[0]
    		case cmd
   				when "finish"
      				stop = true
      				out = eval line
      				puts out if out
    			when "sofar"       # end of one testcase
      				ADB.ignite act # restart the target app
    			else
      				begin
        
        				out = eval line if cmds.include? cmd
        				print "now: " + out + "\n"
        				puts out if out
      				rescue SyntaxError => se
        				puts "unknown command: #{line}"
        				rec_cmds.pop if record
      				end
    			end
    		break
  			end
  		break if stop
		end
	end
end





avd_name = "testAVD"
dev_name = ""
avd_opt = "" # e.g. "-no-window"

pkg_file_exists = false
activities_file_name=""
noloop=true

Dir.foreach(PARENT) {|f| fn = File.join(PARENT, f); File.delete(fn) if f != '.' && f != '..'}
SOFAR="sofar"
pattern = /\(|\s|\)/
record = true
rec_cmds = []
OptionParser.new do |opts|
  opts.banner = "Usage: ruby #{__FILE__} target.apk [options]"
  opts.on("--avd avd", "your own Android Virtual Device") do |n|
    avd_name = n
  end
  opts.on("--dev serial", "serial of device that you uses") do |s|
    dev_name = s
  end
  opts.on("--opt opt", "avd options") do |o|
    avd_opt = o
  end
  opts.on("--act file", "activity names") do |a|
    activities_file_name = a
#     print "pkg file name "+activities_file_name+"\n"
	pkg_file_exists = true
  end
  opts.on("--no-rec", "do not record commands") do
    record = false
  end
  opts.on("-loop", "run a3e mode") do
    noloop=false
  end
  opts.on("-noloop", "run a3e mode") do
    noloop=true
  end
  opts.on_tail("-h", "--help", "show this message") do
    puts opts
    exit
  end
end.parse!

if ARGV.length < 1
  puts "target file is not given"
  exit
end

apk = ARGV[0]

use_emulator = false
#ADB.restart
if not ADB.online?
  # start and synchronize with emulator
  avd = AVD.new(avd_name, avd_opt)
  if not avd.exists?
    avd.create
  end
  avd.start
  use_emulator = true
  sleep(6)
end

if dev_name != ""
  ADB.device dev_name
end

# rebuild and install troyd
pkg = AAPT.pkg apk
Troyd.setenv
Troyd.rebuild pkg

# resign and install target app
ADB.uninstall pkg
shareduid = pkg + ".shareduid.apk"
Uid.change_uid(apk, shareduid)
resigned = pkg + ".resigned.apk"
Resign.resign(shareduid, resigned)
# system("rm -f #{shareduid}")
ADB.install resigned
APKS = REC + "/../apks"
system("mv #{resigned} #{APKS}/#{pkg}.apk")

# start troyd
#act = AAPT.launcher apk
#my own function
act = ""
if pkg_file_exists != true
	act = AAPT.launcher apk
# 	print "\n package not found 1 " + act +"\n"
else
	acts=ACT.extract_act(activities_file_name)
# 	print "\n package found 2 " + acts[0]+"\n"
	act=acts[0]
end
run_app act, 1, noloop, record, rec_cmds, apk


code = ""
if record
  code += <<CODE
# auto-generated via bin/rec.rb
require 'test/unit'
require 'timeout'

class TroydTest < Test::Unit::TestCase

#   SCRT = File.dirname(__FILE__) + "/../bin"
  require_relative '../bin/cmd'
  include Commands

  def assert_text(txt)
    found = search txt
    assert(found.include? "true")
  end

  def assert_not_text(txt)
    found = search txt
    assert(found.include? "false")
  end

  def assert_checked(txt)
    check = checked txt
    assert(check.include? "true")
  end

  def assert_died
    assert_raise(Timeout::Error) {
      Timeout.timeout(6) do
        getViews
      end
    }
  end

  def assert_ads
    found = false
    views = getViews
    views.each do |v|
      found = found || (v.include? "AdView")
    end
    assert(found)
  end

  def setup
    ADB.ignite "#{act}"
  end

CODE
  partial = []
  rec_cmds.each do |cmd|
    if cmd.include? SOFAR
      tname = cmd.split(pattern)[1]
      code += <<CODE
  def test_#{tname}
CODE
      partial.each do |cmdp|
      code += <<CODE
    #{cmdp.strip}
CODE
      end
      code += <<CODE
  end

CODE
      partial = []
    else
      partial << cmd
    end
  end
  code += <<CODE
  def teardown
    Timeout.timeout(6) do
      acts = getActivities
      finish
      puts acts
    end
  end

end
CODE

  tcs = REC + "/../testcases/"
  f = File.open(tcs+pkg+".rb",'w')
  f.puts code
  f.close
end


