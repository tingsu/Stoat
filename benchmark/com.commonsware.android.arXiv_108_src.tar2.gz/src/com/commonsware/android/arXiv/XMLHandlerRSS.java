/*
    arXiv mobile - a Free arXiv app for android
    http://code.google.com/p/arxiv-mobile/

    Copyright (C) 2010 Jack Deslippe

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */

package com.commonsware.android.arXiv;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * SAXParser implementation for arXiv RSS feeds.
 * Based roughly on the SAXParser tutorial at the anddev forum -
 * http://www.anddev.org/parsing_xml_from_the_net_-_using_the_saxparser-t353.html
 * Assumed to be public domain.
 *
 */

public class XMLHandlerRSS extends DefaultHandler {

    // Fields

    private boolean in_items = false;
    private boolean in_item = false;
    private boolean in_title = false;
    private boolean in_link = false;
    private boolean in_date = false;
    private boolean in_description = false;
    private boolean in_dccreator = false;
    public int icount = 0;
    public int numItems = 0;
    public String date = "";
    public String[] descriptions;
    public String[] titles;
    public String[] links;
    public String[] creators;

    // Methods

    // Gets be called on the following structure: <tag>characters</tag>
    @Override
    public void characters(char ch[], int start, int length) {
        if (this.in_items) {
        } else if (this.in_item) {
            if (this.in_description) {
                descriptions[icount] += new String(ch, start, length);
            } else if (this.in_title) {
                titles[icount] += new String(ch, start, length);
            } else if (this.in_link) {
                links[icount] += new String(ch, start, length);
            } else if (this.in_dccreator) {
                creators[icount] += new String(ch, start, length);
            }
        } else {
            if (this.in_date) {
                date += new String(ch, start, length);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    // Gets be called on closing tags like: </tag>
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        if (localName.equals("items")) {
            this.in_items = false;
            // JRD Allocate space for string arrays
            titles = new String[numItems];
            creators = new String[numItems];
            links = new String[numItems];
            descriptions = new String[numItems];
        } else if (localName.equals("item")) {
            this.in_item = false;
            icount++;
        } else if (localName.equals("title")) {
            this.in_title = false;
        } else if (localName.equals("link")) {
            this.in_link = false;
        } else if (localName.equals("date")) {
            this.in_date = false;
        } else if (localName.equals("description")) {
            this.in_description = false;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        // Nothing to do
    }

    // Gets be called on opening tags like: <tag>
    @Override
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        if (localName.equals("items")) {
            this.in_items = true;
        } else if (localName.equals("item")) {
            this.in_item = true;
            titles[icount] = "";
            creators[icount] = "";
            links[icount] = "";
            descriptions[icount] = "";
        } else if (localName.equals("title")) {
            this.in_title = true;
        } else if (localName.equals("link")) {
            this.in_link = true;
        } else if (localName.equals("creator")) {
            this.in_dccreator = true;
        } else if (localName.equals("description")) {
            this.in_description = true;
        } else if (localName.equals("date")) {
            this.in_date = true;
        } else if (localName.equals("li")) {
            numItems++;
        }
    }
}
