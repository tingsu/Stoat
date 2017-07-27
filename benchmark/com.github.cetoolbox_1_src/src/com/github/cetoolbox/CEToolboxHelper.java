/*******************************************************************************
 * Copyright (C) 2012-2013 CNRS and University of Strasbourg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.github.cetoolbox;

public class CEToolboxHelper {

        static final public double ParseDouble(String string) {
                double myDouble = 0;
                try {  
                        myDouble = Double.parseDouble(string);
               
                } catch(NumberFormatException nfe) {
               
                        System.out.println("Could not parse " + nfe);
                }
                return myDouble;
        }
}
