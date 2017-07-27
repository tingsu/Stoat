## Copyright (c) 2014-2015
##  Ting Su <tsuletgo@gmail.com>
## All rights reserved.

# parse an action string
# @return action_id, action_cmd, view_type
def parseActionString(action_string)
    loc = action_string.index("@")
    loc_type=action_string.rindex(":")
    loc_scope=action_string.rindex(")")    
    action_id = action_string[0..loc-1]
    if loc_type!=nil && loc_scope!=nil && loc_scope<loc_type
        action_cmd = action_string[loc+1..loc_type-1]
        view_string = action_string[loc_type+1..action_string.length]
        loc_view_text=view_string.index("@")
        if loc_view_text !=nil
            view_type=view_string[0..loc_view_text-1]
            view_text=view_string[loc_view_text+1..view_string.length]           
        else
            view_type=view_string
            view_text=""
        end
    else
        action_cmd = action_string[loc+1..action_string.length]
        view_type =""
        view_text=""
    end

    return action_id, action_cmd, view_type, view_text
end


# actions: action_id@action_cmd
class Action
    @executed_times
    @action_id
    @action_cmd
    @action_string
    @action_activity
    #add by buka
    @viewType
    # children array store the action ids of this action
    @ChildrenViewIdArray
    @weight

    # constructor
    # @param $action_string the action string in the form of
    def initialize(action_string, action_activity)
        id, cmd, vtype = parseActionString(action_string)
        @action_id = id
        @action_cmd = cmd
        @action_string = action_string
        @executed_times = 0
        @action_activity = action_activity
        #add by buka
        @viewType=vtype
        @ChildrenViewIdArray=Array.new
        @weight=vtype.downcase=="textview"?1:2
    end
    
    # increase the execution times of an action
    def incr_execution_times()
        @executed_times += 1
    end
    
    # @return action execution times
    def get_execution_times()
        @executed_times
    end
    
    # @return action id
    def get_action_id()
        @action_id
    end
    
    # @return action cmd
    def get_action_cmd()
        @action_cmd
    end
    
    def get_action_activity()
        @action_activity
    end

    #add by buka
    def get_view_type()
        @viewType
    end

    def get_children_view_id_array()
        @ChildrenViewIdArray
    end

    def add_child(child_id)
        @ChildrenViewIdArray.push(child_id)
        @ChildrenViewIdArray=@ChildrenViewIdArray&@ChildrenViewIdArray #insure that each item is unique in this array
    end

    def get_action_weight()
        @weight
    end

    def set_action_weight(weight)
        @weight=weight
    end
    
    def set_execution_time(execution_times)
        @executed_times=execution_times
    end
    
    # @return the action string
    def toString()
        @action_string
    end
end

#require 'test/unit'

