## Copyright (c) 2015-2017
## Ting Su <tsuletgo@gmail.com>
## All rights reserved.
require_relative 'util'


# CrashReporter records app crashes at runtime. Once an app crash is detected, a bug report will be dumped, which contains the crash stack, 
# the bug triggering trace, the screenshots, and the coverage meta data when the crash happens.
# The basic idea is to check the logcat message when one event or one event sequence was executed. One logcat thread is created before each test execution, and is killed after the execution is finished.
# Due to we leverage the logcat information to filter error message, the crash stack may contain redundant information, but the last crash
# stack is the real crash. 
# Despite the logcat format of android sdk versions may be different, this uitility should be compatible with all versions.
 

class CrashReporter
  
  @apk_file
  @package_name # the package name of the app
  @app_code_version # app code version 
  
  # environment 
  @android_sdk_version # the sdk version, e.g., "android-18", getprop ro.build.version.sdk
  @ro_product_model # the product model, e.g., "Nexus 5X", getprop ro.product.model
  @app_meminfo # the memory info of the app, dumpsys meminfo <package_name|pid>
  
  
  @logcat_file_name
  @logcat_thread
  
  @dev_serial # the device serial
  @instrumentation_tool
  @bugreport_dir # the dir of bugreports 
  @bugreport_id # the id of this bugreport
  @bugreport_date # the date of this bugreport
  
  @executed_test_id
  @executed_test_log_dir # the dir of logging execution info for one test, named by the id of the executed test
  @bug_event_trace_file # the file of recording the bug event trace
  @has_crash # the flag to indicate whether some crash happens
  
  
  # the logcat message filter, configurable, work for different android versions
  @@logcat_filter="AndroidRuntime:E CrashAnrDetector:D ActivityManager:E SQLiteDatabase:E WindowManager:E ActivityThread:E Parcel:E *:F *:S"
  
  
  def initialize(apk_file, dev_serial, bugreport_dir)
    @apk_file = apk_file
    @dev_serial = dev_serial
    @bugreport_dir = bugreport_dir + "/crashes"
    
    @bugreport_id = 1
    @executed_test_id = 0
    @logcat_thread = nil # init it with nil
    @instrumentation_tool = "emma"
    
    # init. the setting
    init_setting()
    puts "D: Crash Reporter Inited!"
  end
  
  def init_setting()
    @androi_sdk_version = UTIL.execute_shell_cmd("adb -s #{@dev_serial} shell getprop ro.build.version.sdk").strip()
    @ro_product_model =  UTIL.execute_shell_cmd("adb -s #{@dev_serial} shell getprop ro.product.model").strip()
    @package_name = UTIL.get_package_name(@apk_file)
    @app_code_version = UTIL.get_app_version(@apk_file)
    UTIL.execute_shell_cmd("mkdir -p #{@bugreport_dir}")
  end
  
  # start the logging
  def start_logging()
    @executed_test_id = @executed_test_id + 1
    # create the temp dir for logging execution info.
    @executed_test_log_dir = @bugreport_dir + "/" + @executed_test_id.to_s
    UTIL.execute_shell_cmd("mkdir -p #{@executed_test_log_dir}")
    @bug_event_trace_file = @bugreport_dir + "/" + @executed_test_id.to_s + "/bug_event_trace.txt"
    @logcat_file_name = @bugreport_dir + "/" + @executed_test_id.to_s + "/#{@dev_serial}_logcat.txt"
    # set false
    @has_crash = false 
    # clear the logcat buffer before logging
    UTIL.execute_shell_cmd("adb -s #{@dev_serial} logcat -c") 
    # start the logcat
    UTIL.execute_shell_cmd("adb -s #{@dev_serial} logcat #{@@logcat_filter} > #{@logcat_file_name} &")
    puts "D: start logging"
  end
  
  # kill the logging thread
  def exit_logging()
    puts "D: kill the previous logcat process"
    UTIL.execute_shell_cmd("for pid in $(ps aux | grep \"[-]s #{@dev_serial} logcat\" | awk '{print $2}'); do kill -9 $pid; done")
    if not @has_crash then
      clean_up() 
    end
  end
  
  # if not crash happens, we need to clean up all the logged info
  def clean_up()
    UTIL.execute_shell_cmd("rm -rf #{@executed_test_log_dir}")
  end
  
  # make sure this is a crash stack (has 'at' keywords). Note these lines already contain the keyword "package name"
  def check_crash_details(lines)
    #puts "lines: #{lines}"
    lines.each_line do |l|
        loc = l.index(":")
        if loc != nil
          length = l.length
          core_line = l[(loc+1)..(length-1)].strip() # remove the logcat message head
          #puts "core line: #{core_line}"
          if core_line.start_with?("at") # check "at"
            return true
          end
        end
    end
    return false
  end
  
  def has_crash()
    lines=UTIL.execute_shell_cmd("wc -l #{@logcat_file_name} | awk '{print $1}'").strip()
    puts "lines=#{lines}"
    # make sure the error is related to the app under test
    lines=UTIL.execute_shell_cmd("grep #{@package_name} #{@logcat_file_name}").strip()
    if not lines.eql?("") then
      if check_crash_details(lines) then
        @has_crash = true # set true
        return true # return true
      end
    end
    return false
  end
  
  # the bug report: the crash stack from logcat; the screenshots; the event sequence 
  def dump_crash_report()
    sep = "***********"
    UTIL.execute_shell_cmd("echo \"#{sep}\" >> #{@logcat_file_name}")
    UTIL.execute_shell_cmd("echo \"package name: #{@package_name}\" >> #{@logcat_file_name}")
    UTIL.execute_shell_cmd("echo \"app code version: #{@app_code_version}\" >> #{@logcat_file_name}")
    UTIL.execute_shell_cmd("echo \"android sdk version: #{@androi_sdk_version}\" >> #{@logcat_file_name}")
    UTIL.execute_shell_cmd("echo \"product model: #{@ro_product_model}\" >> #{@logcat_file_name}")
    UTIL.execute_shell_cmd("timeout 5s adb -s #{@dev_serial} shell dumpsys meminfo #{@package_name} >> #{@logcat_file_name}")
    UTIL.execute_shell_cmd("echo \"#{sep}\" >> #{@logcat_file_name}")
  end
  
  # dump crash report especially for model construction, restrict to #backtrace_step
  def dump_crash_report_for_model_construction(backtrace_step)
    
    puts "*************************************************************"
    # dump the crash report
    dump_crash_report()
    
    # get the last #{backtrace_step} events
    bug_event_trace_new_file = File.dirname(@bug_event_trace_file) + "/" + File.basename(@bug_event_trace_file, ".txt") + "_back_" + backtrace_step.to_s + ".txt"
    UTIL.execute_shell_cmd("tail -#{backtrace_step} #{@bug_event_trace_file} >> #{bug_event_trace_new_file}")
    
    # get the last #{backtrace_step} screenshots
    remove_files(File.dirname(@bug_event_trace_file), backtrace_step)
    
    puts "*************************************************************"
  end
  
  # keep the last #{backtrace_step} screenshots, and remove all the others
  def remove_files(dir, backtrace_step)
    png_files = []  # the array of numeric ids in the png file names
    files = Dir.entries(dir)
    for file in files do
      if(file.start_with?("screen")) then
        loc1 = file.rindex("_")
        loc2 = file.rindex(".png")
        id = file[(loc1+1)..(loc2-1)].to_i
        puts "id: #{id}"
        png_files.push(id)
      end
    end
    sorted_png_files = png_files.sort() # sort the numeric ids
    #for i in 0..sorted_png_files.length()-1
    #  puts "i: #{sorted_png_files[i]}"
    #end
    sorted_png_files_cnt = sorted_png_files.length()
    for i in 0..(sorted_png_files_cnt-backtrace_step-1) do
      id = sorted_png_files[i]
      for file in files do
        if file.include?("_#{id}.png") then
           png_file_path = dir + "/" + file
           puts "png_file_path: #{png_file_path}"
           UTIL.execute_shell_cmd("rm -rf #{png_file_path}")
        end
      end
    end
  end
  
  # log the overal test execution info.
  def log_test_execution_info(event_cmd, action_view_text, event_id, is_system_event=false)
    record_event(event_cmd, action_view_text)
    #dump_screenshot(event_id, @executed_test_log_dir,is_system_event)
    #dump_event_coverage(event_id, @executed_test_log_dir, @instrumentation_tool, is_system_event)
  end
  
  # dump coverage for each event
  def dump_event_coverage(event_id, dir, tool, is_system_event=false)
    if tool.eql?("emma")
      # put timeout before coverage dump in case that some app crash fails it 
      UTIL.execute_shell_cmd("timeout 3s adb -s #{@dev_serial} shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE")
      if is_system_event then
        UTIL.execute_shell_cmd("adb -s #{@dev_serial} pull /sdcard/coverage.ec #{dir}/coverage_sysE_#{event_id}.ec")
      else
        UTIL.execute_shell_cmd("adb -s #{@dev_serial} pull /sdcard/coverage.ec #{dir}/coverage_uiE_#{event_id}.ec")
      end
      UTIL.execute_shell_cmd("adb -s #{@dev_serial} shell rm /sdcard/coverage.ec")
    end
  end
  
  # record the executed event
  def record_event(event_cmd, action_view_text)
    #puts "*************************************************************"
    #puts "D: record event: #{event_cmd}, and its text: #{action_view_text}"
    cmd = event_cmd.chomp() + "@" + action_view_text.chomp() # remove "\n" 
    # quote #{event_cmd} with "" since itself may contain '' !!!
    UTIL.execute_shell_cmd("echo \"#{cmd}\" >> #{@bug_event_trace_file}")
    #puts "*************************************************************"
  end
  
  # dump the screenshots during testing, if not crash happens, delete them
  # http://blog.shvetsov.com/2013/02/grab-android-screenshot-to-computer-via.html
  def dump_screenshot(id, dir, is_system_event=false)
    UTIL.execute_shell_cmd("adb -s #{@dev_serial} shell screencap -p /sdcard/stoat_screen.png")
    if is_system_event then
      # record the screenshot of the system event
      UTIL.execute_shell_cmd("adb -s #{@dev_serial} pull /sdcard/stoat_screen.png #{dir}/screen_sysE_#{id}.png")
    else
      UTIL.execute_shell_cmd("adb -s #{@dev_serial} pull /sdcard/stoat_screen.png #{dir}/screen_uiE_#{id}.png")
    end
    UTIL.execute_shell_cmd("adb -s #{@dev_serial} shell rm /sdcard/stoat_screen.png")
  end
  
end


##test
#def test_crash_reporter()
#  crash_reporter = CrashReporter.new("","","","emulator-5554", "/tmp/log.txt","/tmp/log/")
#  crash_reporter.start_logging()
#  sleep 10
#  puts crash_reporter.has_crash()
#  crash_reporter.start_logging()
#  sleep 10
#  puts crash_reporter.has_crash()
#end
#
#test_crash_reporter
