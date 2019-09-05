import java.util.ArrayList;
import java.util.List;

public class TraceDiversity{
	
	protected List<ArrayList<Integer>> testSuitCmdIds = new ArrayList<ArrayList<Integer>>();
	protected List<Integer> events = new ArrayList<Integer>();
	
	public double diversity(){
		List<Vector> traceVectors = new ArrayList<Vector>();
		Vector centroid = new Vector(events.size());
		for (ArrayList<Integer> tr : testSuitCmdIds) {
			Vector traceVector = traceVector(tr);
			//System.out.println(traceVector);
			traceVectors.add(traceVector);
			centroid.plusSelf(traceVector);
		}
		centroid.times(1.0 / testSuitCmdIds.size());
		//System.out.println("Centroid: " + centroid);
		double diverse = 0.0;
		for (Vector trace : traceVectors) {
			diverse += centroid.distanceTo(trace);
		}
		return diverse / traceVectors.size();
	}
	/**
	 * return a vector for the trace
	 * @param trace - the event ids of this trace
	 * @return
	 */	
	public Vector traceVector(ArrayList<Integer> trace){
		if (trace == null || trace.size() == 0) {
			throw new IllegalArgumentException("empty trace is not allowed");
		}
		Vector vec = eventVector(trace.get(0));
		for (int i = 1; i < trace.size(); i++) {
			Vector inc = eventVector(trace.get(i));
			Vector tmp = vec.plus(inc);
			vec = tmp.scale(tmp.consin(vec));
		}
		return vec;
	}
	/**
	 * return a vector for the event
	 * @param id - the event id
	 * @return
	 */
	public Vector eventVector(int id) {
		double[] tmp = new double[events.size()];
		for (int i = 0 ; i < events.size(); i++){
			tmp[i] = (events.get(i) == id)?0.0:1.0;				
		}
		return new Vector(tmp);
	}
	public static void main(String[]args) {
		TraceDiversity td = new TraceDiversity();
		td.events.add(1);
		td.events.add(2);
		td.events.add(3);
		td.events.add(4);
		
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(3);
		td.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(4);	tmp.add(1); tmp.add(2); tmp.add(3);
		td.testSuitCmdIds.add(tmp);
		
		System.out.println("1 Diversity: " + td.diversity());
		
		TraceDiversity td1 = new TraceDiversity();
		td1.events.add(1);
		td1.events.add(2);
		td1.events.add(3);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(1);
		td1.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(2);	tmp.add(1); tmp.add(2);
		td1.testSuitCmdIds.add(tmp);
		
		System.out.println("2 Diversity: " + td1.diversity());
	}
	public static void main2(String[]args) {
		TraceDiversity td = new TraceDiversity();
		td.events.add(1);
		td.events.add(2);
		td.events.add(3);
		
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(3);
		td.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		
		System.out.println("1 Diversity: " + td.diversity());
		
		TraceDiversity td1 = new TraceDiversity();
		td1.events.add(1);
		td1.events.add(2);
		td1.events.add(3);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td1.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(3);
		td1.testSuitCmdIds.add(tmp);
		
		System.out.println("2 Diversity: " + td1.diversity());
	}
	public static void main1(String[] args){
		TraceDiversity td = new TraceDiversity();
		td.events.add(1);
		td.events.add(2);
		td.events.add(3);
		
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(3);
		td.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(2);	tmp.add(1); tmp.add(3);
		td.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(3);	tmp.add(3); tmp.add(3);
		td.testSuitCmdIds.add(tmp);
		
		System.out.println("1 Diversity: " + td.diversity());
		
		
		TraceDiversity td2 = new TraceDiversity();
		td2.events.add(1);
		td2.events.add(2);
		td2.events.add(3);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(3);
		td2.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(2);	tmp.add(3); tmp.add(1);
		td2.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(3); tmp.add(2);
		td2.testSuitCmdIds.add(tmp);
				
		System.out.println("2 Diversity: " + td2.diversity());
		
		
		TraceDiversity td3 = new TraceDiversity();
		td3.events.add(1);
		td3.events.add(2);
		td3.events.add(3);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td3.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(2);
		td3.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(2);	tmp.add(3); tmp.add(3);
		td3.testSuitCmdIds.add(tmp);
				
		System.out.println("3 Diversity: " + td3.diversity());
		
		TraceDiversity td4 = new TraceDiversity();
		td4.events.add(1);
		td4.events.add(2);
		td4.events.add(3);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td4.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(2);
		td4.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td4.testSuitCmdIds.add(tmp);
				
		System.out.println("4 Diversity: " + td4.diversity());
		
		TraceDiversity td5 = new TraceDiversity();
		td5.events.add(1);
		td5.events.add(2);
		td5.events.add(3);
		td5.events.add(4);
		td5.events.add(5);
		td5.events.add(6);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(3);
		td5.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(6);	tmp.add(5); tmp.add(4);
		td5.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td5.testSuitCmdIds.add(tmp);
				
		System.out.println("5 Diversity: " + td5.diversity());

		
		TraceDiversity td6 = new TraceDiversity();
		td6.events.add(1);
		td6.events.add(2);
		td6.events.add(3);
		td6.events.add(4);
		td6.events.add(5);
		td6.events.add(6);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(2); tmp.add(3); tmp.add(5);
		td6.testSuitCmdIds.add(tmp);
		
		tmp = new ArrayList<Integer>();
		tmp.add(1); tmp.add(2); tmp.add(3); tmp.add(4); //	tmp.add(5); tmp.add(6);
		td6.testSuitCmdIds.add(tmp);
		
		/*tmp = new ArrayList<Integer>();
		tmp.add(1);	tmp.add(1); tmp.add(1);
		td5.testSuitCmdIds.add(tmp);*/
				
		System.out.println("6 Diversity: " + td6.diversity());
	}
}