class ActionPicker # < Test::Unit::TestCase
    
    # some basic selection strategies:
    # 1. random
    #      Pure randomly pick an action.
    # 2. leastfirst
    #      Pick the least frequently executed actions in the past ripping history.
    #      Here the execution frequency is computed from the action itself.
    # 3. leastfirstwithsummary  (TODO)
    #      Pick the least frequently executed actions.
    #      Here the execution frequency of an action is the summary of all its children actions.
    #
    @selection_strategy
    @@random_picking = "random"
    @@leastfirst_picking = "leastfirst"
    @@leastfirst_summary_picking = "leastfirstwithsummary"
    @@bias_random = "biasrandom"
    #add by buka
    @@weighted_search = "weighted"
    @last_action_id   # for advanced bias random
    @last_executable_action_ids  # for advanced bias random

    # the executed actions
    # stored in a hash <action_id, action>
    @@actions
    
    # constructor
    def initialize()
        puts "Action Picker is initiliazed [with default search strategy: #{@@random_picking} ]... "
        @selection_strategy = @@random_picking
        @@actions = Hash.new
        @last_executable_action_ids=Array.new
    end
    
    # set the search strategy
    def setStrategy(strategy)
        if !strategy.eql?(@@random_picking) && !strategy.eql?(@@leastfirst_picking) &&
            !strategy.eql?(@@leastfirst_summary_picking) &&
            !strategy.eql?(@@bias_random) &&
             !strategy.eql?(@@weighted_search) then
            puts "invalid search strategy!"
            exit
        end
        puts "the current search strategy is set as [ #{strategy} ] "
        @selection_strategy = strategy
    end
    # @return string
    def getStrategy()
        @selection_strategy
    end

    
    # put an action into the global action list
    # @param $action_string the action in the string format
    #        $action_activity the activity which the action belongs to
    def putAction(action_string, action_activity)
        # puts "put action: #{action_string}"
        # get the action id and cmd
        action_id, action_cmd = parseActionString(action_string)
        # find the action by id
        act = getActionById(action_id)
        puts act
        if act.eql?(nil)
            act = Action.new(action_string, action_activity)
            @@actions.store(action_id,act)
        end
        
    end
    
    # put actions
    # @param $action_strings actions in the string format
    #        $action_activity the activity which the action belongs to
    def putActions(action_strings, action_activity)
        action_strings.each { |act_string|
                putAction(act_string, action_activity)
        }
        puts "the current number of actions: #{@@actions.length}."
    end
    
    # get the action by id
    def getActionById(action_id)
        @@actions[action_id]
    end
    
    # update the execution times of an action
    def updateActionExecutionTimes(action_string)
        # get the action id and cmd
        action_id, action_cmd = parseActionString(action_string)
        # find the action by id
        act = getActionById(action_id)
        #assert(!act.eql?(nil), "failed to update an non-exisiting action")
        # increase the execution times
        act.incr_execution_times
        
    end
    
    # dump all actions
    def dumpExecutedActions()
        puts "\n---[all actions list]-------"
        puts "action_id@action_cmd action_activity <executed_times> <view_type> <weight> <children num>"
        @@actions.each { |key,value| 
           valueString=value.toString[0..value.toString.length-2]
           puts "#{valueString} <#{value.get_action_activity}> <#{value.get_execution_times}> <#{value.get_view_type}> <#{value.get_action_weight}> <#{value.get_children_view_id_array.size}>" 
        }
        puts "total #{@@actions.length} executable actions."
        puts "----------\n"
    end
    
