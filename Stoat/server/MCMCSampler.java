import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import jdistlib.Normal;
/**
 * the MCMC sampler
 * Here, one sample corresponds to a Stochastic FSM (i.e., a Markov Chain Model)
 */
public class MCMCSampler{
	
	public final static String debugFlag = "[Gibbs Sampler] ";
	
	/** the gibbs vector which stores (appStateName, transitions) pair */
	private Map<String, List<GibbsTriple>> gibbsVector; 
	
	/** the gibbs vector size */
	public static int fsmStateCount = 0;
	public static int fsmTransitionCount = 0;
	
	public static int MCMCIteration;
	public static int MaxMCMCIteration;

	
	/** the fitness function value: the current value*/
	public double current_fitness_value;
	/** the fitness function value: the new value */
	public double new_fitness_value;
	
	public double line_coverage_value = 0.0;
	public double node_coverage_value = 0.0;
	public double edge_coverage_value = 0.0;
	public double test_suite_diversity_value = 0.0;
	public double fitness_value_substraction = 0.0;
	public double accept_ratio = 0.0;
	public int best_markov_model_id = 0;
	public int accept_flag = -1;
	
	/** the parameter sigma in normal distribution */
	private static double sigma = 0.5;
	
	/** the last selected state name in MCMC sampling */
	private static String lastSelectedStateName = "";
	
	/** the instance of the MCMC sampler */
	private static MCMCSampler sampler = null;
	private MCMCSampler(){
		gibbsVector = new HashMap<String, List<GibbsTriple>>();
		MCMCIteration = 1;
		MaxMCMCIteration = ConfigOptions.MAX_MCMC_ITERATION;
		
		//init. fitness function values
		current_fitness_value = (double) 0;
		new_fitness_value = (double) 0;
	}
	public static MCMCSampler v(){
		if(sampler == null){
			sampler = new MCMCSampler();
		}
		return sampler;
	}
	
