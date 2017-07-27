package com.sunyata.kindmind.Setup;

import com.sunyata.kindmind.SingleFragmentActivityC;

import android.app.Fragment;

public class ItemSetupActivityC extends SingleFragmentActivityC {
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	public Fragment createFragment(Object inAttachedData){ //Fragment
		return ItemSetupFragmentC.newInstance(inAttachedData);
	}
}
