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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ArxivLoader {
	/**
	 * loader exception.
	 */
	public static class LoaderException extends Exception
	{
		public static final long serialVersionUID = 1L;
		
		LoaderException()
		{
			super();
		}
		
		LoaderException(String msg)
		{
			super(msg);
		}
		
		LoaderException(String msg, Throwable cause)
		{
			super(msg, cause);
		}
	}
	
	/**
	 * singleton.
	 */
	static private ArxivLoader _loader = null;
	
	/**
	 * current query start point.
	 */
	private int _qStart = 0;
	
	/**
	 * the query url.
	 */
	private  String _qUrl = null;
	
	/**
	 * the query category.
	 */
	private String _qCat = null;
	
	/**
	 * max results.
	 */
	private int _maxResults = 10;
	
	public static ArxivLoader getInstance()
	{
		if (ArxivLoader._loader == null)
			ArxivLoader._loader = new ArxivLoader();
		return ArxivLoader._loader;
	}
	
	/**
	 * 
	 * @param maxResults
	 */
	public void setMaxResults(int maxResults)
	{
		_maxResults = maxResults;
	}
	
	/**
	 * getter for _maxResults.
	 */
	public int getMaxResults()
	{
		return _maxResults;
	}
	
	/**
	 * reset loader.
	 */
	public void reset()
	{
		_qStart = 0;
		 _qCat = null;
	}
	public List<Map<String, Object>> loadPapers(String category) throws LoaderException
	{
		/* invalid query string. */
		if(category == null || category.equals(""))
			throw new LoaderException("Invalid category name.");
		
		/* check if query changed. */
		/* call equals from category since _qCat may be null. */
		if(category.equals(_qCat) == false)
		{
			_qCat = category;
			_qStart = 0;
		}
		
		/* get url. */
		_qUrl = UrlTable.makeQueryUrl(_qCat, _qStart, _maxResults);
		
		/* query the url using URL. */
		Document doc = null;
		
		try
		{
			/* open url and set timeout. */
			URL Url = new URL(_qUrl);
			HttpURLConnection conn = (HttpURLConnection)Url.openConnection();
			conn.setConnectTimeout(ConstantTable.getPaperListLoadTimeout());
			conn.setReadTimeout(ConstantTable.getPaperListLoadTimeout());

			/* prepare xml parser. */
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			/* get input stream. */
			InputStream httpInStream = conn.getInputStream();
			doc = db.parse(httpInStream);
			
			/* parse xml. */
			NodeList entryList = doc.getElementsByTagName("entry");
			
			/* allocate paper list. */
			List<Map<String, Object>> paperList = new ArrayList<Map<String, Object>>();
			
			/* extract paper info. */
			for(int i = 0; i < entryList.getLength(); i ++)
			{
				Element node = (Element)entryList.item(i);
				
				/* get simple tags. */
				String id = node.getElementsByTagName("id").item(0).getFirstChild().getNodeValue();
				String title = node.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
				String summary = node.getElementsByTagName("summary").item(0).getFirstChild().getNodeValue();
				String date = node.getElementsByTagName("published").item(0).getFirstChild().getNodeValue();
				
				/* get author list. */
				ArrayList<String> authors = new ArrayList<String>();
				NodeList authorList = node.getElementsByTagName("author");
				for(int j = 0; j < authorList.getLength(); j ++)
				{
					Element authorNode = (Element)authorList.item(j);
					String ath = authorNode.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
					authors.add(ath);
				}
				
				/* get url. */
				String url = ((Element)node.getElementsByTagName("link").item(1)).getAttribute("href");
				
				/* fill in paper structure. */
				//Paper entry = new Paper();
				Map<String, Object> paperMap = new HashMap<String, Object>();
//				entry._id = id;
//				entry._date = date.replace('T',	' ').replace('Z', ' ');
//				entry._title = title.replace("\n ", " ");
//				entry._summary = summary.replace('\n', ' ').replace("  ", "\n  ").substring(1);
//				entry._authors = authors;
//				entry._url = url;
//				entry._fileSize = ArxivFileDownloader.getFileSize(url);
				
				//paperList.add(entry);
				String author = authors.size() == 1 ? authors.get(0) : authors.get(0) + ", et al";
				
				paperMap.put("date", date.replace('T',	' ').replace('Z', ' '));
				paperMap.put("title", title.replace("\n ", " "));
				paperMap.put("summary", summary.replace('\n', ' ').replace("  ", "\n  ").substring(1));
				paperMap.put("author", author);
				paperMap.put("authorlist", authors);
				paperMap.put("url", url);
				paperMap.put("id", id);
//				paperMap.put("filesize", paper._fileSize);
				
				paperList.add(paperMap);
			}
			
			/* increase starting point. */
			_qStart += _maxResults;
			
			return paperList;
		}
		catch(MalformedURLException e)
		{
			throw new LoaderException(e.getMessage(), e);
		}
		catch(SocketTimeoutException e)
		{
			throw new LoaderException(e.getMessage(), e);
		}
		catch(IOException e)
		{
			throw new LoaderException(e.getMessage(), e);
		}
		catch(SAXException e)
		{
			throw new LoaderException("Bad data received, possibly bad connection.", e);
		}
		catch(Exception e)
		{
			throw new LoaderException(e.getMessage());
		}
	}

	/**
	 * load the info of a specific paper by its id.
	 */
	public static HashMap<String, Object> loadPaperById(String paperId) throws LoaderException
	{
		if (paperId == null || "".equals(paperId))
			throw new LoaderException("Invalid paper id.");
		
		/* make url. */
		String qUrl = UrlTable.makeQueryByIdUrl(paperId);
		
		/* prepare document. */
		Document doc = null;
		
		/* do the query. */
		try
		{
			/* open url and set timeout. */
			URL Url = new URL(qUrl);
			HttpURLConnection conn = (HttpURLConnection)Url.openConnection();
			conn.setConnectTimeout(ConstantTable.getPaperListLoadTimeout());
			conn.setReadTimeout(ConstantTable.getPaperListLoadTimeout());
			
			/* prepare xml parser. */
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			/* get input stream. */
			InputStream httpInStream = conn.getInputStream();
			doc = db.parse(httpInStream);
			
			/* parse xml. */
			NodeList entryList = doc.getElementsByTagName("entry");
			
			if (entryList.getLength() == 0)
				throw new LoaderException("No paper found for id " + paperId);
			
			/* fill in the results. */
			Element node = (Element)entryList.item(0);
			
			/* get simple tags. */
			String id = node.getElementsByTagName("id").item(0).getFirstChild().getNodeValue();
			String title = node.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
			String summary = node.getElementsByTagName("summary").item(0).getFirstChild().getNodeValue();
			String date = node.getElementsByTagName("published").item(0).getFirstChild().getNodeValue();
			
			/* get author list. */
			ArrayList<String> authors = new ArrayList<String>();
			NodeList authorList = node.getElementsByTagName("author");
			for(int j = 0; j < authorList.getLength(); j ++)
			{
				Element authorNode = (Element)authorList.item(j);
				String ath = authorNode.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
				authors.add(ath);
			}
			
			/* get url. */
			String url = ((Element)node.getElementsByTagName("link").item(1)).getAttribute("href");
			
			/* fill in paper structure. */
			HashMap<String, Object> paperMap = new HashMap<String, Object>();
			
			//paperList.add(entry);
			String author = authors.size() == 1 ? authors.get(0) : authors.get(0) + ", et al";
			
			paperMap.put("date", date.replace('T',	' ').replace('Z', ' '));
			paperMap.put("title", title.replace("\n ", " "));
			paperMap.put("summary", summary.replace('\n', ' ').replace("  ", "\n  ").substring(1));
			paperMap.put("author", author);
			paperMap.put("authorlist", authors);
			paperMap.put("url", url);
			paperMap.put("id", id);
		
			return paperMap;
		}
		catch(MalformedURLException e)
		{
			throw new LoaderException(e.getMessage(), e);
		}
		catch(SocketTimeoutException e)
		{
			throw new LoaderException(e.getMessage(), e);
		}
		catch(IOException e)
		{
			throw new LoaderException(e.getMessage(), e);
		}
		catch(SAXException e)
		{
			throw new LoaderException("Bad data received, possibly bad connection.", e);
		}
		catch(Exception e)
		{
			throw new LoaderException(e.getMessage());
		}
	}
}
