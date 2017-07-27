def testHash()
    # create a hash
    myHash = Hash.new
    # set the default value
    # myHash.default = "hello"
    # get a value by an non-existing key
    nilValue = myHash["hello"]
    puts nilValue
    nilValue = myHash[0]
    puts nilValue
    # store key-value pairs
    myHash.store("hello", "world")
    myHash.store(0,"int")
    # fetch values
    puts myHash["hello"]
    puts myHash[0]
    # iterate the hash
    myHash.each { |key,value| puts "#{key} is #{value}"  }
end

def testPassByReference(num)
    puts "num: #{num.object_id}"
    # after num is changed to 6,
    # it is actually pointed to another object
    num = 6
    puts "num: #{num.object_id}"
end

def testBlock(nums)
    # mulitple statement in the block
    nums.each { |num|
            puts "e: #{num}"
            puts "e+1: #{num+1}"
        }
    min = nums.min
    puts "min: #{min}"
    puts "min,index: #{nums.each_with_index.min}"
end


def testDriver()
    puts "[test] this is the test driver"
    
    puts "[test] this is a test about hash"
    testHash()
    
    puts "[test] test pass by reference"
    n = 5
    puts "n: #{n.object_id}"
    testPassByReference(n)
    puts n
    
    puts "[test] test block"
    nums = Array.[](1,2,1,4)
    testBlock(nums)
    
end

testDriver