package marto.rtl_tcp_andro.EmmaInstrument;


public interface FinishListener {
	void onActivityFinished();
	void dumpIntermediateCoverage(String filePath);
}
