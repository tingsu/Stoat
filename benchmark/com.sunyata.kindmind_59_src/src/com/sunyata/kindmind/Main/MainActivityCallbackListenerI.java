package com.sunyata.kindmind.Main;

public interface MainActivityCallbackListenerI {
	public void fireSavePatternEvent();
	public void fireUpdateTabTitlesEvent();
	public void fireResetDataEvent();
	public void fireClearDatabaseAndUpdateGuiEvent();
}
