#! /usr/bin/env ruby

## Copyright (c) 2011-2012,
##  Jinseong Jeon <jsjeon@cs.umd.edu>
##  Tanzirul Azim <mazim002@cs.ucr.edu>
##  Jeff Foster   <jfoster@cs.umd.edu>
## All rights reserved.

## Copyright (c) 2015-2017,
## Ting Su <tsuletgo@gmail.com>
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
require 'time'

REC = File.dirname(__FILE__)
PARENT = File.expand_path(File.dirname(__FILE__))+"/../temp"

require_relative 'avd'
require_relative 'adb'
require_relative 'aapt'
require_relative 'uid'
require_relative 'act'
require_relative 'util'
require_relative 'picker'
require_relative 'conf'
require_relative 'coverage'

require_relative 'util'
require_relative 'crash_reporter'

#include Commands

# strip away debug info to get the activity name
def parse_act_names act_name
	last_act=""
	act_name.each_line do |line|
 		print "I: [A3E] parse activity name: "+line+"\n"
		last_act=line.split(/:/).last.strip!
 		print "I: [A3E] the activity name: "+last_act+"\n"
	end
	last_act=last_act
end

$default_A3E_iteration = 0


##################################################################

##### MCMC-Droid #####

# notify the server
def notify_server (ui_file_name, package_name, activity_name)
    clientSession = TCPSocket.new( "localhost", $g_port )
    puts "[D] starting connection with the server"

    puts "------[info to notify the server]------"
    puts "ui file name: #{ui_file_name}"
    puts "package name: #{package_name}"
    puts "activity name: #{activity_name}"
    puts "------------"
    
    # send the app state to the server
    clientSession.puts ui_file_name
    clientSession.puts "UI_FILE_NAME_EOM" # end of message
    
    clientSession.puts package_name
    clientSession.puts "PACKAGE_NAME_EOM" #end of message
    
    clientSession.puts activity_name
    clientSession.puts "ACTIVITY_NAME_EOM" #end of message
    
    # send the executed action id
    clientSession.puts $executed_action_list[$executed_action_list.length-1]
    clientSession.puts "ACTION_EOM" # end of message
    
    clientSession.puts "FINISH_EOM"  #end of client message
    
    puts "[D] notify server succeed!"
    
    # the firable actions from the current app state
    firableActions = ""
    
    while !(clientSession.closed?) &&
        (serverMessage = clientSession.gets)
        
       
        # if one of the messages contains 'Goodbye' we'll disconnect
        ## we disconnect by 'closing' the session.
        if serverMessage.include?("Goodbye")
            puts "I: the [A3E] client received the *Goodbye* message from the server, the connection with the [Stoat] server is closed. "
            clientSession.close
            
            # write into the command file
            puts "I: try to store all the received *firable actions* from the [Stoat] server... "
            
            File.open($myConf.get_fsm_building_dir + "/"+activity_name+"_command.txt", 'w') do |f|
                f.puts firableActions
            end
            
            puts "I: all *firable actions* are saved in the [a3e] client."
            puts "I: list all *firable actions* in the current app state."
            puts "------actions-----"
            param="cat "+$myConf.get_fsm_building_dir+"/"+activity_name+"_command.txt"
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
            clientSession = TCPSocket.new("localhost", $g_port)
            if !clientSession.eql?(nil) then
                puts "[A3E] I: the connection is set up. "
                break
            end
            rescue
            puts "[A3E] is waiting the server connection at port #{$g_port}... "
            sleep(5)
        end
    end
    
    if crawler_state.eql?("READY") then
      clientSession.puts crawler_state
      clientSession.puts "AVD_STATE_EOM" # end of message
      clientSession.puts activity
      clientSession.puts "ENTRY_ACTIVITY_EOM"
    elsif crawler_state.eql?("STOP") then
      clientSession.puts crawler_state
      clientSession.puts "AVD_STATE_EOM" # end of message
    end
    
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
# the coverage monitor
$coverager = Coverage.new()
# the maximum number of UI events to be executed
$g_maximum_events=0
# the maximum line coverage
$g_maximum_line_coverage=0
# the number of executed events when reaching the maximum line coverage 
$g_maximum_line_coverage_events=0
# the coverage txt file name
$g_coverage_txt = ""
#########
#
$recovery_keyevent_back=0
$emulator_name=""
$emulator_serial=""
# record all tabu action ids
$tabu_action_list = Array.new

