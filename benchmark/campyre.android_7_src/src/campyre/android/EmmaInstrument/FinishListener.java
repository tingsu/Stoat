package campyre.android.EmmaInstrument;


public interface FinishListener {
	void onActivityFinished();
	void dumpIntermediateCoverage(String filePath);
}
