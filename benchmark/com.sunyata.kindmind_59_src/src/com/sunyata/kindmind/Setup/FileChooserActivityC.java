package com.sunyata.kindmind.Setup;

import com.sunyata.kindmind.SingleFragmentActivityC;

import android.app.Fragment;

//Please note: FragmentActivity is only for support
public class FileChooserActivityC extends SingleFragmentActivityC{
	//The onCreate method in the parent (SingleFragmentActivityC) calls createFragment
	@Override
	public Fragment createFragment(Object inAttachedData){
		return (Fragment)FileChooserFragmentC.newInstance(); //Attached data is not used in this subclass
	}
}