# dump the executed $action into the $activity's history command list
def dump_executed_actions (activity, action)
    open($myConf.get_fsm_building_dir+ "/" + activity + "_command_history.txt", 'a') { |f|
        f.puts action
    }
    
    # dump the whole action execution history
    open($myConf.get_fsm_building_dir+ "/" + "all_action_execution_history.txt", 'a') { |f|
        f.puts action
    }
end

# get the current focused package name
def get_current_package_name()
    
    dump_package_and_activity_cmd = "adb -s #{$emulator_serial} shell dumpsys window windows | grep -E 'mFocusedApp' "
    lines = `#{dump_package_and_activity_cmd}`
    package_name_re = /(u0\s)(.*)\//
    pkg = lines.match(package_name_re)
    current_package = pkg[2] # get the current focused package
    puts "[D] the current package name: #{current_package}"
    return current_package
end

# get the current focus activity within the target package under test
def get_current_activity_name(package_name_under_test)
  
    dump_package_and_activity_cmd = "adb -s #{$emulator_serial} shell dumpsys window windows | grep -E 'mFocusedApp' "
    line = `#{dump_package_and_activity_cmd}`
    act_re = /\/(.*)\}\}\}/
    act_match = line.match act_re
    s = act_match[1]
    # Note, different android devices give different activity name
    current_activity = ""
    
    if s.include?(' ') then
      space_loc = s.index(' ')
      s = s[0..space_loc-1]
    end

    if s[0].eql?('.') then
	# The activity name (not complete) should be appended with the package name
    	# get the current focused activity when we are under the target package under test
    	current_activity = package_name_under_test + s
    else
	current_activity = s
    end
    
    return current_activity
end

# dump the current ui xml
def dump_ui_xml()
    # Note since we use the uiautomator wrapper to execute events, which makes "adb shell uiautomator dump --compressed" fail,
    # so we also use this wrapper to dump xml files
    ui_file_name = $myConf.get_ui_files_dir() + "/S_#{$default_A3E_iteration}.xml"

    # Use this version after finishing the running, it will dump more overall view hierarchy info
    # removed "timeout 10s", wait until get the xml layout file
    UTIL.execute_shell_cmd("python ./bin/events/dump_verbose.py #{$emulator_serial} #{ui_file_name}") 
    UTIL.execute_shell_cmd("adb -s #{$emulator_serial} pull /storage/sdcard/window_dump.xml #{ui_file_name}")
    UTIL.execute_shell_cmd("adb -s #{$emulator_serial} shell rm /storage/sdcard/window_dump.xml")

    return ui_file_name
end


# dump screenshots
def dump_screenshot()
    # dump the screenshot
    png_file_name = $myConf.get_ui_files_dir() + "/S_#{$default_A3E_iteration}.png"
    UTIL.execute_shell_cmd("timeout 5s adb -s #{$emulator_serial} shell screencap -p /sdcard/stoat_screen.png")
    UTIL.execute_shell_cmd("adb -s #{$emulator_serial} pull /sdcard/stoat_screen.png #{png_file_name}")
    UTIL.execute_shell_cmd("adb -s #{$emulator_serial} shell rm /sdcard/stoat_screen.png")
end


