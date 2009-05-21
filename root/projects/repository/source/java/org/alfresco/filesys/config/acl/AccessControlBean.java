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

package org.alfresco.filesys.config.acl;

import org.alfresco.jlan.server.auth.acl.ACLParseException;
import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.AccessControlParser;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Simple description of a JLAN Access control that can be configured via JMX or a Spring bean definition.
 */
public abstract class AccessControlBean implements BeanNameAware
{

    /** The name. */
    private String name;

    /** The access type. */
    private int accessType = AccessControl.ReadWrite;

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Return the access control name.
     * 
     * @return the access control name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Return the access control check type.
     * 
     * @return the access control check type
     */
    public final String getAccessType()
    {
        return AccessControl.asAccessString(this.accessType);
    }

    /**
     * Set the the access control check type.
     * 
     * @param accessType
     *            the access type
     * @throws ACLParseException
     */
    public void setAccessType(String accessType) throws ACLParseException
    {
        this.accessType = AccessControlParser.parseAccessTypeString(accessType);
    }

    /**
     * Converts this object to a JLAN access control
     * 
     * @return the access control
     */
    public abstract AccessControl toAccessControl();

    /**
     * Gets the access level as an integer.
     * 
     * @return the access level as an integer
     */
    protected int getAccessLevel()
    {
        return this.accessType;
    }
}