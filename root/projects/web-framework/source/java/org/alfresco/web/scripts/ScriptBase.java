/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.web.scripts;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.web.config.WebFrameworkConfigElement;
import org.alfresco.web.site.HttpRequestContext;
import org.alfresco.web.site.Model;
import org.alfresco.web.site.RequestContext;

/**
 * Base class for all Web Framework Root-Scope and Script Model objects
 * 
 * This class can accept a binding to a RequestContext (for convenience).
 * The RequestContext object is the primary interface to the request
 * for the Java API.
 * 
 * @author muzquiano
 */
public abstract class ScriptBase implements Serializable
{
    protected RequestContext context;
    protected ScriptableMap<String, Serializable> properties;

    /**
     * Instantiates a new web framework script base
     * 
     * @param context the context
     */
    public ScriptBase(RequestContext context)
    {
        // store a reference to the request context
        this.context = context;
    }
    
    public ScriptBase()
    {
        super();
    }

    /**
     * Gets the request context.
     * 
     * @return the request context
     */
    public RequestContext getRequestContext()
    {
        return context;
    }
    
    
    /**
     * Returns the HttpServletRequest bound to the RequestContext.
     * 
     * If the RequestContext was not produced from an Http request, then null
     * will be returned. 
     * 
     * @return the http servlet request
     */
    protected HttpServletRequest getHttpServletRequest()
    {
        if (context instanceof HttpRequestContext)
        {
            return ((HttpRequestContext) context).getRequest();
        }
        return null;
    }        

    /**
     * Retrieves a model object from the underlying store and hands it back
     * wrapped as a ScriptModelObject.  If the model object cannot be found,
     * null will be returned.
     * 
     * @param id the id
     * 
     * @return the script model object
     */
    public ScriptModelObject getObject(String objectTypeId, String objectId)
    {
        return ScriptHelper.getObject(getRequestContext(), objectTypeId, objectId);
    }        
    
    public ScriptableMap<String, Serializable> getProperties()
    {
        if (this.properties == null)
        {
            this.properties = buildProperties();
        }
        
        return this.properties;
    }    
    
    public Model getModel()
    {
        return context.getModel();
    }
    
    public WebFrameworkConfigElement getConfig()
    {
        return context.getConfig();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (getProperties() != null)
        {
            return getProperties().toString();
        }
        else
        {
            return this.context.toString();
        }
    }
    
    protected abstract ScriptableMap<String, Serializable> buildProperties();
}
