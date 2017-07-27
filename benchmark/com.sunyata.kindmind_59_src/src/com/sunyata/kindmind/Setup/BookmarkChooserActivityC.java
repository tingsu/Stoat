package com.sunyata.kindmind.Setup;

import android.app.Fragment;

import com.sunyata.kindmind.SingleFragmentActivityC;

public class BookmarkChooserActivityC extends SingleFragmentActivityC{
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	public Fragment createFragment(Object inAttachedData){
		return (Fragment)BookmarkChooserFragmentC.newInstance(); //Attached data is not used in this subclass
	}
}
