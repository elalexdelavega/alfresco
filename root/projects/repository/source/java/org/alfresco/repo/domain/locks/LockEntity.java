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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.locks;

import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>alf_lock</b> table.
 * <p>
 * These are unique (see {@link #equals(Object) equals} and {@link #hashCode() hashCode}) based
 * on the shared and exclusive resource ID combination.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockEntity
{
    private Long id;
    private Long version;
    private Long sharedResourceId;
    private Long exclusiveResourceId;
    private String lockHolder;
    private Long startTime;
    private Long expiryTime = Long.MAX_VALUE;           // TODO: 
    
    @Override
    public int hashCode()
    {
        return (sharedResourceId == null ? 0 : sharedResourceId.hashCode()) +
                (exclusiveResourceId == null ? 0 : exclusiveResourceId.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof LockEntity)
        {
            LockEntity that = (LockEntity) obj;
            return EqualsHelper.nullSafeEquals(this.sharedResourceId, that.sharedResourceId) &&
                   EqualsHelper.nullSafeEquals(this.exclusiveResourceId, that.exclusiveResourceId);
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Determine if the lock is logically exclusive.  A lock is <b>exclusive</b> if the
     * shared lock resource matches the exclusive lock resource.
     * 
     * @return      Returns <tt>true</tt> if the lock is exclusive or <tt>false<tt> if it is not
     */
    public boolean isExclusive()
    {
        if (sharedResourceId == null || exclusiveResourceId == null)
        {
            throw new IllegalStateException("LockEntity has not been populated");
        }
        return sharedResourceId.equals(exclusiveResourceId);
    }
    
    public boolean hasExpired()
    {
        return System.currentTimeMillis() > expiryTime;
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public void incrementVersion()
    {
        this.version = new Long(version.longValue() + 1L);
    }

    /**
     * @return                  Returns the ID of the shared lock resource
     */
    public Long getSharedResourceId()
    {
        return sharedResourceId;
    }

    /**
     * 
     * @param sharedResourceId  the ID of the shared lock resource
     */
    public void setSharedResourceId(Long sharedResourceId)
    {
        this.sharedResourceId = sharedResourceId;
    }

    public Long getExclusiveResourceId()
    {
        return exclusiveResourceId;
    }

    public void setExclusiveResourceId(Long exclusiveResourceId)
    {
        this.exclusiveResourceId = exclusiveResourceId;
    }

    /**
     * @return              Returns the ID of the lock holder
     */
    public String getLockHolder()
    {
        return lockHolder;
    }

    /**
     * @param lockHolder    the ID of the lock holder
     */
    public void setLockHolder(String lockHolder)
    {
        this.lockHolder = lockHolder;
    }

    /**
     * 
     * @return              Returns the time when the lock was started
     */
    public Long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Long startTime)
    {
        this.startTime = startTime;
    }

    public Long getExpiryTime()
    {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime)
    {
        this.expiryTime = expiryTime;
    }
}