# get executable UI events from the current UI after a previous event has been executed
def get_executable_ui_events (package_name_under_test)
  
  puts "[D] dump the current UI hierarchy ..."
  
  ui_file_name = dump_ui_xml()

	# check page, TODO uncomment this when you are not testing real-world apps that requires login (e.g., wechat)
	#if UTIL.need_login($emulator_serial, ui_file_name, "wechat") then
	#	UTIL.login($emulator_serial, "wechat")
	#	ui_file_name = dump_ui_xml() # redump the xml file
	#end
	

  if $g_enable_screenshot then
    # if screenshot is enabled, always dump the screenshot after the layout xml 
    dump_screenshot()
  end
  
  # A workaround to handle the failure of dumping ui xml
  if !File.exist?(ui_file_name) then
      # if fail to dump the ui xml
      current_package = get_current_package_name()
      if current_package == package_name_under_test then
        # if we are under the target package, execute "back" to get back from the view which we fail to dump ui xml
        # construct the back cmd
        back_cmd = "python ./bin/events/back.py #{$emulator_serial}"
        puts "$ #{back_cmd}"
        `#{back_cmd}` # back two times to ensure we escape from the error location, oops...
        `#{back_cmd}`
        ui_file_name = dump_ui_xml()
        if $g_enable_screenshot then
            dump_screenshot()
        end
        if !File.exist?(ui_file_name) then
          puts "[E]: failed to dump ui xml, Give up!!"
          exit 0
        end
        # if package_name_under_test.include?("aagtl") then
        #    ui_file_name = "/home/suting/proj/fsmdroid/special/aagtl_overflow_menu.xml"
        # elsif package_name_under_test.include?("sanity") then
        #    ui_file_name = "/home/suting/proj/fsmdroid/special/sanity_overflow_menu.xml"
        # elsif package_name_under_test.include?("wiki") then
        #    ui_file_name = "/home/suting/proj/fsmdroid/special/wiki_overflow_menu.xml"
        # elsif package_name_under_test.include?("bubble") then
        #    ui_file_name = "/home/suting/proj/fsmdroid/special/frozenbubble_overflow_menu.xml"
        # end
      end
  end
  
  # get the current package name
  current_package = get_current_package_name()
  current_activity = ""
  
  if current_package == package_name_under_test then
      # we are on the right way
      $recovery_keyevent_back = 0
      
      current_activity = get_current_activity_name(package_name_under_test)

      # record the acitivity coverage
      open($myConf.get_fsm_building_dir+ "/" + "explored_activity_list.txt", 'a') { |f|
	f.puts current_activity
      }
       
   elsif current_package != package_name_under_test && $recovery_keyevent_back ==0 then
      # we lost the focus of the app ?
			
			puts "current_package: #{current_package}"
			puts "package_name_under_test: #{package_name_under_test}"

			$recovery_keyevent_back = $recovery_keyevent_back + 1

			#if $recovery_keyevent_back == 2 then # TODO Note this allows the app under test to step forward 2 steps, uncomment it when you do not need it
      	ui_file_name = "/EMPTY_APP_STATE.xml"; # return an empty state
			#	$recovery_keyevent_back = 3
			#end

  elsif current_package != package_name_under_test && $recovery_keyevent_back == 1 then
      # fail to recover the app by the "back" event?
    
      ui_file_name = "/RESET_APP_STATE.xml"; # return a reset state
      $recovery_keyevent_back = 0
  end
  
  
  # upload the app state to the server
  puts "[A3E] I: upload the app state"
  notify_server ui_file_name, current_package, current_activity
         
  if File.exist?($myConf.get_fsm_building_dir+"/"+current_activity+"_command.txt") then
      actions=ACT.extract_act($myConf.get_fsm_building_dir+"/"+current_activity+"_command.txt")
  elsif
      puts "[A3E] I: file not exist??"
      exit 0
  end
  
  # debug 
  cmd_i = 0
  puts "[A3E] cmd list in the current screen: "
  while cmd_i < actions.size do
      puts actions[cmd_i]
      cmd_i += 1
  end

  # explicit return the currently executable events, the current focused package and activity
  return actions,current_package,current_activity 
  
end

    
def reset_app (apk)
    
    # Assume the emulator is fine, we need to re-start the app
    puts "[A3E] I: restart the app"
    pkg = $aapt.pkg apk
    act = $aapt.launcher apk
    # Here we add an additional option "-S", so that we will force stop the target app before starting the activity.
    # Without "-S", we cannot restart the app, since the app is already there, e.g., we will get this message
    # "Activity not started, its current task has been brought to the front" 
	  if act != nil then
    	UTIL.execute_shell_cmd("adb -s " + $emulator_serial + " shell am start -S -n " + pkg + "/" + act)
    else
			UTIL.execute_shell_cmd("adb -s " + $emulator_serial + " shell monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1")
		end
    UTIL.execute_shell_cmd("sleep #{$g_app_start_wait_time}") # Note this time is set to ensure the app can enter into a stable state
        
end

