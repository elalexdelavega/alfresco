/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Content Model Constants
 */
public interface ContentModel
{
    // base type constants
    static final QName TYPE_BASE = QName.createQName(NamespaceService.ALFRESCO_URI, "base");
    static final QName ASPECT_REFERENCABLE = QName.createQName(NamespaceService.ALFRESCO_URI, "referencable");
    static final QName PROP_STORE_PROTOCOL = QName.createQName(NamespaceService.ALFRESCO_URI, "store-protocol");
    static final QName PROP_STORE_IDENTIFIER = QName.createQName(NamespaceService.ALFRESCO_URI, "store-identifier");
    static final QName PROP_NODE_UUID = QName.createQName(NamespaceService.ALFRESCO_URI, "node-uuid");
    
    // content management type constants
    static final QName TYPE_CMOBJECT = QName.createQName(NamespaceService.ALFRESCO_URI, "cmobject");
    static final QName PROP_NAME = QName.createQName(NamespaceService.ALFRESCO_URI, "name");
    
    // referenceable aspect constants
    static final QName TYPE_REFERENCE = QName.createQName(NamespaceService.ALFRESCO_URI, "reference");
    static final QName PROP_REFERENCE = QName.createQName(NamespaceService.ALFRESCO_URI, "reference");
      
    // copy aspect constants
    static final QName ASPECT_COPIEDFROM = QName.createQName(NamespaceService.ALFRESCO_URI, "copiedfrom");
    static final QName PROP_COPY_REFERENCE = QName.createQName(NamespaceService.ALFRESCO_URI, "source");
	
    // working copy aspect contants
    static final QName ASPECT_WORKING_COPY = QName.createQName(NamespaceService.ALFRESCO_URI, "workingcopy");
    static final QName PROP_WORKING_COPY_OWNER = QName.createQName(NamespaceService.ALFRESCO_URI, "workingCopyOwner");
	
    // container type constants
    static final QName TYPE_CONTAINER = QName.createQName(NamespaceService.ALFRESCO_URI, "container");
    /** child association type supported by {@link #TYPE_CONTAINER} */
    static final QName ASSOC_CHILDREN =QName.createQName(NamespaceService.ALFRESCO_URI, "children");

    // roots
    static final QName ASPECT_ROOT = QName.createQName(NamespaceService.ALFRESCO_URI, "aspect_root");
    static final QName TYPE_STOREROOT = QName.createQName(NamespaceService.ALFRESCO_URI, "store_root");
    static final QName TYPE_CATEGORYROOT = QName.createQName(NamespaceService.ALFRESCO_URI, "category_root");  
    
    // content type and aspect constants
    static final QName PROP_CONTENT_URL = QName.createQName(NamespaceService.ALFRESCO_URI, "contentUrl");
    static final QName PROP_MIME_TYPE = QName.createQName(NamespaceService.ALFRESCO_URI, "mimetype");
    static final QName PROP_ENCODING = QName.createQName(NamespaceService.ALFRESCO_URI, "encoding");
    static final QName PROP_SIZE = QName.createQName(NamespaceService.ALFRESCO_URI, "size");
    
    // ui facets aspect
    static final QName ASPECT_UIFACETS = QName.createQName(NamespaceService.ALFRESCO_URI, "uifacets");
    static final QName PROP_ICON = QName.createQName(NamespaceService.ALFRESCO_URI, "icon");
    
    // title aspect
    static final QName ASPECT_TITLED = QName.createQName(NamespaceService.ALFRESCO_URI, "titled");
    static final QName PROP_TITLE = QName.createQName(NamespaceService.ALFRESCO_URI, "title");
    static final QName PROP_DESCRIPTION = QName.createQName(NamespaceService.ALFRESCO_URI, "description");
    
    // auditable aspect
    static final QName ASPECT_AUDITABLE = QName.createQName(NamespaceService.ALFRESCO_URI, "auditable");
    static final QName PROP_CREATED = QName.createQName(NamespaceService.ALFRESCO_URI, "created");
    static final QName PROP_CREATOR = QName.createQName(NamespaceService.ALFRESCO_URI, "creator");
    static final QName PROP_MODIFIED = QName.createQName(NamespaceService.ALFRESCO_URI, "modified");
    static final QName PROP_MODIFIER = QName.createQName(NamespaceService.ALFRESCO_URI, "modifier");
    static final QName PROP_ACCESSED = QName.createQName(NamespaceService.ALFRESCO_URI, "accessed");
    
