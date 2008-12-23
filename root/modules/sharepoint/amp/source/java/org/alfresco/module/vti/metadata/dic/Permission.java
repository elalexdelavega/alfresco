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
package org.alfresco.module.vti.metadata.dic;

/**
 * <p>Enum of the standard permissions that user may have in dws.</p> 
 * 
 * @author PavelYur
 */
public enum Permission
{
    
    /**
     * Add items to lists, add documents to document libraries.
     */
    INSERT_LIST_ITEMS ("InsertListItems"),    
    
    /**
     * Edit items in lists, edit documents in document libraries.
     */
    EDIT_LIST_ITEMS ("EditListItems"),        
    
    /**
     * Delete items from a list, documents from a document library.
     */
    DELETE_LIST_ITEMS ("DeleteListItems"),    
    
    /**
     * Manage a site, including the ability to perform all administration tasks for the site and manage contents and permissions
     */
    MANAGE_WEB ("ManageWeb"),                 
    
    /**
     * Create, change, and delete site groups, including adding users to the site groups and specifying which rights are assigned to a site group.
     */
    MANAGE_ROLES ("ManageRoles"),             
    
    /**
     * Manage or create subsites.
     */
    MANAGE_SUBWEBS ("ManageSubwebs"),         
    
    /**
     * Approve content in lists, add or remove columns in a list, and add or remove public views of a list.
     */
    MANAGE_LISTS  ("ManageLists");               
    
    private final String value;
    
    Permission(String value) 
     {
         this.value = value;
     }
     
     public String toString()
     {
         return value;
     }
}