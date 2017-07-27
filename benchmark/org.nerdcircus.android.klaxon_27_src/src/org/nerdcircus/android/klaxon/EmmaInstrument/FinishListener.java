package org.nerdcircus.android.klaxon.EmmaInstrument;


public interface FinishListener {
	void onActivityFinished();
	void dumpIntermediateCoverage(String filePath);
}
