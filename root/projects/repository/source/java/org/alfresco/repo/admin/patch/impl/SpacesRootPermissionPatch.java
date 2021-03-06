/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.admin.patch.impl;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Change Spaces Root Node permission from Guest to Read
 * 
 * Guest (now Consumer) permission is not valid for sys:store_root type.
 */
public class SpacesRootPermissionPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.spacesRootPermission.result";

    private ImporterBootstrap spacesBootstrap;
    private NodeService nodeService;
    private PermissionService permissionService;
    
    
    public SpacesRootPermissionPatch()
    {
        super();
    }

    public void setSpacesBootstrap(ImporterBootstrap spacesBootstrap)
    {
        this.spacesBootstrap = spacesBootstrap;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    
    @Override
    protected String applyInternal() throws Exception
    {
        NodeRef rootNodeRef = nodeService.getRootNode(spacesBootstrap.getStoreRef());
        permissionService.deletePermission(rootNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.CONSUMER);
        permissionService.setPermission(rootNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

}
