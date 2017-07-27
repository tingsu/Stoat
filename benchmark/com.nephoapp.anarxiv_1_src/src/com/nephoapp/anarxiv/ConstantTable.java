/*
 * Copyright (C) 2011 Nephoapp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nephoapp.anarxiv;


public class ConstantTable 
{
	/** timeout for loading paper list. */
	private static int _timeoutPaperListLoad = 5000;
	
	/** consts for fling. */
	public static final int FLING_MIN_DISTANCE = 100;
	public static final int FLING_MIN_VELOCITY = 25;
	
	/**
	 * set paper list loading timeout.
	 */
	public static void setPaperListLoadTimeout(int timeout)
	{
		_timeoutPaperListLoad = timeout;
	}
	
	/**
	 * get paper list loading timeout.
	 */
	public static int getPaperListLoadTimeout()
	{
		return _timeoutPaperListLoad;
	}
	
	/**
	 * get application root dir.
	 */
	public static String getAppRootDir()
	{
		return StorageUtils.getExternalStorageRoot() + "/aNarXiv";
	}
}
