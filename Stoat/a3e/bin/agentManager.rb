#! /usr/bin/env ruby

require 'socket'

PARENT = File.expand_path(File.dirname(__FILE__))+"/../temp"

require_relative 'conf'
require_relative 'agent'
require_relative 'devices'
require_relative 'aapt'
require_relative 'util'

# the coverage reporter
class Coverage
    
    @total_packages
    @total_classes
    @total_methods
    @total_blocks
    @total_executable_files
    @total_executable_lines
    
    @covered_classes
    @covered_methods
    @covered_blocks
    @covered_lines
    
    # TODO the coverage weight is set uniformly as 0.25
    @@coverage_weight
    
    def initialize ()
        # set the coverage weight
        @@coverage_weight = 0.25
    end
    
    # set class coverage
    def setClassCoverage (cclasses, tclasses)
        @covered_classes =cclasses
        @total_classes = tclasses
    end
    # set method coverage
    def setMethodCoverage (ccmethods, tmethods)
        @covered_methods = ccmethods
        @total_methods = tmethods
    end
    # set block coverage
    def setBlockCoverage (cblocks, tblocks)
        @covered_blocks = cblocks
        @total_blocks = tblocks
    end
    # set line coverage
    def setLineCoverage (clines, tlines)
        @covered_lines = clines
        @total_executable_lines = tlines
    end
    # set total packages
    def setTotalpackages (packages)
        @total_packages = packages
    end
    # set total executable files
    def setTotalFiles (files)
        @total_executable_files = files
    end
    
    # get the final coverage which will be used in MCMC sampling
    # we currently compute a weighted coverage result based on
    # these four types of coverage
    def getCoverage
        cov = @covered_lines
        puts "I: the final line coverage: #{cov} "
        cov
    end
    
    def getLineCoveragePercentage
        (@covered_lines.to_i)*1.0/(@total_executable_lines.to_i)
    end
    
    # output coverage
    def outputCoverage
        open($myConf.get_test_execution_dir+ "/" + "mcmc_sampling_progress.txt", 'a') { |f|
            f.puts "----------------------------"
            f.puts "class coverage: #{@covered_classes}/#{@total_classes}"
            f.puts "method coverage: #{@covered_methods}/#{@total_methods}"
            f.puts "block coverage: #{@covered_blocks}/#{@total_blocks}"
            f.puts "line coverage: #{@covered_lines}/#{@total_executable_lines}"
            f.puts "total packages: #{@total_packages}"
            f.puts "total executable files: #{@total_executable_files}"
            f.puts "----------------------------"
        }
        puts "----------------------------"
        puts "class coverage: #{@covered_classes}/#{@total_classes}"
        puts "method coverage: #{@covered_methods}/#{@total_methods}"
        puts "block coverage: #{@covered_blocks}/#{@total_blocks}"
        puts "line coverage: #{@covered_lines}/#{@total_executable_lines}"
        puts "total packages: #{@total_packages}"
        puts "total executable files: #{@total_executable_files}"
        puts "----------------------------"
    end
    
end


# the agent manager
class AgentManager
    
    # the server socket port number
    @socket_port
    # the number of agents
    @agents_count
    # the agents, stored in a hash map <agent_id, agent>
    @agents
    @agents_threads
    
    # the real devices
    @devices
    
    # the manager name
    @manager_name
    
    @mcmc_iteration
    @total_test_suite_size
    
    @mcmc_sampling_log_dir
    @mcmc_sampling_log_file
    @all_test_suite_log_file
    @mcmc_sampling_coverage_log_dir
    
    # the coverage object
    @coverager
    
    @event_delay
	  @restart_delay
    
    # constructor
    # @param port the socket port number
    # @param count the number of agents
    def initialize (port, count, event_delay, restart_delay)
        @socket_port = port
        @agents_count = count
        @agents = Hash.new
        @agents_threads = Hash.new
        @devices = []
        @manager_name = "[AgentManager]"
        
        @mcmc_iteration = 0
        @total_test_suite_size = 0
        
        
        @mcmc_sampling_log_dir = $myConf.get_test_execution_dir()
        @mcmc_sampling_log_file = @mcmc_sampling_log_dir+ "/" + "mcmc_sampling_progress.txt"
        @all_test_suite_log_file = @mcmc_sampling_log_dir+ "/" + "test_suite.txt"
        
        # create the coverage directory for devices to dump coverage files (for ant/gradle projects)
        @mcmc_sampling_coverage_log_dir = @mcmc_sampling_log_dir + "/MCMC_coverage/"
    		UTIL.execute_shell_cmd("mkdir -p #{@mcmc_sampling_coverage_log_dir}")
    		        
        # create the coverage object
        @coverager = Coverage.new()
        
        @event_delay = event_delay
		    @restart_delay = restart_delay
    end
    
    def get_mcmc_iteration ()
        @mcmc_iteration
    end
    
    def get_event_delay ()
        return @event_delay*1.0/1000.0
    end

	def get_restart_delay()
		return @restart_delay*1.0/1000.0
	end

  	def get_project_type ()
  		return $g_project_type
    end

    # output the current system configurations
    def outputConfig ()
        puts "#{@manager_name} I: the system configurations: "
        puts "---------------"
        puts "server port number: #{@socket_port}"
        puts "the number of emulators: #{@agents_count}"
        puts "---------------"
    end
    
    
    # identify real devices
    def identifyRealDevices(dev_serials)
        
        dev = "adb devices | grep -w \"device\" "
        out = `#{dev}`
        puts out
        
        file_name = "real_devices.txt"
        if File.exist?(file_name)
            File.delete(file_name)
        end
        
