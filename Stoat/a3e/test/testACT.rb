require "../bin/act"

puts "this is a test for ACT module"
action_file_name = "actions.txt"

# test the action cmd extraction
action_cmds = ACT.extract_act(action_file_name)
for cmd in action_cmds
    puts cmd
end