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
package org.alfresco.connector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.alfresco.tools.XMLUtil;
import org.alfresco.web.config.RemoteConfigElement.CredentialVaultDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * A basic implementation of a persistent credential vault where
 * credentials are stored in XML on disk.
 * 
 * Note:  You should never use anything like this in production.  Rather,
 * it is suitable for test suites (locally and within test frameworks).
 * 
 * It also is a useful example of another kind of credential vault
 * 
 * @author muzquiano
 */
public class XMLCredentialVault extends SimpleCredentialVault
{
    private static Log logger = LogFactory.getLog(XMLCredentialVault.class);
    
    /**
     * Instantiates a new xML credential vault.
     * 
     * @param id the id
     * @param descriptor the descriptor
     */
    public XMLCredentialVault(String id, CredentialVaultDescriptor descriptor)
    {
        super(id, descriptor);
    }
        
    /**
     * Gets the location.
     * 
     * @return the location
     */
    public String getLocation()
    {
        String location = null;
        
        if(this.descriptor != null)
        {
            location = (String) this.descriptor.getProperty("location");
        }
        if(location == null)
        {
            location = "/vault";
        }
        
        return location;
    }
        
    /* (non-Javadoc)
     * @see org.alfresco.connector.CredentialVault#load()
     */
    public void load()
    {
        File file = new File(getLocation() + "/" + this.id + ".xml");
        if(file.exists())
        {
            StringBuilder contents = new StringBuilder();
            try 
            {
                BufferedReader input = new BufferedReader(new FileReader(file));
                try
                {
                    String line = null;
    
                    while (( line = input.readLine()) != null)
                    {
                        contents.append(line);
                        contents.append(System.getProperty("line.separator"));
                    }
                    
                    // get the xml
                    String xml = contents.toString();
                    
                    // deserialize
                    deserialize(xml);
                }
                finally 
                {
                    input.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }   
        }
        else
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Unable to load XML Credential Vault");
                logger.debug("Not found: " + file.getAbsolutePath());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.connector.CredentialVault#save()
     */
    public void save()
    {
        String xml = serialize();
        
        File file = new File(getLocation() + "/" + this.id + ".xml");
        if(file.exists())
        {
            file.delete();
        }
        
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(file, true);
            fw.write(xml);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if(fw != null)
            {
                try { fw.close(); } catch(Exception ex) { }
            }
        }
    }
    
    /**
     * Serialize.
     * 
     * @return the string
     */
    protected String serialize()
    {
        Element rootElement = org.dom4j.DocumentHelper.createElement("vault");
        rootElement.addAttribute("id", this.id);
        
        Document d = org.dom4j.DocumentHelper.createDocument(rootElement);
        
        // walk through all of the endpoints
        Iterator it = credentialsMap.keySet().iterator();
        while(it.hasNext())
        {
            String endpointId = (String) it.next();
            
            Element endpointElement = rootElement.addElement("endpoint");
            endpointElement.addAttribute("id", endpointId);
                        
            Credentials credentials = retrieve(endpointId);

            String[] keys = credentials.getPropertyKeys();
            for(int i = 0; i < keys.length; i++)
            {
                String value = (String) credentials.getProperty(keys[i]);
                
                Element credentialElement = endpointElement.addElement("credential");
                credentialElement.addAttribute("id", keys[i]);
                credentialElement.setText(value);
            }
        }

        return XMLUtil.toXML(d, true);
    }
    
    /**
     * Deserialize.
     * 
     * @param xml the xml
     */
    protected void deserialize(String xml)
    {
        // reset the map
        credentialsMap = new HashMap<String, Credentials>(16, 1.0f);
        
        // doc
        Document d = null;
        try
        {
            d = org.dom4j.DocumentHelper.parseText(xml);
            
            Element rootElement = d.getRootElement();
            this.id = rootElement.attributeValue("id");
            
            List endpoints = rootElement.elements("endpoint");
            for(int i = 0; i < endpoints.size(); i++)
            {
                Element endpointElement = (Element) endpoints.get(i);
                String endpointId = endpointElement.attributeValue("id");
                
                // create the credentials object
                Credentials credz = new SimpleCredentials(endpointId);
                
                // populate
                List credentialsList = endpointElement.elements("credential");
                for(int k = 0; k < credentialsList.size(); k++)
                {
                    Element credential = (Element) credentialsList.get(k);
                    
                    String credentialId = credential.attributeValue("id");
                    String credentialValue = credential.getTextTrim();
                    
                    credz.setProperty(credentialId, credentialValue);
                }
                
                // set onto map
                store(credz);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "XMLCredentialVault - " + credentialsMap.toString();
    }        
}