# execute the event by parsing the cmd
def execute_event(action_cmd)
  
  puts "[D] the action cmd: #{action_cmd}"
  action_type = ""
  action_param = "" 
  edit_input_value = ""
  
  if action_cmd.start_with?("adb") then
    cmd = action_cmd.sub("adb", "adb -s #{$emulator_serial}")
    puts "$ #{cmd}"
    `#{cmd}`
    return
  end
  
  if action_cmd.eql?("menu\n") then # "menu"
    # construct the python cmd
    menu_cmd = "python ./bin/events/menu.py #{$emulator_serial}"
    puts "$ #{menu_cmd}"
    `#{menu_cmd}`
    return
  end
  
  if action_cmd.eql?("back\n") || action_cmd.eql?("keyevent_back\n") then # "back"
    # construct the python cmd
    back_cmd = "python ./bin/events/back.py #{$emulator_serial}"
    puts "$ #{back_cmd}"
    `#{back_cmd}`
    return
  end
  
  if action_cmd.include?("click(") # get the action type, "click", "long click"
    action_type = "click"
  elsif action_cmd.include?("clickLong(")
    action_type = "long_click"
  elsif action_cmd.include?("edit(") then
    action_type = "edit"
  elsif action_cmd.include?("scroll(") then
    action_type = "scroll"
  end
  
  if action_cmd.include?("(text=") # get the action param, "text", "content_desc", "resource_id"
    action_param = "_by_text"
  elsif action_cmd.include?("(content-desc=")
    action_param = "_by_content_desc"
  elsif action_cmd.include?("(resource-id=")
    action_param = "_by_resource_id"
  elsif action_cmd.include?("direction=")
    action_param = "_by_direction"
  elsif action_cmd.include?("className=") && action_cmd.include?("instance=")
    action_param = "_by_classname_instance"
  end
  
  if action_param.eql?("_by_classname_instance") then
    # execute action by classname and instance
    class_name_pattr = /className=\'(.*)\',/
    class_name = (action_cmd.match class_name_pattr)[1]
    instance_pattr = /instance=\'(.*)\'/
    instance = (action_cmd.match instance_pattr)[1]
    puts "[D]: #{action_type}, #{action_param}, #{class_name} #{instance}"
    
    # construct the python cmd
    event_cmd = "timeout 60s python ./bin/events/#{action_type}#{action_param}.py #{$emulator_serial} #{class_name} #{instance}"
    puts "$ #{event_cmd}"
    `#{event_cmd}`
    
  else
    # get the action param value
    first_quote_index = action_cmd.index("\'")  # get the first occurrence of ' 
    last_quote_index = action_cmd.rindex("\'")  # get the last occurrence of '
    # Note we should include the quotes to avoid the existence of whitespaces in the action_param_value
    action_param_value = action_cmd[first_quote_index..last_quote_index]
    puts "[D]: #{action_type}, #{action_param}, #{action_param_value}"
    
    # construct the python cmd
    event_cmd = "timeout 60s python ./bin/events/#{action_type}#{action_param}.py #{$emulator_serial} #{action_param_value}"
    puts "$ #{event_cmd}"
    `#{event_cmd}`
    
  end
  
end

def do_main_job (package_name_under_test)
   
    # Substitute by Me, check it !!!
    actions, current_package, current_activity = get_executable_ui_events(package_name_under_test)
   
    #  print the current activity
    puts "[D] the current activity: <#{current_activity}>"
   
    # put actions
    $picker.putActions(actions, current_activity)
    $picker.dumpExecutedActions
    
    pick_times = 0
    max_pick_times = 3

    while true do
        # pick the next action
        action = $picker.selectNextAction(actions)
        action_id, action_cmd, action_view_type, action_view_text = parseActionString(action)
        puts "executing command: #{action}"
    
        if action_cmd.eql?("reset\n") then
            # If the action command is RESET, we need to reset the app
            reset_app($myConf.get_instrumented_apk)
            break
            
        elsif $tabu_action_list.include?(action_id) then
            # tabu action, do nothing
            puts "[A3E]: I: this is a tabu action, id [#{action_id}], try to pick another action"
            
            pick_times = pick_times + 1
            if pick_times >= max_pick_times then
                puts "[A3E]: W: stop event picking here..."
                break
            end
        else
        		  
            execute_event(action_cmd)

            # delay the next event
            sleep ($g_event_delay)
            
            if not $g_disable_crash_report then
              # record the execution info.
              $g_crash_reporter.log_test_execution_info(action_cmd, action_view_text, $default_A3E_iteration)
              
              # check whether some crash happens after the event is executed
              if $g_crash_reporter.has_crash() then
                 # record the crash
                 $g_crash_reporter.dump_crash_report_for_model_construction(10)
                 
                 # exit and restart the crash reporter 
                 # the exit logging call is paired with the start logging call before the event-triggering loop
                 # when the ripping process ends, the start logging call is paired with the end logging call outside of the event-triggering loop
                 $g_crash_reporter.exit_logging()
                 $g_crash_reporter.start_logging()
                 
              end
            end
            
            #sleep 1 # NOTE this sleep time may not be necessary, just for some long waiting events to take effect
            break
        end
    end
    
   
    # record the executed action id
    $executed_action_list.push(action_id)
    $picker.updateActionExecutionTimes(action)
    
    #add by buka
    $picker.updateActionsWeight()

    # dump the executed action
    dump_executed_actions(current_activity, action)

    return
end