#    # are all executable actions in the current activity executed at least once ?
#    def areAllActionsExecuted(executable_actions)
#        act_ids = Array.new
#        act_execution_times = Array.new
#        executable_actions.each {|act|
#            act_id, act_cmd = parseActionString(act)
#            act_ids.push(act_id)
#            act = @@actions[act_id]
#            act_times = act.get_execution_times
#            act_execution_times.push(act_times)
#        }
#        
#        least_times, index = act_execution_times.each_with_index.min
#        puts "I: the least executed times: #{least_times}"
#        
#        if not least_times.eql? (0) then
#           # If all actions are executed at least once, we will try to trace back
#           puts "I: all actions are executed at least once, try to press \"back\". "
#           # we will press back with 0.5 probability
#           ret_random = Random.rand(2)
#           if ret_random.eql? (0) then
#              true
#           else
#              false
#           end
#        else
#            false
#        end
#
#    end

    # select the next action to execute
    # @param $actions the currently invokable action ids
    # @return the selected action id
    def selectNextAction(executable_actions)
        if @selection_strategy.eql?(@@leastfirst_picking)
            leastFirst(executable_actions)
        elsif @selection_strategy.eql?(@@random_picking)
            random(executable_actions)
        elsif @selection_strategy.eql?(@@bias_random)
            biasrandom(executable_actions)
        elsif @selection_strategy.eql?(@@weighted_search)
            advancedBiasRandom (executable_actions)
        end
    end
    
    # pick the least frequently executed actions
    # @return the selected action cmd
    def leastFirst(executable_actions)
        puts "start least frequently picking ... "
        act_ids = Array.new
        act_execution_times = Array.new
        executable_actions.each {|act|
            act_id, act_cmd = parseActionString(act)
            act_ids.push(act_id)
            act = @@actions[act_id]
            act_times = act.get_execution_times
            act_execution_times.push(act_times)
        }
        
        least_times, index = act_execution_times.each_with_index.min
        least_act = executable_actions [ index ]
        
        puts "the least executed action: "
        puts "#{least_act}"
        
        # get action string
        least_act
        
    end

    
    # select a ui event randomly from a group of least frequently executed events.
    # this strategy is kind of combination of the random & leastfirst strategies
    def biasrandom (executable_actions)
        puts "I: start bias random picking ... "
        act_ids = Array.new
        act_execution_times = Array.new
        executable_actions.each {|act|
            act_id, act_cmd = parseActionString(act)
            act_ids.push(act_id)
            act = @@actions[act_id]
            act_times = act.get_execution_times
            act_execution_times.push(act_times)
        }
        
        least_times, index = act_execution_times.each_with_index.min
        puts "I: the least executed times: #{least_times}"

        # target action ids: the actions have least executed times
        target_act_ids = Array.new
        executable_actions.each_with_index {|act, index|
            act_id, act_cmd = parseActionString(act)
            act = @@actions[act_id]
            act_times = act.get_execution_times
            # collect the action whose id is equal to the least executed times
            if act_times == least_times then
                puts "the qualified action: #{act.get_action_cmd}"
                target_act_ids.push(act_id)
            end
        }
        puts ""
        # randomly select an action
        length = target_act_ids.size
        puts "I: there are total #{length} actions who have the same least executed times"
        
        while true do
            rint = Random.rand(length)
            random_id = target_act_ids[ rint ]
            puts "I: random id: #{random_id}, from [0, #{length})"
            bias_act = @@actions[ random_id ]
            puts "I: the selected bais action: #{bias_act.toString}"
            # make sure the selected action is invokable
            if executable_actions.include?(bias_act.toString) then
                puts "I: the selected bais action is in executable actions."
                break
            end
        end
        
        bias_act_string = bias_act.toString
        
        puts "the selected biasrandom executed action: "
        puts "#{bias_act_string}"
        
        # get action string
        bias_act_string
    end
    
    # pure randomly pick an action
    # @return the selected action cmd
    def random(executable_actions)
        puts "start random picking ... "
        length = executable_actions.length
        # generate a random number from [0, length),
        # i.e., 0 is inclusive, length is exclusive
        rand_index = Random.rand(length)
        puts "index: #{rand_index}"
        act = executable_actions[ rand_index ]
        
        puts "the random action:"
        puts "#{act}"
        
        return act
    end


    # update actions weight, used in advanced bias random
    def updateActionsWeight()
        #=================================================        
        # update all the weights in actions
        puts "[D] update action weight"
        @@actions.each { |key,act|
            # get execution time, when it is zero, set it to 0.5
            excutionTimePara=act.get_execution_times==0?0.1:act.get_execution_times
            # get weight of view type
            if act.get_view_type.downcase=="textview" then
                # Now, we do not need to differentiate TextViews from others, since we use UI xml files to parse actions,
                # which will not include non-executable TextViews
                # but we still have to give it lower weight
                viewTypeWeight = 1.0  
            else
                viewTypeWeight = 1.0
            end
            
            # get weight of children
            childrenWeight=0;
            act.get_children_view_id_array.each {|act_cid|
               if @@actions[act_cid].get_execution_times==0
                  childrenWeight+=1
               end
            }
            
            act_weight = 0
            if act.get_action_cmd.eql?("menu\n") then  
              # Give "menu" higher weight
              act_weight = (viewTypeWeight + childrenWeight/1.5) / (1.0*excutionTimePara)
            else 
              # calculate act_weight where Weight = [ ViewType (if viewType == TextView then 0.5 else 1) + 未被执行过的子控件的个数 (num) ] / Execution_times
              act_weight = (viewTypeWeight + childrenWeight/3.0) / (1.5*excutionTimePara)
            end
            # act_weight = (viewTypeWeight + childrenWeight/2) * (Math::E**(-1*excutionTimePara))
            act.set_action_weight(act_weight)
        }
    end


    # select a ui event randomly from a group of executed events with largest weight.
    # this strategy is kind of combination of the random & weight & leastfirst strategies
    def advancedBiasRandom (executable_actions)
        puts "I: start advanced bias random picking ... "
        #=================================================
        # add actions to its parents as children
        if @last_action_id!=nil && @last_executable_action_ids.size>0
           tmp_ids=Array.new
           executable_actions.each {|act|
             act_id, act_cmd = parseActionString(act)
             tmp_ids.push(act_id)
           }

           # add children
           result_array=tmp_ids-@last_executable_action_ids
           if result_array.size > 0
              result_array.each {|act_id|
                 act = @@actions[@last_action_id]
                 # "back", "keyevent_back", and "reset" will not add any children
                 if act.get_action_cmd!="back\n" && act.get_action_cmd!="keyevent_back\n" && act.get_action_cmd!="reset" && act.get_action_cmd!="scroll(direction='up')" && act.get_action_cmd!="scroll(direction='down')" then
#                    puts " ------------- action : #{act.get_action_cmd} !!!! ------------- "
#                    puts "put #{act_id} "
                     act.add_child(act_id)
                 end
                 
              }
           end
        end
        #=================================================        
        # get all the weights in actions
        act_ids = Array.new
        act_execution_weights = Array.new

        executable_actions.each {|act|
            act_id, act_cmd = parseActionString(act)
            act_ids.push(act_id)
            act = @@actions[act_id]
            
            if @last_action_id != nil then
              last_act = @@actions[@last_action_id] 
              # if the last action is "menu", decrease the chance to invoke "back"
              if last_act.get_action_cmd.eql?("menu\n") then 
                if act.get_action_cmd.eql?("back\n") then
                  act.set_action_weight(act.get_action_weight()/10.0)
                end
              end
            end
            
            # when an action is executed last time, its weight should be turn down to avoid being executed continuous twice
            if act_id == @last_action_id then
                # special handle on "back" action when it is shared between two different screens but in the same activity
                # because its weight will be reduced in one screen, as a result, it lost its execution chance in another screen
                if act.get_action_cmd.eql?("back\n") then
#                    act_execution_time=act.get_execution_times()
#                    act.set_execution_time(act_execution_time-1)
#                    new_execution_time=act.get_execution_times()==0?0.5:act.get_execution_times()
#                    act.set_action_weight(act.get_action_weight*act_execution_time/(new_execution_time*1.0))
                    act.set_action_weight(act.get_action_weight()/3.0)
                elsif act.get_action_cmd.eql?("scroll(direction='up')") || act.get_action_cmd.eql?("scroll(direction='down')") then
                    puts "[D] the second \"up\" or \"down\", decrease its weight ... " 
                    act.set_action_weight(act.get_action_weight()/4.0)
                else
                    # decrease the execution prob. when the same action appears again
                    act.set_action_weight(act.get_action_weight()/2.0)
                end
            end
            act_weight =act.get_action_weight()
            act_execution_weights.push(act_weight)    
        }
        
        #=================================================
        # get the largest weight
        largest_weight, index = act_execution_weights.each_with_index.max
        puts "I: the largest weight: #{largest_weight}"
        
        # fix a bug: when largest weight is equal to zero
        if largest_weight == 0.0 then
            puts "E: the largest weigth is zero, quit!!!"
            exit
        end
        
        #=================================================
        # target action ids: the actions have largest weight
        target_act_ids = Array.new
        executable_actions.each_with_index {|act, index|
            act_id, act_cmd = parseActionString(act)
            act = @@actions[act_id]
            act_weight = act.get_action_weight
            # collect the action whose id is equal to the largest weight
            if act_weight == largest_weight then
                    puts "the qualified action: #{act.get_action_cmd}"
                    target_act_ids.push(act_id)
            end
        }
        puts ""

        #=================================================
        # randomly select an action
        length = target_act_ids.size
        puts "I: there are total #{length} actions who have the same weight"
        
        random_id=0
        while true do
            rint = Random.rand(length)
            random_id = target_act_ids[ rint ]
            puts "I: random id: #{random_id}, from [0, #{length})"
            advanced_bias_act = @@actions[ random_id ]
            puts "I: the selected advanced bais action: #{advanced_bias_act.toString}"
            # make sure the selected action is invokable
            if executable_actions.include?(advanced_bias_act.toString) then
                puts "I: the selected advanced bais action is in executable actions."
                break
            end
        end

        #=================================================
        # get action string
        advanced_bias_act_string = advanced_bias_act.toString
        puts "the selected weighted executed action: "
        puts "#{advanced_bias_act_string}"


        #=================================================  
        # update last_executable_action_ids and last_action_id to current
        @last_executable_action_ids.clear
        executable_actions.each {|act|
           act_id, act_cmd = parseActionString(act)
           @last_executable_action_ids.push(act_id)
        }
        @last_action_id=random_id

        #=================================================        
        # return action string
        advanced_bias_act_string

    end
    
end
