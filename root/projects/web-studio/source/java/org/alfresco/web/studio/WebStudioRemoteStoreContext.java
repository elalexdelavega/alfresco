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
package org.alfresco.web.studio;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.web.framework.WebFrameworkRemoteStoreContext;
import org.alfresco.web.site.RequestContext;
import org.alfresco.web.site.ThreadLocalRequestContext;

/**
 * An implementation class for RemoteStoreContext which empowers the
 * RemoteStore to pull values from the Web Studio.
 * 
 * @author muzquiano
 */
public class WebStudioRemoteStoreContext extends WebFrameworkRemoteStoreContext
{
    public WebStudioRemoteStoreContext()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.framework.WebFrameworkRemoteStoreContext#getStoreId()
     */
    public String getStoreId()
    {
        String storeId = null;

        // retrieve the request context
        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        if (context != null)
        {
            HttpServletRequest request = context.getRequest();

            // get the storeId
            storeId = WebStudioUtil.getCurrentStore(request);
        }

        // if we didn't find a storeId on the Web Studio context
        // then we'll try to draw one from the Web Framework context
        if (storeId == null)
        {
            storeId = super.getStoreId();
        }

        return storeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.framework.WebFrameworkRemoteStoreContext#getWebappId()
     */
    public String getWebappId()
    {
        String webappId = null;

        // retrieve the request context
        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        if (context != null)
        {
            HttpServletRequest request = context.getRequest();

            // get the webappId
            webappId = WebStudioUtil.getCurrentWebapp(request);
        }

        // if we didn't find a webappId on the Web Studio context
        // then we'll try to draw one from the Web Framework context
        if (webappId == null)
        {
            webappId = super.getWebappId();
        }

        return webappId;
    }

}