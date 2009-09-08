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
package org.alfresco.repo.domain.avm;

import java.util.Date;
import java.util.List;

import org.springframework.dao.ConcurrencyFailureException;

/**
 * DAO services for <b>avm_version_roots</b> table
 *
 * @author janv
 * @since 3.2
 */
public interface AVMVersionRootDAO
{
    /**
     * Create a new AVM version root
     * 
     */
    public AVMVersionRootEntity createVersionRoot(long storeId, long rootNodeId, int version, String creator, String tag, String description);
    
    /**
     * Update an AVM version root (tag and description only)
     * 
     * @param vrEntity
     */
    public void updateVersionRoot(AVMVersionRootEntity vrEntity);
    
    /**
     * Delete an existing AVM version root
     * 
     * @param vrEntity           the version root entity
     * @throws                   ConcurrencyFailureException if the ID does not exist
     */
    public void deleteVersionRoot(long versionRootId);
    
    /**
     * Get all the version roots in a given store
     * 
     * @param store The store.
     * @return A List of VersionRoots.  In id order.
     */
    public List<AVMVersionRootEntity> getAllInStore(long storeId);
    
    /**
     * Get the VersionRoot corresponding to the given id.
     * @param store The store
     * @param id The version id.
     * @return The VersionRoot or null if not found.
     */
    public AVMVersionRootEntity getByVersionID(long storeId, int version);
    
    /**
     * Get one from its root.
     * @param root The root to match.
     * @return The version root or null.
     */
    public AVMVersionRootEntity getByRoot(long rootNodeId);
    
    /**
     * Get the version of a store by dates.
     * @param store The store.
     * @param from The starting date.  May be null but not with to null also.
     * @param to The ending date.  May be null but not with from null also.
     * @return A List of VersionRoots.
     */
    public List<AVMVersionRootEntity> getByDates(long storeId, Date from, Date to);
    
    /**
     * Get the highest numbered version in a store.
     * @param store The store.
     * @return The highest numbered version.
     */
    public AVMVersionRootEntity getMaxVersion(long storeId);
    
    /**
     * Get the highest numbered id from all the versions in a store.
     * @param store The store.
     * @return The highest numbered id.
     */
    public Long getMaxVersionID(long storeId);
    
    public AVMVersionLayeredNodeEntryEntity createVersionLayeredNodeEntry(long versionRootId, String md5sum, String path);
    
    public void deleteVersionLayeredNodeEntries(long versionRootId);
    
    public List<AVMVersionLayeredNodeEntryEntity> getVersionLayeredNodeEntries(long versionRootId);
}