#        open(file_name, "a") { |f|
#            out.each_line do |line|
#                f.puts line
#                dev = line.split()
#                device_name = dev[0]
#                device_serial = dev[0]
#                device_state = dev[1]
#                puts "I: Detec a real device: device name: #{device_name}"
#                # add the device name
#                @devices.push(device_name)
#            end
#        }
        
        # add all available devices
        for serial in dev_serials
          @devices.push(serial)
        end
        
        puts 'I: there are total %d devices ' % @devices.length
        
    end

    

    def setup (dev_serials)
    # create all agents
    #   - an agent is responsible for manging its emulator and communicate directly with the server
    #   - the agent report its state directly to the server
    #   - the name of agent is namely the name of emulator
    #   - when all agents finish their tasks, the server will send a command to this manager to get the coverage information
    
    
        identifyRealDevices(dev_serials)
    
        for i in 1..@agents_count
            
            # NOTE: uncomment it when using emulators
            # agent_name = "testAVD_" + i.to_s
            
            # NOTE: uncomment it when using real devices
            agent_name = @devices[i-1]
            
            # TODO agent_option is not specified now
            agent_option = ""
            agent = Agent.new(agent_name, agent_option, @socket_port, $myConf, $closed_source_apk, $g_disable_coverage_report, self)
            
            # record all agents (for futher management, e.g., pull and merge coverage files)
            @agents.store(i, agent)
            
            # these agents run in respecitve threads (otherwise they have to execute sequentially)
            # TODO manage all threads in a more uniform way, here i simply let the main thread wait them after they are created, and move the "join" operation to the main entry
            thread = Thread.new{agent.start()}
            sleep 2
            puts "#{@manager_name} I: agent thread #{agent_name} is created"
            @agents_threads.store(i, thread)
        end
         
        puts "#{@manager_name} I: total #{@agents.size} agents have been created. "

    end
    
    # parse the emma coverage report with the "txt" format
    # we currently focus on the overall coverage summary
    # An example file :
