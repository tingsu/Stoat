package com.nephoapp.anarxiv.EmmaInstrument;


public interface FinishListener {
	void onActivityFinished();
	void dumpIntermediateCoverage(String filePath);
}
