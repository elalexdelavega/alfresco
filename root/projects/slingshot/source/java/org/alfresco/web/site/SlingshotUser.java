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
package org.alfresco.web.site;

import java.util.Map;

import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.site.AlfrescoUser;

/**
 * User object extended to provide persistence back to an Alfresco repo.
 * 
 * @author Kevin Roast
 */
public class SlingshotUser extends AlfrescoUser
{
    /**
     * Instantiates a new user.
     * 
     * @param id
     *            the id
     * @param capabilities
     *            map of string keyed capabilities given to the user
     */
    public SlingshotUser(String id, Map<String, Boolean> capabilities)
    {
        super(id, capabilities);
    }
    
    /**
     * @see org.alfresco.connector.User#save()
     */
    @Override
    public void save()
    {
        try
        {
            ((SlingshotUserFactory)FrameworkUtil.getServiceRegistry().getUserFactory()).saveUser(this);
        }
        catch (UserFactoryException err)
        {
            throw new PlatformRuntimeException("Unable to save user details: " + err.getMessage(), err);
        }
    }
}
