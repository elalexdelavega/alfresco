/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.module.org_alfresco_module_dod5015.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_dod5015.RecordsManagementAdminService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.scripts.Cache;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides the implementation for the customrefs.get webscript.
 * 
 * @author Neil McErlean
 */
public class CustomRefsGet extends AbstractRmWebScript
{
    private static Log logger = LogFactory.getLog(CustomRefsGet.class);
    private RecordsManagementAdminService rmAdminService;
    
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        NodeRef node = parseRequestForNodeRef(req);
        
    	if (logger.isDebugEnabled())
    	{
    		logger.debug("Getting custom reference instances for " + node);
    	}

    	List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(node);
    	List<AssociationRef> retrievedAssocs = nodeService.getTargetAssocs(node, RegexQNamePattern.MATCH_ALL);

    	List<Object> allAssocs = new ArrayList<Object>(childAssocs.size() + retrievedAssocs.size());
    	//TODO This amalgamation of two different types may or may not be suitable for the FTL.
    	
    	allAssocs.addAll(childAssocs);
    	allAssocs.addAll(retrievedAssocs);

    	if (logger.isDebugEnabled())
    	{
    		logger.debug("Retrieved custom reference instances: " + allAssocs);
    	}

    	model.put("customRefs", allAssocs);

        return model;
    }
}