#    [EMMA v2.0.5312 report, generated Mon Feb 09 10:38:28 PST 2015]
#    -------------------------------------------------------------------------------
#    OVERALL COVERAGE SUMMARY:
#    
#    [class, %]	[method, %]	[block, %]	[line, %]	[name]
#    100% (1/1)	50%  (3/6)!	36%  (27/76)!	38%  (9/24)!	all classes
#    
#    OVERALL STATS SUMMARY:
#    
#    total packages:	1
#    total classes:	1
#    total methods:	6
#    total executable files:	1
#    total executable lines:	24
#    
#    COVERAGE BREAKDOWN BY PACKAGE:
#    
#    [class, %]	[method, %]	[block, %]	[line, %]	[name]
#    100% (1/1)	50%  (3/6)!	36%  (27/76)!	38%  (9/24)!	course.examples.UI.MenuExample
#    -------------------------------------------------------------------------------
    def parse_emma_coverage_report (report_file)
        
       puts "#{@manager_name} I: the coverage report file: #{report_file}. "


       # the coverage info type: "1" - coverage summary, "2" - stats summary, "3" - detail summary
       coverage_info_type = 0
       # is the data here?
       data = 0
       IO.foreach(report_file) { |line|
           
           if line.include?("COVERAGE SUMMARY") then
               #puts "the coverage [summary] line ... "
               coverage_info_type = 1;
           elsif line.include?("STATS SUMMARY") then
               #puts "the coverage [stats] line ... "
               coverage_info_type = 2;
           end
           
           ########### extract coverage summary
           if (coverage_info_type == 1) && (line.include?("[class, %]")) then
               # puts "the coverage label line ... "
               data = 1
           end
           if coverage_info_type == 1 && data == 1 then
               
               # puts "the coverage data line ..."
               i = 1
               
               # match the pattern "(digits/digits)", note "digits" could be "float"
               line.gsub(/(\d|\.)+\/(\d)+/){ |match|
                   #puts "#{match}"
                   # get the covered/total entities
                   arr = match.split("/")
                   #puts "#{arr[0]}, #{arr[1]}"
                   if (i==1) then
                       @coverager.setClassCoverage arr[0], arr[1]
                   elsif (i==2) then
                       @coverager.setMethodCoverage arr[0], arr[1]
                   elsif (i==3) then
                       @coverager.setBlockCoverage arr[0], arr[1]
                   elsif (i==4) then
                       @coverager.setLineCoverage arr[0], arr[1]
                   end
                   i += 1
               }
           end
           
           ######## extract stats summary
           if (coverage_info_type == 2) && (line.include?("total")) then
               #puts "the stats line ... "
               # get the desciption_statement/number
               arr = line.split(":")
               #puts "#{arr[0]}, #{arr[1].strip}"
               if line.include?("total packages") then
                    @coverager.setTotalpackages arr[1].strip
               elsif line.include?("total executable files") then
                    @coverager.setTotalFiles arr[1].strip
               end
           end
       }
       
    end
    
    
    # merge [all] coverage files, if return false, it fail to merge files, otherwise, it succeed.
    def merge_coverage_files (all=false)
        coverage_files = ""
        if all==false then
            puts "#{@manager_name} I: merge coverage files in one MCMC ..."
            pattern = "*_MCMC_#{@mcmc_iteration}.ec"
            coverage_files = `find #{@mcmc_sampling_coverage_log_dir} -name #{pattern}`
        else
            puts "#{@manager_name} I: merge coverage files in all MCMC ..."
            pattern = "MCMC_ITERATION_*.ec"
            coverage_files = `find #{@mcmc_sampling_coverage_log_dir} -name #{pattern}`
        end
        puts "#{coverage_files}"
        str_coverage_files = ""
        coverage_files.each_line do |line|
            str_coverage_files += line.strip + ","
        end
        
        if str_coverage_files.eql?("") then
            return false # fail to merge files!!!
        end
        
        # get coverage .em file
        coverage_em = $myConf.get_em_coverage_file
        $g_coverage_txt = @mcmc_sampling_log_dir + "/" + "coverage.txt"
        coverage_merge_cmd = "java -cp " + $myConf.get_emma_jar() + " emma report -r txt -in " + str_coverage_files + coverage_em + " -Dreport.txt.out.file=" + $g_coverage_txt
        puts "$#{coverage_merge_cmd}"
        # execute the shell cmd
        `#{coverage_merge_cmd}`
        
        
        if all==false then
            # merge all coverage.ec files from one mcmc iteration into one single coverage.ec file, otherwise the argument list
            # could be too long for emma when computing the history coverage
            coverage_merge_ec_cmd = "java -cp "+ $myConf.get_emma_jar() + " emma merge -in " + str_coverage_files[0..(str_coverage_files.length-1)] + " -out #{@mcmc_sampling_coverage_log_dir}/MCMC_ITERATION_#{@mcmc_iteration}.ec"
            puts "$#{coverage_merge_ec_cmd}"
            `#{coverage_merge_ec_cmd}`
            
            #delete all these intermediate coverage files
            rm_ecs_cmd = "rm -rf #{@mcmc_sampling_coverage_log_dir}/*_MCMC_#{@mcmc_iteration}.ec"
            puts "$#{rm_ecs_cmd}"
            `#{rm_ecs_cmd}`
            
        end
        
        return true
        
    end
    
    def manager_working_loop (max_iteration)
        
        
        # make sure all agents are ready
        done = false
        while !done do
            done=true
            for i in 1..@agents_count do
                agent = @agents[i]
                if !agent.is_ready() then
                    puts "#{@manager_name} I: agent ##{i} is not ready, wait for it..."
                    done = false
                    break
                end
            end
            # wait for a while
            sleep 5
        end


        # try to connect with the java server
        while true do
            begin
                # create the socket connection
                clientSession = TCPSocket.new("localhost", @socket_port)
                if !clientSession.eql?(nil) then
                    puts "#{@manager_name} I: the connection is set up. "
                    break
                end
                rescue
                puts "#{@manager_name} is waiting the java server connection at port #{@socket_port}... "
                sleep(5)
            end
        end


        # a test sequence
        test_sequence = ""
        # a test suite
        test_suite = Array.new
        
        # total time of mcmc sampling
        total_exec_time = 0.0
        start_time = Time.now
        
        while true do
            
        
          # the socket connection has been setup
          clientSession.puts "REQ_TS"
          
          # clear the test suite
          test_suite.clear
      	  if File.exist?(@mcmc_sampling_log_dir+ "/" + "test_suite.txt") then
         	  	File.delete(@mcmc_sampling_log_dir+ "/" + "test_suite.txt") # delete the old file
      	  end
      	  
          @mcmc_iteration += 1
          
          while !(clientSession.closed?) &&
              (serverMessage = clientSession.gets)
              
              puts "serverMessage: #{serverMessage}"
              
              # Message: "END_TC", the end of a test sequence
              if serverMessage.include?("END_TC") then
                  
                  File.open(@mcmc_sampling_log_dir+ "/" + "test_suite.txt", 'a') do |f|
                      f.puts test_sequence
                      f.puts "**"
                  end
                  
                  puts "#{@manager_name} I: get a test sequence: #{test_sequence}"
                  test_suite.push(test_sequence)
                  
                  # clear the test sequence string
                  test_sequence = ""
              
              # Message: "END_TS", the end of a test suite
              elsif serverMessage.include?("END_TS") then
              
                  puts "#{@manager_name} I: get a test suite!!!"
                  break
              else
                  # get the test cases
                  puts "#{@manager_name} received test event: #{serverMessage}"
                  test_sequence += serverMessage
              end
          end
          
          
          # TODO comment out 
