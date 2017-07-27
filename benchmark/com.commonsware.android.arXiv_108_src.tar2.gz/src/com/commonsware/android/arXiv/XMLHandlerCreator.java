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
 * SAXParser implementation for arXiv authors API.
 * Based roughly on the SAXParser tutorial at the anddev forum -
 * http://www.anddev.org/parsing_xml_from_the_net_-_using_the_saxparser-t353.html
 * Assumed to be public domain.
 */

public class XMLHandlerCreator extends DefaultHandler {

    // Fields

    private boolean in_a = false;
    public int numItems = 0;
    public String[] creators = new String[100];

    // Methods

    // Gets be called on the following structure: <tag>characters</tag>
    @Override
    public void characters(char ch[], int start, int length) {
        if (this.in_a) {
            if (numItems < 100) {
                creators[numItems] += new String(ch, start, length);
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
        if (localName.equals("a")) {
            this.in_a = false;
            numItems++;
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
        if (localName.equals("a")) {
            this.in_a = true;
            creators[numItems] = "";
        }
    }

}
