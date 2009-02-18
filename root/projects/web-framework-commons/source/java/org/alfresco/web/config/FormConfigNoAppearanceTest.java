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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.web.config.FormConfigElement.Mode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JUnit tests to exercise the forms-related capabilities in to the web client
 * config service.
 * These tests are aimed at the XML config file that has no &lt;appearance&gt; tag.
 * 
 * @author Neil McErlean
 */
public class FormConfigNoAppearanceTest extends AbstractFormConfigTest
{
    private static Log logger = LogFactory.getLog(FormConfigNoAppearanceTest.class);
    
    @Override
    protected String getConfigXmlFile()
    {
        return "test-config-forms-no-appearance.xml";
    }
    
    public void testGetFormFieldVisibilitiesForModes()
    {
        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("name", Mode.CREATE));
        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("title", Mode.CREATE));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("rubbish", Mode.CREATE));

        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("name", Mode.EDIT));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("title", Mode.EDIT));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("rubbish", Mode.EDIT));

        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("name", Mode.VIEW));
        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("title", Mode.VIEW));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("rubbish", Mode.VIEW));
    }
    
    public void testFieldsVisibleInViewModeShouldStillBeVisibleWithNoAppearanceTag()
    {
        List<String> fieldNames = formConfigElement.getVisibleViewFieldNames();
        
        // The order specified in the config XML should also be preserved.
        List<String> expectedFieldNames = new ArrayList<String>();
        expectedFieldNames.add("name");
        expectedFieldNames.add("title");
        
        expectedFieldNames.add("cm:name");
        expectedFieldNames.add("cm:title");
        expectedFieldNames.add("cm:description");
        expectedFieldNames.add("cm:content");
        expectedFieldNames.add("my:text");
        expectedFieldNames.add("my:mltext");
        expectedFieldNames.add("my:date");
        
        assertEquals("Visible fields wrong.", expectedFieldNames, fieldNames);
    }
}