# rip an app under test
def ripping_app (package_name_under_test, entry_activity_under_test, startr, noloop)

    
    # remove the old log file
    if File.exist?($myConf.get_fsm_building_dir+ "/" + "fsm_building_progress.txt") then
        File.delete($myConf.get_fsm_building_dir+ "/" + "fsm_building_progress.txt")
    elsif
        open($myConf.get_fsm_building_dir+ "/" + "fsm_building_progress.txt", 'a') { |f|
            f.puts "#executed_events #covered_lines #line_coverage_percentage(%) #total_exec_time (min)"
        }
    end
    
    if File.exist?($myConf.get_fsm_building_dir+ "/" + "tabu_action_list.txt") then
        File.delete($myConf.get_fsm_building_dir+ "/" + "tabu_action_list.txt")
    elsif
        open($myConf.get_fsm_building_dir+ "/" + "tabu_action_list.txt", 'a') { |f|
            f.puts "#tabu action id listed in execution order"
        }
    end
    
    if File.exist?("coverage.txt") then
        File.delete("coverage.txt")
    end
    
    if File.exist?($myConf.get_fsm_building_dir+ "/" + "a3e_runtime_log.txt") then
        File.delete($myConf.get_fsm_building_dir+ "/" + "a3e_runtime_log.txt")
    end

    
    total_exec_time = 0.0
    
    if not $g_disable_crash_report then 
      # start crash logging
      $g_crash_reporter.start_logging()
    end
    
    # from here just use the get views command and save it to a file
    # now get input from a file and do as described
    if noloop == false
        
        
        $default_A3E_iteration = 1
        
        # the main working loop
        while true do
            
            puts "-------[Iteration: #{$default_A3E_iteration}]----------"
            
            start_time = Time.now
            
            # do main job --> drive the app to execute
            do_main_job(package_name_under_test)
            
            end_time = Time.now
            # We profile the execution time here. Note the execution time is not very precise, since when we rip the app, we
            # may set some "sleep" interval during the execution to wait
            elapsed_time = ((end_time - start_time).to_f)/60
            total_exec_time += elapsed_time
            
            puts "-----------------\n\n\n"
            
            
            # dump coverage files every five iterations, we do not dump coverage for closed-source apps
            if $closed_source_apk == false && $default_A3E_iteration % 100 == 0 && (not $g_disable_coverage_report) then
            
              # dump code coverage
              # Note we use timeout to solve non-responding cases when dumping code coverage
              UTIL.execute_shell_cmd("timeout 2s adb -s #{$emulator_serial} shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE")
              puts "I: the code coverage is dumped. "
              
              # pull out the coverage file
              coverage_ec =  $myConf.get_coverage_files_dir() + "/" + "coverage_" + $default_A3E_iteration.to_s + ".ec"
              $adb.pullCov coverage_ec
              $adb.rmCov
              
              lineCov = 0
              lineCovPercentage = 0
              
              if File.exist?(coverage_ec) then

                  # if the project was compiled by ant, get code coverage by emma
                  if $g_project_type.eql?("ant") then
                    
                    coverage_files = `find #{$myConf.get_coverage_files_dir()} -name "*.ec"`
                    puts "#{coverage_files}"
                    str_coverage_files = ""
                    coverage_files.each_line do |line|
                        str_coverage_files += line.strip + ","
                    end
                    # get the coverage em file
                    coverage_em = $myConf.get_em_coverage_file()
                    $g_coverage_txt = $myConf.get_coverage_files_dir() + "/" + "coverage.txt"
                    merge_cmd = "java -cp " + $myConf.get_emma_jar() + " emma report -r txt -in " + str_coverage_files + coverage_em + " -Dreport.txt.out.file=" + $g_coverage_txt 
                    puts "$#{merge_cmd}"
                    # execute the shell cmd
                    `#{merge_cmd}`
                    
                    # parse the coverage file
                    $coverager.parse_emma_coverage_report $g_coverage_txt
                    # get the coverage info
                    lineCov = $coverager.getLineCoverage
                    lineCovPercentage = $coverager.getLineCoveragePercentage
                    
                  else # if the project was compiled by gradle, get code coverage by Jacoco
                    
                    # coverage info format: "#covered_lines #line_coverage_percentage" 
                    #cmd = "python #{$myConf.get_stoat_tool_dir()}/android_instrument/dump_coverage.py #{$myConf.get_app_absolute_dir_path()} fsm"
		    #puts "$ #{cmd}"
		    #coverage_info = `#{cmd}`
                    #puts "coverage_info: #{coverage_info}"
                    
                    #coverage_data = coverage_info.split(' ')
                    #lineCov = coverage_data[0].to_f
                    #lineCovPercentage = coverage_data[1].to_f*100
		    lineCov = 0
		    lineCovPercentage = 0
                    
                    puts "lineCov = #{lineCov}"
                    puts "lineCovPercentage = #{lineCovPercentage}"
                    
                  end
                  
                  # record the maximum line coverage and the #events to reach this peak coverage
                  if lineCov.to_i > $g_maximum_line_coverage then
                     $g_maximum_line_coverage = lineCov
                     $g_maximum_line_coverage_events = $default_A3E_iteration
                  end
                  
              else
                  lineCov = 0
                  lineCovPercentage = 0
              end
                  
              
              open($myConf.get_fsm_building_dir+ "/" + "fsm_building_progress.txt", 'a') { |f|
                  f.puts "#{$default_A3E_iteration} #{lineCov} #{lineCovPercentage} #{total_exec_time}"
                  puts "[A3E] Iteration: #{$default_A3E_iteration} lineCov: #{lineCov} lineCovPer: #{lineCovPercentage} totalTime(min): #{total_exec_time}"
              }
              
            # when it is a closed-source apk, we calculate method coverage
            elsif $closed_source_apk == true && $default_A3E_iteration % 5 == 0 && (not $g_disable_coverage_report) then
               
               dump_coverage_cmd = "python #{$myConf.get_ella_tool_dir()}/coverage_fsm.py #{$myConf.get_app_dir_loc()}"
               puts "$ #{dump_coverage_cmd}"
               res = `#{dump_coverage_cmd}`
               method_coverage = res.strip
               
               open($myConf.get_fsm_building_dir+ "/" + "fsm_building_progress.txt", 'a') { |f|
                   f.puts "#{$default_A3E_iteration} #{method_coverage} #{total_exec_time}"
                   puts "[A3E] Iteration: #{$default_A3E_iteration} Method Coverage: #{method_coverage} totalTime(min): #{total_exec_time}"
               }
            end

            $default_A3E_iteration = $default_A3E_iteration + 1
            
            
            # check the maximum ui events (Note convert string to int)
            if $closed_source_apk == false && $default_A3E_iteration>=$g_maximum_events.to_i then
                puts "[D] we have reached the maximum #{$g_maximum_events} UI events"
                puts "[D] exit A3E"
                
                if not $g_disable_coverage_report then
                  
                  open($myConf.get_stoat_tool_dir() +  "/fsm_building_results.csv", 'a') { |f|
  
                      if $g_project_type.eql?("ant") then
                        # parse the coverage
                        $coverager.parse_emma_coverage_report $g_coverage_txt
                        covered_lines = $coverager.getLineCoverage()
                        total_executable_lines = $coverager.getTotalExecutableLine()
                        line_coverage_percentage = $coverager.getLineCoveragePercentage()
                        
                        covered_methods = $coverager.getMethodCoverage()
                        total_methods = $coverager.getTotalMethods()
                        method_coverage_percentage = $coverager.getMethodCoveragePercentage()
                        
                        total_classes = $coverager.getTotalClasses()
                        app_dir_name = $myConf.get_app_dir_name()
                        
                        output = "#{app_dir_name},#{total_classes},#{total_methods},#{covered_methods},#{method_coverage_percentage}," +
                                "#{total_executable_lines},#{covered_lines},#{line_coverage_percentage},#{$g_maximum_line_coverage_events}"
                        f.puts output
                      else
                        # TODO do nothing for gradle project
                      end
                      
                  }
                end
                
                # stop the ripping
                break 
                
                # when it is a closed-source apk
            elsif $closed_source_apk == true && $default_A3E_iteration>=$g_maximum_events.to_i then
                puts "[D] we have reached the maximum #{$g_maximum_events} UI events"
                puts "[D] exit A3E"
                
                if not $g_disable_coverage_report then
                  dump_coverage_cmd = "python #{$myConf.get_ella_tool_dir()}/coverage_fsm.py #{$myConf.get_app_dir_loc()}"
                  puts "$ #{dump_coverage_cmd}"
                  res = `#{dump_coverage_cmd}`
                  method_coverage = res.strip
                  
                  open($myConf.get_stoat_tool_dir() +  "/fsm_building_results.csv", 'a') { |f|
                    
                      app_dir_name = $myConf.get_app_dir_loc()
                      
                      output = "#{app_dir_name},#{method_coverage}"
                              
                      f.puts output
                      
                  }
                end
                
                # stop the ripping
                break 
            end
        end
    end
    
    if not $g_disable_crash_report then
      # exit crash logging
      $g_crash_reporter.exit_logging()
    end

