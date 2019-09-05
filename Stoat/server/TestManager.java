import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;


/** test manager:
 * 1. generate test cases from the Markov Chain model
 * 		need to specify:
 * 		1) the number of test cases
 * 		2) the length of one test case
 * 		3) stop criteria: full states/arcs coverage?
 * 2. execute test cases against the app under test
 * 3. monitor the coverage information from test executions 
 */
public class TestManager{
	
	
	public static final String debugFlag = "[TestManager] ";
	
	/********************************************************/
	
	/** the generated test suite is composed of a set of test cases;
	 * 	and a test case is composed of a set of actions
	 */
	public List<ArrayList<String>> testSuite;
	public List<ArrayList<Integer>> testSuiteCmdsIds;
	
	/**
	 * the max test suite size in test generation
	 * TODO set temporarily, this should be specified by the user according to the app size, testing budgets, computing resources
	 */
	public static int maxTestSuiteSize;
	
	/** the actual test suite size in test generation */
	public static int actualTestSuiteSize;
	/**
	 * the max test case length in test generation
	 * TODO set temporarily, the same as the above
	 */
	public static int maxTestCaseLength;
	
	/********************************************************/
	
	/**
	 * the maximum size of android emulators
	 * TODO set temporarily, but it should be specified by the user according to the app size, testing budgets, computing resources
	 */
	public static int emulatorsPoolSize;
	/**
	 * the emulators pool which stores their names
	 * TODO the emulator name can be semi-randomly generated according to the specified emulators pool size,
	 * 		e.g., if size = 5, then emulators' names are: testAVD1, testAVD2, ..., testAVD5
	 * 		their corresponding coverage files' names are: testAVD1_appName_testcaseID_coverage.ec
	 * 		And then the server merges these coverage files to get the total coverage achieved by these test cases
	 */
	public List<String> emulatorsPool;
	
	/********************************************************/
	
	/** the emulator controller which is used by the test manager */
	private EmulatorController controller;
	
	/**
	 * the standard test data generation according to the transition probabilities:
	 * the higher the transition probability is, the more likely the transition will be picked.
	 */
	public static final int HIGH_PROBAB_TEST_GENERATION = 0;
	
	/**
	 * the lower the transition probability is, the more likely the transition will be picked.
	 */
	public static final int LOW_PROBAB_TEST_GENERATION = 1;
	
	
	
	/** the instance of TestManger */
	private static TestManager manager = null;
	private TestManager(){
		//init the test suite
		testSuite = new ArrayList<ArrayList<String>>();
		testSuiteCmdsIds = new ArrayList<ArrayList<Integer>>();
	}
	/** get the test manager instance 
	 * @return TestManager
	 * */
	public static TestManager v(){
		if(manager == null){
			manager = new TestManager();
		}
		return manager;
	}
	
	/**
	 * initialize the configurations of TestManager
	 */
	public void initConfiguration(AndroidAppFSM fsm){
		maxTestSuiteSize = ConfigOptions.MAX_TEST_SUITE_SIZE;
		//maxTestSuiteSize = fsm.getUniqueStatesCount();
		
		//limit the maximum test length 
		maxTestCaseLength = (int)Math.sqrt(fsm.getUniqueTransitionsCount()*1.0);
		if(maxTestCaseLength > ConfigOptions.MAX_TEST_SEQUENCE_LENGTH){
			maxTestCaseLength = ConfigOptions.MAX_TEST_SEQUENCE_LENGTH;
		}
		emulatorsPoolSize = 1;
		System.out.println(debugFlag + "I: the test manager configuration< emulators pool size: " + 
							emulatorsPoolSize + ", maxTestSuiteSize: " + maxTestSuiteSize + 
							", maxTestCaseLength: " + maxTestCaseLength + " >");
		//start the test manager's emulator controller
		startEmulatorController("MCMC-droid-Controller");
	}
	
	/**
	 * start the emulator controller
	 * @return
	 */
	public void startEmulatorController(String controllerName){
		System.out.println(debugFlag + "I: start the emulator controller ... ");
		if(controller == null){
			controller = new EmulatorController(controllerName);
		}
		//start the controller
		controller.start();
	}
	
