/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of the GPL,
 * you may redistribute this Program in connection with Free/Libre and Open
 * Source Software ("FLOSS") applications as described in Alfresco's FLOSS
 * exception. You should have recieved a copy of the text describing the FLOSS
 * exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.site.model;

import org.alfresco.web.site.RequestContext;
import org.dom4j.Document;

/**
 * @author muzquiano
 */
public class Component extends ModelObject
{
    public static String TYPE_NAME = "component";
    public static String PROP_REGION_ID = "region-id";
    public static String PROP_SOURCE_ID = "source-id";
    public static String PROP_SCOPE = "scope";
    public static String PROP_COMPONENT_TYPE_ID = "component-type-id";
    public static String PROP_FRAME_TYPE = "frame-type";
    
    public Component(Document document)
    {
        super(document);
    }

    public String getRegionId()
    {
        return getProperty(PROP_REGION_ID);
    }

    public void setRegionId(String regionId)
    {
        setProperty(PROP_REGION_ID, regionId);
    }

    public String getSourceId()
    {
        return getProperty(PROP_SOURCE_ID);
    }

    public void setSourceId(String sourceId)
    {
        setProperty(PROP_SOURCE_ID, sourceId);
    }

    public String getScope()
    {
        return getProperty(PROP_SCOPE);
    }

    public void setScope(String scope)
    {
        setProperty(PROP_SCOPE, scope);
    }

    public String getComponentTypeId()
    {
        return getProperty(PROP_COMPONENT_TYPE_ID);
    }

    public void setComponentTypeId(String componentTypeId)
    {
        setProperty(PROP_COMPONENT_TYPE_ID, componentTypeId);
    }
    
    public String getFrameType()
    {
        return getProperty(PROP_FRAME_TYPE);
    }
    
    public void setFrameType(String frameType)
    {
        setProperty(PROP_FRAME_TYPE, frameType);
    }

    // Helpers

    // TODO
    public ModelObject getSourceObject(RequestContext context)
    {
        // either 'global', template or page
        return context.getModel().loadObject(context, getSourceId());
    }

    public ComponentType getComponentType(RequestContext context)
    {
        // either 'global', template or page
        return context.getModel().loadComponentType(context,
                getComponentTypeId());
    }

    public String getTypeName() 
    {
        return TYPE_NAME;
    }
}