    // categories
    static final QName ASPECT_CLASSIFIABLE = QName.createQName(NamespaceService.ALFRESCO_URI, "classifiable");
    //static final QName ASPECT_CATEGORISATION = QName.createQName(NamespaceService.ALFRESCO_URI, "aspect_categorisation");
    static final QName ASPECT_GEN_CLASSIFIABLE = QName.createQName(NamespaceService.ALFRESCO_URI, "generalclassifiable");
    static final QName TYPE_CATEGORY = QName.createQName(NamespaceService.ALFRESCO_URI, "category");
    static final QName PROP_CATEGORIES = QName.createQName(NamespaceService.ALFRESCO_URI, "categories");
    static final QName ASSOC_CATEGORIES = QName.createQName(NamespaceService.ALFRESCO_URI, "categories");
    static final QName ASSOC_SUBCATEGORIES = QName.createQName(NamespaceService.ALFRESCO_URI, "subcategories");

    // lock aspect
    public final static QName ASPECT_LOCKABLE = QName.createQName(NamespaceService.ALFRESCO_URI, "lockable");
    public final static QName PROP_LOCK_OWNER = QName.createQName(NamespaceService.ALFRESCO_URI, "lockOwner");
    public final static QName PROP_LOCK_TYPE = QName.createQName(NamespaceService.ALFRESCO_URI, "lockType");
	
    // version aspect
	static final QName ASPECT_VERSIONABLE = QName.createQName(NamespaceService.ALFRESCO_URI, "versionable");
	static final QName PROP_VERSION_LABEL = QName.createQName(NamespaceService.ALFRESCO_URI, "versionLabel");

    // content type constants
    static final QName TYPE_CONTENT = QName.createQName(NamespaceService.ALFRESCO_URI, "content");

    // expected application types
    static final QName TYPE_FOLDER = QName.createQName(NamespaceService.ALFRESCO_URI, "folder");
    /** child association type supported by {@link #TYPE_FOLDER} */
    static final QName ASSOC_CONTAINS = QName.createQName(NamespaceService.ALFRESCO_URI, "contains");
    
    // person
    static final QName TYPE_PERSON = QName.createQName(NamespaceService.ALFRESCO_URI, "person");
    static final QName PROP_USERNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "userName");
    static final QName PROP_HOMEFOLDER = QName.createQName(NamespaceService.ALFRESCO_URI, "homeFolder");
    static final QName PROP_FIRSTNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "firstName");
    static final QName PROP_LASTNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "lastName");
    static final QName PROP_EMAIL = QName.createQName(NamespaceService.ALFRESCO_URI, "email");
    static final QName PROP_ORGID = QName.createQName(NamespaceService.ALFRESCO_URI, "organizationId");
    
    // user
    public static final QName TYPE_USER = QName.createQName(NamespaceService.ALFRESCO_URI, "user");
    public static final QName PROP_USER_USERNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "username");
    public static final QName PROP_PASSWORD = QName.createQName(NamespaceService.ALFRESCO_URI, "password");
    public static final QName PROP_SALT = QName.createQName(NamespaceService.ALFRESCO_URI, "salt");
    
    // system folder type
    static final QName TYPE_SYTEM_FOLDER = QName.createQName(NamespaceService.ALFRESCO_URI, "systemfolder");

    // rules
    static final QName ASPECT_ACTIONABLE = QName.createQName(NamespaceService.ALFRESCO_URI, "actionable");
    static final QName ASPECT_CONFIGURABLE = QName.createQName(NamespaceService.ALFRESCO_URI, "configurable");
    static final QName TYPE_CONFIGURATIONS = QName.createQName(NamespaceService.ALFRESCO_URI, "configurations");
    static final QName TYPE_RULE_CONTENT = QName.createQName(NamespaceService.ALFRESCO_URI, "rulecontent");
    static final QName ASSOC_CONFIGURATIONS = QName.createQName(NamespaceService.ALFRESCO_URI, "configurations");
	
	// workflow
	static final QName ASPECT_SIMPLE_WORKFLOW = QName.createQName(NamespaceService.ALFRESCO_URI, "simpleworkflow");
	static final QName PROP_APPROVE_STEP = QName.createQName(NamespaceService.ALFRESCO_URI, "approveStep");
	static final QName PROP_APPROVE_FOLDER = QName.createQName(NamespaceService.ALFRESCO_URI, "approveFolder");
	static final QName PROP_APPROVE_MOVE = QName.createQName(NamespaceService.ALFRESCO_URI, "approveMove");
	static final QName PROP_REJECT_STEP = QName.createQName(NamespaceService.ALFRESCO_URI, "rejectStep");
	static final QName PROP_REJECT_FOLDER = QName.createQName(NamespaceService.ALFRESCO_URI, "rejectFolder");
	static final QName PROP_REJECT_MOVE = QName.createQName(NamespaceService.ALFRESCO_URI, "rejectMove");
}
