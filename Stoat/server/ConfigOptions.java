import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/** the class configuration option */
public class ConfigOptions {

	private static final String DEBUG_STRING = "[ConfigOptions] ";
	
	// General configurations
	public static String ANDROID_SDK_DIR;
	public static String ANDROID_LIB_DIR;
	
	// FSM Building Parameters
	public static int MAX_FSM_BUILDING_EVENTS;
	/** the communication port */
	public static int PORT;
	/** the main testing directory */
	public static String MAIN_TEST_DIR;
	
	/** the app directory */
	public static String APP_DIR;
	
	/** the fsm building config file */
	public static String FSM_BUILDING_CONFIG_FILE;
	/** the fsm building output dir */
	public static String APP_FSM_BUILDING_OUTPUT_DIR;
	
	/** the mcmc sampling config file */
	public static String MCMC_SAMPLING_CONFIG_FILE;
	/** the mcmc sampling output dir */
	public static String APP_MCMC_SAMPLING_OUTPUT_DIR;
	
	/** the entry activity name */
	public static String ENTRY_ACTIVITY_NAME;
	
	// MCMC Sampling Parameters
	/** the fsm file location */
	public static String FSM_FILE_LOCATION;
	public static String MCMC_SAMPLING_RESULT_DIR;
	public static int MAX_MCMC_ITERATION;
	public static int MAX_TEST_SUITE_SIZE;
	public static int MAX_TEST_SEQUENCE_LENGTH;
	public static int MAX_TEST_SUITE_SIZE_COMPARE_MODEL;
	public static int PROB_MODEL_MODE;
	public static String PROB_MODEL_FILE_NAME;
	
	/** whether we use the instance to invoke UI events, using ui instance should be more accurate since
	 * a UI page may have more than one same text, but the readability of test is affected.
	 * The UI instance starts from 0, and assigned to each UI in depth-first order in the UI xml.
	 * true: we use ui instance instead of text
	 * false: we use text instead of ui instance
	 * */
	public static boolean USE_UI_INSTANCE = true;
	
	/** 
	 * If true, compute the probability value of a transition according to its execution times 
	 * from FSM building. 
	 * If false, initialize the probability value as the average value.  
	 */
	public static boolean COMPUTE_PROB_FROM_FSM_BUILDING = true; 
	
	
	/** 
	 * generate tests from the model based on the transition probability
	 */
	public static final int PROBAB_TEST_GENERATION = 0;
	public static final int COMPARE_PROBAB_TEST_GENERATION = 1;
	
	
	/**
	 * The strategy to mutate app state's transition probabilities
	 * 1, randomly pick one "new" app state (w.r.t the last selection) to mutate
	 * 2, randomly mutate all app states (the intuition: mutate only one app state may not affect the generated test suite, make
	 * the converge much lower )
	 */
	public static int APP_STATE_PROBAB_MUTATION_STRATEGY = 2;
	public static final int ONE_NEW_APP_STATE_MUTATION = 1;
	public static final int RANDOM_APP_STATE_MUTATION = 2;
	
	/**
	 * If true, the higher the transition probability is, the more likely the transition will be picked.
	 * If false, the lower the transition probability is, the more likely the transition will be picked.
	 */
	public static boolean HIGH_PROB_TEST_GENERATIN = true;
	
	
	
	/** set up the output dir for fsm building */
	public static void setupFSMBuildingOutputDir(String appdir){
		APP_DIR = appdir;
		APP_FSM_BUILDING_OUTPUT_DIR = appdir + "/" + "stoat_fsm_output";
		FSM_BUILDING_CONFIG_FILE = APP_FSM_BUILDING_OUTPUT_DIR + "/" + "CONF.txt";
	}
	
