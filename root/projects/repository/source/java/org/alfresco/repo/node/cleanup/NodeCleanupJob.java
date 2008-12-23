/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.node.cleanup;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled job to call a {@link NodeCleanupWorker}.
 * <p>
 * Job data is: <b>nodeCleanupWorker</b>
 * 
 * @author Derek Hulley
 * @since 2.2SP2
 */
public class NodeCleanupJob implements Job
{
    private static Log logger = LogFactory.getLog(NodeCleanupJob.class);
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the content Cleanup to use
        Object nodeCleanupWorkerObj = jobData.get("nodeCleanupWorker");
        if (nodeCleanupWorkerObj == null || !(nodeCleanupWorkerObj instanceof NodeCleanupWorker))
        {
            throw new AlfrescoRuntimeException(
                    "NodeCleanupJob data must contain valid 'nodeCleanupWorker' reference");
        }
        NodeCleanupWorker nodeCleanupWorker = (NodeCleanupWorker) nodeCleanupWorkerObj;
        List<String> cleanupLog = nodeCleanupWorker.doClean();
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Node cleanup log:");
            for (String log : cleanupLog)
            {
                logger.debug(log);
            }
        }
    }
}