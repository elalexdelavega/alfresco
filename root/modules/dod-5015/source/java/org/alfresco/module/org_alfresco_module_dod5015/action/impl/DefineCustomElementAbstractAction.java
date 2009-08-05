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
package org.alfresco.module.org_alfresco_module_dod5015.action.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_dod5015.RecordsManagementAdminServiceImpl;
import org.alfresco.module.org_alfresco_module_dod5015.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This superclass of DefineCustomPropertyAction and DefineCustomAssociationAction provides handling of bad 'name'
 * parameters, which is consistent across both subtypes.
 * 
 * @author Neil McErlean
 */
public abstract class DefineCustomElementAbstractAction extends RMActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(DefineCustomElementAbstractAction.class);

    public static final String PARAM_NAME = "name";
    /**
     * This is the hard-coded NodeRef where the RMC Custom Model is stored.
     */
    public static final NodeRef RM_CUSTOM_MODEL_NODE_REF = new NodeRef("workspace://SpacesStore/records_management_custom_model");

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // The name of the new custom type must be prefixed with a namespace e.g. rmc:foo
        String nameParam = (String) action.getParameterValue(PARAM_NAME);
        if (nameParam.startsWith(RecordsManagementAdminServiceImpl.CUSTOM_MODEL_PREFIX) == false)
        {
            // It might seem reasonable at this point to prepend the prefix if it is not there.
            // But because the creation of new custom props and assocs is something that must be
            // got right first time, the decision was made to throw an exception.

            StringBuilder msg = new StringBuilder();
            msg.append("Cannot create custom type '").append(nameParam).append("' as required prefix (").append(RecordsManagementAdminServiceImpl.CUSTOM_MODEL_PREFIX).append(
                    ") is missing.");

            if (logger.isWarnEnabled())
            {
                logger.warn(msg.toString());
            }
            throw new AlfrescoRuntimeException(msg.toString());
        }
    }

    @Override
    public NodeRef getImplicitTargetNodeRef()
    {
        return DefineCustomElementAbstractAction.RM_CUSTOM_MODEL_NODE_REF;
    }
}