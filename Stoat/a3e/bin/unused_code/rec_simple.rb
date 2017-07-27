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
require 'socket'

REC = File.dirname(__FILE__)
PARENT  =File.expand_path(File.dirname(__FILE__))+"/../temp"

require_relative 'avd'
require_relative 'troyd'
require_relative 'uid'
require_relative 'cmd'
require_relative 'act'
require_relative 'util'
require_relative 'picker'
require_relative 'emulator'
include Commands

# strip away debug info to get the activity name
def parse_act_names act_name
    last_act=""
    act_name.each_line do |line|
        print "[A3E] <parse_act_names>  "+line+"\n"
        last_act=line.split(/:/).last.strip!
        print "[A3E] <parse_act_names> "+last_act+"\n"
    end
    last_act=last_act
end

def run_app (act, startr, noloop)
    
    if startr==1
        print "[A3E] start the activity...\n"
        ADB.ignite act
    end
    
    #SOFAR = "sofar"
    pattern = /\(|\s|\)/
    
    # interact with user
    cmds = ["getViews", "getActivities", "back", "down", "up", "menu",
    "edit", "clear", "search", "checked", "click", "clickLong",
    "clickOn", "clickIdx", "clickImg", "clickItem", "drag", "clickImgView", "clickImgBtn", "clickTxtView"]
    
    
    # from here just use the get views command and save it to a file
    #now get input from a file and do as described
    if noloop==false
        out= eval "getViews"
        File.open(PARENT+"/"+"test.out", 'w') do |f2|
            
            f2.puts out
            #output to terminal screen
            puts out
        end
        # 	test whether the activity was explored before
        if File.exist?(PARENT+"/"+act+"_command.txt")
            # 		prepare for the new command set
            print "[A3E] this activity was explored before.\n"
            param="java -Xmx512m -jar TroydWrapper.jar "+PARENT+"/"+"test.out "+PARENT+"/"+act+"_command.txt"
            system param
            
            if File.exist?(PARENT+"/"+act+"_command_history.txt")
                UTIL.compare_output(PARENT+"/"+act+"_command.txt", PARENT+"/"+act+"_command_history.txt", PARENT+"/"+act+"_command.txt")
            end
            
            else
            # 		activity was not explored before
            print "[A3E] this activity was not explored before.\n"
            param="java -Xmx512m -jar TroydWrapper.jar "+PARENT+"/"+"test.out "+PARENT+"/"+act+"_command.txt"
            system param
        end
        
        #       print the current activity
        current_act=act
        print "current activity: "+act+"\n"
        
        opt_cmds=ACT.extract_act(PARENT+"/"+act+"_command.txt")
        cmd_i = 0
        puts "[A3E] the cmd list in the current screen:"
        while cmd_i < opt_cmds.size do
            puts opt_cmds[cmd_i]
            cmd_i += 1
        end
        
        last_act=""
        for c in opt_cmds
            print "executing command...\n"
            print c + "\n"
            # execute the command
            out = eval c
            print "execution completed.\n"
            open(PARENT+"/"+act+"_command_history.txt", 'a') { |f|
                f.puts c
            }
            current_act=current_act + " "
            # 		saving the previous activity
            last_act=current_act.strip!
            # 		get all activities
            current_act= eval "getActivities"
            # 		parse the names
            # now last act is the old activity
            current_act=parse_act_names current_act
            if current_act==last_act
                print "[A3E] now the activity is " + current_act + " ... activity unchanged.\n"
                # 			sleep(1)
                run_app current_act, 0, noloop
                else
                print "[A3E] now the activity is " + current_act + " ... new activity detected.\n"
                # 			last_act=current_act
                # 			sleep(3)
                run_app current_act, 0, noloop
                print "[A3E] press back to the last activity.\n"
                out= eval "back"
            end
            #run_app
        end
        out= eval "back"
        run_app last_act, 0, noloop
        
        # current_act= eval "getActivities"
        # 	current_act=parse_act_names current_act
        # 	run_app current_act,hashActs,0
        else
        while true
            print "> "
            stop = false
            $stdin.each_line do |line|
                #     	rec_cmds << line if record
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
                        #         			rec_cmds.pop if record
                    end
                end
                break
            end
            break if stop
        end
        end
    end
    
    def run_app2 (act, startr, noloop)
        
        if startr==1
            print "[A3E] start the activity...\n"
            ADB.ignite act
        end
        
        #SOFAR = "sofar"
        pattern = /\(|\s|\)/
        
        # interact with user
        cmds = ["getViews", "getActivities", "back", "down", "up", "menu",
        "edit", "clear", "search", "checked", "click", "clickLong",
        "clickOn", "clickIdx", "clickImg", "clickItem", "drag", "clickImgView", "clickImgBtn", "clickTxtView"]
        
        
        # from here just use the get views command and save it to a file
        #now get input from a file and do as described
        if noloop==false
            out= eval "getViews"
            File.open(PARENT+"/"+"test.out", 'w') do |f2|
                
                f2.puts out
                #output to terminal screen
                puts out
            end
            # 	test whether the activity was explored before
            if File.exist?(PARENT+"/"+act+"_command.txt")
                # 		prepare for the new command set
                print "[A3E] this activity was explored before.\n"
                param="java -Xmx512m -jar TroydWrapper.jar "+PARENT+"/"+"test.out "+PARENT+"/"+act+"_command.txt"
                system param
                
                else
                # 		activity was not explored before
                print "[A3E] this activity was not explored before.\n"
                param="java -Xmx512m -jar TroydWrapper.jar "+PARENT+"/"+"test.out "+PARENT+"/"+act+"_command.txt"
                system param
            end
            
            # print the current activity
            current_act=act
            print "current activity: "+act+"\n"
            
            opt_cmds=ACT.extract_act(PARENT+"/"+act+"_command.txt")
            cmd_i = 0
            print "[A3E] cmd list:\n"
            while cmd_i < opt_cmds.size do
                print opt_cmds[cmd_i] + "\n"
                cmd_i += 1
            end
            
            last_act=""
            cmd_cnt = opt_cmds.size
            cmd_index= Random.rand(0..cmd_cnt)
            c = opt_cmds[cmd_index]
            print "executing command...\n"
            puts c
            # execute the command
            out = eval c
            print "execution completed.\n"
            open(PARENT+"/"+act+"_command_history.txt", 'a') { |f|
                f.puts c
            }
            current_act=current_act + " "
            # 		saving the previous activity
            last_act=current_act.strip!
            # 		get all activities
            current_act= eval "getActivities"
            # 		parse the names
            # now last act is the old activity
            current_act=parse_act_names current_act
            if current_act==last_act
                print "[A3E] now the activity is " + current_act + " ... activity unchanged.\n"
                # 			sleep(1)
                run_app current_act, 0, noloop
                else
                print "[A3E] now the activity is " + current_act + " ... new activity detected.\n"
                # 			last_act=current_act
                # 			sleep(3)
                run_app current_act, 0, noloop
                print "[A3E] press back to the last activity.\n"
                out= eval "back"
            end
            end
        end
        
        
        
        # upload the app state to the server
        def upload_app_state (app_state, activity)
            clientSession = TCPSocket.new( "localhost", 2008 )
            puts "[A3E] log: starting connection"
            
            puts "------[app state]------"
            puts "#{app_state}"
            puts "------------"
            
            # send the app state to the server
            clientSession.puts app_state
            clientSession.puts "APP_STATE_EOM" # end of message
            
            puts "[A3E] app state sent."
            
            puts "[A3E] the activity: #{activity}"
            clientSession.puts activity
            clientSession.puts "ACTIVITY_EOM" #end of message
            
            puts "[A3E] activity sent."
            
            puts "[A3E] the executed action count:  #{$executed_action_list.length}"
            # send the executed action id
            clientSession.puts $executed_action_list[$executed_action_list.length-1]
            clientSession.puts "ACTION_EOM" # end of message
            
            puts "[A3E] the executed action sent."
            
            clientSession.puts "FINISH_EOM"  #end of client message
            
            puts "[A3E] action id sent."
            
            # the firable actions from the current app state
            firableActions = ""
            
            while !(clientSession.closed?) &&
                (serverMessage = clientSession.gets)
                
                
                # if one of the messages contains 'Goodbye' we'll disconnect
                ## we disconnect by 'closing' the session.
                if serverMessage.include?("Goodbye")
                    puts "I: the [A3E] client received the *Goodbye* message from the server, the connection with the [MCMC-droid] server is closed. "
                    clientSession.close
                    
                    # write into the command file
                    puts "I: try to store all the received *firable actions* from the [MCMC-droid] server... "
                    File.open(PARENT+"/"+activity+"_command.txt", 'w') do |f|
                        f.puts firableActions
                    end
                    
                    puts "I: all *firable actions* are saved in the [a3e] client."
                    puts "I: list all *firable actions* in the current app state."
                    puts "------actions-----"
                    param="cat "+PARENT+"/"+activity+"_command.txt"
                    system param
                    puts "------------------"
                    
                    else
                    # accept the message from server
                    puts "I: received the *firable actions* from the server"
                    firableActions += serverMessage
                end
                
                
            end
        end
        
        # report crawler state
        def report_crawler_state (crawler_state, activity)
            
            # try to connect with the server
            while true do
                begin
                    # create the socket connection
                    clientSession = TCPSocket.new("localhost", 2008)
                    if !clientSession.eql?(nil) then
                        puts "[A3E] I: the connection is set up. "
                        break
                    end
                    rescue
                    puts "[A3E] is waiting the server connection at port 2008... "
                    sleep(5)
                end
            end
            
            clientSession.puts crawler_state
            clientSession.puts "AVD_STATE_EOM" # end of message
            clientSession.puts activity
            clientSession.puts "ENTRY_ACTIVITY_EOM"
            
            while !(clientSession.closed?) &&
                # "gets" gets a line at one time
                (serverMessage = clientSession.gets)
                ## lets output our server messages
                puts serverMessage
                
                #if one of the messages contains 'Goodbye' we'll disconnect
                ## we disconnect by 'closing' the session.
                if serverMessage.include?("Goodbye")
                    puts "log: closing the connection with the server"
                    clientSession.close
                end
            end
            end
            
            #########
            # global vars
            # record the executed action ids
            $executed_action_list=Array.new
            # the action picker
            $picker = ActionPicker.new()
            #########
            
            
            # dump the executed $action into the $activity's history command list
            def dump_executed_actions (activity, action)
                open(PARENT+ "/" + activity + "_command_history.txt", 'a') { |f|
                    f.puts action
                }
            end
            
            # get invokable actions from $activity
            def get_invokable_actions (activity)
                
                # get the current app state
                puts "[A3E] I: getViews"
                sleep(10)
                out= eval "getViews"
                
                # test whether the activity was explored before
                if File.exist?(PARENT+"/"+activity+"_command.txt")
                    # prepare for the new command set
                    puts "[A3E] I: <#{activity}> was explored before."
                    
                    # upload the app state to the server
                    puts "[A3E] I: upload the app state"
                    upload_app_state out, activity
                    
                    #if File.exist?(PARENT+"/"+activity+"_command_history.txt")
                    #    UTIL.compare_output(PARENT+"/"+activity+"_command.txt", PARENT+"/"+activity+"_command_history.txt", PARENT+"/"+activity+"_command.txt")
                    #end
                    
                    else
                    # activity was not explored before
                    puts "[A3E] I: <#{activity}> was not explored before."
                    
                    # upload the app state to the server
                    puts "[A3E] I: upload the app state"
                    upload_app_state out, activity
                    
                end
                
                
                if File.exist?(PARENT+"/"+activity+"_command.txt") then
                    actions=ACT.extract_act(PARENT+"/"+activity+"_command.txt")
                    elsif
                    puts "[A3E] I: file not exist??"
                    exit 0
                end
                
                cmd_i = 0
                puts "[A3E] cmd list in the current screen: "
                while cmd_i < actions.size do
                    puts actions[cmd_i]
                    cmd_i += 1
                end
                
                actions
                
                end
                
                def do_main_job (activity)
                    
                    # get invokable actions
                    actions = get_invokable_actions (activity)
                    
                    #  print the current activity
                    current_act = activity
                    puts "[A3E] I: the current activity: <#{current_act}>"
                    
                    # put actions
                    $picker.putActions(actions, activity)
                    $picker.dumpExecutedActions
                    
                    
                    # pick the next action
                    action = $picker.selectNextAction(actions)
                    action_id, action_cmd = parseActionString(action)
                    puts "executing command: #{action}"
                    
                    # execute the command
                    out = eval action_cmd
                    print "execution completed.\n"
                    
                    # record the executed action id
                    $executed_action_list.push(action_id)
                    $picker.updateActionExecutionTimes(action)
                    
                    # dump the executed action
                    dump_executed_actions(activity, action)
                    
                    current_act=current_act + " "
                    # saving the previous activity
                    last_act=current_act.strip!
                    # get all activities
                    sleep(10)
                    current_act= eval "getActivities"
                    
                    # parse the names
                    # now last act is the old activity
                    current_act=parse_act_names current_act
                    
                    # return current activity & last activity
                    return current_act, last_act
                end
                
                
                # rip an app
                def ripping_app (act, startr, noloop)
                    
                    if startr==1
                        puts "[A3E] I: start the activity <#{act}>"
                        ADB.ignite act
                    end
                    
                    #SOFAR = "sofar"
                    pattern = /\(|\s|\)/
                    
                    # interact with user
                    cmds = ["getViews", "getActivities", "getCurrentActivity", "back", "down", "up", "menu",
                    "edit", "clear", "search", "checked", "click", "clickLong",
                    "clickOn", "clickIdx", "clickImg", "clickItem", "drag", "clickImgView", "clickImgBtn", "clickTxtView",
                    "key_event", "clickMenuItem", "clickCheckBox", "clickRadioButton", "clickToggleButton"]
                    
                    
                    # from here just use the get views command and save it to a file
                    # now get input from a file and do as described
                    if noloop == false
                        
                        current_act = act
                        last_act = ""
                        
                        # the entry activity
                        entry_activity = act
                        
                        iteration = 1
                        
                        # the main working loop
                        while true do
                            
                            puts "-------[Iteration: #{iteration}]----------"
                            if current_act == last_act then
                                print "[A3E] now the activity is " + current_act + " ... activity unchanged.\n"
                                else
                                print "[A3E] now the activity is " + current_act + " ... new activity detected.\n"
                            end
                            #sleep(1)
                            current_act, last_act = do_main_job(current_act)
                            puts "-----------------\n\n\n"
                            
                            iteration = iteration + 1
                            
                        end
                        
                        
                        else
                        while true
                            print "> "
                            stop = false
                            $stdin.each_line do |line|
                                #     	rec_cmds << line if record
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
                                        #         			rec_cmds.pop if record
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
                    record = true
                    pkg_file_exists = false
                    activities_file_name=""
                    noloop=true
                    
                    Dir.foreach(PARENT) {|f| fn = File.join(PARENT, f); File.delete(fn) if f != '.' && f != '..'}
                    
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
                        opts.on("--search search", "the search strategy to execute actions") do |h|
                            $picker.setStrategy(h)
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
                    # set up device map
                    EMULATOR.setup_device_map()
                    
                    # ADB.restart
                    if not ADB.online?
                        
                        # start and synchronize with emulator
                        avd = AVD.new(avd_name, avd_opt)
                        if not avd.exists?
                            puts "I: adb does not exist, create it ... "
                            avd.create
                        end
                        
                        # query the avd
                        avd_serial = EMULATOR.query_avd_serial (avd_name)
                        if avd_serial == nil then
                            avd.start
                            puts "I: the emulator is started ... "
                            sleep(30)
                            else
                            puts "I: avd serial: #{avd_serial} "
                            # specify adb command
                            ADB.device avd_serial
                        end
                        
                        use_emulator = true
                        sleep(6)
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
                        print "[A3E] package not found 1 " + act +"\n"
                        else
                        acts=ACT.extract_act(activities_file_name)
                        print "[A3E] package found 2 " + acts[0]+"\n"
                        act=acts[0]
                    end
                    
                    # report A3E state and wait for the server
                    report_crawler_state "READY", act
                    
                    # the original version of running app
                    # run_app act, 1, noloop
                    # my version of running app
                    ripping_app act, 1, noloop
                    
                    # auto-generated via bin/rec.rb
                    require 'test/unit'
                    require 'timeout'
                    
                    class TroydTest < Test::Unit::TestCase
                    
                    SCRT = File.dirname(__FILE__) + "/../bin"
                    require "\#{SCRT}/cmd"
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
        
        
