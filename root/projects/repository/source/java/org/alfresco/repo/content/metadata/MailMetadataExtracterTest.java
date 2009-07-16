/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * @author Derek Hulley
 * @since 3.2
 */
public class MailMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private MailMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new MailMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);
        extracter.register();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        for (String mimetype : MailMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testOutlookMsgExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_OUTLOOK_MSG);
    }
    
    protected void testCommonMetadata(String mimetype, Map<QName, Serializable> properties)
    {
        assertEquals(
                "Property " + ContentModel.PROP_AUTHOR + " not found for mimetype " + mimetype,
                "KEVIN.ROAST@BEN",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
        assertEquals(
                "Property " + ContentModel.PROP_DESCRIPTION + " not found for mimetype " + mimetype,
                "Test the content transformer",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_DESCRIPTION)));
    }
}