	/**
	 * pick an action according to its transition probability: 
	 * the higher the probability is, the more likely it will be picked
	 * @param triples
	 * @return the picked transition id
	 */
	private int pickTransitionWithProb(List<GibbsTriple> triples){
        int transitionID = -1;
        //generate a random number in [0,1)
		double randomDouble = Math.random();
		int cnt = triples.size();
		double k = 0.0;
		for(int i=0; i<cnt; i++){
			GibbsTriple triple = triples.get(i);
			k += triple.getTransitionProb();
			if(randomDouble <= k){
				transitionID = triple.getTransitionID();
				triple.incrSelectionTimes();
				break;
			}
		}
		//when an app state do not have transitions, transitionID will be set -1
		return transitionID;
	}
	
	private int pickTransitionWithoutProbButBound(List<GibbsTriple> triples){
		int transitionID = -1;
		int cnt = triples.size();
		
		if (cnt == 0) //there is no transitions, return -1
			return -1;
		
		//select candidate triples which should satisfy "selection_time<=3"
		List<GibbsTriple> candidates = new ArrayList<GibbsTriple>();
		for(int i=0; i<cnt; i++){
			GibbsTriple triple = triples.get(i);
			if(triple.getSelectionTimes() < 3){
				candidates.add(triple);
			}
		}
		
		if(candidates.size() == 0){ //if for all of triples, selection times == 3
			//clear their selection times
			for(int i=0; i<cnt; i++){
				GibbsTriple triple = triples.get(i);
				triple.clearSelectionTimes();
				candidates.add(triple);
			}
		}
		
		int candidates_cnt = candidates.size();
		assert(candidates_cnt > 0 );
		
		Random rand = new Random();
		int randomIndex = Math.abs(rand.nextInt()) % candidates_cnt; //randomly choose an transition
		transitionID = candidates.get(randomIndex).getTransitionID();
		
		//when an app state do not have transitions, transitionID will be set -1
		return transitionID;
	}
	
	
	/**
	 * pick transition with probability and selection bound 
	 * */
	private int pickTransitionWithProbAndBound(List<GibbsTriple> triples, int selectionMaxTimes){
		int transitionID = -1;
		int cnt = triples.size();
		if(cnt==0){ //if do not have transitions, return -1
			return transitionID;
		}
		
		//select candidate triples which should satisfy "selection_time<=selectionMaxTimes"
		List<GibbsTriple> candidates = new ArrayList<GibbsTriple>();
		for(int i=0; i<cnt; i++){
			GibbsTriple triple = triples.get(i);
			if(triple.getSelectionTimes() < selectionMaxTimes){
				candidates.add(triple);
			}
		}
		
		if(candidates.size() == 0){ //if for all of triples, selection times == selectionMaxTimes
			//clear their selection times
			for(int i=0; i<cnt; i++){
				GibbsTriple triple = triples.get(i);
				triple.clearSelectionTimes();
				candidates.add(triple);
			}
		}
		
		int candidates_cnt = candidates.size();
		
		double total_prob = 0.0;
		for(int i=0; i<candidates_cnt; i++)
			total_prob += candidates.get(i).getTransitionProb();
		double randomDouble;
		if(ConfigOptions.HIGH_PROB_TEST_GENERATIN){
			//scale the random value, prioritize higher probability when selecting transitions
			randomDouble = Math.random() * total_prob; 
		}else{
			//scale the random value, reverse the probability value to prioritize lower probability when selecting transitions
			randomDouble = total_prob - Math.random() * total_prob;
		}
		
		double k = 0.0;
		for(int i=0; i<candidates_cnt; i++){
			GibbsTriple triple = candidates.get(i);
			k += triple.getTransitionProb();
			if(randomDouble <= k){
				transitionID = triple.getTransitionID();
				triple.incrSelectionTimes();
				break;
			}
		}
		
		System.out.println("selected transition id: " + transitionID);
		//when an app state do not have transitions, transitionID will be set -1
		return transitionID;
	}
	
