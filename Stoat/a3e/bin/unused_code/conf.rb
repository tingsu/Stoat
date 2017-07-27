# copyright
# Ting Su: tsuletgo@gmail.com

# This module reads and provides configuration options
module CONF
    
    @@DEBUG_STRING = "[CONF]"
    
    # the maximum events when building FSM
    @@MAX_FSM_BUILDING_EVENTS
    
    # the target instrumented apk file location
    # used by A3E
    @@TARGET_INSTRUMENTED_APK_FILE
    
    # the emma coverage file from the app under test
    @@EMMA_COVERAGE_EM_FILE
    
    # the emma coverage result file name : "coverage.txt"
    @@EMMA_COVERAGE_RESULT_FILE_NAME
    
    # the test suite size in one MCMC execution
    @@MAX_TEST_SUITE_SIZE
    
    # the troyd apk file
    @@TROYD_APK_FILE
    
    
    @@MAIN_TEST_DIR # the main test dir
    @@FSM_BUILDING_DIR # the dir which stores all result files during FSM building
    @@TEST_EXECUTION_DIR # the dir which store all result files during test execution
    
    def CONF.set_instrumented_apk_file (apk)
        @@TARGET_INSTRUMENTED_APK_FILE = apk
        puts "#{@@DEBUG_STRING} I: apk file location: #{@@TARGET_INSTRUMENTED_APK_FILE} "
        last_slash_loc = @@TARGET_INSTRUMENTED_APK_FILE.rindex("/")
        if last_slash_loc == nil then
            puts "#{@@DEBUG_STRING} I: Error in set_instrumented_apk_file!!"
            exit
        else
            dir_prefix = @@TARGET_INSTRUMENTED_APK_FILE[0..last_slash_loc-1]
            @@EMMA_COVERAGE_EM_FILE = dir_prefix + "/coverage.em"
            puts "#{@@DEBUG_STRING} I: emma coverage file location: #{@@EMMA_COVERAGE_EM_FILE}"
        end
    end
    
    def CONF.get_app_dir (apk)
        app_dir_name = ""
        last_slash_loc = apk.rindex("/bin")
        if last_slash_loc == nil then
            puts "#{@@DEBUG_STRING} I: Error in get_fsm_building_dir!"
            exit
        else
            dir_prefix = apk[0..last_slash_loc-1]
            last_slash_loc = dir_prefix.rindex("/")
            if last_slash_loc == nil then
                puts "#{@@DEBUG_STRING} I: Error in get_fsm_building_dir!!"
                exit
            else
                app_dir_name = dir_prefix[last_slash_loc+1..(dir_prefix.length)]
                puts "#{@@DEBUG_STRING} I: the app dir name: #{app_dir_name}"
            end
        end
        app_dir_name
    end
    

    
    def CONF.create_fsm_building_dir (apk)
        # get the app directory name
        app_dir_name = get_app_dir (apk)
        if Dir.exist?(@@MAIN_TEST_DIR) then
            counter = 0
            Dir.foreach(@@MAIN_TEST_DIR) do |entry|
                if entry.include?(app_dir_name) && entry.include?("fsm_building") then
                    counter += 1
                end
            end
            @@FSM_BUILDING_DIR = @@MAIN_TEST_DIR + app_dir_name + "_fsm_building_" + counter.to_s
            if !Dir.exist?(@@FSM_BUILDING_DIR) then
                Dir.mkdir(@@FSM_BUILDING_DIR)
            end
        end
    end
    
    def CONF.create_test_execution_dir (apk)
        # get the app directory name
        app_dir_name = get_app_dir (apk)
        if Dir.exist?(@@MAIN_TEST_DIR) then
            counter = 0
            Dir.foreach(@@MAIN_TEST_DIR) do |entry|
                if entry.include?(app_dir_name) && entry.include?("mcmc_sampling") then
                    counter += 1
                end
            end
            @@TEST_EXECUTION_DIR = @@MAIN_TEST_DIR + app_dir_name + "_mcmc_sampling_" + counter.to_s
            if !Dir.exist?(@@TEST_EXECUTION_DIR) then
                Dir.mkdir(@@TEST_EXECUTION_DIR)
                puts "#{@@DEBUG_STRING} the mcmc sampling dir is created at #{@@TEST_EXECUTION_DIR}"
            end
        end
    end
    
    def CONF.read_config (file)
        # read line by line
        IO.foreach (file){ |line|
            # is it a "key=value" pair?
            if line.include?("=") then
                res = line.split("=")
                entry_name = res[0].strip
                if entry_name.eql?("EMMA_COVERAGE_RESULT_FILE_NAME") then
                    @@EMMA_COVERAGE_RESULT_FILE_NAME = res[1].strip
                    print "#{@@DEBUG_STRING} EMMA_COVERAGE_RESULT_FILE_NAME = "
                    puts @@EMMA_COVERAGE_RESULT_FILE_NAME
                elsif entry_name.eql?("MAX_TEST_SUITE_SIZE") then
                    @@MAX_TEST_SUITE_SIZE = res[1].strip
                    print "#{@@DEBUG_STRING} MAX_TEST_SUITE_SIZE = "
                    puts @@MAX_TEST_SUITE_SIZE
                elsif entry_name.eql?("MAX_FSM_BUILDING_EVENTS") then
                    @@MAX_FSM_BUILDING_EVENTS = res[1].strip
                    print "#{@@DEBUG_STRING} MAX_FSM_BUILDING_EVENTS = "
                    puts @@MAX_FSM_BUILDING_EVENTS
                elsif entry_name.eql?("MAIN_TEST_DIRECTORY") then
                    @@MAIN_TEST_DIR = res[1].strip
                    print "#{@@DEBUG_STRING} MAIN_TEST_DIR = "
                    puts @@MAIN_TEST_DIR
                elsif entry_name.eql?("TROYD_APK_FILE") then
                    @@TROYD_APK_FILE = res[1].strip
                    print "#{@@DEBUG_STRING} TROYD_APK_FILE = "
                    puts @@TROYD_APK_FILE
                else
                    puts "#{@@DEBUG_STRING} I: undefined configuration entry - #{entry_name}"
                end
            else
            # it may be a comment line or an empty line
            # do nothing
            end
        }
    end
    
    def CONF.get_instrumented_apk ()
        @@TARGET_INSTRUMENTED_APK_FILE
    end
    
    def CONF.get_em_coverage_file ()
        @@EMMA_COVERAGE_EM_FILE
    end
    
    def CONF.get_coverage_result_file_name ()
        @@EMMA_COVERAGE_RESULT_FILE_NAME
    end
    
    def CONF.get_test_suite_size ()
        # convert string to integer
        @@MAX_TEST_SUITE_SIZE.to_i
    end
    
    def CONF.get_max_fsm_building_events ()
        # convert string to integer
        @@MAX_FSM_BUILDING_EVENTS.to_i
    end
    
    def CONF.get_fsm_building_dir ()
        @@FSM_BUILDING_DIR
    end
    
    def CONF.get_test_execution_dir ()
        @@TEST_EXECUTION_DIR
    end
    
    def CONF.get_troyd_apk ()
        @@TROYD_APK_FILE
    end
end