package com.sunyata.kindmind.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class FileU {

	/*
	public static String getKindMindDirectory(){
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/KindMind";
		//return Environment.getRootDirectory().getAbsolutePath() + "/KindMind";
	}
	*/
	
	public static String getFilePathFromMediaIntent(Context inContext, Intent inIntent){
		Uri tmpUri = inIntent.getData();
		String retFilePath = "";
		Cursor tmpCursor = inContext.getContentResolver().query(tmpUri, null, null, null, null);
		//-Please note that the sorttype is set to null here because this method will be
		//used for other content providers than our own
		try{
			if(tmpCursor != null && tmpCursor.moveToFirst()){
				retFilePath = tmpCursor.getString(tmpCursor.getColumnIndexOrThrow(
						MediaStore.Images.Media.DATA));
				//TODO: Why can this be ***.Images.*** and still work for video and audio?
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), e);
		}finally{
			if(tmpCursor != null){
				tmpCursor.close();
			}else{
				Log.w(DbgU.getAppTag(), DbgU.getMethodName()
						+ " Cursor was null when trying to close");
			}
		}
		return retFilePath;
	}
	
	/**
	 * \brief copyFile copies one file to another place, possibly with another file name.
	 */
	public static void copyFile(File inInFile, File inOutFile){
		try {
			inOutFile.createNewFile(); //-creating the new file
			FileInputStream tmpSourceStream = new FileInputStream(inInFile);
			FileOutputStream tmpDestinationStream = new FileOutputStream(inOutFile);
			FileChannel tmpSourceChannel = tmpSourceStream.getChannel();
			FileChannel tmpDestinationChannel = tmpDestinationStream.getChannel();
			tmpDestinationChannel.transferFrom(tmpSourceChannel, 0, tmpSourceChannel.size()); //-copying
			tmpSourceStream.close();
			tmpDestinationStream.close();
			tmpSourceChannel.close();
			tmpDestinationChannel.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
}
