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
package org.alfresco.web.scripts;

import java.io.Serializable;
import java.util.Iterator;

import org.alfresco.connector.CredentialVault;
import org.alfresco.connector.Credentials;
import org.alfresco.connector.User;

/**
 * Represents the credential vault to the script engine
 * This exposes credentials from the vault which are "user" managed
 * 
 * @author muzquiano
 */
public final class ScriptCredentialVault
{    
    final private CredentialVault vault;
    final private User user;
    
    protected ScriptableMap<String, Serializable> properties;
    
    /**
     * Constructs a new ScriptCredentialVault object.
     * 
     * @param vault   The credential vault instance
     * @param user    The user to whom the credential vault belongs
     */
    public ScriptCredentialVault(CredentialVault vault, User user)
    {
        this.vault = vault;
        this.user = user;
    }
    
    // --------------------------------------------------------------
    // JavaScript Properties

    /**
     * Returns the properties of the credential vault
     */
    public ScriptableMap<String, Serializable> getProperties()
    {
        if (this.properties == null)
        {
            this.properties = new ScriptableLinkedHashMap<String, Serializable>();
            
            // put credentials onto the map
            String[] ids = this.vault.getStoredIds();
            for(int i = 0; i < ids.length; i++)
            {
                Credentials credentials = this.vault.retrieve(ids[i]);
                ScriptCredentials scriptCredentials = new ScriptCredentials(this.vault, credentials, true);
                this.properties.put(ids[i], scriptCredentials);
            }
        }
        
        return this.properties;
    }
    
    /**
     * Returns the user to whom this credential vault belongs
     */
    public User getUser()
    {
        return this.user;
    }
    
    /**
     * Returns whether the given endpoint credentials are stored on this vault
     * 
     * @param endpointId
     * @return
     */
    public boolean hasCredentials(String endpointId)
    {
        return getProperties().containsKey(endpointId);
    }
    
    /**
     * Creates new credentials and binds them into this vault.
     * If the credentials already exist, the old ones will be returned
     * 
     * @param endpointId
     * @return
     */
    public ScriptCredentials newCredentials(String endpointId)
    {
        ScriptCredentials scriptCredentials = (ScriptCredentials) getProperties().get(endpointId);
        if (scriptCredentials == null)
        {
            Credentials creds = this.vault.newCredentials(endpointId);
            this.vault.save();
            
            // update our properties map
            scriptCredentials = new ScriptCredentials(this.vault, creds);
            getProperties().put(endpointId, scriptCredentials);
        }
        
        return scriptCredentials;
    }
    
    /**
     * Removes credentials from the vault
     * 
     * @param endpointId
     */
    public void removeCredentials(String endpointId)
    {
        // remove from the actual vault
        this.vault.remove(endpointId);
        this.vault.save();
        
        // remove from our map
        getProperties().remove(endpointId);
    }
    
    /**
     * Saves the credential vault
     */
    public void save()
    {
        // get the actual vault and clear it
        String[] storedIds = this.vault.getStoredIds();
        for(int i = 0; i < storedIds.length; i++)
        {
            this.vault.remove(storedIds[i]);
        }
        
        // now walk through our properties and place them back into the vault
        Iterator it = getProperties().keySet().iterator();
        while (it.hasNext())
        {
            String endpointId = (String) it.next();

            // get the script credentials
            ScriptCredentials scriptCredentials = (ScriptCredentials) getProperties().get(endpointId);
            
            // create a new actual credentials onto which we will map
            Credentials creds = this.vault.newCredentials(endpointId);
            
            // now do the mapping
            Iterator it2 = scriptCredentials.getProperties().keySet().iterator();
            while (it2.hasNext())
            {
                String propertyKey = (String) it2.next();
                Object propertyValue = scriptCredentials.getProperties().get(propertyKey);
                
                if (propertyValue != null)
                {
                    creds.setProperty(propertyKey, propertyValue);
                }
            }
            
            // store the creds back onto the actual vault
            this.vault.store(creds);
        }
        
        // persist the vault (if needed)
        this.vault.save();
        
        // null our properties map so it reloads on next access
        this.properties = null;
    }
}