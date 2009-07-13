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
package org.alfresco.repo.management.subsystems.test;

import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseSpringTest;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Ensures the following features of subsystems are working:
 * <ul>
 * <li>Subsystem default properties
 * <li>Global property overrides (via alfresco-global.properties)
 * <li>Subsystem instance specific property overrides (via extension classpath)
 * <li>Subsystem instance specific Spring overrides (via extension classpath)
 * <li>Composite property defaults
 * <li>Composite property instance overrides
 * </ul>
 * 
 * @see ChildApplicationContextFactory
 * @author dward
 */
public class SubsystemsTest extends BaseSpringTest
{

    /*
     * (non-Javadoc)
     * @see org.alfresco.util.BaseSpringTest#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations()
    {
        return new String[]
        {
            ApplicationContextHelper.CONFIG_LOCATIONS[0], "classpath:subsystem-test-context.xml"
        };
    }

    /**
     * Test subsystems.
     * 
     * @throws Exception
     *             the exception
     */
    public void testSubsystems() throws Exception
    {
        ApplicationContextFactory subsystem = (ApplicationContextFactory) getApplicationContext().getBean(
                "testsubsystem");
        ConfigurableApplicationContext childContext = (ConfigurableApplicationContext) subsystem
                .getApplicationContext();
        assertTrue("Subsystem not started", childContext.isActive());
        TestService testService = (TestService) childContext.getBean("testService");
        // Make sure subsystem defaults work
        assertEquals("Subsystem Default1", testService.getSimpleProp1());
        // Make sure global property overrides work
        assertEquals(true, testService.getSimpleProp2().booleanValue());
        // Make sure extension classpath property overrides work
        assertEquals("Instance Override3", testService.getSimpleProp3());
        // Make sure extension classpath Spring overrides work
        assertEquals("An extra bean I changed", childContext.getBean("anotherBean"));
        // Make sure composite properties and their defaults work
        TestBean[] testBeans = testService.getTestBeans();
        assertNotNull("Composite property not set", testBeans);
        assertEquals(3, testBeans.length);
        assertEquals("inst1", testBeans[0].getId());
        assertEquals(false, testBeans[0].isBoolProperty());
        assertEquals(123456789123456789L, testBeans[0].getLongProperty());
        assertEquals("Global Default", testBeans[0].getAnotherStringProperty());
        assertEquals("inst2", testBeans[1].getId());
        assertEquals(true, testBeans[1].isBoolProperty());
        assertEquals(123456789123456789L, testBeans[1].getLongProperty());
        assertEquals("Global Default", testBeans[1].getAnotherStringProperty());
        assertEquals("inst3", testBeans[2].getId());
        assertEquals(false, testBeans[2].isBoolProperty());
        assertEquals(123456789123456789L, testBeans[2].getLongProperty());
        assertEquals("Global Instance Default", testBeans[2].getAnotherStringProperty());
    }

}
