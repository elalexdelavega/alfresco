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
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import junit.framework.TestCase;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CMISCustomTypeTest extends TestCase
{
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static ApplicationContext applicationContext;
    private static AuthenticationService authenticationService;

    private static String folderId;
    private static String repositoryId;
    private static RepositoryServicePort repositoryServicePort;
    private static ObjectServicePort objectServicePort;
    private static DiscoveryServicePort discoveryServicePort;
    private static NavigationServicePort navigationServicePort;

    @Override
    protected void setUp() throws Exception
    {
        if (null == applicationContext)
        {
            applicationContext = new ClassPathXmlApplicationContext(new String[] {"classpath:alfresco/application-context.xml", "classpath:alfresco/cmis-ws-context.xml", "classpath:cmis/cmis-test-context.xml"});
            ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
            authenticationService = serviceRegistry.getAuthenticationService();
        }

        authenticationService.authenticate(USERNAME, PASSWORD.toCharArray());

        if (repositoryServicePort == null)
        {
            repositoryServicePort = (RepositoryServicePort) applicationContext.getBean("dmRepositoryService");
        }
        if (objectServicePort == null)
        {
            objectServicePort = (ObjectServicePort) applicationContext.getBean("dmObjectService");
        }
        if (navigationServicePort == null)
        {
            navigationServicePort = (NavigationServicePort) applicationContext.getBean("dmNavigationService");
        }
        if (discoveryServicePort == null)
        {
            discoveryServicePort = (DiscoveryServicePort) applicationContext.getBean("dmDiscoveryService");
        }
        repositoryId = repositoryId == null ? repositoryServicePort.getRepositories().get(0).getRepositoryId() : repositoryId;
        if (folderId == null)
        {
            String rootFolderId = repositoryServicePort.getRepositoryInfo(repositoryId).getRootFolderId();
            GetChildren getChildren = new GetChildren();
            getChildren.setRepositoryId(repositoryId);
            getChildren.setFolderId(rootFolderId);
            GetChildrenResponse response = navigationServicePort.getChildren(getChildren);
            assertNotNull(response);
            assertNotNull(response.getObject());
            for (CmisObjectType cmisObjectType : response.getObject())
            {
                CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(cmisObjectType.getProperties(), "Name");
                if (propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() > 0 && "CMIS Tests".equals(propertyString.getValue().get(0)))
                {
                    folderId = ((CmisPropertyId) getCmisProperty(cmisObjectType.getProperties(), "ObjectId")).getValue().get(0);
                    break;
                }
            }
            if (folderId == null)
            {
                folderId = createTestFolder(repositoryId, "CMIS Tests", rootFolderId, false);
            }
        }

    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationService.clearCurrentSecurityContext();
    }

    public void testCreateFolder() throws Exception
    {
        String folderId1 = createTestFolder(repositoryId, "testCreateCustomFolder" + System.currentTimeMillis(), folderId, true);
        assertNotNull(folderId1);

        GetProperties getProperties = new GetProperties();
        getProperties.setRepositoryId(repositoryId);
        getProperties.setObjectId(folderId1);
        GetPropertiesResponse propertiesResponse = objectServicePort.getProperties(getProperties);
        assertNotNull(propertiesResponse);
        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesResponse.getObject().getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "F/cmiscustom_folder".equals(objectTypeId.getValue().get(0)));
        CmisPropertyString customProp = (CmisPropertyString) getCmisProperty(propertiesResponse.getObject().getProperties(), "cmiscustom_folderprop_string");
        assertTrue(customProp != null && customProp.getValue() != null && customProp.getValue().size() == 1 && "custom string".equals(customProp.getValue().get(0)));
    }

    public void testCreateDocument() throws Exception
    {
        String documentId = createTestDocument(repositoryId, "testCreateCustomDocument" + System.currentTimeMillis(), folderId, true);
        assertNotNull(documentId);

        GetProperties getProperties = new GetProperties();
        getProperties.setRepositoryId(repositoryId);
        getProperties.setObjectId(documentId);
        GetPropertiesResponse propertiesResponse = objectServicePort.getProperties(getProperties);
        assertNotNull(propertiesResponse);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesResponse.getObject().getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "D/cmiscustom_document".equals(objectTypeId.getValue().get(0)));
        CmisPropertyString customProp = (CmisPropertyString) getCmisProperty(propertiesResponse.getObject().getProperties(), "cmiscustom_docprop_string");
        assertTrue(customProp != null && customProp.getValue() != null && customProp.getValue().size() == 1 && "custom string".equals(customProp.getValue().get(0)));

        CmisPropertyBoolean propertyMulti = (CmisPropertyBoolean) getCmisProperty(propertiesResponse.getObject().getProperties(), "cmiscustom_docprop_boolean_multi");
        assertTrue(propertyMulti != null && propertyMulti.getValue() != null && propertyMulti.getValue().size() == 2 && propertyMulti.getValue().get(0) && !propertyMulti.getValue().get(1));
    }

    public void testUpdate() throws Exception
    {
        String documentId = createTestDocument(repositoryId, "testUpdateCustomDocument" + System.currentTimeMillis(), folderId, true);
        assertNotNull(documentId);

        Holder<String> holder = new Holder<String>(documentId);
        CmisPropertiesType properties = new CmisPropertiesType();
        String newName = "Updated Title " + System.currentTimeMillis();
        String customProp = "custom " + System.currentTimeMillis();
        CmisPropertyString cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setName("Name");
        cmisPropertyString.getValue().add(newName);
        properties.getProperty().add(cmisPropertyString);
        cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setName("cmiscustom_docprop_string");
        cmisPropertyString.getValue().add(customProp);
        properties.getProperty().add(cmisPropertyString);
        CmisPropertyBoolean cmisPropertymulti = new CmisPropertyBoolean();
        cmisPropertymulti.setName("cmiscustom_docprop_boolean_multi");
        cmisPropertymulti.getValue().add(false);
        cmisPropertymulti.getValue().add(true);
        properties.getProperty().add(cmisPropertymulti);
        objectServicePort.updateProperties(repositoryId, holder, null, properties);

        GetProperties getProperties = new GetProperties();
        getProperties.setRepositoryId(repositoryId);
        getProperties.setObjectId(documentId);
        GetPropertiesResponse propertiesResponse = objectServicePort.getProperties(getProperties);
        assertNotNull(propertiesResponse);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesResponse.getObject().getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "D/cmiscustom_document".equals(objectTypeId.getValue().get(0)));
        CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(propertiesResponse.getObject().getProperties(), "Name");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && newName.equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(propertiesResponse.getObject().getProperties(), "cmiscustom_docprop_string");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && customProp.equals(propertyString.getValue().get(0)));
        CmisPropertyBoolean propertyMulti = (CmisPropertyBoolean) getCmisProperty(propertiesResponse.getObject().getProperties(), "cmiscustom_docprop_boolean_multi");
        assertTrue(propertyMulti != null && propertyMulti.getValue() != null && propertyMulti.getValue().size() == 2 && !propertyMulti.getValue().get(0) && propertyMulti.getValue().get(1));
    }

    public void testDelete() throws Exception
    {
        String documentId = createTestDocument(repositoryId, "testDeleteCustomDocument" + System.currentTimeMillis(), folderId, true);
        assertNotNull(documentId);

        assertTrue(isObjectInFolder(repositoryId, documentId, folderId));

        objectServicePort.deleteObject(repositoryId, documentId);

        assertFalse(isObjectInFolder(repositoryId, documentId, folderId));
    }

    public void testQuery() throws Exception
    {
        String folderId1 = createTestFolder(repositoryId, "testQueryCustom" + System.currentTimeMillis(), folderId, false);
        String documentId1 = createTestDocument(repositoryId, "apple1", folderId1, false);
        assertNotNull(documentId1);
        String doc2name = "name" + System.currentTimeMillis();
        String documentId2 = createTestDocument(repositoryId, doc2name, folderId1, true);
        assertNotNull(documentId2);
        String documentId3 = createTestDocument(repositoryId, "banana1", folderId1, true);
        assertNotNull(documentId3);

        String query = "SELECT ObjectId, Name, ObjectTypeId, cmiscustom_docprop_string, cmiscustom_docprop_boolean_multi FROM cmiscustom_document " +
        "WHERE IN_FOLDER('" + folderId1 + "') " +
        "AND cmiscustom_docprop_string = 'custom string' ";
        CmisQueryType queryType = new CmisQueryType();
        queryType.setRepositoryId(repositoryId);
        queryType.setStatement(query);
        queryType.setSkipCount(BigInteger.valueOf(0));
        queryType.setPageSize(BigInteger.valueOf(5));

        QueryResponse response = discoveryServicePort.query(queryType);
        assertNotNull(response);
        assertEquals(2, response.getObject().size());

        CmisObjectType objectType1 = null;
        CmisObjectType objectType2 = null;
        for (int i = 0; i < 2; i++)
        {
            CmisPropertyId cmisPropertyId = (CmisPropertyId) getCmisProperty(response.getObject().get(i).getProperties(), "ObjectId");
            if (documentId2.equals(cmisPropertyId.getValue().get(0)))
            {
                objectType1 = response.getObject().get(i);
            }
            else if (documentId3.equals(cmisPropertyId.getValue().get(0)))
            {
                objectType2 = response.getObject().get(i);
            }
        }
        assertNotNull(objectType1);
        assertNotNull(objectType2);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(objectType1.getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "D/cmiscustom_document".equals(objectTypeId.getValue().get(0)));
        CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(objectType1.getProperties(), "Name");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && doc2name.equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType1.getProperties(), "cmiscustom_docprop_string");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "custom string".equals(propertyString.getValue().get(0)));

        CmisPropertyBoolean propertyBoolean = (CmisPropertyBoolean) getCmisProperty(objectType1.getProperties(), "cmiscustom_docprop_boolean_multi");
        assertTrue(propertyBoolean != null && propertyBoolean.getValue() != null && propertyBoolean.getValue().size() == 2 && propertyBoolean.getValue().get(0)
                && !propertyBoolean.getValue().get(1));

        objectTypeId = (CmisPropertyId) getCmisProperty(objectType2.getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "D/cmiscustom_document".equals(objectTypeId.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType2.getProperties(), "Name");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "banana1".equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType2.getProperties(), "cmiscustom_docprop_string");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "custom string".equals(propertyString.getValue().get(0)));

        propertyBoolean = (CmisPropertyBoolean) getCmisProperty(objectType2.getProperties(), "cmiscustom_docprop_boolean_multi");
        assertTrue(propertyBoolean != null && propertyBoolean.getValue() != null && propertyBoolean.getValue().size() == 2 && propertyBoolean.getValue().get(0)
                && !propertyBoolean.getValue().get(1));
    }

    private CmisProperty getCmisProperty(CmisPropertiesType cmisPropertiesType, String propertyName)
    {
        for (CmisProperty property : cmisPropertiesType.getProperty())
        {
            if (property.getName().equals(propertyName))
            {
                return property;
            }
        }
        return null;
    }

    private boolean isObjectInFolder(String repositoryId, String objectId, String folderId) throws CmisException
    {
        GetChildren getChildren = new GetChildren();
        getChildren.setRepositoryId(repositoryId);
        getChildren.setFolderId(folderId);
        GetChildrenResponse response = navigationServicePort.getChildren(getChildren);
        assertNotNull(response);
        assertNotNull(response.getObject());
        for (CmisObjectType cmisObjectType : response.getObject())
        {
            CmisPropertyId propertyId = (CmisPropertyId) getCmisProperty(cmisObjectType.getProperties(), "ObjectId");
            if (propertyId != null && propertyId.getValue() != null && propertyId.getValue().size() > 0 && objectId.equals(propertyId.getValue().get(0)))
            {
                return true;
            }
        }
        return false;
    }

    private String createTestDocument(String repositoryId, String name, String folderId, boolean custom) throws CmisException
    {
        CmisPropertiesType properties = new CmisPropertiesType();
        CmisPropertyString cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setName("Name");
        cmisPropertyString.getValue().add(name);
        properties.getProperty().add(cmisPropertyString);
        if (custom)
        {
            cmisPropertyString = new CmisPropertyString();
            cmisPropertyString.setName("cmiscustom_docprop_string");
            cmisPropertyString.getValue().add("custom string");
            properties.getProperty().add(cmisPropertyString);
            CmisPropertyBoolean cmisPropertyBoolean = new CmisPropertyBoolean();
            cmisPropertyBoolean.setName("cmiscustom_docprop_boolean_multi");
            cmisPropertyBoolean.getValue().add(true);
            cmisPropertyBoolean.getValue().add(false);
            properties.getProperty().add(cmisPropertyBoolean);
        }
        CmisContentStreamType cmisStream = new CmisContentStreamType();
        cmisStream.setFilename(name);
        cmisStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        cmisStream.setStream(new DataHandler(name, MimetypeMap.MIMETYPE_TEXT_PLAIN));
        return objectServicePort.createDocument(repositoryId, custom ? "D/cmiscustom_document" : "document", properties, folderId, cmisStream, null);
    }

    private String createTestFolder(String repositoryId, String name, String folderId, boolean custom) throws CmisException
    {
        CmisPropertiesType properties = new CmisPropertiesType();
        CmisPropertyString cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setName("Name");
        cmisPropertyString.getValue().add(name);
        properties.getProperty().add(cmisPropertyString);
        if (custom)
        {
            cmisPropertyString = new CmisPropertyString();
            cmisPropertyString.setName("cmiscustom_folderprop_string");
            cmisPropertyString.getValue().add("custom string");
            properties.getProperty().add(cmisPropertyString);
        }
        return objectServicePort.createFolder(repositoryId, custom ? "F/cmiscustom_folder" : "folder", properties, folderId);
    }
}