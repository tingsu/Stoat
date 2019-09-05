/**
 *  a triple class used in Gibbs Sampling 
 *  */
public class GibbsTriple{
	
    /** the app state name */
    private String appStateName;
	/** the app state *simple* name */
	private String appStateSimpleName;
    /** the transition id */
	private int transitionID;
    /** the transition prob */
	private double transitionProb;
	
	/** the upper bound of selection */
	private int selected_times;
	
	/** the constructor */
	public GibbsTriple(String appStateName, String appStateSimpleName, int transitionID, double transitionProb){
        this.appStateName = appStateName;
		this.appStateSimpleName = appStateSimpleName;
		this.transitionID = transitionID;
		this.transitionProb = transitionProb;
		this.selected_times = 0;
	}
	
	/** the copy constructor */
	public GibbsTriple(GibbsTriple tripple){
		this(tripple.appStateName, tripple.appStateSimpleName, tripple.transitionID, tripple.transitionProb);
	}
	
    //getter
    public String getStateName(){
        return this.appStateName;
    }
	public String getStateSimpleName(){
		return this.appStateSimpleName;
	}
	public int getTransitionID(){
		return this.transitionID;
	}
	public double getTransitionProb(){
		return this.transitionProb;
	}
	public int getSelectionTimes(){
		return this.selected_times;
	}
	
    //setter
	public void setTransitionProb(double prob){
		this.transitionProb = prob;
	}
	public void incrSelectionTimes(){
		this.selected_times ++;
	}
	public void clearSelectionTimes(){
		this.selected_times = 0;
	}
}