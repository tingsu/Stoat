# copyright
# Ting Su: tsuletgo@gmail.com

# This module reads and provides configuration options
class CONF
    
    @@DEBUG_STRING = "[CONF]"
    
    # the android sdk dir
    @ANDROID_SDK_DIR
    @EMMA_JAR
    
    # the dir of the tool Stoat
    @STOAT_TOOL_DIR
    
    # the dir of the tool Ella
    @ELLA_TOOL_DIR
    
    # the maximum events when building FSM
    @MAX_FSM_BUILDING_EVENTS
    
    # the target instrumented apk file location
    # used by A3E
    @TARGET_INSTRUMENTED_APK_FILE
    
    # the emma coverage file from the app under test
    @EMMA_COVERAGE_EM_FILE
    
    # the emma coverage result file name : "coverage.txt"
    @EMMA_COVERAGE_RESULT_FILE_NAME
    
    # the test suite size in one MCMC execution
    @MAX_TEST_SUITE_SIZE
    
    # the troyd apk file
    @TROYD_APK_FILE
    
    # the app directory name
    @APP_DIR_NAME
    
    # the app absolute dir path
    @APP_DIR_ABSOLUTE_DIR_PATH
    
    # the app directory location
    @APP_DIR_LOC

  	# the project type, "ant" or "gradle"
  	@PROJECT_TYPE
  	
  	# the wait time for app start/restart
  	@APP_START_WAIT_TIME
    
    @MAIN_TEST_DIR # the main test dir
    @FSM_BUILDING_DIR # the dir which stores all result files during FSM building
    @INTERMEDIATE_COVERAGE_DIR # the dir which stores all intermediate coverage files
    @UI_XML_DIR # the dir which stores all ui xml files
    @TEST_EXECUTION_DIR # the dir which store all result files during test execution
    @A3E_DFS_TEST_DIR

  	def set_project_type (type)
  		@PROJECT_TYPE = type
  	end
    
    def set_instrumented_apk_file (app_dir, apk_path)
        puts "apk path: #{apk_path}"
        
        # make sure the apk already exists
    		if @PROJECT_TYPE.eql?("ant") then
            	apk_name = `ls #{app_dir}/bin/*-debug.apk`.sub!("\n", "")
            	@TARGET_INSTRUMENTED_APK_FILE = apk_name
            	last_slash_loc = @TARGET_INSTRUMENTED_APK_FILE.rindex("/")
              @EMMA_COVERAGE_EM_FILE = app_dir + "/bin/coverage.em"
              puts "#{@DEBUG_STRING} I: emma coverage file location: #{@EMMA_COVERAGE_EM_FILE}"
    		else
    			    @TARGET_INSTRUMENTED_APK_FILE = apk_path #absolute path
    		end
       
        puts "#{@@DEBUG_STRING} I: apk file location: #{@TARGET_INSTRUMENTED_APK_FILE}"
        
    end
    
    def create_fsm_building_dir (app_dir, apk_path, is_apk)
      
        if is_apk == false then
          # if the target is an open-source app
          if Dir.exist?(app_dir) then
              
              @APP_DIR_ABSOLUTE_DIR_PATH = app_dir
            
              app_dir_name = `basename #{app_dir}`
              @APP_DIR_NAME = app_dir_name.strip
              # set the instrumented app
              set_instrumented_apk_file(app_dir, apk_path)
              
              @FSM_BUILDING_DIR = app_dir + "/" + "stoat_fsm_output"
                
              # remove the old output dir
              `rm -rf #{@FSM_BUILDING_DIR}`
              
              # create the ouput dir
              Dir.mkdir(@FSM_BUILDING_DIR)
              
              @INTERMEDIATE_COVERAGE_DIR = @FSM_BUILDING_DIR + "/coverage"
              Dir.mkdir(@INTERMEDIATE_COVERAGE_DIR)
              @UI_XML_DIR = @FSM_BUILDING_DIR + "/ui"
              Dir.mkdir(@UI_XML_DIR)
          else # the app dir does not exist
              puts "Error, the app dir does not exist!"
          end
          
        else # if the target is a closed-source apk
            
          if File.exist?(app_dir) then
            
            slash_loc = app_dir.rindex("/")
            if slash_loc != nil then
              @APP_DIR_LOC = app_dir[0..slash_loc]
              puts "#{@APP_DIR_LOC}"
            end
            
            loc = app_dir.rindex(".apk")
            if loc!=nil then
               app_dir_name = app_dir[0..loc-1]
               app_name = `basename #{app_dir_name}`
               @APP_DIR_NAME = app_name.strip
               
               # set the instrumented app
               @TARGET_INSTRUMENTED_APK_FILE = app_dir
               
               @FSM_BUILDING_DIR = app_dir_name + "-output" + "/" + "stoat_fsm_output"
               
               # remove the old output dir
               `rm -rf #{@FSM_BUILDING_DIR}`
               
               `mkdir -p #{@FSM_BUILDING_DIR}`
               
               @INTERMEDIATE_COVERAGE_DIR = @FSM_BUILDING_DIR + "/coverage"
               Dir.mkdir(@INTERMEDIATE_COVERAGE_DIR)
               @UI_XML_DIR = @FSM_BUILDING_DIR + "/ui"
               Dir.mkdir(@UI_XML_DIR)
            end
          else
            puts "Error, the app apk does not exist!"
          end
        end
    end
    
    def create_test_execution_dir (app_dir, apk_path, is_apk)
      
      if is_apk == false then
        
          @APP_DIR_ABSOLUTE_DIR_PATH = app_dir
        
          app_dir_name = `basename #{app_dir}`
          @APP_DIR_NAME = app_dir_name.strip
          
          # set the instrumented app
          set_instrumented_apk_file(app_dir, apk_path)
          
          @FSM_BUILDING_DIR = app_dir + "/" + "stoat_fsm_output"
          @TEST_EXECUTION_DIR = app_dir + "/" + "stoat_mcmc_sampling_output"
            
          # remove the old output dir
          `rm -rf #{@TEST_EXECUTION_DIR}`
          
          # create the ouput dir
          Dir.mkdir(@TEST_EXECUTION_DIR)
          
      else # it is a closed-source apk  
        
        if File.exist?(app_dir) then
                    
          slash_loc = app_dir.rindex("/")
          if slash_loc != nil then
            @APP_DIR_LOC = app_dir[0..slash_loc]
            puts "#{@APP_DIR_LOC}"
          end
          
          loc = app_dir.rindex(".apk")
          if loc!=nil then
             app_dir_name = app_dir[0..loc-1]
             app_name = `basename #{app_dir_name}`
             @APP_DIR_NAME = app_name.strip
             
             # set the instrumented app
             @TARGET_INSTRUMENTED_APK_FILE = app_dir
             
             @FSM_BUILDING_DIR = app_dir_name + "-output" + "/" + "stoat_fsm_output"
             @TEST_EXECUTION_DIR = app_dir_name + "-output" + "/" + "stoat_mcmc_sampling_output"
             
             # remove the old output dir
             `rm -rf #{@TEST_EXECUTION_DIR}`
             
             puts "herererere"
             
             # create the ouput dir
             Dir.mkdir(@TEST_EXECUTION_DIR)
          end
       end
     end
    end
    
    def create_monkey_test_dir (apk)
        
    end
    
    def create_a3e_dfs_test_dir (apk)
        if Dir.exist?(@MAIN_TEST_DIR) then
            counter = 0
            Dir.foreach(@MAIN_TEST_DIR) do |entry|
                if entry.include?(@APP_DIR_NAME) && entry.include?("a3e_dfs") then
                    counter += 1
                end
            end
            @A3E_DFS_TEST_DIR = @MAIN_TEST_DIR + @APP_DIR_NAME + "_a3e_dfs_" + counter.to_s
            if !Dir.exist?(@A3E_DFS_TEST_DIR) then
                Dir.mkdir(@A3E_DFS_TEST_DIR)
            end
        end
    end

    def read_config (file)
        # read line by line
        IO.foreach(file){ |line|
            # is it a "key=value" pair?
            if line.include?("=") then
                res = line.split("=")
                entry_name = res[0].strip
                if entry_name.eql?("ANDROID_SDK_DIR") then
                    @ANDROID_SDK_DIR = res[1].strip
                    print "#{@@DEBUG_STRING} ANDROID_SDK_DIR = "
                    puts @ANDROID_SDK_DIR
                    @EMMA_JAR = @ANDROID_SDK_DIR + "/tools/lib/emma.jar"
                elsif entry_name.eql?("STOAT_TOOL_DIR") then
                    @STOAT_TOOL_DIR = res[1].strip
                    print "#{@@DEBUG_STRING} STOAT_TOOL_DIR = "
                    puts @STOAT_TOOL_DIR
                elsif entry_name.eql?("ELLA_TOOL_DIR") then
                    @ELLA_TOOL_DIR = res[1].strip
                    print "#{@@DEBUG_STRING} ELLA_TOOL_DIR = "
                    puts @ELLA_TOOL_DIR
                elsif entry_name.eql?("EMMA_COVERAGE_RESULT_FILE_NAME") then
                    @EMMA_COVERAGE_RESULT_FILE_NAME = res[1].strip
                    print "#{@@DEBUG_STRING} EMMA_COVERAGE_RESULT_FILE_NAME = "
                    puts @EMMA_COVERAGE_RESULT_FILE_NAME
                elsif entry_name.eql?("MAX_TEST_SUITE_SIZE") then
                    @MAX_TEST_SUITE_SIZE = res[1].strip
                    print "#{@@DEBUG_STRING} MAX_TEST_SUITE_SIZE = "
                    puts @MAX_TEST_SUITE_SIZE
                elsif entry_name.eql?("MAX_FSM_BUILDING_EVENTS") then
                    @MAX_FSM_BUILDING_EVENTS = res[1].strip
                    print "#{@@DEBUG_STRING} MAX_FSM_BUILDING_EVENTS = "
                    puts @MAX_FSM_BUILDING_EVENTS
                elsif entry_name.eql?("MAIN_TEST_DIRECTORY") then
                    @MAIN_TEST_DIR = res[1].strip
                    print "#{@@DEBUG_STRING} MAIN_TEST_DIR = "
                    puts @MAIN_TEST_DIR
                elsif entry_name.eql?("APP_START_WAIT_TIME") then
                    @APP_START_WAIT_TIME = res[1].strip
                else 
                    puts "#{@@DEBUG_STRING} I: undefined configuration entry - #{entry_name}"
                end
            else
            # it may be a comment line or an empty line
            # do nothing
            end
        }
    end
    
    def get_stoat_tool_dir ()
        @STOAT_TOOL_DIR
    end
    
    def get_ella_tool_dir()
        @ELLA_TOOL_DIR
    end
    
    def get_instrumented_apk ()
        @TARGET_INSTRUMENTED_APK_FILE
    end
    
    def get_em_coverage_file ()
        @EMMA_COVERAGE_EM_FILE
    end
    
    def get_coverage_result_file_name ()
        @EMMA_COVERAGE_RESULT_FILE_NAME
    end
    
    def get_test_suite_size ()
        # convert string to integer
        @MAX_TEST_SUITE_SIZE.to_i
    end
    
    def get_max_fsm_building_events ()
        # convert string to integer
        @MAX_FSM_BUILDING_EVENTS.to_i
    end
    
    def get_fsm_building_dir ()
        @FSM_BUILDING_DIR
    end
    
    def get_emma_jar()
        @EMMA_JAR
    end
    
    def get_app_dir_name()
        @APP_DIR_NAME
    end
    
    def get_app_dir_loc()
        @APP_DIR_LOC
    end
    
    def get_coverage_files_dir()
        @INTERMEDIATE_COVERAGE_DIR
    end
    
    def get_app_absolute_dir_path()
        @APP_DIR_ABSOLUTE_DIR_PATH
    end
    
    def get_ui_files_dir()
        @UI_XML_DIR
    end
    
    def get_a3e_dfs_test_dir ()
        @A3E_DFS_TEST_DIR
    end
    
    def get_test_execution_dir ()
        @TEST_EXECUTION_DIR
    end
    
    def get_troyd_apk ()
        @TROYD_APK_FILE
    end
    
    def get_app_start_wait_time()
        @APP_START_WAIT_TIME
    end
end

