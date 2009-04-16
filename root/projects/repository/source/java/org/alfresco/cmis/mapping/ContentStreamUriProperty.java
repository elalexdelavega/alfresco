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
package org.alfresco.cmis.mapping;

import java.io.Serializable;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Accessor for CMIS content stream length property
 * 
 * @author andyh
 */
public class ContentStreamUriProperty extends AbstractProperty
{
    /**
     * Construct 
     *  
     * @param serviceRegistry
     */
    public ContentStreamUriProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, CMISDictionaryModel.PROP_CONTENT_STREAM_URI);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("/api/node/");
        sb.append(nodeRef.getStoreRef().getProtocol());
        sb.append("/");
        sb.append(nodeRef.getStoreRef().getIdentifier());
        sb.append("/");
        sb.append(nodeRef.getId());
        sb.append("/content");
        String name = (String)getServiceRegistry().getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
        if (name.lastIndexOf('.') != -1)
        {
            sb.append(name.substring(name.lastIndexOf('.')));
        }
        return sb.toString();
    }
}