end


def pull_log_file()
    
    # pull log file
    puts "[A3E] pull the adb logcat file from sdcard to local location. "
    pull_log_cmd = "adb -s #{$emulator_serial} pull /sdcard/adb_logcat.log #{$myConf.get_fsm_building_dir}/adb_logcat_#{$default_A3E_iteration}.log"
    `#{pull_log_cmd}`
    
end


# install the app under test
def install_app (apk)
  $adb.install apk
  sleep 1
end

# uninstall the app under test
def uninstall_app (apk)
  pkg = $aapt.pkg apk
  uninstall_cmd = "adb -s #{$emulator_serial} uninstall #{pkg}"
  puts "$ #{uninstall_cmd}"
  `#{uninstall_cmd}`
  sleep 1
end

def install_troyd_and_app (apk)
  
    puts "[D] install troyd and the app under test ..."
    
    # get the package name of the APK
    pkg = $aapt.pkg apk
    
    ## build Troyd
    Troyd.setenv
    Troyd.rebuild_without_install(pkg)
    
    # install re-compiled troyd
    $adb.uninstall
    $adb.install $myConf.get_troyd_apk()

    # resign and install target app
    $adb.uninstall pkg
    shareduid = pkg + ".shareduid.apk"
    Uid.change_uid(apk, shareduid)
    resigned = pkg + ".resigned.apk"
    Resign.resign(shareduid, resigned)
    # system("rm -f #{shareduid}")
    $adb.install resigned

