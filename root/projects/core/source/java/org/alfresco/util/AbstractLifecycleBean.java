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
package org.alfresco.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;


/**
 * Abstract helper for assisting the bootstrap and termination of Alfresco Components
 *  
 * @author davidc
 */
public abstract class AbstractLifecycleBean implements ApplicationListener, ApplicationContextAware
{
    protected final static Log log = LogFactory.getLog(AbstractLifecycleBean.class);    
    private ApplicationContext applicationContext = null;
    
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent)event;
            ApplicationContext refreshContext = refreshEvent.getApplicationContext();
            if (refreshContext != null && refreshContext.equals(applicationContext))
            {
                if (log.isDebugEnabled())
                    log.debug("Bootstrapping component " + this.getClass().getName());
                onBootstrap(refreshEvent);
            }
        }
        else if (event instanceof ContextClosedEvent)
        {
            ContextClosedEvent closedEvent = (ContextClosedEvent)event;
            ApplicationContext closedContext = closedEvent.getApplicationContext();
            if (closedContext != null && closedContext.equals(applicationContext))
            {
                if (log.isDebugEnabled())
                    log.debug("Shutting down component " + this.getClass().getName());
                onShutdown(closedEvent);
            }
        }
    }

    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Gets the Application Context
     * 
     * @return  application context
     */
    protected ApplicationContext getApplicationContext()
    {
        return this.applicationContext;
    }
    
    /**
     * Callback for initialising Component on first startup of Alfresco Server
     * 
     * @param event
     */
    protected abstract void onBootstrap(ApplicationEvent event);
    
    /**
     * Callback for terminating Component on shutdown of Alfresco Server
     * 
     * @param event
     */
    protected abstract void onShutdown(ApplicationEvent event);
    
}
