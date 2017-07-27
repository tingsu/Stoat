require_relative 'avd'

# reference links
# adb devices: http://developer.android.com/tools/help/adb.html
# how to shut down an emulator: http://stackoverflow.com/questions/20155376/android-stop-emulator-from-command-line
# manipulate emulators: https://devmaze.wordpress.com/2011/12/12/starting-and-stopping-android-emulators/

class ExceptionHandler

# the debug string
    @@DEBUG_STRING = "ExceptionHandler"
    
    ###################
    
    # NOTE: module constants should be accessed by "ExceptionHandler::ONLINE"
    @@ONLINE = "ONLINE"
    @@OFFLINE = "OFFLINE"
    
    @@EMULATOR_OK = "emulator-ok"
    @@EMULATOR_FAIL = "emulator-fail"
    @@EMULATOR_STOP = "emulator-giveup"
    
    def initialize (avd)
        @avd = avd
    end
    
    def get_emulator_ok
        @@EMULATOR_OK
    end
    
    def get_emulator_fail
        @@EMULATOR_FAIL
    end
    
    # check the emulator state: online or offline
    def check_emulator_state (emulator_serial)
        avd_state = @avd.query_avd_state(emulator_serial)
        puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] D: avd_state: \"#{avd_state}\""
        if avd_state.eql?("device")
            puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: the emulator is online. "
            @@ONLINE
        elsif avd_state.eql?("offline")
            puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: the emulator is offline. "
            @@OFFLINE
        else
            puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: you are using real devices? "
            @@ONLINE
        end
    end
    
    # shut down the emulator
    def shutdown_emulator (emulator_serial)
        puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: shut down the emulator: #{emulator_serial}"
        shutdown_cmd = "adb -s #{emulator_serial} emu kill"
        puts "$#{shutdown_cmd}"
        `#{shutdown_cmd}`
    end
    
    # restart the emulator
    def restart_emulator (emulator_name)
       puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: restart the emulator: #{emulator_name}"
       restart_cmd = "emulator -avd #{emulator_name}"
       puts "$#{restart_cmd}"
       `#{restart_cmd}`
       sleep 30
    end
    
    # recreate the emulator
    def recreate_emulator (emulator_name)
        puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: remove the orginal emulator: #{emulator_name} and recreate it"
        rm_emulator_cmd = "android delete avd -n #{emulator_name}"
        puts "$#{rm_emulator_cmd}"
        `#{rm_emulator_cmd}`
        sleep 2
        create_emulator_cmd = "android create avd -n #{emulator_name} -t android-16 -s HVGA -c 128M --abi armeabi-v7a -d \"Nexus 7\""
        puts "$#{create_emulator_cmd}"
        `#{create_emulator_cmd}`
        sleep 2
    end
    
    # check emulator
    # @exception_time: the number of continuous exceptions
    def check_emulator (emulator_serial,emulator_name,exception_time=0)
        puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: check the status of emulator: #{emulator_name}"
        res = check_emulator_state(emulator_serial)
        if res.eql?(@@ONLINE) && (exception_time<2) then
            # do nothing
            puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: the emulator is fine"
            @@EMULATOR_OK
        elsif res.eql?(@@ONLINE) && (exception_time>=2) then
            puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: the emulator is fine"
            @@EMULATOR_STOP
        else
            # If we encounter continuously more than 5 exceptions, we assume the emulator ran into an non-responding state, we will re-create and re-start the emulator.
            puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: the emulator has trouble, #{@@EMULATOR_FAIL}"
            @@EMULATOR_FAIL
        end
    end
    
    # reset the emulator (shutdown, recreate, restart)
    def reset_emulator(emulator_serial, emulator_name)
        shutdown_emulator(emulator_serial)
        recreate_emulator(emulator_name)
        restart_emulator(emulator_name)
    end
    
#    def reinstall_app (apk)
#        
#        puts "#{@@DEBUG_STRING} I: the apk file: #{apk}"
#        
#        # rebuild and install troyd
#        pkg = AAPT.pkg apk
#        Troyd.setenv
#        Troyd.rebuild pkg
#        
#        puts "#{@@DEBUG_STRING} I: the app package name: #{pkg}"
#        
#        # resign and install the target app
#        ADB.uninstall pkg
#        shareduid = pkg + ".shareduid.apk"
#        Uid.change_uid(apk, shareduid)
#        resigned = pkg + ".resigned.apk"
#        Resign.resign(shareduid, resigned)
#        # system("rm -f #{shareduid}")
#        ADB.install resigned
#        
#        system("mv #{resigned} #{APKS}/#{pkg}.apk")
#    end

    
    #################
    
    
    # restart the app
    def restart_app (adb, aapt, apk)
        puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: restart the app"
        entry_activity = aapt.launcher apk
        puts "[#{@@DEBUG_STRING}-#{@avd.get_avd_name}] I: the entry activity: #{entry_activity}"
        adb.ignite entry_activity
    end
    
    
end


#res = ExceptionHandler.check_emulator("emulator-5554", "testAVD_1", 3 )
#puts "#{res}"
#if res.eql?("ok") then
#    puts "#{res}"
#end