	/** set up the output dir for fsm building */
	public static void setupMCMCSamplingOutputDir(String appdir){
		APP_DIR = appdir;
		APP_MCMC_SAMPLING_OUTPUT_DIR = appdir + "/" + "stoat_mcmc_sampling_output";
		MCMC_SAMPLING_CONFIG_FILE = APP_MCMC_SAMPLING_OUTPUT_DIR + "/" + "CONF.txt";
	}
	
	
	public static void readConfigOptions(String configFile){
		
		System.out.println("config file name: " + configFile);
		
		File f = new File(configFile);
		while(!f.exists()){
			System.out.println("D: wait for the config file ");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			InputStream input = new FileInputStream(configFile);
			InputStreamReader isr = new InputStreamReader(input, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			String line;
			try{
				while( (line = br.readLine()) != null){
					//is it a "key=value" pair?
					if(line.contains("=")){
						String res[] = line.split("=");
						String entry_name = res[0].trim();
						if(entry_name.equals("ANDROID_SDK_DIR")){
							ANDROID_SDK_DIR = res[1].trim();
							System.out.println(DEBUG_STRING + "I: ANDROID_SDK_DIR = " + ANDROID_SDK_DIR);
						}else if(entry_name.equals("ANDROID_LIB_DIR")){
							ANDROID_LIB_DIR = res[1].trim();
							System.out.println(DEBUG_STRING + "I: ANDROID_LIB_DIR = " + ANDROID_LIB_DIR);
						}else if(entry_name.equals("PORT")){
							PORT = Integer.parseInt(res[1].trim());
							System.out.println(DEBUG_STRING + "I: PORT = " + PORT);
						}else if(entry_name.equals("FSM_FILE_LOCATION")){
							FSM_FILE_LOCATION = res[1].trim();
							System.out.println(DEBUG_STRING + "I: FSM_FILE_LOCATION = " + FSM_FILE_LOCATION);
						}else if(entry_name.equals("MAX_MCMC_ITERATION")){
							MAX_MCMC_ITERATION = Integer.parseInt(res[1].trim());
							System.out.println(DEBUG_STRING + "I: MAX_MCMC_ITERATION =  " + MAX_MCMC_ITERATION);
						}else if(entry_name.equals("MAX_TEST_SUITE_SIZE")){
							MAX_TEST_SUITE_SIZE = Integer.parseInt(res[1].trim());
							System.out.println(DEBUG_STRING + "I: MAX_TEST_SUITE_SIZE =  " + MAX_TEST_SUITE_SIZE);
						}else if(entry_name.equals("MAX_TEST_SEQUENCE_LENGTH")){
							MAX_TEST_SEQUENCE_LENGTH = Integer.parseInt(res[1].trim());
							System.out.println(DEBUG_STRING + "I: MAX_TEST_SEQUENCE_LENGTH =  " + MAX_TEST_SEQUENCE_LENGTH);
						}else if(entry_name.equals("MAX_FSM_BUILDING_EVENTS")){
							MAX_FSM_BUILDING_EVENTS = Integer.parseInt(res[1].trim());
							System.out.println(DEBUG_STRING + "I: MAX_FSM_BUILDING_EVENTS =  " + MAX_FSM_BUILDING_EVENTS);
						}else if(entry_name.equals("MAIN_TEST_DIRECTORY")){
							MAIN_TEST_DIR = res[1].trim();
							System.out.println(DEBUG_STRING + "I: MAIN_TEST_DIR = " + MAIN_TEST_DIR);
						}else if(entry_name.equals("MAX_TEST_SUITE_SIZE_COMPARE_MODEL")){
							MAX_TEST_SUITE_SIZE_COMPARE_MODEL = Integer.parseInt(res[1].trim());
							System.out.println(DEBUG_STRING + "I: MAX_TEST_SUITE_SIZE_COMPARE_MODEL = " + MAX_TEST_SUITE_SIZE_COMPARE_MODEL);
						}else if(entry_name.equals("PROB_MODEL_MODE")){
							PROB_MODEL_MODE = Integer.parseInt(res[1].trim());
							System.out.println(DEBUG_STRING + "I: PROB_MODEL_MODE = " + PROB_MODEL_MODE);
						}else if(entry_name.equals("PROB_MODEL_FILE_NAME")){
							PROB_MODEL_FILE_NAME = res[1].trim();
							System.out.println(DEBUG_STRING + "I: PROB_MODEL_FILE_NAME = " + PROB_MODEL_FILE_NAME);
						}else{
							//do nothing for other entry names
							continue;
						}
					}
					else {
					//it may be (1) a comment line starting with "#"; (2) a empty line
					}
				}
				//close thr reader
				br.close();
			}catch(IOException e){
				System.out.println(DEBUG_STRING + "E: Error occurs when reading the configuration file. ");
			}
		}catch(FileNotFoundException e){
			System.out.println(DEBUG_STRING + "E: Can not find the configuration file: CONF.txt! ");
			e.printStackTrace();
		}
	}
	
	   /**
	    * @deprecated
     * create a unique testing directory for each test running
     * @param mainDir the main directory which contains the testing directory
     * @param appPackageName the app package name
     */
//    public static void createTestDirectory(String appPackageName){
//    	File dir = new File(ConfigOptions.MAIN_TEST_DIR);
//    	if(! dir.exists()){
//    		System.out.println("Fail to find the main testing directory: " + ConfigOptions.MAIN_TEST_DIR);
//    		System.exit(0);
//    	}
//    	String[] subDirs = dir.list();
//    	//find out the next unique testing directory name 
//    	int counter = 0;
//    	for(String s: subDirs){
//    		if(s.contains(appPackageName))
//    			counter ++;
//    	}
//    	//the testing directory
//    	String testDir = ConfigOptions.MAIN_TEST_DIR + appPackageName + "_" + counter;
//    	
//    	//create the testing directory
//    	File appTestDir = new File(testDir);
//    	if(! appTestDir.exists()){
//    		System.out.println("[Android Analysis] the app state directory is created: " + appTestDir);
//    		appTestDir.mkdir();
//    	}
//    	APP_TEST_DIR = testDir;
//    }
    
    /** create the mcmc sampling directory */
    public static void createMCMCSamplingDir(String dirName){
    	File dir = new File(dirName);
    	if(! dir.exists()){
    		System.out.println("[ConfigOptions] create the mcmc sampling dir: " + dirName);
    		dir.mkdir();
    	}else{
    		System.out.println("[ConfigOptions] the mcmc sampling dir: " + dirName + " already exists!!");
    	}
    }
	
	public void outputConfigOptions(){
		//TODO: output config options
	}
}