end

def write_conf(communication_port)
  
  output = "PORT = #{communication_port}"
  open($myConf.get_fsm_building_dir() + "/" + "CONF.txt", 'a') { |f|
     f.puts output
  }
  puts "D: write the port number into the config file"
end

def prepare_env()

  # uncomment the followings to enable offline logcat recording
  # clear the logcat buffer
#  clear_log_buffer_cmd = "adb -s #{$emulator_serial} logcat -c"
#  puts "$ #{clear_log_buffer_cmd}"
#  `#{clear_log_buffer_cmd}`
  
  # start adb logcat filter, we focus on runtime errors, fatal errors, and ANR errors
  # see https://developer.android.com/studio/command-line/logcat.html 
#  error_log_file_name = $myConf.get_fsm_building_dir + "/" + "error_log.txt"
#  error_log_cmd = "adb -s #{$emulator_serial} logcat *:E > #{error_log_file_name} &"
#  puts "$ #{error_log_cmd}"
#  `#{error_log_cmd}`
  
  # copy the config
  conf_file =  $myConf.get_stoat_tool_dir() + "/" + "CONF.txt"
  `cp #{conf_file} #{$myConf.get_fsm_building_dir()}`
  
  # get the idle port to communicate with the server
  #$g_port = identifyIdlePorts($g_port)
  
  # write the configuration for the app under test
  write_conf($g_port)
  
  # copy coverage.em
  #em_file = $myConf.get_em_coverage_file()
  #`cp #{em_file} #{$myConf.get_fsm_building_dir()}`
  
  
end

def identifyIdlePorts(start_port_number)
    # always scan ports from start_port_number, until we find enough ports
    port_number = start_port_number
    while true do
        cmd = "nc -z 127.0.0.1 #{port_number}; echo $?"
        output = `#{cmd}`
        puts "output=#{output}"
        if output.eql?("1\n")
            puts "I: Port #{port_number} is idle"
            return port_number
        else
            # increase the port number, try again !
            puts "I: Port #{port_number} is busy, try others ... "
            port_number = port_number + 1
            sleep 5
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
apk_name = ""
app_dir = ""


$closed_source_apk = false
$ella_coverage = false
$g_port = 2008
$g_crash_reporter = nil
$g_event_delay = 0 # the delay time between events
# the project type: "ant" or "gradle", default set as "ant"
$g_project_type = "ant"
$g_disable_crash_report = false
$g_disable_coverage_report = false
$g_enable_screenshot = false
$g_app_start_wait_time = 5

Dir.foreach(PARENT) {|f| fn = File.join(PARENT, f); File.delete(fn) if f != '.' && f != '..'}

OptionParser.new do |opts|
  opts.banner = "Usage: ruby #{__FILE__} target.apk [options]"
  opts.on("--avd avd", "your own Android Virtual Device") do |n|
    avd_name = n
  end
  opts.on("--dev serial", "serial of device that you uses") do |s|
    dev_name = s
  end
  opts.on("--apk apk", "the apk under test") do |l|
    apk_name = l
  end
  opts.on("--app app", "the app under test") do |i|
    app_dir = i
  end
  opts.on("--opt opt", "avd options") do |o|
    avd_opt = o
  end
  opts.on("--act file", "activity names") do |a|
    activities_file_name = a
