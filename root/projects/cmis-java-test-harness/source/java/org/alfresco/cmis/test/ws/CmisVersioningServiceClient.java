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
package org.alfresco.cmis.test.ws;

import java.math.BigInteger;

import org.alfresco.repo.cmis.ws.CancelCheckOut;
import org.alfresco.repo.cmis.ws.CheckIn;
import org.alfresco.repo.cmis.ws.CheckInResponse;
import org.alfresco.repo.cmis.ws.CheckOut;
import org.alfresco.repo.cmis.ws.CheckOutResponse;
import org.alfresco.repo.cmis.ws.CmisContentStreamType;
import org.alfresco.repo.cmis.ws.CmisFaultType;
import org.alfresco.repo.cmis.ws.CmisObjectType;
import org.alfresco.repo.cmis.ws.CmisPropertiesType;
import org.alfresco.repo.cmis.ws.DeleteObject;
import org.alfresco.repo.cmis.ws.EnumIncludeRelationships;
import org.alfresco.repo.cmis.ws.EnumPropertiesBase;
import org.alfresco.repo.cmis.ws.EnumPropertiesDocument;
import org.alfresco.repo.cmis.ws.EnumServiceException;
import org.alfresco.repo.cmis.ws.GetAllVersions;
import org.alfresco.repo.cmis.ws.GetPropertiesOfLatestVersion;
import org.alfresco.repo.cmis.ws.GetPropertiesOfLatestVersionResponse;
import org.alfresco.repo.cmis.ws.VersioningServicePort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Client for Versioning Service
 */
public class CmisVersioningServiceClient extends AbstractServiceClient
{
    private static Log LOGGER = LogFactory.getLog(CmisVersioningServiceClient.class);

    private static final String TEST_CHECK_IN_COMMENT_MESSAGE = "Test Check In Comment";
    private static final String INVALID_REPOSITORY_ID = "Wrong Repository Id";

    private String documentId;

    private String documentIdHolder;

    public CmisVersioningServiceClient()
    {
    }

    public CmisVersioningServiceClient(AbstractService abstractService)
    {
        super(abstractService);
    }