#          test_suite_index = 0
#          test_suite_size = test_suite.size
#          while test_suite_index < test_suite_size do
#              sleep 1
#              
#              #puts "#{@manager_name} I: the test suite size: #{test_suite_size}, agent count: #{@agents_count}, the test #{test_suite_index+1} is waiting ..."
#              
#              for i in 1..@agents_count do
#                  agent = @agents[i]
#                  if agent.is_test_sequence_empty then
#                      agent.set_test_sequence(test_suite[test_suite_index])
#                      test_suite_index += 1
#                      puts "#{@manager_name} I: test sequence ##{test_suite_index} is given to agent ##{i}"
#                      if test_suite_index >= test_suite_size then
#                          # If no remaining test sequence, break
#                          break
#                      end
#                  else
#                      #puts "#{@manager_name} I: agent ##{i} is busy."
#                  end
#                  
#              end
#          end
          
	        test_suite_size = test_suite.size
          # After the copy, the emulator will read this file to execute the test sequence
          copy_test_suite = "cp " + @mcmc_sampling_log_dir+ "/" + "test_suite.txt" + " " + @mcmc_sampling_log_dir+ "/" + "test_suite_to_execute.txt"
          puts "$ #{copy_test_suite}"
          `#{copy_test_suite}`
          sleep 5
          
          # make sure all agents have finished test execution
          done = false
          while !done do
              done=true
              #puts "#{@manager_name} I: the whole test suite has been sent out !!"
              for i in 1..@agents_count do
                  agent = @agents[i]
                  if !agent.is_idle() then
                      #puts "#{@manager_name} I: but agent ##{i} is busy, wait for it..."
                      done = false
                      break
                  end
              end
              sleep 1 # wait for a while
          end
              
          # get the end time of one mcmc iteration
          end_time = Time.now
          elapsed_time = ((end_time - start_time).to_f)/60
          total_exec_time += elapsed_time
          
          puts "#{@manager_name} I: compute coverage ... "
          # compute coverage for this mcmc iteration
          
      if ($closed_source_apk == false) then # open-source app
            
            lineCov = 0
            lineCovPer = 0
            
            # if use "ant"
            if $g_project_type.eql?("ant") then
              res = merge_coverage_files(false)
              if res == true then
                  parse_emma_coverage_report($g_coverage_txt)
                  lineCov = @coverager.getCoverage
                  lineCovPer = @coverager.getLineCoveragePercentage
              end
              
            # if use "gradle"
            else
              
			  if not $g_disable_coverage_report then
              	# coverage info format: "#covered_lines #line_coverage_percentage" 
              	#coverage_info = UTIL.execute_shell_cmd("python ../android_instrument/dump_coverage.py #{$myConf.get_app_absolute_dir_path()} mcmc | tail -1")
              
              	#coverage_data = coverage_info.split(' ')
              	#lineCov = coverage_data[0]
              	#lineCovPer = coverage_data[1]
			  end

	      	  lineCov = 0
	      	  lineCovPer = 0.5 # TODO FAKE coverage
              
              # rename the coverage data dir name to help gradle coverage 
              UTIL.execute_shell_cmd("mv #{@mcmc_sampling_coverage_log_dir} #{@mcmc_sampling_log_dir}/MCMC_coverage_#{@mcmc_iteration}")
              UTIL.execute_shell_cmd("mkdir -p #{@mcmc_sampling_coverage_log_dir}")
             
            end
            
            # record the statistic info
            open(@mcmc_sampling_log_file, 'a') { |f|
                f.puts "MCMC: iteration: #{@mcmc_iteration}, test suite size: #{test_suite_size}, the line coverage: #{lineCov}, the elapsed time: #{elapsed_time}"
            }
            puts "#{@manager_name} I: MCMC: iteration: #{@mcmc_iteration}, test suite size: #{test_suite_size}, the line coverage: #{lineCov}"
            
            # send the code coverage to the server
            clientSession.puts "PULL_COV"
            clientSession.puts lineCovPer
            puts "#{@manager_name} I: the code coverage has been sent. "
            
            @total_test_suite_size += test_suite_size
            
            lineCov = 0
            lineCovPer = 0
            # get overall coverage from all mcmc iterations
            if $g_project_type.eql?("ant") then
              
              res = merge_coverage_files(true)
            
              if res == true then
                  parse_emma_coverage_report($g_coverage_txt)
                  lineCov = @coverager.getCoverage
              end
            else
              # TODO do nothing for gradle
              lineCov = "-"
            end

            open(@mcmc_sampling_log_file, 'a') { |f|
                f.puts "MCMC: iteration: #{@mcmc_iteration}, total test suite size: #{@total_test_suite_size}, the total line coverage in history : #{lineCov}"
                f.puts "----------------------------\n\n"
            }
            puts "#{@manager_name} I: MCMC: iteration: #{@mcmc_iteration}, total test suite size: #{@total_test_suite_size}, the total line coverage in history: #{lineCov}"
    

          elsif $closed_source_apk == true then # closed-source apk
		
			method_coverage = ""

			if not $g_disable_coverage_report then
            	merge_cov_cmd = "python #{$myConf.get_ella_tool_dir()}/coverage.py #{$myConf.get_app_dir_loc()} MCMC_#{@mcmc_iteration}"
            	puts "$ #{merge_cov_cmd}"
            	res = `#{merge_cov_cmd}`
            	puts "***** merge coverage, finished!!! ******* "
            	method_coverage = res.strip
			end

			method_coverage = 0.5 #TODO fake data
            
            open(@mcmc_sampling_log_file, 'a') { |f|
               f.puts "MCMC: iteration: #{@mcmc_iteration}, test suite size: #{test_suite_size}, the method coverage: #{method_coverage}, the elapsed time: #{elapsed_time}"
            }
            puts "#{@manager_name} I: MCMC: iteration: #{@mcmc_iteration}, test suite size: #{test_suite_size}, the method coverage: #{method_coverage}"
            
            # send the code coverage to server
            clientSession.puts "PULL_COV"
            clientSession.puts method_coverage
            puts "#{@manager_name} I: the method coverage has been sent. "
            
            
          end
          
          # stop the mcmc sampling
          if @mcmc_iteration >= max_iteration.to_i
            clientSession.puts "STOP"
            break
          end

        end

    end
    

