package com.sunyata.kindmind.List;


import com.sunyata.kindmind.Main.ToastOrActionC;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class SetupActionOnClickListenerC implements OnClickListener {

	private Context mContext;
	private String mAction; 
	
	//Fields and constructor
	public SetupActionOnClickListenerC(Context inContext, String inAction){
		mContext = inContext;
		mAction = inAction;
	}
	
	@Override
	public void onClick(View v) {
		ToastOrActionC.kindAction(mContext, mAction);
	}

}
