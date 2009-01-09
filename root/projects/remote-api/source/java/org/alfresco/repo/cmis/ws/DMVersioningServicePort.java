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
package org.alfresco.repo.cmis.ws;

import java.util.List;

import javax.xml.ws.Holder;

import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;

/**
 * Port for versioning service.
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "VersioningServicePort", serviceName = "VersioningService", portName = "VersioningServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.VersioningServicePort")
public class DMVersioningServicePort extends DMAbstractServicePort implements VersioningServicePort
{
    private LockService lockService;

    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }


    /**
     * Reverses the effect of a check-out. Removes the private working copy of the checked-out document object, allowing other documents in the version series to be checked out
     * again.
     * 
     * @param repositoryId repository Id
     * @param documentId document Id
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     */
    public void cancelCheckOut(String repositoryId, String documentId)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException
    {
        checkRepositoryId(repositoryId);
        NodeRef workingCopyNodeRef = cmisObjectsUtils.getIdentifierInstance(documentId, AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier();
        assertWorkingCopy(workingCopyNodeRef);
        checkOutCheckInService.cancelCheckout(workingCopyNodeRef);
    }

    /**
     * Makes the private working copy the current version of the document.
     * 
     * @param repositoryId repository Id
     * @param documentId document Id
     * @param major is major True (Default)
     * @param properties CMIS properties
     * @param contentStream content stream
     * @param checkinComment check in comment
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws StorageException
     * @throws StreamNotSupportedException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void checkIn(String repositoryId, Holder<String> documentId, Boolean major, CmisPropertiesType properties, CmisContentStreamType contentStream, String checkinComment)
        throws PermissionDeniedException, UpdateConflictException, StorageException, StreamNotSupportedException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);
        NodeRef workingCopyNodeRef = cmisObjectsUtils.getIdentifierInstance(documentId.value, AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier();
        assertWorkingCopy(workingCopyNodeRef);

        if (contentStream != null)
        {
            try
            {
                ContentWriter writer = fileFolderService.getWriter(workingCopyNodeRef);
                writer.setMimetype(contentStream.getMimeType());
                writer.putContent(contentStream.getStream().getInputStream());
            }
            catch (Exception e)
            {
                throw new RuntimeException("Exception while updating content stream");
            }
        }

        if (properties != null)
        {
            setProperties(workingCopyNodeRef, properties);
        }

        NodeRef nodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, createVersionProperties(checkinComment, major != null && major ? VersionType.MAJOR : VersionType.MINOR));
        documentId.value = (String) cmisPropertyService.getProperty(nodeRef, CMISMapping.PROP_OBJECT_ID);
    }

    /**
     * Create a private working copy of the object, copies the metadata and optionally content.
     * 
     * @param repositoryId repository Id
     * @param documentId ObjectID of document version to checkout
     * @param contentCopied
     * @return ObjectID of private working copy as documentId; True if succeed, False otherwise as contentCopied
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void checkOut(String repositoryId, Holder<String> documentId, Holder<Boolean> contentCopied)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);

        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(documentId.value, AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier();
        LockStatus lockStatus = lockService.getLockStatus(documentNodeRef);

        if (lockStatus.equals(LockStatus.LOCKED) || lockStatus.equals(LockStatus.LOCK_OWNER) || nodeService.hasAspect(documentNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            throw new OperationNotSupportedException("Object is already checked out");
        }

        NodeRef pwcNodeRef = checkoutNode(documentNodeRef);
        documentId.value = (String) cmisPropertyService.getProperty(pwcNodeRef, CMISMapping.PROP_OBJECT_ID);
        contentCopied.value = true;
    }

    /**
     * Deletes all document versions in the specified version series.
     * 
     * @param repositoryId repository Id
     * @param versionSeriesId version series Id
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void deleteAllVersions(String repositoryId, String versionSeriesId)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(versionSeriesId, AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier();
        versionService.deleteVersionHistory(documentNodeRef);
    }

    /**
     * Gets the list of all document versions for the specified version series.
     * 
     * @param parameters repositoryId: repository Id; versionSeriesId: version series Id; filter: property filter; includeAllowableActions; includeRelationships;
     * @return list of CmisObjectType
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws FilterNotValidException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public GetAllVersionsResponse getAllVersions(GetAllVersions parameters)
        throws PermissionDeniedException, UpdateConflictException, FilterNotValidException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(parameters.getRepositoryId());

        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(parameters.getVersionSeriesId(), AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier();
        documentNodeRef = getLatestNode(documentNodeRef, false);
        PropertyFilter propertyFilter = createPropertyFilter(parameters.getFilter());

        GetAllVersionsResponse response = new GetAllVersionsResponse();
        List<CmisObjectType> objects = response.getObject();

        searchWorkingCopy(documentNodeRef, propertyFilter, objects);
        objects.add(createCmisObject(documentNodeRef, propertyFilter));

        VersionHistory versionHistory = versionService.getVersionHistory(documentNodeRef);

        if (versionHistory == null)
        {
            return response;
        }

        Version version = versionService.getCurrentVersion(documentNodeRef);
        while (version != null)
        {
            objects.add(createCmisObject(version.getFrozenStateNodeRef(), propertyFilter));
            version = versionHistory.getPredecessor(version);
        }

        return response;
    }

    /**
     * Gets the properties of the latest version, or the latest major version, of the specified version series.
     * 
     * @param parameters repositoryId: repository Id; versionSeriesId: version series Id; majorVersion: whether or not to return the latest major version. Default=FALSE; filter:
     *        property filter
     * @return CmisObjectType with properties
     */
    public GetPropertiesOfLatestVersionResponse getPropertiesOfLatestVersion(GetPropertiesOfLatestVersion parameters)
        throws PermissionDeniedException, UpdateConflictException, FilterNotValidException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException
    {
        checkRepositoryId(parameters.getRepositoryId());
        PropertyFilter propertyFilter = createPropertyFilter(parameters.getFilter());

        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(parameters.getVersionSeriesId(), AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier();
        NodeRef latestVersionNodeRef = getLatestNode(documentNodeRef, parameters.isMajorVersion());

        GetPropertiesOfLatestVersionResponse response = new GetPropertiesOfLatestVersionResponse();
        response.setObject(new CmisObjectType());
        CmisObjectType object = response.getObject();
        object.setProperties(getPropertiesType(latestVersionNodeRef.toString(), propertyFilter));

        return response;
    }

    private void searchWorkingCopy(NodeRef documentNodeRef, PropertyFilter propertyFilter, List<CmisObjectType> resultList)
    {
        NodeRef workingCopyNodeReference = cmisObjectsUtils.isWorkingCopy(documentNodeRef) ? documentNodeRef : checkOutCheckInService.getWorkingCopy(documentNodeRef);
        if (workingCopyNodeReference != null)
        {
            resultList.add(createCmisObject(workingCopyNodeReference, propertyFilter));
        }
    }

    private void assertWorkingCopy(NodeRef nodeRef) throws OperationNotSupportedException
    {
        if (!cmisObjectsUtils.isWorkingCopy(nodeRef))
        {
            throw new OperationNotSupportedException("Object isn't checked out");
        }
    }
}
