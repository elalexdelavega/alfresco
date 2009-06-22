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
package org.alfresco.module.org_alfresco_module_dod5015;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public class DispositionActionImpl implements DispositionAction, RecordsManagementModel
{
    private ServiceRegistry services;
    
    private NodeRef dispositionActionNodeRef;
    
    private int index;
    
    public DispositionActionImpl(ServiceRegistry services, NodeRef nodeRef, int index)
    {
        this.services = services;
        this.dispositionActionNodeRef = nodeRef;
        this.index = index;
    }
    
    public int getIndex()
    {
        return this.index;
    }
    
    public String getId()
    {
        return this.dispositionActionNodeRef.getId();
    }

    public String getDescription()
    {
        return (String)this.services.getNodeService().getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_DESCRIPTION);
    }

    public String getName()
    {
        return (String)this.services.getNodeService().getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_ACTION_NAME);
    }

    public Period getPeriod()
    {
        return (Period)this.services.getNodeService().getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_PERIOD);
    }

    public QName getPeriodProperty()
    {
        QName result = null;
        String value = (String)this.services.getNodeService().getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_PERIOD_PROPERTY);
        if (value != null)
        {
            result = QName.createQName(value);
        }
        return result;        
    }
}