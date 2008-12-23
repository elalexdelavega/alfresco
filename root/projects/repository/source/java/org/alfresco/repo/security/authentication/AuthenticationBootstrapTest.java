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
package org.alfresco.repo.security.authentication;

import org.alfresco.util.ApplicationContextHelper;

import junit.framework.TestCase;

/**
 * Checks that no residual authentications are left over after bootstrap.  It is important that
 * this test run on its own and not part of a suite.
 * 
 * @author Derek Hulley
 * @since 3.0.1
 */
public class AuthenticationBootstrapTest extends TestCase
{
    /**
     * Creates the application context in the context of the test (not statically) and checks
     * that no residual authentication is left hanging around.
     */
    public void testBoostrap()
    {
        // Start the context
        ApplicationContextHelper.getApplicationContext();
        
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        assertNull(
                "Found user '" + user + "' still authenticated after bootstrap.\n" +
                "Use AuthenticationUtil.runAs or AuthenticationUtil.pushAuthentication " +
                "and AuthenticationUtil.popAuthentication to keep the thread clean of unwanted authentication tokens.",
                user);
    }
}