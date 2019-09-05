import java.util.Map;

import soot.SceneTransformer;

public class TestAnalysis extends SceneTransformer{

	@Override
	protected void internalTransform(String phaseName, Map options) {
		// TODO Auto-generated method stub
		System.out.println("[Android Analysis] TestAnalysis.internalTransform");
		System.out.println("Return....");
	}
	
}