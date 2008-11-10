/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript to get the Classdefinitions using classfilter , namespaceprefix and name
 * @author Saravanan Sellathurai
 */

public class GetClassDetail extends DeclarativeWebScript
{
	private DictionaryService dictionaryservice;
	private DictionaryHelper dictionaryhelper;
	
	private static final String MODEL_PROP_KEY_CLASS_DEFS = "classdefs";
	private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
	private static final String MODEL_PROP_KEY_ASSOCIATION_DETAILS = "assocdefs";
	
    private static final String CLASS_FILTER_OPTION_TYPE1 = "all";
    private static final String CLASS_FILTER_OPTION_TYPE2 = "aspect";
    private static final String CLASS_FILTER_OPTION_TYPE3 = "type";
    
    private static final String REQ_URL_TEMPL_VAR_CLASS_FILTER = "cf";
    private static final String REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX = "nsp";
    private static final String REQ_URL_TEMPL_VAR_NAME = "n";
    
	/**
     * Set the dictionaryService property.
     * 
     * @param dictionaryService The dictionary service instance to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryservice = dictionaryService; 
    }
    
    /**
     * Set the dictionaryhelper class
     * 
     * @param dictionaryService The dictionary service instance to set
     */
    public void setDictionaryHelper(DictionaryHelper dictionaryhelper)
    {
        this.dictionaryhelper = dictionaryhelper; 
    }
    
    /**
     * @Override  method from DeclarativeWebScript 
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String classfilter = req.getParameter(REQ_URL_TEMPL_VAR_CLASS_FILTER);
        String namespaceprefix = req.getParameter(REQ_URL_TEMPL_VAR_NAMESPACE_PREFIX);
        String name = req.getParameter(REQ_URL_TEMPL_VAR_NAME);
        String classname = null;
        
        Map<QName, ClassDefinition> classdef = new HashMap<QName, ClassDefinition>();
        Map<QName, Collection<PropertyDefinition>> propdef = new HashMap<QName, Collection<PropertyDefinition>>();
        Map<QName, Collection<AssociationDefinition>> assocdef = new HashMap<QName, Collection<AssociationDefinition>>();
        Map<String, Object> model = new HashMap<String, Object>();
        
        boolean cfGiven = (classfilter != null) && (classfilter.length() > 0);
        boolean nspGiven = (namespaceprefix != null) && (namespaceprefix.length() > 0);
        boolean nameGiven = (name != null) && (name.length() > 0);
        boolean ignoreCheck ,isValidClassfilter ,isValidTriplet, isValidTwins ,hasData;
        ignoreCheck = isValidClassfilter = isValidTriplet = isValidTwins = hasData = false;
        
        classname = namespaceprefix + "_" + name;
        isValidClassfilter = (cfGiven ) && (classfilter.equals(CLASS_FILTER_OPTION_TYPE1) || classfilter.equals(CLASS_FILTER_OPTION_TYPE2) || classfilter.equals(CLASS_FILTER_OPTION_TYPE3));
        
        if (cfGiven && nspGiven && nameGiven) 
        {
        	// in this case, even if the classfilter is absurd, like asking for a type, but classname (namespaceprefix+" _"+name) in reality is an aspect,then it ignores the classfilter
        	// and fetches the correct results provided the classname is a valid one, classname should be of type either aspect or type and not a property name or association name...
        	isValidTriplet = this.dictionaryhelper.isValidPrefix(namespaceprefix) && isValidClassfilter && this.dictionaryhelper.isValidClassname(classname);
        }
        else if (cfGiven && nspGiven && !nameGiven)
        {
        	isValidTwins =  isValidClassfilter && this.dictionaryhelper.isValidPrefix(namespaceprefix);
        }
        else if (cfGiven && !nspGiven && !nameGiven)
        {
        	//since classfilter is given and namespaceprefix, name is not given  , we can ignore the check for particular qname for classname
        	ignoreCheck = isValidClassfilter;
        }
        else if (!cfGiven && !nspGiven && !nameGiven)
        {
        	// if nothing is given then throw all data and ignoreCheck for particular classname
        	classfilter = CLASS_FILTER_OPTION_TYPE1;
        	ignoreCheck = true;
        }
        else if (!cfGiven && nspGiven && !nameGiven)
        {
        	// if namespace alone is given , then classfilter is assumed to be all and considered as valid twins
        	classfilter = CLASS_FILTER_OPTION_TYPE1;
        	isValidTwins = this.dictionaryhelper.isValidPrefix(namespaceprefix);
        }
        else if (!cfGiven && nspGiven && nameGiven){
        	//if namespace and name are given , then classfilter is assumed to be all (as we don't know whether its a aspect or a type ) and considered as valid twins
        	classfilter = CLASS_FILTER_OPTION_TYPE1;
        	isValidTwins = this.dictionaryhelper.isValidPrefix(namespaceprefix) && this.dictionaryhelper.isValidClassname(classname);
        }
        else if (!cfGiven && !nspGiven && nameGiven)
        {
        	//this case is considered an invalid option so throws all aspects and types and considered as hasNothing
        	classfilter = CLASS_FILTER_OPTION_TYPE1;
        	ignoreCheck = true;
        }
        
        if ((isValidTriplet) || (isValidTwins) || (ignoreCheck)) 
        { 
        	Collection<QName> qname = null;
        	int maxIteration = 1;
        	if (classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE1)) maxIteration = 2;
        	else if(classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE3)) qname = this.dictionaryservice.getAllTypes();
	       	else if (classfilter.equalsIgnoreCase(CLASS_FILTER_OPTION_TYPE2)) qname = this.dictionaryservice.getAllAspects();
	       		        	
	    	boolean flipflag = true;
	    		
	    	for (int i=0; i<maxIteration; i++)
    		{
        		if (maxIteration==2)
        		{
        			if(flipflag) qname = this.dictionaryservice.getAllAspects();
        			else qname = this.dictionaryservice.getAllTypes();
        			flipflag = false;
        		}
        		
        		for(QName qnameObj: qname)
		        {	
		        	String url = this.dictionaryhelper.getNamespaceURIfromQname(qnameObj);
		        	if(ignoreCheck  || url.equals(this.dictionaryhelper.getPrefixesAndUrlsMap().get(namespaceprefix)))
		        	{
		        		if(isValidTriplet) qnameObj = QName.createQName(this.dictionaryhelper.getFullNamespaceURI(classname));
		        		classdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj));
		        		propdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj).getProperties().values());
		        		assocdef.put(qnameObj, this.dictionaryservice.getClass(qnameObj).getAssociations().values());
		        		if (isValidTriplet) break;
		        	}
		         }// end of for loop
    		  }// end of for loop
    		 
	    	hasData = true;
	    	    
        } // end of else block
	    	
        if(hasData)
        {
        	model.put(MODEL_PROP_KEY_CLASS_DEFS, classdef.values());
        	model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, propdef.values());
        	model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, assocdef.values());
        	return model;
        }
        else
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "The exact parameter has not been provided in the URL");
        } 
    }
   
 }