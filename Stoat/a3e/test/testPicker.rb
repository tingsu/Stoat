require "../bin/picker"

$action_file_name = "actions.txt"

def testPicker()
    puts "[test] this is a test for picker"
    actions = IO.readlines($action_file_name)
    actions.each { |act|
            puts "#{act}"
        }
    
    puts "[test] test the random selection strategy... "
    
    picker = ActionPicker.new()
    picker.putActions(actions)
    picker.dumpExecutedActions
    
    puts "--------------"
    
    executed_act = "2@clickImgView(1)"
    puts "the action: #{executed_act} is executed."
    picker.updateActionExecutionTimes(executed_act)
    picker.dumpExecutedActions
    
    puts "--------------"
    
    for i in 0..5
        sleep 1
        act = picker.selectNextAction(actions)
        picker.updateActionExecutionTimes(act)
        
        picker.dumpExecutedActions
    end
    
    puts "--------------"
    
    puts "[test] another round for actions"
    picker.putActions(actions)
    picker.dumpExecutedActions
    
    puts "--------------"
    
    executed_act = "2@clickImgView(1)"
    puts "the action: #{executed_act} is executed."
    picker.updateActionExecutionTimes(executed_act)
    picker.dumpExecutedActions
    
    
    puts "--------------"

    puts "[test] test the least first strategy... "
    
    picker2 = ActionPicker.new()
    picker2.setStrategy("leastfirst")
    picker2.putActions(actions)
    picker2.dumpExecutedActions
    
    puts "--------------"
    
    executed_act = "2@clickImgView(1)"
    puts "the action: #{executed_act} is executed."
    picker2.updateActionExecutionTimes(executed_act)
    picker2.dumpExecutedActions
    
    puts "--------------"
    
    for i in 0..5
        sleep 1
        # test with subarray [0..2]
        act = picker2.selectNextAction(actions[0..2])
        picker2.updateActionExecutionTimes(act)
        
        picker2.dumpExecutedActions
    end

    
end


testPicker