end

def write_conf(communication_port)
  
  output = "PORT = #{communication_port}"
  puts "D: write the config file: #{output}"
  open($myConf.get_test_execution_dir() + "/" + "CONF.txt", 'a') { |f|
     f.puts output
  }
  
  output = "FSM_FILE_LOCATION = #{$myConf.get_fsm_building_dir()}"
  puts "D: write the config file: #{output}"
  open($myConf.get_test_execution_dir() + "/" + "CONF.txt", 'a') { |f|
       f.puts output
  }
end


def prepare_env(dev_serials)
  
  # copy the config
  conf_file =  $myConf.get_stoat_tool_dir() + "/" + "CONF.txt"
  `cp #{conf_file} #{$myConf.get_test_execution_dir()}`
  
  # write the configuration for the app under test
  write_conf($serverPortNumber)
  
end

def clean_up(dev_serials)
  # clean up
  for serial in dev_serials
      puts "D: clean up for #{serial}"
      
      # clean up logcat in the target device
      #killall_logcat_cmd = "adb -s " + serial + " shell killall logcat"
      #puts "$ #{killall_logcat_cmd}"
      #`#{killall_logcat_cmd}`
      
      # uninstall the app
      pkg = AAPT.pkg($myConf.get_instrumented_apk())
      uninstall_app_cmd = "timeout 5s adb -s " + serial + " uninstall #{pkg}"
      puts "$ #{uninstall_app_cmd}"
      `#{uninstall_app_cmd}`
      
      # clean up adb in the localhost for the target device
      logcat_pids = `ps | grep 'adb' | awk '{print $1}'`
      logcat_pids_list = logcat_pids.sub!("\n", " ")
      kill_adb_cmd = "kill -9 #{logcat_pids_list}"  # kill the adb logcat process
      puts "$ #{kill_adb_cmd}"
      `#{kill_adb_cmd}`
      
  end
