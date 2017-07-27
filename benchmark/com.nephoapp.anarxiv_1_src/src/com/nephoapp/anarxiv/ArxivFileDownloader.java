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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * class for downloading pdf files from arxiv.
 */
public class ArxivFileDownloader 
{
	/**
	 * exception.
	 */
	public static class FileDownloaderException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		FileDownloaderException()
		{
			super();
		}
		
		/**
		 * 
		 */
		FileDownloaderException(String msg)
		{
			super(msg);
		}
		
		/**
		 * 
		 */
		FileDownloaderException(String msg, Throwable cause)
		{
			super(msg, cause);
		}
	}
	
	/**
	 * get the size of a file
	 */
	public static int getFileSize(String urlName)
	{
		try
		{
			 /* open the url. */
			URL url = new URL(urlName);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			
			/* get file size */
			return conn.getContentLength();
		}
		catch (Exception e)
		{
			return -1;
		}
	}
	
	/**
	 * download a file.
	 */
	public void download(String urlName, String localFileName) throws FileDownloaderException
	{
		try
		{
			/* open the url. */
			URL url = new URL(urlName);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			
			/* create local file. */
			File localFile = new File(localFileName);
			
			/* copy the file from url. */
			InputStream httpInStream = conn.getInputStream();
			FileOutputStream localFileOutStream = new FileOutputStream(localFile);
			
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while ( (bytesRead = httpInStream.read(buffer)) != -1)
			{
				localFileOutStream.write(buffer, 0, bytesRead);
			}
		}
		catch (MalformedURLException e)
		{
			throw new FileDownloaderException(e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new FileDownloaderException(e.getMessage(), e);
		}
		catch (NullPointerException e)
		{
			throw new FileDownloaderException(e.getMessage(), e);
		}
		catch (Exception e)
		{
			throw new FileDownloaderException(e.getMessage(), e);
		}
	}
}