    /**
     * Initializes Versioning Service client
     */
    @Override
    public void initialize() throws Exception
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Initializing client...");
        }
        documentId = createAndAssertDocument();
        documentIdHolder = getServicesFactory().getVersioningService().checkOut(new CheckOut(getAndAssertRepositoryId(), documentId)).getDocumentId();
    }

    /**
     * Invokes all methods in Versioning Service
     */
    @Override
    public void invoke() throws Exception
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Invoking client...");
        }
        VersioningServicePort versioningService = getServicesFactory().getVersioningService(getProxyUrl() + getService().getPath());

        versioningService.cancelCheckOut(new CancelCheckOut(getAndAssertRepositoryId(), documentIdHolder));

        documentIdHolder = versioningService.checkOut(new CheckOut(getAndAssertRepositoryId(), documentId)).getDocumentId();

        versioningService.checkIn(new CheckIn(getAndAssertRepositoryId(), documentIdHolder, true, new CmisPropertiesType(), new CmisContentStreamType(BigInteger.valueOf(0),
                MIMETYPE_TEXT_PLAIN, generateTestFileName(), null, new byte[0], null), TEST_CHECK_IN_COMMENT_MESSAGE, null, null, null));

        GetAllVersions getAllVersions = new GetAllVersions();
        getAllVersions.setRepositoryId(getAndAssertRepositoryId());
        getAllVersions.setVersionSeriesId(documentId);
        getAllVersions.setFilter("*");
        getAllVersions.setIncludeAllowableActions(false);
        getAllVersions.setIncludeRelationships(EnumIncludeRelationships.none);
        versioningService.getAllVersions(getAllVersions);

        GetPropertiesOfLatestVersion getPropertiesOfLatestVersion = new GetPropertiesOfLatestVersion();
        getPropertiesOfLatestVersion.setRepositoryId(getAndAssertRepositoryId());
        getPropertiesOfLatestVersion.setVersionSeriesId(documentId);
        getPropertiesOfLatestVersion.setFilter("*");
        versioningService.getPropertiesOfLatestVersion(getPropertiesOfLatestVersion);

        // FIXME: uncomment this when schema will be corrected
        // versioningService.deleteAllVersions(getAndAssertRepositoryId(), documentId);
    }

    /**
     * Remove initial data
     */
    @Override
    public void release() throws Exception
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Releasing client...");
        }
        getServicesFactory().getObjectService().deleteObject(new DeleteObject(getAndAssertRepositoryId(), documentId, true));
    }

    /**
     * Main method to start client
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:wsi-context.xml");
        AbstractServiceClient client = (CmisVersioningServiceClient) applicationContext.getBean("cmisVersioningServiceClient");
        try
        {
            client.initialize();
            client.invoke();
            client.release();
        }
        catch (Exception e)
        {
            LOGGER.error("Some error occured during client running. Exception message: " + e.getMessage());
        }
    }

    @Override
    protected void onSetUp() throws Exception
    {
        documentId = createAndAssertDocument();
        super.onSetUp();
    }

    @Override
    protected void onTearDown() throws Exception
    {
        try
        {
            if (documentIdHolder != null)
            {
                getServicesFactory().getVersioningService().cancelCheckOut(new CancelCheckOut(getAndAssertRepositoryId(), documentIdHolder));
            }
        }
        catch (Exception e)
        {
        }
        getServicesFactory().getObjectService().deleteObject(new DeleteObject(getAndAssertRepositoryId(), documentId, true));
        super.onTearDown();
    }

    public void testCheckOut() throws Exception
    {
        CheckOutResponse response = null;
        try
        {
            LOGGER.info("[VersioningService->checkOut]");
            response = getServicesFactory().getVersioningService().checkOut(new CheckOut(getAndAssertRepositoryId(), documentId));
            assertNotNull("CheckOut response is NULL", response);
            documentIdHolder = response.getDocumentId();
            assertNotNull("Checked out document id is NULL", documentIdHolder);
            if (!isVersioningAllowed())
            {
                fail("No Exception was thrown");
            }
        }
        catch (Exception e)
        {
            if (!isVersioningAllowed())
            {
                assertTrue("Invalid exception was thrown", e instanceof CmisFaultType && ((CmisFaultType) e).getType().equals(EnumServiceException.constraint));
            }
            else
            {
                fail(e.toString());
            }
        }
    }

    public void testCheckOutCheckInDefault()
    {
        try
        {
            LOGGER.info("[VersioningService->checkOut]");
            CheckOutResponse checkOutResponse = getServicesFactory().getVersioningService().checkOut(new CheckOut(getAndAssertRepositoryId(), documentId));
            assertNotNull("Checkout response is NULL", checkOutResponse);
            documentIdHolder = checkOutResponse.getDocumentId();
            assertTrue("Content was not copied", checkOutResponse.isContentCopied());
            assertFalse("Checked out document id is equal to document id", documentId.equals(documentIdHolder));
            if (!isVersioningAllowed())
            {
                fail("No Exception was thrown");
            }

            LOGGER.info("[VersioningService->checkIn]");
            CheckInResponse checkInResponse = getServicesFactory().getVersioningService().checkIn(
                    new CheckIn(getAndAssertRepositoryId(), documentIdHolder, null, null, null, null, null, null, null));
            assertNotNull("checkin response is NULL", checkInResponse);
            documentId = checkInResponse.getDocumentId();
            if (!isVersioningAllowed())
            {
                fail("No Exception was thrown");
            }
        }
        catch (Exception e)
        {
            if (!isVersioningAllowed())
            {
                assertTrue("Invalid exception was thrown", e instanceof CmisFaultType && ((CmisFaultType) e).getType().equals(EnumServiceException.constraint));
            }
            else
            {
                fail(e.toString());
            }
        }
    }

    public void testCheckOutCancelCheckOut()
    {
        try
        {
            LOGGER.info("[VersioningService->checkOut]");
            CheckOutResponse checkOutResponse = getServicesFactory().getVersioningService().checkOut(new CheckOut(getAndAssertRepositoryId(), documentId));
            assertNotNull("Checkout response is NULL", checkOutResponse);
            documentIdHolder = checkOutResponse.getDocumentId();
            if (!isVersioningAllowed())
            {
                fail("No Exception was thrown");
            }
            assertTrue("Content was not copied", checkOutResponse.isContentCopied());
            assertFalse("Checked out document id is equal to document id", documentId.equals(documentIdHolder));

            LOGGER.info("[VersioningService->cancelCheckOut]");
            getServicesFactory().getVersioningService().cancelCheckOut(new CancelCheckOut(getAndAssertRepositoryId(), documentIdHolder));
            if (!isVersioningAllowed())
            {
                fail("No Exception was thrown");
            }
            assertFalse("Document property '" + EnumPropertiesDocument._value7 + "' value is true after cancelCheckOut was performed", getBooleanProperty(documentId,
                    EnumPropertiesDocument._value7));
        }
        catch (Exception e)
        {
            if (!isVersioningAllowed())
            {
                assertTrue("Invalid exception was thrown", e instanceof CmisFaultType && ((CmisFaultType) e).getType().equals(EnumServiceException.constraint));
            }
            else
            {
                fail(e.toString());
            }
        }
    }

    public void testCheckinNoExistsCheckOut() throws Exception
    {
        try
        {
            LOGGER.info("[VersioningService->checkIn]");
            getServicesFactory().getVersioningService().checkIn(
                    new CheckIn(getAndAssertRepositoryId(), documentId, true, new CmisPropertiesType(), new CmisContentStreamType(BigInteger.valueOf(0), MIMETYPE_TEXT_PLAIN,
                            generateTestFileName(), null, TEST_CONTENT.getBytes(), null), TEST_CHECK_IN_COMMENT_MESSAGE, null, null, null));
            fail("No Exception was thrown");

        }
        catch (Exception e)
        {
            assertTrue("Invalid exception was thrown", e instanceof CmisFaultType);
        }
    }

    public void testCancelNotExistsCheckOut() throws Exception
    {
        try
        {
            LOGGER.info("[VersioningService->cancelCheckOut]");
            getServicesFactory().getVersioningService().cancelCheckOut(new CancelCheckOut(getAndAssertRepositoryId(), documentId));
            fail("Expects exception");

        }
        catch (Exception e)
        {
            assertTrue("Invalid exception was thrown", e instanceof CmisFaultType);
        }
    }

    public void testGetPropertiesOfLatestVersionDefault() throws Exception
    {
        if (isVersioningAllowed())
        {
            GetPropertiesOfLatestVersionResponse response = null;
            String versionSeriesId = getIdProperty(documentId, EnumPropertiesDocument._value6);
            assertNotNull("'" + EnumPropertiesDocument._value6 + "' property is NULL", versionSeriesId);
            try
            {
                LOGGER.info("[VersioningService->getPropertiesOfLatestVersion]");
                response = getServicesFactory().getVersioningService().getPropertiesOfLatestVersion(
                        new GetPropertiesOfLatestVersion(getAndAssertRepositoryId(), versionSeriesId, true, null, false));
            }
            catch (Exception e)
            {
                fail(e.toString());
            }
            assertNotNull("GetPropertiesOfLatestVersion response is NULL", response);
            assertNotNull("GetPropertiesOfLatestVersion response is empty", response.getObject());
            CmisObjectType objectType = response.getObject();
            assertNotNull("GetPropertiesOfLatestVersion properties are NULL", objectType.getProperties());
            assertTrue("'" + EnumPropertiesDocument._value2 + "' property value is FALSE", (Boolean) getBooleanProperty(objectType.getProperties(), EnumPropertiesDocument._value2));
        }
        else
        {
            LOGGER.info("testGetPropertiesOfLatestVersionDefault was skipped: Versioning isn't supported");
        }
    }

    public void testGetPropertiesOfLatestVersionFiltered() throws Exception
    {
        if (isVersioningAllowed())
        {
            GetPropertiesOfLatestVersionResponse response = null;
            String versionSeriesId = getIdProperty(documentId, EnumPropertiesDocument._value6);
            assertNotNull("'" + EnumPropertiesDocument._value6 + "' property is NULL", versionSeriesId);
            try
            {
                LOGGER.info("[VersioningService->getPropertiesOfLatestVersion]");
                response = getServicesFactory().getVersioningService().getPropertiesOfLatestVersion(
                        new GetPropertiesOfLatestVersion(getAndAssertRepositoryId(), versionSeriesId, true, EnumPropertiesBase._value1 + ", " + EnumPropertiesBase._value2 + ", "
                                + EnumPropertiesDocument._value2, false));
            }
            catch (Exception e)
            {
                fail(e.toString());
            }
            assertNotNull("GetPropertiesOfLatestVersion response is NULL", response);
            assertNotNull("GetPropertiesOfLatestVersion response is empty", response.getObject());
            CmisObjectType objectType = response.getObject();
            assertNotNull("Properties are NULL", objectType.getProperties());

            assertNull("Not expected properties were returned", objectType.getProperties().getPropertyDecimal());
            assertNull("Not expected properties were returned", objectType.getProperties().getPropertyHtml());
            assertNull("Not expected properties were returned", objectType.getProperties().getPropertyInteger());
            assertNull("Not expected properties were returned", objectType.getProperties().getPropertyUri());
            assertNull("Not expected properties were returned", objectType.getProperties().getPropertyXml());
            assertNull("Not expected properties were returned", objectType.getProperties().getPropertyDateTime());

            assertNotNull("Expected properties were not returned", objectType.getProperties().getPropertyId());
            assertNotNull("Expected properties were not returned", objectType.getProperties().getPropertyString());
            assertNotNull("Expected properties were not returned", objectType.getProperties().getPropertyBoolean());

            assertEquals("Expected property was not returned", 1, objectType.getProperties().getPropertyId().length);
            assertEquals("Expected property was not returned", 1, objectType.getProperties().getPropertyString().length);
            assertEquals("Expected property was not returned", 1, objectType.getProperties().getPropertyBoolean().length);

            assertNotNull("Expected property was not returned", getIdProperty(objectType.getProperties(), EnumPropertiesBase._value2));
            assertNotNull("Expected property was not returned", getStringProperty(objectType.getProperties(), EnumPropertiesBase._value1));
            assertNotNull("Expected property was not returned", getBooleanProperty(objectType.getProperties(), EnumPropertiesDocument._value2));

            assertTrue("'" + EnumPropertiesDocument._value2 + "' property value is FALSE", (Boolean) getBooleanProperty(objectType.getProperties(), EnumPropertiesDocument._value2));
        }
        else
        {
            LOGGER.info("testGetPropertiesOfLatestVersionFiltered was skipped: Versioning isn't supported");
        }
    }

    public void testGetAllVersionsDefault() throws Exception
    {
        if (isVersioningAllowed())
        {
            String checkinComment = "Test checkin" + System.currentTimeMillis();

            LOGGER.info("[VersioningService->checkOut]");
            CheckOutResponse checkOutResponse = getServicesFactory().getVersioningService().checkOut(new CheckOut(getAndAssertRepositoryId(), documentId));
            assertNotNull("Checkout response is NULL", checkOutResponse);
            documentIdHolder = checkOutResponse.getDocumentId();
            LOGGER.info("[VersioningService->checkIn]");
            CheckInResponse checkInResponse = getServicesFactory().getVersioningService().checkIn(
                    new CheckIn(getAndAssertRepositoryId(), documentIdHolder, true, new CmisPropertiesType(), new CmisContentStreamType(BigInteger.valueOf(0), MIMETYPE_TEXT_PLAIN,
                            generateTestFileName(), null, TEST_CONTENT.getBytes(), null), checkinComment, null, null, null));
            assertNotNull("Checkin response is NULL", checkInResponse);
            documentId = checkInResponse.getDocumentId();

            CmisObjectType[] response = null;
            String versionSeriesId = getIdProperty(documentId, EnumPropertiesDocument._value6);
            assertNotNull("'" + EnumPropertiesDocument._value6 + "' property is NULL", versionSeriesId);
            try
            {
                LOGGER.info("[VersioningService->getAllVersions]");
                response = getServicesFactory().getVersioningService().getAllVersions(new GetAllVersions(getAndAssertRepositoryId(), versionSeriesId, null, null, null));
            }
            catch (Exception e)
            {
                fail(e.toString());
            }
            assertNotNull("GetAllVersions response is NULL", response);
            assertTrue("GetAllVersions response is empty", response.length > 0);
            assertNotNull("GetAllVersions response is empty", response[0]);
            assertEquals("'" + EnumPropertiesDocument._value10 + "' property value is invalid", checkinComment, getStringProperty(response[0].getProperties(),
                    EnumPropertiesDocument._value10));
        }
        else
        {
            LOGGER.info("testGetAllVersionsDefault was skipped: Versioning isn't supported");
        }
    }

    public void testGetAllVersionsFiltered() throws Exception
    {
        if (isVersioningAllowed())
        {
            String checkinComment = "Test checkin" + System.currentTimeMillis();

            LOGGER.info("[VersioningService->checkOut]");
            CheckOutResponse checkOutResponse = getServicesFactory().getVersioningService().checkOut(new CheckOut(getAndAssertRepositoryId(), documentId));
            assertNotNull("Checkout response is NULL", checkOutResponse);
            documentIdHolder = checkOutResponse.getDocumentId();
            LOGGER.info("[VersioningService->checkIn]");
            CheckInResponse checkInResponse = getServicesFactory().getVersioningService().checkIn(
                    new CheckIn(getAndAssertRepositoryId(), documentIdHolder, true, new CmisPropertiesType(), new CmisContentStreamType(BigInteger.valueOf(0), MIMETYPE_TEXT_PLAIN,
                            generateTestFileName(), null, TEST_CONTENT.getBytes(), null), checkinComment, null, null, null));
            assertNotNull("Checkin response is NULL", checkInResponse);
            documentId = checkInResponse.getDocumentId();

            CmisObjectType[] response = null;
            String versionSeriesId = getIdProperty(documentId, EnumPropertiesDocument._value6);
            assertNotNull("'" + EnumPropertiesDocument._value6 + "' property is NULL", versionSeriesId);
            try
            {
                LOGGER.info("[VersioningService->getAllVersions]");
                response = getServicesFactory().getVersioningService().getAllVersions(
                        new GetAllVersions(getAndAssertRepositoryId(), versionSeriesId, EnumPropertiesBase._value1 + ", " + EnumPropertiesBase._value2 + ", "
                                + EnumPropertiesDocument._value10, null, null));
            }
            catch (Exception e)
            {
                fail(e.toString());
            }
            assertNotNull("GetAllVersions response is NULL", response);
            assertTrue("GetAllVersions response is empty", response.length > 0);
            assertNotNull("GetAllVersions response is empty", response[0]);

            for (CmisObjectType object : response)
            {
                CmisPropertiesType properties = object.getProperties();

                assertNull("Not expected properties were returned", properties.getPropertyBoolean());
                assertNull("Not expected properties were returned", properties.getPropertyDecimal());
                assertNull("Not expected properties were returned", properties.getPropertyHtml());
                assertNull("Not expected properties were returned", properties.getPropertyInteger());
                assertNull("Not expected properties were returned", properties.getPropertyUri());
                assertNull("Not expected properties were returned", properties.getPropertyXml());
                assertNull("Not expected properties were returned", properties.getPropertyDateTime());

                assertNotNull("Expected properties were not returned", properties.getPropertyId());
                assertNotNull("Expected properties were not returned", properties.getPropertyString());

                assertEquals("Expected properties were not returned", 1, properties.getPropertyId().length);
                assertTrue("Expected properties were not returned", 2 >= properties.getPropertyString().length);

                assertNotNull("Expected property was not returned", getIdProperty(properties, EnumPropertiesBase._value2));
                assertNotNull("Expected property was not returned", getStringProperty(properties, EnumPropertiesBase._value1));
            }
        }
        else
        {
            LOGGER.info("testGetAllVersionsFiltered was skipped: Versioning isn't supported");
        }
    }

    public void testGetAllVersionsForNoVersionHistory() throws Exception
    {
        if (isVersioningAllowed())
        {
            CmisObjectType[] response = null;
            String versionSeriesId = getIdProperty(documentId, EnumPropertiesDocument._value6);
            assertNotNull("'" + EnumPropertiesDocument._value6 + "' property is NULL", versionSeriesId);
            try
            {
                LOGGER.info("[VersioningService->getAllVersions]");
                response = getServicesFactory().getVersioningService().getAllVersions(new GetAllVersions(getAndAssertRepositoryId(), versionSeriesId, "*", null, null));
            }
            catch (Exception e)
            {
                fail(e.toString());
            }
            assertNotNull("GetAllVersions response is NULL", response);
            assertTrue("GetAllVersions response is empty", response.length > 0);
            assertNotNull("GetAllVersions response is empty", response[0]);
        }
        else
        {
            LOGGER.info("testGetAllVersionsForNoVersionHistory was skipped: Versioning isn't supported");
        }
    }

    public void testGetAllVersionsPWC() throws Exception
    {
        if (isVersioningAllowed())
        {
            LOGGER.info("[VersioningService->checkOut]");
            CheckOutResponse checkOutResponse = getServicesFactory().getVersioningService().checkOut(new CheckOut(getAndAssertRepositoryId(), documentId));
            assertNotNull("Checkout response is NULL", checkOutResponse);
            documentIdHolder = checkOutResponse.getDocumentId();

            CmisObjectType[] response = null;
            String versionSeriesId = getIdProperty(documentId, EnumPropertiesDocument._value6);
            assertNotNull("'" + EnumPropertiesDocument._value6 + "' property is NULL", versionSeriesId);
            try
            {
                LOGGER.info("[VersioningService->getAllVersions]");
                response = getServicesFactory().getVersioningService().getAllVersions(new GetAllVersions(getAndAssertRepositoryId(), versionSeriesId, "*", null, null));
            }
            catch (Exception e)
            {
                fail(e.toString());
            }
            assertNotNull("GetAllVersions response is NULL", response);
            assertTrue("GetAllVersions response is empty", response.length > 0);
            assertNotNull("GetAllVersions response is empty", response[0]);

            // TODO uncomment when PWC Id will be corrected
            // boolean pwcFound = false;
            // for (CmisObjectType cmisObjectType : response.getObject())
            // {
            // if (!pwcFound)
            // {
            // pwcFound = getIdProperty(cmisObjectType.getProperties(), "ObjectId").equals(documentIdHolder);
            // }
            // }
            // assertTrue("No private working copy version found", pwcFound);
        }
        else
        {
            LOGGER.info("testGetAllVersionsPWC was skipped: Versioning isn't supported");
        }
    }

    // FIXME: uncomment this when schema will be corrected
    // public void testDeleteAllVersions() throws Exception
    // {
    // if (isVersioningAllowed())
    // {
    // String versionSeriesId = getIdProperty(documentId, EnumPropertiesDocument._value6);
    // assertNotNull("'VersionSeriesId' property is NULL", versionSeriesId);
    // try
    // {
    // getServicesFactory().getVersioningService().deleteAllVersions(new DeleteAllVersions(getAndAssertRepositoryId(), versionSeriesId));
    // }
    // catch (Exception e)
    // {
    // fail(e.toString());
    // }
    // }
    // else
    // {
    // LOGGER.info("testDeleteAllVersions was skipped: Versioning isn't supported");
    // }
    // }

    public void testWrongRepositoryIdUsing() throws Exception
    {
        if (isVersioningAllowed())
        {
            try
            {
                LOGGER.info("[VersioningService->cancelCheckOut]");
                getServicesFactory().getVersioningService().cancelCheckOut(new CancelCheckOut(INVALID_REPOSITORY_ID, documentId));
                fail("Repository with specified Id was not described with RepositoryService");
            }
            catch (Exception e)
            {
            }
            try
            {
                LOGGER.info("[VersioningService->checkOut]");
                documentIdHolder = getServicesFactory().getVersioningService().checkOut(new CheckOut(INVALID_REPOSITORY_ID, documentId)).getDocumentId();
                fail("Repository with specified Id was not described with RepositoryService");
            }
            catch (Exception e)
            {
            }
            try
            {
                LOGGER.info("[VersioningService->checkIn]");
                getServicesFactory().getVersioningService().checkIn(
                        new CheckIn(INVALID_REPOSITORY_ID, documentId, true, new CmisPropertiesType(), new CmisContentStreamType(BigInteger.valueOf(0), MIMETYPE_TEXT_PLAIN,
                                generateTestFileName(), null, new byte[0], null), TEST_CHECK_IN_COMMENT_MESSAGE, null, null, null));
                fail("Repository with specified Id was not described with RepositoryService");
            }
            catch (Exception e)
            {
            }
            String versionSeriesId = getIdProperty(documentId, EnumPropertiesDocument._value6);
            assertNotNull("'VersionSeriesId' property is NULL", versionSeriesId);
            try
            {
                LOGGER.info("[VersioningService->getAllVersions]");
                getServicesFactory().getVersioningService().getAllVersions(new GetAllVersions(INVALID_REPOSITORY_ID, versionSeriesId, "*", null, null));
                fail("Repository with specified Id was not described with RepositoryService");
            }
            catch (Exception e)
            {
            }
            try
            {
                LOGGER.info("[VersioningService->getPropertiesOfLatestVersion]");
                getServicesFactory().getVersioningService().getPropertiesOfLatestVersion(
                        new GetPropertiesOfLatestVersion(INVALID_REPOSITORY_ID, versionSeriesId, true, null, false));
                fail("Repository with specified Id was not described with RepositoryService");
            }
            catch (Exception e)
            {
            }
            try
            {
                // FIXME: uncomment this when schema will be corrected
                // getServicesFactory().getVersioningService().deleteAllVersions(new DeleteAllVersions(INVALID_REPOSITORY_ID, versionSeriesId));
                // fail("Repository with specified Id was not described with RepositoryService");
            }
            catch (Exception e)
            {
            }
        }
        else
        {
            LOGGER.info("testWrongRepositoryIdUsing was skipped: Versioning isn't supported");
        }
    }
}
