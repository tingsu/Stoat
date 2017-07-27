/*
	Copyright 2010 Kwok Ho Yin

   	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

    	http://www.apache.org/licenses/LICENSE-2.0

   	Unless required by applicable law or agreed to in writing, software
   	distributed under the License is distributed on an "AS IS" BASIS,
   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   	See the License for the specific language governing permissions and
   	limitations under the License.
*/

package com.hykwok.CurrencyConverter;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class CurrencyRateParser_ECB {
	// This variable is used for debug log (LogCat)
	private static final String TAG = "CC:Parser_ECB";
	
	private List<CurrencyRate> 	data = new ArrayList<CurrencyRate>();
	private XMLReader 			xr;
	
	private Boolean createParser() {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			
			xr = sp.getXMLReader();
			MyHandler handler = new MyHandler();
			xr.setContentHandler(handler);
			
			return true;
		} catch (Exception e) {
			Log.e(TAG, "createParser:"+e.toString());
			data.clear();
		}
		
		return false;
	}
	
	public List<CurrencyRate> getRates() {
		return data;
	}
	
	public boolean StartParser(String szURL) {
		if(createParser() == true) {
			try {
				URL url = new URL(szURL);
				InputStream stream = url.openStream();
				xr.parse(new InputSource(stream));
				return true;
			} catch (Exception e) {
				Log.e(TAG, "Cannot start parser for Input steam");
				data.clear();
			}
		}
		
		return false;
	}
	
	public boolean StartParser(Context context, int raw_src_id) {
		Resources	res = context.getResources();
		
		if(createParser() == true) {
			try {
				xr.parse(new InputSource(res.openRawResource(raw_src_id)));
				return true;
			} catch (Exception e) {
				Log.e(TAG, "Cannot start parser for resource " + Integer.toString(raw_src_id));
				data.clear();
			}
		}
		
		return false;
	}
	
	private class MyHandler extends DefaultHandler {
		@Override
		public void startDocument() throws SAXException {
			Log.d(TAG, "***** start document *****");
		}
		
		@Override
		public void endDocument() throws SAXException {
			Log.d(TAG, "***** end document *****");
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			CurrencyRate		rate_data;
			String				name = "EUR";
			double				rate = 1.0;
			
			Log.d(TAG, "start element: localname=" + localName);
			for(int i=0; i<attributes.getLength(); i++) {
				Log.d(TAG, "start element: attr=" + attributes.getLocalName(i) + " value=" + attributes.getValue(i));
			}
			
			if(localName == "Cube") {
				for(int i=0; i<attributes.getLength(); i++) {
					if(attributes.getLocalName(i) == "currency") {
						name = attributes.getValue(i);
					} else if(attributes.getLocalName(i) == "rate") {
						try {
							rate = Double.parseDouble(attributes.getValue(i));
						} catch (Exception e) {
							Log.e(TAG, "startElement:"+e.toString());
							rate = 1.0;
						}
						
						// create a new element
						rate_data = new CurrencyRate();
						rate_data.m_name = name;
						rate_data.m_rate = rate;
						
						// add new element in the list
						data.add(rate_data);
					}
				}
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			Log.d(TAG, "end element: localname=" + localName);
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			//String content = new String(ch, start, length);
			//Log.d(TAG, "content=" + content);
		}
	}
}
