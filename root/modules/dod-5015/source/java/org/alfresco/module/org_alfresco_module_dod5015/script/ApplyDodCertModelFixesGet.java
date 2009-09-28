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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.module.org_alfresco_module_dod5015.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_dod5015.RecordsManagementAdminServiceImpl;
import org.alfresco.module.org_alfresco_module_dod5015.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_dod5015.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2ClassAssociation;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.scripts.Cache;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This webscript applies necessary changes to the RM custom model in the repository. These changes
 * are to 'patch' a deployed RM custom model during the DoD certification process. With that in mind
 * they are safe to apply to a live database i.e. without side-effect to existing data and safe
 * to call multiple times.
 * <P>
 * 
 * TODO This webscript should be removed after DOD certification as none of these patches are needed
 * for a newly-installed DoD amp.
 * 
 * @author neilm
 */
@Deprecated
public class ApplyDodCertModelFixesGet extends DeclarativeWebScript
                                  implements RecordsManagementModel
{
    private static final NodeRef RM_CUSTOM_MODEL_NODE_REF = new NodeRef("workspace://SpacesStore/records_management_custom_model");
    private static final String RMC_CUSTOM_RECORD_SERIES_PROPERTIES = RecordsManagementCustomModel.RM_CUSTOM_PREFIX + ":customRecordSeriesProperties";
    private static final String RMC_CUSTOM_RECORD_CATEGORY_PROPERTIES = RecordsManagementCustomModel.RM_CUSTOM_PREFIX + ":customRecordCategoryProperties";
    private static final String RMC_CUSTOM_RECORD_FOLDER_PROPERTIES = RecordsManagementCustomModel.RM_CUSTOM_PREFIX + ":customRecordFolderProperties";
    private static final String RMC_CUSTOM_RECORD_PROPERTIES = RecordsManagementCustomModel.RM_CUSTOM_PREFIX + ":customRecordProperties";
    
    /** Logger */
    private static Log logger = LogFactory.getLog(ApplyDodCertModelFixesGet.class);

    private ContentService contentService;

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Applying webscript-based patches to RM custom model in the repo.");
        }
        
        M2Model customModel = readCustomContentModel();
        
        M2Aspect customAssocsAspect = customModel.getAspect(RecordsManagementAdminServiceImpl.RMC_CUSTOM_ASSOCS);
        
        if (customAssocsAspect == null)
        {
            final String msg = "Unknown aspect: "+RecordsManagementAdminServiceImpl.RMC_CUSTOM_ASSOCS;
            if (logger.isErrorEnabled())
            {
                logger.error(msg);
            }
            throw new AlfrescoRuntimeException(msg);
        }
        
        
        // MOB-1573. All custom references should have many-many multiplicity.
        if (logger.isInfoEnabled())
        {
            logger.info("MOB-1573. All custom references should have many-many multiplicity.");
        }

        for (M2ClassAssociation classAssoc : customAssocsAspect.getAssociations())
        {
            classAssoc.setSourceMany(true);
            classAssoc.setTargetMany(true);
            
        }

        
        
        //MOB-1621. Custom fields should be created as untokenized by default.
        if (logger.isInfoEnabled())
        {
            logger.info("MOB-1621. Custom fields should be created as untokenized by default.");
        }
        
        List<String> allCustomPropertiesAspects = new ArrayList<String>(4);
        allCustomPropertiesAspects.add(RMC_CUSTOM_RECORD_SERIES_PROPERTIES);
        allCustomPropertiesAspects.add(RMC_CUSTOM_RECORD_CATEGORY_PROPERTIES);
        allCustomPropertiesAspects.add(RMC_CUSTOM_RECORD_FOLDER_PROPERTIES);
        allCustomPropertiesAspects.add(RMC_CUSTOM_RECORD_PROPERTIES);
        for (String aspectName : allCustomPropertiesAspects)
        {
            M2Aspect aspectObj = customModel.getAspect(aspectName);
            List<M2Property> customProperties = aspectObj.getProperties();
            for (M2Property propertyObj : customProperties)
            {
                propertyObj.setIndexed(true);
                propertyObj.setIndexedAtomically(true);
                propertyObj.setStoredInIndex(false);
                propertyObj.setIndexTokenisationMode(IndexTokenisationMode.FALSE);
            }
        }

        
        writeCustomContentModel(customModel);
        
        if (logger.isInfoEnabled())
        {
            logger.info("Completed application of webscript-based patches to RM custom model in the repo.");
        }

        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
    	model.put("success", true);
    	
        return model;
    }
    
    private M2Model readCustomContentModel()
    {
        ContentReader reader = this.contentService.getReader(RM_CUSTOM_MODEL_NODE_REF,
                                                             ContentModel.TYPE_CONTENT);
        
        if (reader.exists() == false) {throw new AlfrescoRuntimeException("RM CustomModel has no content.");}
        
        InputStream contentIn = null;
        M2Model deserializedModel = null;
        try
        {
            contentIn = reader.getContentInputStream();
            deserializedModel = M2Model.createModel(contentIn);
        }
        finally
        {
            try
            {
                if (contentIn != null) contentIn.close();
            }
            catch (IOException ignored)
            {
                // Intentionally empty.`
            }
        }
        return deserializedModel;
    }

    private void writeCustomContentModel(M2Model deserializedModel)
    {
        ContentWriter writer = this.contentService.getWriter(RM_CUSTOM_MODEL_NODE_REF,
                                                             ContentModel.TYPE_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding("UTF-8");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        deserializedModel.toXML(baos);
        
        String updatedModelXml;
        try
        {
            updatedModelXml = baos.toString("UTF-8");
            writer.putContent(updatedModelXml);
            // putContent closes all resources.
            // so we don't have to.
        } catch (UnsupportedEncodingException uex)
        {
            throw new AlfrescoRuntimeException("Exception when writing custom model xml.", uex);
        }
    }
}