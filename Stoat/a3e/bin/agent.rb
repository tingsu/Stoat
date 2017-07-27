## Copyright (c) 2014-2015
##  Ting Su <tsuletgo@gmail.com>
## All rights reserved.


# On Mac platform, the emulator is created in the default location: /Users/tingsu/.android/avd/
# the created emulator have these properties: Android 4.1.2, HVGA, armeabi-v7a, sdcard (128MB)
# the files: testAVD_1.ini & testAVD_1.avd

# log files: 1) coverage file, "coverage_testAVD_1_TEST_1_MCMC_1.ec", "coverage_testAVD_2_TEST_2_MCMC_2.ec",  2) testcase file, "testcases_testAVD_1.txt", 3) suspicious testcase file, "suspicious_testcase_testAVD_1.txt"


require 'rubygems'
require 'optparse'
require 'socket'

REC = File.dirname(__FILE__)
APKS = REC + "/../apks"

require_relative 'avd'
require_relative 'adb'
require_relative 'aapt'
require_relative 'uid'
require_relative 'act'
require_relative 'util'
require_relative 'crash_reporter'

class Agent

    @emulator_name
    @emulator_serial
    @emulator_state
    @emulator_option
    @socket_port
    
    @test_sequence
    @error_id
    
    @avd
    @adb
    @aapt
    
    @manager
    @config
    
    @apk
    @closed_source_apk
    @stoat_tool_dir
    @ella_tool_dir
    @test_execution_dir
    
    @suspicious_testcase_file
    @testcase_file
    @coverage_file
    @error_log_file_name
    @test_sequence_file
    
    # emulate states:
    #   offline (not online)
    #   idle (no test cases are executing, waits for tasks),
    #   busy (test cases are executing)
    #   exception (crashed, non-responding, failed to wake up and etc)
    @@offline = "offline"
    @@ready = "ready"
    @@idle = "idle"
    @@busy = "busy"
    @@exception = "exception"
    
    @@command_emulator_name = "emulator_name="
    # the id of the current executed test
    @executed_test_id
    
    @crash_reporter
    @event_delay # in seconds
    @restart_delay # in seconds
    @disable_coverage
    
    # constructor
    # @param emulator_name the name of emulator
    # @param emulator_opt the options for emulator
    # @param socket_port the socket port to communicate with the remote server
    def initialize(emulator_name, emulator_opt, socket_port, config, closed_source_apk, disable_coverage, manager)
        @avd = AVD.new(emulator_name, emulator_opt)
        @emulator_name = emulator_name
        @emulator_serial = @emulator_name # they are same!!
        @emulator_option = emulator_opt
        @socket_port = socket_port
        @manager = manager
        
        @config = config
        
        
        @apk = @config.get_instrumented_apk()
        @closed_source_apk = closed_source_apk
        @disable_coverage = disable_coverage
        @stoat_tool_dir = @config.get_stoat_tool_dir()
        @ella_tool_dir = @config.get_ella_tool_dir()
        @test_execution_dir = @config.get_test_execution_dir()
        @test_sequence_file = @config.get_test_execution_dir+ "/" + "test_suite_to_execute.txt"
        @event_delay = @manager.get_event_delay()
        @restart_delay = @manager.get_restart_delay()
        
        puts "[#{@emulator_name}] I: the agent is initialized with manager #{@manager}"
        # set the state as "offline"
        @emulator_state = @@offline
        
        # init. the execution times
        @executed_test_id = 1
        
        @error_id = 1
        
        @test_sequence = ""
        
        @adb = ADB.new
        @aapt = AAPT.new
        
        
        # log file name
        @suspicious_testcase_file = @test_execution_dir+ "/" + "suspicious_testcase_" + @emulator_name + ".txt"
        @testcase_file = @test_execution_dir + "/" + "testcases_" + @emulator_name + ".txt"
        
        @coverage_file = ""
        enum_name =""
        if @emulator_name.include?(":") then
          loc = @emulator_name.rindex(":")
  		  enum_name = @emulator_name[0..loc-1]
          else 
  		  enum_name = @emulator_name
          end
        # set the coverage file for both ant/gradle projects
  			@coverage_file = @test_execution_dir + "/MCMC_coverage/" + "coverage_" + enum_name
    	
        @error_log_file_name = @test_execution_dir + "/adb_logcat_#{@emulator_name}.txt"
        
        # get the bug reporter
        @crash_reporter = CrashReporter.new(@apk,@emulator_serial,@test_execution_dir) 
    end
    
    
    def is_ready ()
        if @emulator_state.eql?(@@ready) then
            true
        else
            false
        end
    end
    
    def is_idle()
      if @emulator_state.eql?(@@idle) then
           true
      else
           false
      end 
    end
    
    
    # start the agent
    def start ()
        
        # prepare the logging
        prepare()
        
        # NOTE: uncomment it when using real devices
        start_real_device()
        
        # start the working loop
        working_loop()
    end
    
    # prepare log files
    def prepare ()
        if File.exist?(@suspicious_testcase_file) then
            File.delete(@suspicious_testcase_file)
        end
        
        # uncomment the followings to enable offline logcat recording
        # clear the logcat buffer
        #clear_log_buffer_cmd = "adb -s #{@emulator_serial} logcat -c"
        #puts "$ #{clear_log_buffer_cmd}"
        #`#{clear_log_buffer_cmd}`
      
        # start adb logcat filter, we focus on runtime errors, fatal errors, and ANR errors
        # see https://developer.android.com/studio/command-line/logcat.html 
        # error_log_cmd = "adb -s #{@emulator_serial} logcat AndroidRuntime:E CrashAnrDetector:D ActivityManager:E *:F *:S > #{@error_log_file_name} &"
        
        # capture all errors
        #error_log_cmd = "adb -s #{@emulator_serial} logcat *:E > #{@error_log_file_name} &"
        #puts "$ #{error_log_cmd}"
        #`#{error_log_cmd}`
    end
    
    # start the real device
    def start_real_device()
        
        # use the emulator name as its serial
        avd_serial = @emulator_name
        
        # specify the avd serial, very important!!
        @adb.device avd_serial
        # set the emulator serial
        @emulator_serial = avd_serial
        
        @emulator_state = @@ready
    end

    # remove the appending view type, e.g., convert "clickImgView(1):ImageView@16908332" to "clickImgView(1)"
    def getActionCmd (action_string)
        action_cmd = ""
        action_view_text = ""
        loc_view_type = action_string.rindex(":")
        if loc_view_type != nil then
            loc_view_text = action_string.rindex("@")
            action_view_text = action_string[loc_view_text+1..action_string.length]
            action_cmd = action_string[0..loc_view_type-1]
        else
            action_view_text = ""
            action_cmd = action_string
        end
        #puts "D: action cmd: #{action_cmd}, action view text: #{action_view_text}"
        return action_cmd, action_view_text
    end
    
    # get the current focused package name, this function can be unstable, currently do not use it
    def get_current_package_name()
        
      	puts "in get_current_package_name"
      	lines = ""
      	# It is strange, sometimes lines can be "", I guess the problem is related to buffer, so need to make sure we can get the output
      	while lines.eql?("") do
            	dump_package_and_activity_cmd = "adb -s #{@emulator_serial} shell dumpsys window windows | grep -E 'mFocusedApp' "
            	puts "$ #{dump_package_and_activity_cmd}"
            	lines = `#{dump_package_and_activity_cmd}`
            	puts "lines=#{lines}#"
      	end
      	package_name_re = /(u0\s)(.*)\//
        pkg = lines.match(package_name_re)
        puts "after match pkg[0]:#{pkg[0]},pkg[1]:#{pkg[1]},pkg[2]:#{pkg[2]},pkg[3]:#{pkg[3]}"
        current_package = pkg[2] # get the current focused package
        puts "[D] the current package name: #{current_package}#"
        return current_package
    end
    
    
    # execute the event by parsing the cmd
    # TODO we need to enhance the testing with different types of input values
    def execute_event(action_cmd, pkg)
      
        puts "[D] the action cmd: #{action_cmd}#"
        action_type = ""
        action_param = "" 
        edit_input_value = ""
        
        if action_cmd.start_with?("adb") then
          cmd = action_cmd.sub("adb", "adb -s #{$emulator_serial}")
          puts "$ #{cmd}"
          `#{cmd}`
          return 1
        end
        
        if action_cmd.eql?("back\n") || action_cmd.eql?("keyevent_back\n") then # "back"
          # construct the python cmd
          back_cmd = "python ./bin/events/back.py #{@emulator_serial}"
          `#{back_cmd}`
          puts "$ #{back_cmd}"
          return 1
        end
        
        if action_cmd.eql?("reset\n") then # "reset"
            # Assume the emulator is fine, we need to re-start the app
            #puts "[#{@emulator_name}] restart the app"
            pkg = @aapt.pkg apk
            act = @aapt.launcher apk
            # Here we add an additional option "-S", so that we will force stop the target app before starting the activity.
            # Without "-S", we cannot restart the app, since the app is already there, e.g., we will get this message
            # "Activity not started, its current task has been brought to the front" 
	    if act != nil then
    		UTIL.execute_shell_cmd("adb -s " + @emulator_serial + " shell am start -S -n " + pkg + "/" + act)
    	    else
		UTIL.execute_shell_cmd("adb -s " + @emulator_serial + " shell monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1")
	    end
            return 1
        end
        
        # make sure we are within the target package before we emit the events, 
        # this check will bring some overhead during execution
        # It is Ok to remove this check, but may rip other apps.