#     print "pkg file name "+activities_file_name+"\n"
	pkg_file_exists = true
  end
  opts.on("--search search", "the search strategy to execute actions") do |h|
      $picker.setStrategy(h)
  end
  opts.on("--events events", "the maximum ui events to be executed") do |e|
    $g_maximum_events = e
    puts "I: the maximum ui events to be executed: #{$g_maximum_events}"
  end
  opts.on("--event_delay events", "the delay time between events") do |y|
      $g_event_delay = (y.to_i)*1.0/1000.0
      puts "I: the delay time between events: #{$g_event_delay}"
  end
     opts.on("--project_type type", "the project type: ant or gradle") do |t|
     $g_project_type = t
     puts "I: project type: #{$g_project_type}"
  end
  opts.on("--port port", "the communication port") do |p|
      $g_port = p
      puts "I: the communication port: #{$g_port}"
  end
  opts.on("--disable_crash_report", "disable crash report during execution") do
      $g_disable_crash_report = true
  end
  opts.on("--disable_coverage_report", "disable coverage report during execution") do
      $g_disable_coverage_report = true
  end
  opts.on("--enable_dump_screenshot", "enable dumping screenshot") do
      $g_enable_screenshot = true
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

### Load System Configurations ###
$myConf = CONF.new()
config_file = File.expand_path(File.dirname(__FILE__)) + '/../../CONF.txt'
$myConf.read_config(config_file)

# get the wait time for app start/restart
$g_app_start_wait_time = $myConf.get_app_start_wait_time()

# check whether we are testing an apk
if app_dir.end_with?(".apk") then
  $closed_source_apk = true
  if not $g_disable_coverage_report then
    $ella_coverage = true
  end
end
# create the fsm building dir
$myConf.set_project_type($g_project_type)
$myConf.create_fsm_building_dir(app_dir, apk_name, $closed_source_apk)
apk_name = $myConf.get_instrumented_apk()
####

# get the emulator name and serial
$emulator_name = avd_name
$emulator_serial = dev_name
puts "I: the emulator name: #{$emulator_name}, the emulator serial: #{$emulator_serial}"


$avd = AVD.new(avd_name, avd_opt)
# create "adb" instance
$adb = ADB.new()
# init. the emulator serial
$adb.device $emulator_serial
$aapt = AAPT.new

if not $g_disable_crash_report then
  # create the crash reporter
  crash_report_dir = $myConf.get_fsm_building_dir()
  $g_crash_reporter = CrashReporter.new(apk_name,$emulator_serial,crash_report_dir)
end

# prepare the env
prepare_env()


#########

    

  
puts "[D] Stoat mode, maximum allowed #events: #{$g_maximum_events}"
puts "apk name: #{apk_name}"

pkg = $aapt.pkg apk_name
act = $aapt.launcher apk_name

start_app_cmd = ""
if act != nil then
	start_app_cmd =  "adb -s " + $emulator_serial + " shell am start -S -n " + pkg + "/" + act
else
	start_app_cmd =  "adb -s " + $emulator_serial + " shell monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1"
	act = "stoat-fake-entry-activity" # when the app does not has an explicit launchable activity, we use the monkey approach to start it
end

# start the app
UTIL.execute_shell_cmd(start_app_cmd)
UTIL.execute_shell_cmd("sleep #{$g_app_start_wait_time}")

# report A3E state and wait for the server
report_crawler_state "READY", act
# rip the app
ripping_app pkg, act, 1, noloop
    

# stop the server
report_crawler_state("STOP","")

# stop the coverage recording of Ella
if $ella_coverage == true then
   clear_ella_coverage_cmd = "#{$myConf.get_ella_tool_dir()}/ella.sh e #{$emulator_serial}"
   puts "$ #{clear_ella_coverage_cmd}"
   `#{clear_ella_coverage_cmd}`
end

# uninstall the app
uninstall_app(apk_name)

# clean up adb in the localhost only for the target device
logcat_pids = `ps | grep 'adb' | awk '{print $1}'`
logcat_pids_list = logcat_pids.sub!("\n", " ")
kill_adb_cmd = "kill -9 #{logcat_pids_list}"  # kill the adb logcat process
puts "$ #{kill_adb_cmd}"
`#{kill_adb_cmd}`

exit