end


#### System Configurations ####

# the server socket port number
$serverPortNumber = 2009
# the number of emulators
$agentsCount = 1
# the coverage txt file name
$g_coverage_txt = ""

# is a closed-source apk?
$closed_source_apk = false
$g_disable_coverage_report = false
$ella_coverage = false
$g_project_type = "ant"

# the number of android devices
dev_number = 0
# the list of serials of these devices
dev_serials = Array.new() 
# the target apk name
apk_name = ""
# the target app dir
app_dir = ""
# the maximum iterations of mcmc sampling
max_iteration = 0
# the delay between events
event_delay = 0
# the delay after the restart event
restart_delay = 0

######


OptionParser.new do |opts|
  opts.banner = "Usage: ruby #{__FILE__} target.apk [options]"
  opts.on("--dev serial", "serial of device that you uses") do |s|
    puts "D: the device: #{s}"
    dev_serials.push(s)
  end
  opts.on("--devnum number", "the number of devices") do |n|
    puts "D: dev number: #{n}"
    dev_number = n
  end
  opts.on("--apk apk", "the apk under test") do |l|
    apk_name = l
  end
  opts.on("--app app", "the app under test") do |i|
    puts "D: app dir: #{i}"
    app_dir = i
  end
  opts.on("--max_iteration iteration", "the app under test") do |m|
    puts "D: max iteration: #{m}"
    max_iteration = m
  end
  opts.on("--port port", "the communication port") do |p|
    puts "I: the communication port: #{$serverPortNumber}"
    $serverPortNumber = p
  end
  opts.on("--event_delay time", "the delay time between events in milliseconds") do |y|
      puts "D: the event delay: #{y}"
      event_delay = y.to_i
  end
  opts.on("--restart_delay time", "the delay time after the restart in milliseconds") do |y1|
      puts "D: initial restart delay time: #{y1}"
      restart_delay = y1.to_i
  end
  opts.on("--project_type type", "the project type: ant or gradle") do |t|
      $g_project_type = t
      puts "I: project type: #{$g_project_type}"
  end
  opts.on("--disable_coverage_report", "disable coverage report during execution") do
      $g_disable_coverage_report = true
  end
  opts.on_tail("-h", "--help", "show this message") do
    puts opts
    exit
  end
end.parse!


$myConf = CONF.new()
config_file = File.expand_path(File.dirname(__FILE__)) + '/../../CONF.txt'
# read config file
$myConf.read_config(config_file)

# check whether we are testing an apk
if app_dir.end_with?(".apk") then
  $closed_source_apk = true
  if not $g_disable_coverage_report then
	  $ella_coverage = true
  end
end

$myConf.set_project_type ($g_project_type)
# create the mcmc sampling dir
$myConf.create_test_execution_dir(app_dir, apk_name, $closed_source_apk)

# get the number of available devices
$agentsCount = dev_number.to_i
puts "the number of available devices: #{$agentsCount}"

# prepare
prepare_env(dev_serials)

# create the agent manager
manager = AgentManager.new($serverPortNumber,$agentsCount,event_delay,restart_delay)
# output the current configuration
manager.outputConfig()
# setup the manager
manager.setup(dev_serials)

# start the working loop
looper = Thread.new{manager.manager_working_loop(max_iteration)}

# join all threads
# TODO the agents threads have not been joined, since they will enter into an inifinite loop without termination
#manager.join_agents_threads
looper.join

# clean up
clean_up(dev_serials)