#        if !pkg.eql?(get_current_package_name()) then
#            puts "$ package not same, return"
#            return 0
#        end
#        puts "pkg:#{pkg}"
        
        if action_cmd.eql?("menu\n") then # "menu"
          # construct the python cmd
          menu_cmd = "python ./bin/events/menu.py #{@emulator_serial}"
          `#{menu_cmd}`
          puts "$ #{menu_cmd}"
          return 1
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
           #puts "[D]: #{action_type}, #{action_param}, #{class_name} #{instance}"
           
           # construct the python cmd
           event_cmd = "timeout 2s python ./bin/events/#{action_type}#{action_param}.py #{@emulator_serial} #{class_name} #{instance}"
           `#{event_cmd}`
           puts "$ #{event_cmd}"
        else
        
          first_quote_index = action_cmd.index("\'") # get the action param value
          last_quote_index = action_cmd.rindex("\'")
          # Note we should include the quotes to avoid the existence of whitespaces in the action_param_value
          action_param_value = action_cmd[first_quote_index..last_quote_index]
          #puts "[D]: #{action_type}, #{action_param}, #{action_param_value}"
          
          event_cmd = ""
          # Note we assign each cmd with a maximum execution time so that we will not block by this cmd
          # But for scroll action, we give it more time to finish the scrolling
      	  if action_type.eql?("scroll") then
      	     event_cmd = "timeout 15s python ./bin/events/#{action_type}#{action_param}.py #{@emulator_serial} #{action_param_value}"
      	  else
      	     event_cmd = "timeout 2s python ./bin/events/#{action_type}#{action_param}.py #{@emulator_serial} #{action_param_value}"
      	  end
          `#{event_cmd}`
          puts "$ #{event_cmd}"
        end
        
        return 1
      
    end
    
    # rip an app
    def rip_app (act, pkg, test_sequence)
      
        # start the logging
        @crash_reporter.start_logging()
      
        # Note, before executing the test, we have to restart the app, as a result, we will lose the coverage data 
        # from the last execution, so we have to merge the coverage files from all executions to get the final coverage data
        # So, we dump the coverage before each restart, but if the test crashes the app, we may also lose the coverage data for this test 
	if act != nil then
    		UTIL.execute_shell_cmd("adb -s " + @emulator_serial + " shell am start -S -n " + pkg + "/" + act)
    	else
		UTIL.execute_shell_cmd("adb -s " + @emulator_serial + " shell monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1")
	end

	# TODO delay after restart
	UTIL.execute_shell_cmd("sleep #{@restart_delay}")

	# check page, TODO comment this when you are not testing real-world apps that requires login (e.g., wechat)
	#UTIL.execute_shell_cmd("python ./bin/events/dump.py #{@emulator_serial} /tmp/test.xml")
	#if UTIL.need_login(@emulator_serial, "/tmp/test.xml", "wechat") then
	#  UTIL.login(@emulator_serial, "wechat")
	#end

        # get the test sequence length
        test_sequence_length = test_sequence.size
        
        # randomly inject system events
        inject = Random.new.rand(2)
        if inject == 1 then
           # if inject, randomly decide the inject position
          inject_pos = Random.new.rand(test_sequence_length)
        end
        
        i = 0 # the index of the action cmd
        system_event_output = ""
        
        # execute the test sequence
        for t in test_sequence
          
            if inject == 1 && i == inject_pos then
              # inject the system event
              system_event = "python #{@stoat_tool_dir}/trigger/tester.py -s #{@emulator_serial} -f #{@apk} -p random"
              system_event_output = UTIL.execute_shell_cmd(system_event)
              puts "**********#{system_event_output}**********"
              sleep (@event_delay) # wait seconds
              @crash_reporter.log_test_execution_info(system_event_output, "", i, true)
            end
            
            action_cmd, action_view_text = getActionCmd t
            #puts "[#{@emulator_name}] I: to be executed: #{action_cmd}"
            
            # execute the action (event)
            res = execute_event(action_cmd, pkg)
            sleep (@event_delay) # wait seconds
            
            @crash_reporter.log_test_execution_info(action_cmd, action_view_text, i)

            i = i + 1 # increase the index!!
            
            # check whether some crash happens after each event is executed
            if @crash_reporter.has_crash() then
              @crash_reporter.dump_crash_report()
              break
            end
            
            if res == 0 then  # stop the execution of the test
              #puts "[#{@emulator_name}] D: stop the test, we are not under the target package."
              break
            end

        end # end of the loop
        
        puts "[#{@emulator_name}] I: the test sequence is completed. "
        
        
        # exit the logging after the test is finished
        @crash_reporter.exit_logging()
        
        # dump code coverage
        if @closed_source_apk == false then # open-source apk, use emma
          # Note we use timeout to solve non-responding cases when dumping code coverage
          dump_cov_cmd = "timeout 2s adb -s #{@emulator_serial} shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE"
          puts "[#{@emulator_name}]$ #{dump_cov_cmd}"
          `#{dump_cov_cmd}`
                
          # collect the coverage file
          @adb.pullCov @coverage_file + "_TEST_#{@executed_test_id}_MCMC_#{@manager.get_mcmc_iteration}.ec"
          @adb.rmCov
          @executed_test_id += 1
        else # closed-source apk, use ella
	  if not @disable_coverage then
            dump_cov_cmd = "timeout 5s #{@ella_tool_dir}/ella.sh e #{@emulator_serial}"
            puts "[#{@emulator_name}]$ #{dump_cov_cmd}"
            `#{dump_cov_cmd}`
            puts "[#{@emulator_name}]$ ***** dump coverage for this test sequence ******** "
            @executed_test_id += 1
	  end
        end
    end
    

    # the main working loop
    def working_loop ()
      
      # get the apk file
      apk = @apk
              
      # get the package name and the entry activity
      act = @aapt.launcher apk
      pkg = @aapt.pkg apk
      
      test_sequence = Array.new()
        
        while true do
            
            if !File.exist?(@test_sequence_file) then
                sleep 1
            else
                
                # set busy
                @emulator_state = @@busy
                
                test_sequence.clear() # clear the test sequence array
                
                File.open(@test_sequence_file).each do |line|
                    if line.strip.eql?("**") then
                      puts "[#{@emulator_name}] the end of a test sequence"
                      # execute the test sequence
                      rip_app(act, pkg, test_sequence)
                      # clear the sequence
                      test_sequence.clear()
                    else
                      test_sequence.push(line)
                    end
                end
                
                # delete the file
                File.delete(@test_sequence_file)
                
                # set idle
                @emulator_state = @@idle
            end
        end

    end
    
end

# the basic work flow of an agent
# 1. start_emulator
# 2. setup_agent (install troyd & the inustrumented app)
# 3. receive_test_cases (open the socket, receive test cases, store them in a local file)
#       4. start the app -> execute the test case -> generate the coverage file -> finish the app (tear down) -> pull out the coverage file
#       5. report the agent state to the server, and return to step 3
# 6. finish all test cases execution, notify the controller to merge all coverage files


