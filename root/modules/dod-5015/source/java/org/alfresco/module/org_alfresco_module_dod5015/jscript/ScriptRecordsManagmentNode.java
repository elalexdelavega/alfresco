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
package org.alfresco.module.org_alfresco_module_dod5015.jscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_dod5015.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_dod5015.capability.Capability;
import org.alfresco.module.org_alfresco_module_dod5015.security.RecordsManagementSecurityService;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.mozilla.javascript.Scriptable;

/**
 * Base records managment script node
 * 
 * @author Roy Wetherall
 */
public class ScriptRecordsManagmentNode extends ScriptNode
{
    private static final long serialVersionUID = 8872385533440938353L;

    private RecordsManagementServiceRegistry rmServices;
    
    public ScriptRecordsManagmentNode(NodeRef nodeRef, RecordsManagementServiceRegistry services, Scriptable scope)
    {       
        super(nodeRef, services, scope);
        rmServices = services;
    }

    public ScriptRecordsManagmentNode(NodeRef nodeRef, RecordsManagementServiceRegistry services)
    {
        super(nodeRef, services);
        rmServices = services;
    }

    public ScriptCapability[] getCapabilities()
    {
        // Get the map of capabilities
        RecordsManagementSecurityService rmSecurity = rmServices.getRecordsManagementSecurityService();
        Map<Capability, AccessStatus> cMap = rmSecurity.getCapabilities(this.nodeRef);
        
        List<ScriptCapability> list = new ArrayList<ScriptCapability>(cMap.size());
        for (Map.Entry<Capability, AccessStatus> entry : cMap.entrySet())
        {
            if (AccessStatus.ALLOWED.equals(entry.getValue()) == true ||
                AccessStatus.UNDETERMINED.equals(entry.getValue()) == true)
            {
                Capability cap = entry.getKey();
                String[] actions = (String[])cap.getActionNames().toArray(new String[cap.getActionNames().size()]);
                ScriptCapability scriptCap = new ScriptCapability(cap.getName(), cap.getName(), actions);
                list.add(scriptCap);
            }
        }
        
        return (ScriptCapability[])list.toArray(new ScriptCapability[list.size()]);        
    }
}