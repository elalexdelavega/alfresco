/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.awe.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebEditorFilter implements Filter
{
    public static final String KEY_ENABLED = "awe_enabled";
    public static final String KEY_URL_PREFIX = "awe_url_prefix";
    public static final String KEY_DEBUG = "awe_debug";
    public static final String DEFAULT_CONTEXT_PATH = "/awe";
    
    private static final String PARAM_CONTEXT_PATH = "contextPath";
    private static final String PARAM_DEBUG = "debug";
    private static final Log logger = LogFactory.getLog(WebEditorFilter.class);

    private String urlPrefix;
    private boolean debugEnabled = Boolean.FALSE;

    /*
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException
    {
        // setup the relevant objects in the request
        request.setAttribute(KEY_ENABLED, Boolean.TRUE);
        request.setAttribute(KEY_URL_PREFIX, this.urlPrefix);
        request.setAttribute(KEY_DEBUG, this.debugEnabled);

        if (logger.isDebugEnabled())
        {
            logger.debug("Setup request for Web Editor: (urlPrefix: " + this.urlPrefix
                        + ", debug: " + this.debugEnabled + ")");
        }

        chain.doFilter(request, response);
    }

    /*
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        String contextPathParam = config.getInitParameter(PARAM_CONTEXT_PATH);
        if (contextPathParam != null && contextPathParam.length() > 0)
        {
            if (contextPathParam.startsWith("/") == false)
            {
                contextPathParam = "/" + contextPathParam;
            }
        }

        // TODO: Read host and port information from config and use
        // on URL is present, for now just use the context path
        // as we are mandating that AWE is on the same server.

        if (contextPathParam != null)
        {
            this.urlPrefix = contextPathParam;
        }
        else
        {
            this.urlPrefix = DEFAULT_CONTEXT_PATH;
        }

        String debug = config.getInitParameter(PARAM_DEBUG);
        if (debug != null && debug.equalsIgnoreCase("true"))
        {
            this.debugEnabled = Boolean.TRUE;
        }

        if (logger.isDebugEnabled())
            logger.debug("Initialised Web Editor: (urlPrefix: " + this.urlPrefix + ", debug: " + this.debugEnabled + ")");
    }

    /*
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        // nothing to do
    }
}