	/**
	 *  create the Markov Chain model (i.e., the Gibbs Triple) from the app FSM 
	 *  */
	private void createMarkovChainModel(AndroidAppFSM fsm){
		
		System.out.println(debugFlag + "I: create gibbs vector...");
		//iterate all unique app states
		Map<String, AppState> states = fsm.getStates();
		Iterator<Entry<String, AppState>> stateIter = states.entrySet().iterator();
		while(stateIter.hasNext()){
			Map.Entry<String,AppState> pair = (Entry<String, AppState>)stateIter.next();
			AppState state = pair.getValue();
			String stateName = state.getStateName();
			String stateSimpleName = state.getSimpleStateName();
			if(state.isUniqueState()){
				//iterate all unique transitions of an app state
				List<GibbsTriple> tripleList = new ArrayList<GibbsTriple>();
				Map<Integer, Transition> trans = state.getStateTransitions();
				Iterator<Entry<Integer, Transition>> transitionIter = trans.entrySet().iterator();
				while(transitionIter.hasNext()){
					Map.Entry<Integer, Transition> pair2 = (Entry<Integer, Transition>)transitionIter.next();
					int id = pair2.getKey();
					double prob = pair2.getValue().getExecutionProb();
					GibbsTriple triple = new GibbsTriple(stateName, stateSimpleName, id, prob);
					tripleList.add(triple);
					fsmTransitionCount ++;
				}
				System.out.println("create state name =" + stateName);
				//add it into the gibbs vector
				gibbsVector.put(stateName, tripleList);
			}
		}
		//get vector size
		fsmStateCount = gibbsVector.size();
		System.out.println(debugFlag + "I: the gibbs vector has been created, the number of states:  " + fsmStateCount + 
				", the number of edges: " + fsmTransitionCount);
	}
	
	
	/**
	 * recover the probabilities of FSM transitions
	 * File content example: 
	 * 		/Users/tingsu/AppTest/a2dp.Vol_9/s54.txt	200	0.07142857142857142
			/Users/tingsu/AppTest/a2dp.Vol_9/s54.txt	53	0.14285714285714285
			/Users/tingsu/AppTest/a2dp.Vol_9/s54.txt	203	0.07142857142857142

	 * @param probFileName
	 */
	public void recoverMarkovModelTransitionProb(String probFileName){
		
		System.out.println(debugFlag + "I: restore the transitions probabilites from the file: " + probFileName);
		try {
			InputStream input = new FileInputStream(probFileName);
			InputStreamReader isr = new InputStreamReader(input, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			String line;
			try {
				while( (line = br.readLine())!= null){
					System.out.println("I: " + line);
					String[] content = line.trim().split("\\s+"); //regular expression: multiple whitespace
					String stateName = content[0];
					String transtionId = content[1];
					String transitionProb = content[2];
					List<GibbsTriple> tripleList = gibbsVector.get(stateName);
					for(GibbsTriple triple: tripleList){
						if(triple.getTransitionID() == Integer.parseInt(transtionId)){
							triple.setTransitionProb(Double.parseDouble(transitionProb));
						}
					}
				}
			} catch (IOException e) {
				System.out.println(debugFlag + "E: failed to restore transition probabilites, *readline* !");
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			System.out.println(debugFlag + "E: can not found the optimal markov model file!");
			e.printStackTrace();
		}
			
	}
	
	/**
	 * generate a new probability under normal distribution N(mu, sigma) in the range (0, upperBound)
	 * "0" and "upperBound" are excluded.
	 * @param mu
	 * @param sigma
	 * @param upperBound
	 * @return
	 */
	private double genProbValue(double mu, double sigma, double upperBound){
		//create the normal distribution
		Normal normal = new Normal(mu, sigma);
		double newProb = 0;
		while(true){
			newProb = normal.random();
			if(newProb>0 && newProb<upperBound){
				break;
			}
		}
		return newProb;
	}
	
	/**
	 * @deprecated
	 * mutate the probabilities of those transitions starting from an app state according to a normal distribution function
	 * @param gibbsTripleList the gibbs triples of an app state
	 * http://stackoverflow.com/questions/2751938/random-number-within-a-range-based-on-a-normal-distribution
	 */
	private void mutateTransitionsProb(List<GibbsTriple> gibbsTripleList){
		int size = gibbsTripleList.size();
		if(size<1){
			//when an app state do not have transitions, size will be zero
			return;
		}
		double cumulatedProb = 0.0;
		//change the probabilities of the first "size-1" transitions
		for(int i=0; i<size-1; i++){
			GibbsTriple triple = gibbsTripleList.get(i);
			//get the original probability
			double origProb = triple.getTransitionProb();
			double newProb = genProbValue(origProb, sigma, 1-cumulatedProb);
			triple.setTransitionProb(newProb);
			System.out.println(debugFlag + "I: state: " + triple.getStateSimpleName() + 
					", transition id: " + triple.getTransitionID() + ",  orig prob: " +
					origProb + ", new prob: " + newProb);
			cumulatedProb += newProb;
		}
		//change the probability of the last transition
		GibbsTriple lastTriple = gibbsTripleList.get(size-1);
		double remainingProb = 1-cumulatedProb;
		lastTriple.setTransitionProb(remainingProb);
		System.out.println(debugFlag + "I: state: " + lastTriple.getStateSimpleName() + 
				", transition id: " + lastTriple.getTransitionID() + ", orig prob: " + 
				lastTriple.getTransitionProb() + ", new prob: " + remainingProb );
	}
	
	
	/**
	 * Denerate a prob value by randomly increase or decrease the stepSize
	 * By default, the stepSize is set as 0.1. 
	 * As a result, the values are adjusted like a Gaussian distribution, but actually as a uniform distribution
	 * @param origProb
	 * @return
	 */
	private double genProbValueByRandomStepSize(double origProb, double stepSize){
		
		double newProb = 0;
		
		Random rand = new Random();
		int res = Math.abs(rand.nextInt()) % 2;
		//decrease the prob value
		if(res==0){ 
			newProb = origProb - stepSize;
			if(newProb <=0 ) //if <=0, return the original value
				return origProb;
			else
				return newProb;
		}else{
		//increase the prob value
			newProb = origProb + stepSize;
			if(newProb >=1) //if >=0, return the original value
				return origProb;
			else
				return newProb;
		}
	}
	
	/**
	 * mutate the probability values of all transitions, randomly increase or decrease its value by a fixed step size
	 * @param gibbsTripleList
	 */
	private void mutateTransitionProbByRandomStepSize(List<GibbsTriple> gibbsTripleList, double stepSize){
		
		int size = gibbsTripleList.size();
		
		if(size<1){
			//when an app state do not have transitions, size will be zero
			return;
		}
		
		double cumulatedProb = 0.0;
		List<Double> probList = new ArrayList<Double>();
		//randomly change the probabilities of all transitions
		for(int i=0; i<size; i++){
			GibbsTriple triple = gibbsTripleList.get(i);
			//get the original probability
			double origProb = triple.getTransitionProb();
			double newProb = genProbValueByRandomStepSize(origProb, stepSize);
			probList.add(newProb);
			cumulatedProb += newProb;
		}
		
		for(int i=0; i<size; i++){
			GibbsTriple triple = gibbsTripleList.get(i);
			double origProb = triple.getTransitionProb();
			double newProb = probList.get(i)/cumulatedProb;  //normalize the prob values in [0,1]
			triple.setTransitionProb(newProb);
			System.out.println(debugFlag + "I: state: " + triple.getStateSimpleName() + 
					", transition id: " + triple.getTransitionID() + ",  orig prob: " +
					origProb + ", new prob: " + newProb);
		}
	}
	
	/** copy the gibbs vector
	 * @param from the old vector
	 * @param to the new vector */
	public void copyGibbsVector(Map<String, List<GibbsTriple>> from, Map<String, List<GibbsTriple>> to){
		Iterator<Entry<String, List<GibbsTriple>>> iter = from.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,List<GibbsTriple>> pair = (Entry<String,List<GibbsTriple>>)iter.next();
			String appStateName = pair.getKey();
			List<GibbsTriple> gibbsTripleList = pair.getValue(); 
			//put the key-value pair
			to.put(appStateName, gibbsTripleList);
		}
	}
	
	/** propose a new Markov Chain model (a sample in our MCMC sampling approach is a probabilistic FSM)
	 * this approach randomly decide whether to mutate one app state's transitions 
	 * @return the new proposed gibbs vector
	 * */
	public Map<String, List<GibbsTriple>> proposeNewMarkovChainModelByRandomStateMutation(AndroidAppFSM fsm, Map<String, List<GibbsTriple>> vector){
		
		// create a new gibbs vector
		Map<String, List<GibbsTriple>> newVector = new HashMap<String, List<GibbsTriple>>();
		// copy 
		copyGibbsVector(vector, newVector);
		
		// Randomly Mutate all app states' transitions
		Iterator<Entry<String, List<GibbsTriple>>> iter = newVector.entrySet().iterator();
		while(iter.hasNext()){
			
			// get the gibbs triple of an app state
			Map.Entry<String,List<GibbsTriple>> pair = (Entry<String,List<GibbsTriple>>)iter.next();
			String name = pair.getKey();
			List<GibbsTriple> gibbsTripleList = pair.getValue(); 
			
			Random rand = new Random();
			int res = Math.abs(rand.nextInt()) % 2;
			if(res == 1){
				System.out.println("mutate app state: " + name);
				mutateTransitionProbByRandomStepSize(gibbsTripleList, 0.1);
			}else{
				System.out.println("keep app state unchanged: " + name);
			}
		}
		
		// return the mutated gibbs vectors
		return newVector;
	}

	
	/** propose a new Markov Chain model (a sample in our MCMC sampling approach is a probabilistic FSM)
	 * this approach randomly select one "new" app state (w.r.t the last selection) to mutate its transitions 
	 * @return the new proposed gibbs vector
	 */
	public Map<String, List<GibbsTriple>> proposeNewMarkovChainModelByOneNewStateMutation(AndroidAppFSM fsm, Map<String, List<GibbsTriple>> vector){
		
		//create a new gibbs vector
		Map<String, List<GibbsTriple>> newVector = new HashMap<String, List<GibbsTriple>>();
		//copy 
		copyGibbsVector(vector, newVector);
		
		// Random pick an app state which has not been selected last time, and mutate all its transitions
		String stateName = null;
		List<GibbsTriple> gibbsTripleList = null;
		Random rand = new Random();
		int totalStateCnt = fsm.getUniqueStatesCount();
		int randIndex;
		System.out.println("[MCMCSampler]: the last selected app state name: " + lastSelectedStateName);
		while(true){
			randIndex = Math.abs(rand.nextInt()) % (totalStateCnt);
			//During MCMC sampling, getAllExecutedStates stores all unique app states
			stateName = fsm.getAllExecutedStates().get(randIndex); 
			
			//if it is the last selected state, pick another one
			if(stateName.equals(lastSelectedStateName)){
				continue;
			}else{ 
			//if it is the new selected state, mutate it
				gibbsTripleList = newVector.get(stateName);
				lastSelectedStateName = stateName;
				System.out.println("[MCMCSampler]: now the selected app state name: " + stateName);
				mutateTransitionProbByRandomStepSize(gibbsTripleList, 0.1);
				break;
			}
		}
		return newVector;
	}
	
	/**
	 * 
	 * @param fsm
	 * @param modelFileName the optimal model from MCMC sampling
	 */
	public void executeTestSuiteFromOptimalMarkovModel(AndroidAppFSM fsm){
		//step 1: create the Markov Chain model
		createMarkovChainModel(fsm);

		//step 2: recover the transition probabilities
		recoverMarkovModelTransitionProb(ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/" + ConfigOptions.PROB_MODEL_FILE_NAME);
		
		int repeat_times = 0;
		
		List<ArrayList<String>> tmpTestSuite = new ArrayList<ArrayList<String>>();
		
		
			//step 3: generate test suite
			TestManager.v().generateTestSuite(fsm, gibbsVector, ConfigOptions.COMPARE_PROBAB_TEST_GENERATION);		
			
			tmpTestSuite.clear();
			tmpTestSuite.addAll(TestManager.v().testSuite);
			TestManager.v().testSuite.clear();
			
			for(int i=0; i<tmpTestSuite.size(); i++){
				
					ArrayList<String> testSequence = tmpTestSuite.get(i); //NOTE do not remove test sequence
					TestManager.v().testSuite.add(testSequence);
				
					if((i+1)%5 == 0){
						//step 4: execute test suite
						String lineCoverageValue = TestManager.v().executeTestSuite(MCMCIteration);
						line_coverage_value = Double.parseDouble(lineCoverageValue); //percentage value!!
						System.out.println(debugFlag + "I: the test suite execution is finished. ");
						dumpMCMCData(ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/markov_model_test_suite_data.txt");
						MCMCIteration ++;
						//clear the test suite to be executed
						TestManager.v().testSuite.clear();
					}
			}	
		
	}
	
	/**
	 * execute the Gibbs Sampling
	 * 0. create the Markov Chain model from the app FSM
	 * 1. generate a new Markov Chain model (with new transition probabilities)
	 * 2. generate test cases from the new Markov Chain model
	 * 3. execute the test cases and get the coverage
	 * 4. decide whether to accept the new model or not
	 * 5. if accept
	 * 		dump this new Markov Chain
	 *  	dump the test cases and coverage information
	 *  	return to 1 again
	 * 	  if not accept
	 * 		return to 1.
	 */
	public void executeGibbsSampling(AndroidAppFSM fsm){
		
		//step 1: create the Markov Chain model
		createMarkovChainModel(fsm);
		
		double best_fitness_value = 0.0;
		
		String lineCoverageValue = null;
		Map<String, List<GibbsTriple>> newGibbsVector = null;
		
		dumpMarkovModel(ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/initial_markov_model.txt", gibbsVector);
		
		while(MCMCIteration <= MaxMCMCIteration ){
			
			//output the current gibbs vector
			outputGibbsVector(gibbsVector);
			
			/**
			 * Dump the current gibbs vector into a local file in the FSM_FILE_LOCATION
			 * "mcmc_models.txt" records all accepted stochastic models
			 * "mcmc_data.txt" records the data of each iteration of MCMC sampling
			 */
			dumpAcceptedGibbsVector(ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/mcmc_models.txt", gibbsVector, accept_flag);
			dumpMCMCData(ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/mcmc_data.txt");
			
			
			//step 2: propose a new Markov Chain model
			if(ConfigOptions.APP_STATE_PROBAB_MUTATION_STRATEGY == ConfigOptions.RANDOM_APP_STATE_MUTATION){
				newGibbsVector = proposeNewMarkovChainModelByRandomStateMutation(fsm, gibbsVector);
			}else if(ConfigOptions.APP_STATE_PROBAB_MUTATION_STRATEGY == ConfigOptions.ONE_NEW_APP_STATE_MUTATION){
				newGibbsVector = proposeNewMarkovChainModelByOneNewStateMutation(fsm, gibbsVector);
			}
			
			//step 3: generate test suite
			TestManager.v().generateTestSuite(fsm, newGibbsVector, ConfigOptions.PROBAB_TEST_GENERATION);
			
			// get node coverage, edge coverage, and test diversity (statically computed)
			node_coverage_value = TestManager.v().getNodeCoverage(fsm);
			edge_coverage_value = TestManager.v().getEdgeCoverage(fsm);
			test_suite_diversity_value = TestManager.v().computeTestSuiteDiversityByVector(fsm);
			System.out.println("nodeCov = " + node_coverage_value + ", edgeCov = " + edge_coverage_value + ", diversity = " + test_suite_diversity_value);
			
			//step 4: execute test suite
			lineCoverageValue = TestManager.v().executeTestSuite(MCMCIteration);
			line_coverage_value = Double.parseDouble(lineCoverageValue); //percentage value!!
			
			
			//OLD compute the fitness value
			//new_fitness_value = line_coverage_value;
			//NEW compute the fitness value: alpha*lineCoverage + beta*(nodeCoverage+edgeCoverage)
			new_fitness_value = 0.4*line_coverage_value +  0.3*edge_coverage_value + 0.3*(test_suite_diversity_value);
			fitness_value_substraction = (current_fitness_value - new_fitness_value)*100 ;
			
			if(new_fitness_value >= best_fitness_value){ // the "optimal" model has the highest fitness value 
				best_fitness_value = new_fitness_value;
				dumpMarkovModel(ConfigOptions.APP_MCMC_SAMPLING_OUTPUT_DIR + "/optimal_markov_model.txt", newGibbsVector);
				// set the best markov model id
				best_markov_model_id = MCMCIteration + 1;
				System.out.println(debugFlag + "I: the current optimal model is from " + best_markov_model_id + "th iteration!");
			}
			
			// do we accept the new gibbs vector ?
			if( acceptNewGibbsVector(new_fitness_value) ){
				//if accept, use this new vector to generate the next new model
				gibbsVector = newGibbsVector;
				accept_flag = 1;
			}else{
				//if not accept, restart with the previous old vector
				accept_flag = 0;
			}
			
			System.out.println(debugFlag + "I: the test suite execution is finished. ");
			
			MCMCIteration ++;
		}
	}
	
	
	/** do we accept the new gibbs vector? 
	 *  
	 * */
	public boolean acceptNewGibbsVector(double newFitnessVaule){
		if(current_fitness_value == 0){
			//the first time of gibbs sampling, accept the new gibbs vector
			System.out.println(debugFlag + "I: ACCEPT the new gibbs model (the first iteration) ! ");
			current_fitness_value = newFitnessVaule;
			return true;
		}
		//the fitness function in MCMC Sampling
		//accept_ratio = new_fitness_value / current_fitness_value; //old version
		// beta = -0.033
		accept_ratio = Math.exp(-0.33*((current_fitness_value - newFitnessVaule)*100));
		System.out.println(debugFlag + "I: " + "acceptance ratio = " + accept_ratio);
		if(accept_ratio >= 1.0 ){
			//accept
			System.out.println(debugFlag + "I: ACCEPT the new gibbs model by ratio>=1 ! ");
			current_fitness_value = newFitnessVaule;
			return true;
		}else{
			//accept the new vector with the probability "accept_ratio"
			
			//generate a random number between [0, 1)
			double randomDouble = Math.random();
			System.out.println(debugFlag + "I: the random variable value: " + randomDouble);
			if(randomDouble >=0 && randomDouble <= accept_ratio){
				System.out.println(debugFlag + "I: ACCEPT the new gibbs model by probability :  " + accept_ratio );
				current_fitness_value = newFitnessVaule;
				return true;
			}
			else{
				System.out.println(debugFlag + "I: REJECT the new gibbs model by probability: " + accept_ratio );
				return false;
			}
		}
	}
	
	/** output the gibbs vector to terminal
	 *  */
	public void outputGibbsVector(Map<String, List<GibbsTriple>> vector){
		System.out.println(debugFlag + "I: output the gibbs vector <state name, transition id, probability>");
		Iterator<Entry<String, List<GibbsTriple>>> iter = vector.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,List<GibbsTriple>> pair = (Entry<String,List<GibbsTriple>>)iter.next();
			List<GibbsTriple> value = pair.getValue(); 
			for(GibbsTriple triple: value){
				System.out.println("    		" + triple.getStateSimpleName() + "	" + triple.getTransitionID() +
						"	" + triple.getTransitionProb());
			}
		}
	}
	
	public void dumpMCMCData(String fileName){
		FileWriter fw = null;
		String content = "" ;
		content += "---- " + (MCMCIteration) + "th iteration in MCMC Sampling" + " -----\n";
		content += "nodeCoverage: " + node_coverage_value + ", edgeCoverage: " + edge_coverage_value + 
				", lineCoverage: " + line_coverage_value + ", diversity_value: " + test_suite_diversity_value + 
				", fintess value substraction: "  + fitness_value_substraction + ", current fitness value: " + current_fitness_value +
				", accept ration: " + accept_ratio + ", --> accept(1) or reject(0): " + accept_flag + 
				", best_markov_model_id(iteration): "+ best_markov_model_id + "\n";
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
	
	/** dump the gibbs vector to a file
	 * @param fileName the file name to output
	 * */
	public void dumpAcceptedGibbsVector(String fileName, Map<String, List<GibbsTriple>> vector, int accept_flag){
		FileWriter fw = null;
		String content = "" ;
		content += "---- " + MCMCIteration + "th iteration in MCMC Sampling" + " -----\n";
		if (accept_flag == 1)
			content += "---- new accepted model ---- \n";
		else if(accept_flag == 0)
			content += "---- old accepted model ---- \n";
		try{
			//append the content into a file
			fw = new FileWriter(fileName, true);
			Iterator<Entry<String, List<GibbsTriple>>> iter = vector.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String,List<GibbsTriple>> pair = (Entry<String,List<GibbsTriple>>)iter.next();
				List<GibbsTriple> value = pair.getValue(); 
				for(GibbsTriple triple: value){
					content += triple.getStateName() + "	" + triple.getTransitionID() + 
							"	" + triple.getTransitionProb() + "\n";
				}
			}
			fw.write(content);
			fw.write("-----\n\n");
			fw.close();
		}catch(IOException e){
			System.out.println(debugFlag + "failed to open a file");
			System.exit(0);
		}
	}
	
	/** dump markov model during MCMC sampling */
	private void dumpMarkovModel(String fileName, Map<String, List<GibbsTriple>> vector){
		FileWriter fw = null;
		String content = "" ;
		try{
			//overwrite the content into a file
			fw = new FileWriter(fileName, false);
			Iterator<Entry<String, List<GibbsTriple>>> iter = vector.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String,List<GibbsTriple>> pair = (Entry<String,List<GibbsTriple>>)iter.next();
				List<GibbsTriple> value = pair.getValue(); 
				for(GibbsTriple triple: value){
					content += triple.getStateName() + "	" + triple.getTransitionID() + 
							"	" + triple.getTransitionProb() + "\n";
				}
			}
			fw.write(content);
			fw.close();
		}catch(IOException e){
			System.out.println(debugFlag + "failed to open a file");
			System.exit(0);
		}
	}
	
	public static void MCMCSampling(){
    	
  		AndroidAppFSM fsm = new AndroidAppFSM("MCMC Sampling");
  		//restore android app fsm
  		fsm.restoreAppFSM(ConfigOptions.FSM_FILE_LOCATION + "/FSM.txt");
  		//compute the initial transition probabilities according to the history execution data
  		fsm.initFSMTransitionProb();
  		//init TestManager's configuration before starting MCMC Sampling
  		TestManager.v().initConfiguration(fsm);
  		//start gibbs sampling
  		MCMCSampler.v().executeGibbsSampling(fsm);
    }
	
	public static void MCMCSamplingCompare(){
    	
  		ConfigOptions.readConfigOptions("CONF.txt");
  		
  		AndroidAppFSM fsm = new AndroidAppFSM("MCMC Sampling");
  		//restore android app fsm
  		fsm.restoreAppFSM(ConfigOptions.FSM_FILE_LOCATION + "/FSM.txt");
  		//compute the initial transition probabilities according to the history execution data
  		fsm.initFSMTransitionProb();
  		//init TestManager's configuration before starting MCMC Sampling
  		TestManager.v().initConfiguration(fsm);
  		//start gibbs sampling
  		MCMCSampler.v().executeTestSuiteFromOptimalMarkovModel(fsm);
	}
	
	public static void main(String args[]){
		
		System.out.println("args[0]: " + args[0]);
		
		if(args[0].equals("--test")){
			
			System.out.println("start mcmc sampling");
			// app dir -- args[1]
	  		ConfigOptions.setupMCMCSamplingOutputDir(args[1]);
	  		//read the configuration file
	  		ConfigOptions.readConfigOptions(ConfigOptions.MCMC_SAMPLING_CONFIG_FILE);
	  		
			// MCMC Sampling
			MCMCSampling();
		}else if(args[0].equals("--compare")){
			
			System.out.println("compare mcmc sampling");
			MCMCSamplingCompare();
		}
	}
	
}