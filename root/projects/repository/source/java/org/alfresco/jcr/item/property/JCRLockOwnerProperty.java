/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.jcr.item.property;

import javax.jcr.RepositoryException;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.PropertyImpl;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Implementation for mix:lockable lockOwner property
 * 
 * @author David Caruana
 */
public class JCRLockOwnerProperty extends PropertyImpl
{
    public static QName PROPERTY_NAME = QName.createQName(JCRNamespace.JCR_URI, "lockOwner");
    

    /**
     * Construct
     * 
     * @param node
     */
    public JCRLockOwnerProperty(NodeImpl node)
    {
        super(node, PROPERTY_NAME);
    }

    @Override
    protected Object getPropertyValue() throws RepositoryException
    {
        NodeImpl nodeImpl = getNodeImpl();
        NodeService nodeService = nodeImpl.getSessionImpl().getRepositoryImpl().getServiceRegistry().getNodeService();
        String lockOwner = (String)nodeService.getProperty(nodeImpl.getNodeRef(), ContentModel.PROP_LOCK_OWNER);
        return lockOwner;
    }
    
}
