require_relative 'emulator'

# reference links
# adb devices: http://developer.android.com/tools/help/adb.html
# how to shut down an emulator: http://stackoverflow.com/questions/20155376/android-stop-emulator-from-command-line
# manipulate emulators: https://devmaze.wordpress.com/2011/12/12/starting-and-stopping-android-emulators/

module ExceptionHandler
    
    # the debug string
    @@DEBUG_STRING = "[ExceptionHandler] "
    
    ###################
    
    ONLINE = "ONLINE"
    OFFLINE = "OFFLINE"
    
    EMULATOR_OK = "emulator-ok"
    EMULATOR_FAIL = "emulator-fail"
    
    # check the emulator state: online or offline
    def ExceptionHandler.check_emulator_state (emulator_serial)
        avd_state = EMULATOR.query_avd_state(emulator_serial)
        puts "#{@@DEBUG_STRING} D: avd_state: \"#{avd_state}\""
        if avd_state.eql?("device")
            puts "#{@@DEBUG_STRING} I: the emulator is online. "
            ONLINE
            else
            puts "#{@@DEBUG_STRING} I: the emulator is offline. "
            OFFLINE
        end
    end
    
    # shut down the emulator
    def ExceptionHandler.shutdown_emulator (emulator_serial)
        puts "#{@@DEBUG_STRING} I: shut down the emulator: #{emulator_serial}"
        shutdown_cmd = "adb -s #{emulator_serial} emu kill"
        puts "$#{shutdown_cmd}"
        `shutdown_cmd`
    end
    
    # restart the emulator
    def ExceptionHandler.restart_emulator (emulator_name)
        puts "#{@@DEBUG_STRING} I: restart the emulator: #{emulator_name}"
        restart_cmd = "emulator -avd #{emulator_name}"
        puts "$#{restart_cmd}"
        `restart_cmd`
        sleep 30
    end
    
    # recreate the emulator
    def ExceptionHandler.recreate_emulator (emulator_name)
        puts "#{@@DEBUG_STRING} I: remove the orginal emulator: #{emulator_name} and recreate it"
        rm_emulator_cmd = "rm -rf ~/.android/avd/#{emulator_name}.*"
        puts "$#{rm_emulator_cmd}"
        `rm_emulator_cmd`
        sleep 2
        create_emulator_cmd = "android create avd -n #{emulator_name} -t android-16 -s HVGA -c 128M --abi armeabi-v7a -d \"Nexus 7\""
        puts "$#{create_emulator_cmd}"
        `create_emulator_cmd`
        sleep 2
    end
    
    # check emulator
    # @exception_time: the number of continuous exceptions
    def ExceptionHandler.check_emulator (emulator_serial,emulator_name,exception_time)
        puts "#{@@DEBUG_STRING} I: check the status of emulator: #{emulator_serial}"
        res = check_emulator_state(emulator_serial)
        if res.eql?(ONLINE) && (exception_time<5) then
            # do nothing
            puts "#{@@DEBUG_STRING} I: the emulator is fine"
            EMULATOR_OK
        else
            # If we encounter continuously more than 5 exceptions, we assume the emulator ran into an non-responding state, we will re-create and re-start the emulator.
            puts "#{@@DEBUG_STRING} I: the emulator has trouble"
            EMULATOR_FAIL
        end
    end
    
    # reset the emulator (shutdown, recreate, restart)
    def ExceptionHandler.reset_emulator(emulator_serial, emulator_name)
        shutdown_emulator(emulator_serial)
        recreate_emulator(emulator_name)
        restart_emulator(emulator_name)
    end
    
    
    #################
    
    
    # restart the app
    def ExceptionHandler.restart_app (apk)
        puts "#{@@DEBUG_STRING} I: restart the app"
        entry_activity = AAPT.launcher apk
        puts "#{@@DEBUG_STRING} I: the entry activity: #{entry_activity}"
    end
    
    
    
end


# ExceptionHandler.check_emulator("emulator-5554")