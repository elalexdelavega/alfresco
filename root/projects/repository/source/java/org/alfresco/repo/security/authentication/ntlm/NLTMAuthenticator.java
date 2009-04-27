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
package org.alfresco.repo.security.authentication.ntlm;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.NTLMMode;

/**
 * An specialized {@link AuthenticationComponent} that is capable of handling NTLM authentication directly, either by
 * 'passing through' to a domain server or by validating an MD4 hashed password. Unlike other authentication methods,
 * these operations cannot be chained and must be handled by a specific authentication component.
 * 
 * @author dward
 */
public interface NLTMAuthenticator extends AuthenticationComponent
{
    /**
     * Authenticate using a token.
     * 
     * @param token
     *            Authentication
     * @return Authentication
     * @throws AuthenticationException
     *             the authentication exception
     */
    public Authentication authenticate(Authentication token) throws AuthenticationException;

    /**
     * Get the enum that describes NTLM integration.
     * 
     * @return the NTLM mode
     */
    public NTLMMode getNTLMMode();

    /**
     * Get the MD4 password hash, as required by NTLM based authentication methods.
     * 
     * @param userName
     *            the user name
     * @return the m d4 hashed password
     */
    public String getMD4HashedPassword(String userName);
}