	/**
	 * clear the selection times of all transitions in the FSM
	 *  */
	private void clearTransitionSelectionTimes(Map<String, List<GibbsTriple>> gibbsVector){
		Iterator<Entry<String, List<GibbsTriple>>> iter = gibbsVector.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,List<GibbsTriple>> pair = (Entry<String,List<GibbsTriple>>)iter.next();
			List<GibbsTriple> value = pair.getValue(); 
			for(GibbsTriple triple: value){
				triple.clearSelectionTimes();
			}
		}
	}
	
	/**
	 *  the standard Markov Chain test generation according to the transition probabilities.
	 *  */ 
	public void markovChainTestGenerationComparisonVersion(AndroidAppFSM fsm, Map<String, List<GibbsTriple>> gibbsVector){
		System.out.println(debugFlag + "I: start test generation ...");
		//TODO hard-code the initial state name
		String initialStateName = fsm.getInitialStateName();
		//assert the initial state name is not null
		assert(initialStateName != null);
		System.out.println(debugFlag + "I: the entry state name: " + initialStateName);
		
		actualTestSuiteSize = 0;
		int currentTestCaseLength = 0;
		String nextStateName;
		
		//init node state set
		initNodesStateSet();
		//init edge set
		initEdgesSet();
		
		//NOTE clear the selection times of all transitions before test generation
		clearTransitionSelectionTimes(gibbsVector);
		testSuiteCmdsIds.clear();
		
		int test_generation_iteration = 1;
		
		//generate test sequences until all nodes are covered
		while(true){  
			
			//the test sequence always starts from the entry state
			nextStateName = initialStateName;
			
			//the test case, i.e., the sequence of action commands
			ArrayList<String> actionCmds = new ArrayList<String>(); 
			ArrayList<Integer> actionCmdsIds = new ArrayList<Integer>(); 
			
			while(currentTestCaseLength < maxTestCaseLength){
				//incr. the length of test sequence
				currentTestCaseLength ++;
				
				List<GibbsTriple> triples = gibbsVector.get(nextStateName);
				//OLD
                //int transitionId = pickTransitionWithProb(triples);
				
				int transitionId = -1;
				//NEW
				if(ConfigOptions.PROB_MODEL_MODE == 1)  //pick transitions on a prob. model
					transitionId = pickTransitionWithProbAndBound(triples, 5);
				else if(ConfigOptions.PROB_MODEL_MODE == 2) //pick transitions on a plain model
					transitionId = pickTransitionWithoutProbButBound(triples);
				else{
					//impossible reach here
					System.exit(-1);
				}
				
                if(transitionId == -1){
                	System.out.println(debugFlag + "I: the test sequence ends here!");
                	break;
                }
                
                // get the selected app state
				AppState selectedState = fsm.getStateByName(nextStateName); 
				Transition tran = selectedState.getStateTransition(transitionId);
				String actionCmd = tran.getAssociatedActionCmd();
				int actionCmdId = tran.getAssociatedActionID();
				
				//update nodes state set
				updateNodesStateSet(nextStateName);
				//update edges set
				updateEdgesSet(tran.getTransitionID());
				
                //System.out.print(debugFlag + "I: select an event from state " + nextStateName + ", with transition id @" + transitionId
                //                 + ", action cmd: " + actionCmd);
				//store the action commands
				actionCmds.add(actionCmd);
				//store the action commands ids
				actionCmdsIds.add((actionCmdId));
				
				//choose the next app state
				nextStateName = tran.getEndAppStateName();
                //System.out.println(debugFlag + "I: the next state is: " + nextStateName);
			}
			
			//add the test case into the test suite
			testSuite.add(actionCmds);
			testSuiteCmdsIds.add(actionCmdsIds);
			
			//reinit. the length of test sequence
			currentTestCaseLength = 0;
			actualTestSuiteSize ++;
			
			if(actualTestSuiteSize % 5 ==0){
				dumpCoverageInfo(fsm, ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/markov_model_test_suite_data.txt", test_generation_iteration);
				test_generation_iteration ++;
			}
			
			//set a limit on the maximum test suite size 
			if( actualTestSuiteSize >= ConfigOptions.MAX_TEST_SUITE_SIZE_COMPARE_MODEL ){
				
				System.out.println(debugFlag + "I: exceed the max test suite size: " + ConfigOptions.MAX_TEST_SUITE_SIZE_COMPARE_MODEL 
						+ ", covered nodes cnt: " + nodesStateSet.size()
						+ ", total node cnt: " + fsm.getUniqueStatesCount()
						+ ", covered edges cnt: " + edgesSet.size()
						+ ", total edge cnt: " + fsm.getUniqueTransitionsCount());
				dumpCoverageInfo(fsm, ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/markov_model_test_suite_data.txt", test_generation_iteration);
				
				break;
			}
		}
	}
	
	public void dumpCoverageInfo(AndroidAppFSM fsm, String fileName, int test_generation_iteration){
		FileWriter fw = null;
		String content = "" ;
		content += " ----- " + test_generation_iteration + "th iteration ----\n";
		content += "nodeCoverage: " + getNodeCoverage(fsm) + ", edgeCoverage: " + getEdgeCoverage(fsm) + ", diversity: " + computeDifferentEventCombinations() + "\n";
		try{
			//append the content into a file
			fw = new FileWriter(fileName, true);
			fw.write(content);
			fw.write("-----\n\n");
			fw.close();
		}catch(IOException e){
			System.out.println(debugFlag + "failed to open a file");
			System.exit(0);
		}

	}
	
	/**
	 *  the standard Markov Chain test generation according to the transition probabilities.
	 *  */ 
	public void markovChainTestGenerationWithProb(AndroidAppFSM fsm, Map<String, List<GibbsTriple>> gibbsVector){
		System.out.println(debugFlag + "I: start test generation ...");
		//TODO hard-code the initial state name
		String initialStateName = fsm.getInitialStateName();
		//assert the initial state name is not null
		assert(initialStateName != null);
		System.out.println(debugFlag + "I: the entry state name: " + initialStateName);
		
		actualTestSuiteSize = 0;
		int currentTestCaseLength = 0;
		String nextStateName;
		
		//init node state set
		initNodesStateSet();
		//init edge set
		initEdgesSet();
		
		//NOTE clear the selection times of all transitions before test generation
		clearTransitionSelectionTimes(gibbsVector);
		
		testSuiteCmdsIds.clear();
		
		//generate test sequences until all nodes are covered
		while(!allNodesCovered(fsm.getUniqueStatesCount())){  
			
			//the test sequence always starts from the entry state
			nextStateName = initialStateName;
			
			//the test case, i.e., the sequence of action commands
			ArrayList<String> actionCmds = new ArrayList<String>(); 
			ArrayList<Integer> actionCmdsIds = new ArrayList<Integer>(); 
			
			while(currentTestCaseLength < maxTestCaseLength){
				//incr. the length of test sequence
				currentTestCaseLength ++;
				
				List<GibbsTriple> triples = gibbsVector.get(nextStateName);
				
				// OLD: pick transition only with prob.
                		//int transitionId = pickTransitionWithProb(triples);
				
				//NEW: pick transition with prob. and bound (currently set as 5)
				int transitionId = pickTransitionWithProbAndBound(triples, 3);
				
				if(transitionId == -1){ 
					//when an app state does not have transitions, no transitions can be selected
                	System.out.println(debugFlag + "I: the test sequence ends here!");
                	//but set the node itself as covered !!!
    				updateNodesStateSet(nextStateName);
                	break;
                }
				
				// get the selected app state
				AppState selectedState = fsm.getStateByName(nextStateName); 
				Transition selectedtransition = selectedState.getStateTransition(transitionId);
				System.out.println("selected transition:" + selectedtransition);
				String actionCmd = selectedtransition.getAssociatedActionCmd();
				int actionCmdId = selectedtransition.getAssociatedActionID();
				
				//update nodes state set
				updateNodesStateSet(nextStateName);
				//update edges set
				updateEdgesSet(selectedtransition.getTransitionID());
				
                //System.out.println(debugFlag + "I: select an event from state " + nextStateName + ", with transition id @" + transitionId
                //                + ", action cmd: " + actionCmd);
				//store the action commands
				actionCmds.add(actionCmd);
				//store the action commands ids
				actionCmdsIds.add((actionCmdId));
				
				//choose the next app state
				nextStateName = selectedtransition.getEndAppStateName();
                //System.out.println(debugFlag + "I: the next state is: " + nextStateName);
                
                // when the next state is an empty state, we should stop.
                if(nextStateName.contains(AppState.EMPTY_APP_STATE)){ 
                	System.out.println(debugFlag + "I: th next state is an EMPTY state, the test sequence generation ends here!");
                	break;
                }
			}
			
			//add the test case into the test suite
			testSuite.add(actionCmds);
			testSuiteCmdsIds.add(actionCmdsIds);
			
			//reinit. the length of test sequence
			currentTestCaseLength = 0;
			actualTestSuiteSize ++;
			
			//set a limit on the maximum test suite size 
			if( actualTestSuiteSize >= maxTestSuiteSize ){
				System.out.println(debugFlag + "I: exceed the max test suite size: " + maxTestSuiteSize + 
						", covered nodes cnt: " + nodesStateSet.size()
						+ ", total node cnt: " + fsm.getUniqueStatesCount()
						+ ", covered edges cnt: " + edgesSet.size()
						+ ", total edge cnt: " + fsm.getUniqueTransitionsCount());
				break;
			}
		}
	}
	
	/**
	 * generate the test suite from the current Markov Chain model
	 */
	public void generateTestSuite(AndroidAppFSM fsm, Map<String, List<GibbsTriple>> gibbsVector, int generationStrategy){
		System.out.println(debugFlag + "I: generate test suite according to the new Markov Model ... ");
		if(generationStrategy == ConfigOptions.PROBAB_TEST_GENERATION){
			System.out.println(debugFlag + "test generation strategy: " + "HIGH_PROBAB_TEST_GENERATION");
			markovChainTestGenerationWithProb(fsm, gibbsVector);
		}else if(generationStrategy == ConfigOptions.COMPARE_PROBAB_TEST_GENERATION){
			System.out.println(debugFlag + "test generation strategy: " + "COMPARE_PROBAB_TEST_GENERATION");
			markovChainTestGenerationComparisonVersion(fsm, gibbsVector);
		}
		
		//dump the test suite to a file
		System.out.println(debugFlag + "I: dump the generated test suite to the file ...");
		//TODO the file is hard-coded!!
		dumpTestSuite(ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/mcmc_all_history_testsuites.txt");
	}
	
	/**
	 * compute the test diversity based on the vector idea
	 * @param fsm
	 * @return
	 */
	public double computeTestSuiteDiversityByVector(AndroidAppFSM fsm){
		TraceDiversity td = new TraceDiversity();
		
		System.out.println("unique actions cnt = " + fsm.getFSMActionIdsSet().size());
		//add all unique action ids 
		for(Integer i: fsm.getFSMActionIdsSet()){
			td.events.add(i);
		}
		for(ArrayList<Integer> ids: testSuiteCmdsIds){
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for(Integer id: ids){
				tmp.add(id);
			}
			td.testSuitCmdIds.add(tmp);
		}
		double diversity_value = Math.sqrt(td.diversity()/fsm.getFSMActionIdsSet().size());
		System.out.println("diversity value = " + diversity_value);
		return diversity_value;
	}
	
	/**
	 * compute the diversity of the generated test suite
	 * NOTE the test suite will be cleared when executing them.
	 * The diversity is measured by the number of different commands at the same location of test sequences.
	 * Hamming distance
	 */
	public double computeTestSuiteDiversity(){
		int size = testSuiteCmdsIds.size();
		assert(size>0);
		
		int k = 0; 
		Set<Integer> actionCmdSet = new HashSet<Integer>(); //the set used to count unequal action commands
		
		for(int j=0; j<maxTestCaseLength; j++){  //Note the test sequence length < maxTestCaseLength
			
			for(int i=0; i<size; i++){ //compute the number of different commands at location j
				ArrayList<Integer> testSequence = testSuiteCmdsIds.get(i);
				if(j<testSequence.size()){ //make sure no IndexOutofBound
					Integer cmd = testSequence.get(j);
					actionCmdSet.add(cmd);
				}
			}
			k += actionCmdSet.size();
			actionCmdSet.clear();
		}
		
		return k*1.0/(maxTestCaseLength*size); //scale to (0,1]
	}
	
	/**
	 * the similar version of "computeTestSuiteDiversity"
	 * @return
	 */
	public int computeDifferentEventCombinations(){
		int size = testSuiteCmdsIds.size();
		assert(size>0);
		
		int k = 0; 
		Set<Integer> actionCmdSet = new HashSet<Integer>(); //the set used to count unequal action commands
		
		for(int j=0; j<maxTestCaseLength; j++){  //Note the test sequence length < maxTestCaseLength
			
			for(int i=0; i<size; i++){ //compute the number of different commands at location j
				ArrayList<Integer> testSequence = testSuiteCmdsIds.get(i);
				if(j<testSequence.size()){ //make sure no IndexOutofBound
					Integer cmd = testSequence.get(j);
					actionCmdSet.add(cmd);
				}
			}
			k += actionCmdSet.size();
			actionCmdSet.clear();
		}
		return k;
	}
	
	/** node state list, 0 for uncovered, 1 for covered */
	private Set<String> nodesStateSet = null;
	/** edge list */
	private Set<Integer> edgesSet = null;
	
	/** initialize "nodeStates" 
	 * */
	private void initNodesStateSet(){
		if(nodesStateSet == null){
			System.out.println(debugFlag + "I: the nodes state set is null, init it!");
			nodesStateSet = new HashSet<String>();
		}
		else{
			//clear the node state set
			System.out.println(debugFlag + "I: reset the nodes state set!");
			nodesStateSet.clear();
		}
		System.out.println(debugFlag + "I: output the covered nodes: ");
		for(String node: nodesStateSet){
			System.out.print(node + "  ");
		}
		System.out.println();

	}
	
	/** init edges set */
	private void initEdgesSet(){
		if(edgesSet == null){
			System.out.println(debugFlag + "I: the edge set is null, init it!");
			edgesSet = new HashSet<Integer>();
		}
		else{
			//clear the edge set
			System.out.println(debugFlag + "I: reset the edge set!");
			edgesSet.clear();
		}
		System.out.println(debugFlag + "I: output the covered edges: ");
		for(Integer edge: edgesSet){
			System.out.print(edge + "  ");
		}
		System.out.println();
	}
	
	private void updateNodesStateSet(String nodeName){
		if(nodesStateSet != null){
			//System.out.println(debugFlag + "I: the node \"" + nodeName + "\" is covered ");
			nodesStateSet.add(nodeName);
		}else{
			System.out.println(debugFlag + "E: nodes state set is null? ");
			System.exit(0);
		}
	}
	
	private void updateEdgesSet(int edgeId){
		if(edgesSet != null){
			//System.out.println(debugFlag + "I: the edge \"" + edgeId + "\" is covered ");
			edgesSet.add(edgeId);
		}else{
			System.out.println(debugFlag + "E: edges set is null? ");
			System.exit(0);
		}
	}
	
	/** check are all nodes in the FSM covered, i.e., all states (nodes) coverage */
	public boolean allNodesCovered(int totalNodesCnt){
		int coveredNodesCnt = nodesStateSet.size();
		System.out.println(debugFlag + "I: total nodes cnt: " + totalNodesCnt + ", covered nodes cnt: " + coveredNodesCnt);
		if( coveredNodesCnt == totalNodesCnt){
			//System.out.println(debugFlag + "I: all nodes are covered!");
			return true;
		}
		else{
			return false;
		}
	}
	
	/** get the node coverage of the generated test suite */
	public double getNodeCoverage(AndroidAppFSM fsm){
		int coveredNodesCnt = nodesStateSet.size();
		int totalNodesCnt = fsm.getUniqueStatesCount();
		return coveredNodesCnt*1.0/totalNodesCnt;
	}
	
	/** get the edge coverage of the generated test suite */
	public double getEdgeCoverage(AndroidAppFSM fsm){
		int coveredEdgesCnt = edgesSet.size();
		int totalEdgesCnt = fsm.getUniqueTransitionsCount();
		return coveredEdgesCnt*1.0/totalEdgesCnt;
	}
	
	public String getCoverageInfo(){
		String result = null;
		
		try {
			//send the compute coverage command, e.g., "COMPUTE_COVERAGE@20"
			String command = CoverageReporter.COMPUTE_COVERAGE + "@" + actualTestSuiteSize;
			result = CoverageReporter.v().request(command);
			System.out.println(debugFlag + "I: coverage: " + result);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * execute the test suite against the app under test
	 * (send the test suite to the a3e crawler) 
	 */
	public String executeTestSuite(int MCMCIteration){
		
		System.out.println(debugFlag + "I: this is *" + MCMCIteration + "* th iteration in MCMC sampling.");
	
		//add the test suite
		controller.getTestSuite(testSuite);
		//clear the test Suite
		testSuite.clear();
		
		//the fitness function value
		String fitnessValue = controller.getTestCoverage();
		
		System.out.println(debugFlag + "I: the *" + MCMCIteration + "* th iteration in MCMC sampling is finished!!! ");
		return fitnessValue;
	}
	
	public void dumpTestSuite(String fileName){
		FileWriter fw = null;
		String content = "";
		try{
			//append the content into a file
			fw = new FileWriter(fileName, true);
			//add the MCMC iteration info.
			content += "the " + MCMCSampler.MCMCIteration + "th test suite\n";
			//add the covered nodes info.
			content += "the covered nodes (states): " + nodesStateSet.size() + "\n";
			content += "==================\n";
			for(ArrayList<String> testCase: testSuite){
				for(String action: testCase){
					content += action;
				}
				content += "\n\n";
			}
			fw.write(content);
			fw.close();
		}catch(IOException e){
			System.out.println(debugFlag + "E: failed to open the file: " + fileName + " to dump the test suite.");
		}
	}
}
